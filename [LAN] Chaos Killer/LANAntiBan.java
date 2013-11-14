package scripts;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.tribot.api.General;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;

enum EventScale {
	// Small events happen between 50 and 110 seconds.
	SMALL_EVENT,

	// Large events happen between 150 and 250 seconds.
	LARGE_EVENT,
};

enum Events {
	MOVE_MOUSE      (true, new AntiBanEvent(new MoveMouse(),    EventScale.SMALL_EVENT)),
	ROTATE_CAMERA   (true, new AntiBanEvent(new RotateCamera(), EventScale.SMALL_EVENT)),
	SKILL_CHECK     (true, new AntiBanEvent(new SkillCheck(),   EventScale.SMALL_EVENT)),
	FRIENDS_LIST    (true, new AntiBanEvent(new FriendsList(),  EventScale.SMALL_EVENT)),
	EXAMINE_NPC     (true, new AntiBanEvent(new ExamineNPC(),   EventScale.LARGE_EVENT)),
	EXAMINE_OBJECT  (true, new AntiBanEvent(new ExamineObject(),EventScale.LARGE_EVENT)),
	SHORT_AFK       (false, new AntiBanEvent(new ShortAFK(),    EventScale.LARGE_EVENT));
	
	private boolean enable;
	private final AntiBanEvent event;

	Events(boolean enable, AntiBanEvent event) {
		this.enable = enable; 
		this.event = event; 
	}
	
	public boolean isEnabled() { return enable; }
	public void setEnabled(boolean enable) { this.enable = enable; }
    
	public AntiBanEvent getEvent() { return event; }
};

public class LANAntiBan {
	
	private static AntiBanEvent lastSmallEvent = Events.MOVE_MOUSE.getEvent();
	private static AntiBanEvent lastLargeEvent = Events.SHORT_AFK.getEvent();
	
	private static ScheduledExecutorService executor;
	private static boolean enabled = false;
	
	private static boolean writeLog = true;
	
	public static boolean getWriteLog() {
		return writeLog;
	}

	public static void setWriteLog(boolean writeLog) {
		LANAntiBan.writeLog = writeLog;
	}
	
	public static void startAntiBan() {
		if (!enabled){
			executor = Executors.newSingleThreadScheduledExecutor();
			enabled = true;
			scheduleSmallEvent();
			scheduleLargeEvent();
		}
	}
	
	public static void stopAntiBan() {
		if (enabled){
			General.println("Stopping [LAN]AntiBan.");
			executor.shutdown();
			enabled = false;
		}
	}
	
	public static void useEvent(Events event, boolean use) {
		event.setEnabled(use);
	}
	
	public static void scheduleSmallEvent() {
		if (enabled) {
			ArrayList<AntiBanEvent> smallTasks = new ArrayList<AntiBanEvent>();
			
			for (Events e : Events.values()) {
				if (e.isEnabled() && e.getEvent().getScale() == EventScale.SMALL_EVENT) {
					smallTasks.add(e.getEvent());
				}
			}
			
			if (smallTasks.size() > 0) {
				// Next task can never be the same as the previous task.
				AntiBanEvent nextSmallTask = null;
				while (nextSmallTask == null || (nextSmallTask == lastSmallEvent.getEvent() && smallTasks.size() > 1 ))
					nextSmallTask = smallTasks.get(General.random(0, smallTasks.size() - 1));
		
				int schedule = General.random(50, 110);
				executor.schedule(nextSmallTask, schedule, TimeUnit.SECONDS);
				lastSmallEvent = nextSmallTask;
				if (getWriteLog())
					General.println("[LAN]AntiBan: Scheduled "+ nextSmallTask.getEvent().getClass().getSimpleName()+ " to happen in: "+schedule+ " seconds.");
			}
		}
	}

	public static void scheduleLargeEvent() {
		if (enabled) {
			ArrayList<AntiBanEvent> largeTasks = new ArrayList<AntiBanEvent>();

			for (Events e : Events.values()) {
				if (e.isEnabled() && e.getEvent().getScale() == EventScale.LARGE_EVENT) {
					largeTasks.add(e.getEvent());
				}
			}
			
			if (largeTasks.size() > 0) {
				// Next task can never be the same as the previous task.
				AntiBanEvent nextLargeTask = null;
				while (nextLargeTask == null || (nextLargeTask == lastLargeEvent.getEvent() && largeTasks.size() > 1 ))
					nextLargeTask = largeTasks.get(General.random(0, largeTasks.size() - 1));
	
				int schedule = General.random(150, 250);
				executor.schedule(nextLargeTask, schedule, TimeUnit.SECONDS);
				lastLargeEvent = nextLargeTask;
				if (getWriteLog())
					General.println("[LAN]AntiBan: Scheduled "+nextLargeTask.getEvent().getClass().getSimpleName()+" to happen in: "+schedule+" seconds.");
			}
		}
	}
};

class AntiBanEvent implements Runnable {
	
	private final Runnable event;
	private final EventScale scale;
	public EventScale getScale() { return scale; }
	public Runnable getEvent() { return event; }

	AntiBanEvent(Runnable event, EventScale scale) {
		this.event = event;
		this.scale = scale;
	}
	
	public void run() {
		event.run();
		
		if (scale == EventScale.LARGE_EVENT)
			LANAntiBan.scheduleLargeEvent();
		else
			LANAntiBan.scheduleSmallEvent();
	}
};

class MoveMouse implements Runnable {
	@Override
	public void run() {
		Mouse.move(General.random(60, 300), General.random(40, 450));
	}
};

class RotateCamera implements Runnable {
	@Override
	public void run() {
		Camera.setCameraRotation(Camera.getCameraRotation() + General.random(40, 110));
	}
};

class SkillCheck implements Runnable {
	@Override
	public void run() {
		TABS oldTab = GameTab.getOpen();
		GameTab.open(TABS.STATS);
		General.sleep(300,900);
		int index = General.random(0, 1) == 0 ? General.random(123, 137) : General.random(142, 149);
		final RSInterfaceChild skill = Interfaces.get(320, index);
		if (skill != null) {
			skill.hover();
			General.sleep(1900,3500);
		}
		// Make sure it didnt miss click on a skill guide.
		Interfaces.closeAll();
		GameTab.open(oldTab);
	}
};

class FriendsList implements Runnable {
	@Override
	public void run() {
		TABS oldTab = GameTab.getOpen();
		GameTab.open(TABS.FRIENDS);
			
		General.sleep(2000, 3000);
		GameTab.open(TABS.INVENTORY);
		GameTab.open(oldTab);
	}
};

class ExamineNPC implements Runnable {
	@Override
	public void run() {
		RSNPC[] npcs = NPCs.find(NPCs.generateFilterGroup(new Filter<RSNPC>() {
						@Override
						public boolean accept(RSNPC npc) {
							if (npc != null && npc.getName() != null && npc.getModel() != null && npc.getActions() != null)
								return npc.isOnScreen();
							return false;
						}
					}));
		
		if (npcs != null && npcs.length > 0) {
			Camera.turnToTile(npcs[0]);
			if (npcs[0].isValid())
				npcs[0].click("Examine");
		}
	}
};

class ExamineObject implements Runnable {
	@Override
	public void run() {
		RSObject[] objs = Objects.findNearest(10, new Filter<RSObject>() {
			@Override
			public boolean accept(RSObject obj) {
				if (obj != null && obj.getDefinition() != null && obj.getModel() != null)
					return obj.isOnScreen();
				return false;
			}
		});

		if (objs != null && objs.length > 0) {
			Camera.turnToTile(objs[0]);
			objs[0].click("Examine");
		}
	}
}

class ShortAFK implements Runnable {
	@Override
	public void run() {
		while (Player.getRSPlayer().isInCombat())
			General.sleep(100);
		
		// 50% chance to move mouse offscreen.
		if (new Random().nextBoolean())
			Mouse.move(-1, General.random(0, 900));
		
		// Afk for 8 to 16secs.
		int timeToAFK = General.random(8000, 16000);
		while (!Player.getRSPlayer().isInCombat() && timeToAFK > 0) {
			General.sleep(100);
			timeToAFK =- 100;
		}
	}
};


package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.tribot.api.General;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.MouseActions;
import org.tribot.script.interfaces.Painting;

enum State {
	BANKING, 
	GO_TO_DRUIDS,
	PROCESS_DRUIDS, 
	GO_TO_BANK,
}

@ScriptManifest(authors = { "Laniax" }, category = "Combat", name = "LAN's Chaos Killer", description = "Flawless Ardougne Chaos Druid Killer.")
public class LANChaosKiller extends Script implements Painting, MouseActions {
	// Global defines
	private static boolean quitting = false;
	private static Map<State, Method> stateMap = new HashMap<State, Method>();
	private static String statusText = "Starting..";
	private static final RSPlayer player = Player.getRSPlayer();

	// Script defines
	private static int foodID = 379; // todo: grab from settings
	private static int foodCount = 1; // todo: grab from settings
	private static int towerDoorID = 543;
	private static int logFromBankID = 9330;
	private static int logFromTowerID = 9328;

	private static final RSTile druidTowerDoor = new RSTile(2565, 3356);
	private static final RSTile druidTowerCenter = new RSTile(2562, 3356);
	private static final RSTile bankTile = new RSTile(2617, 3332);
	private static final RSTile log_east = new RSTile(2602, 3336);
	private static final RSTile log_west = new RSTile(2598, 3336);

	private static final RSTile[] failed_log_walk = new RSTile[] {
			new RSTile(2598, 3329), // east (tower side)
			new RSTile(2604, 3330) // west (bank side)
	};

	private final static RSTile[] logToTower = new RSTile[] {
			new RSTile(2598, 3336, 0), 
			new RSTile(2588, 3340, 0),
			new RSTile(2587, 3344, 0), 
			new RSTile(2576, 3350, 0),
			new RSTile(2565, 3356, 0) 
	};

	private final static RSTile[] bankToLog = new RSTile[] {
			new RSTile(2617, 3332, 0), 
			new RSTile(2616, 3337, 0),
			new RSTile(2613, 3339, 0), 
			new RSTile(2610, 3339, 0),
			new RSTile(2602, 3336, 0) 
	};

	@Override
	public void run() {
		try {
			for (State state : State.values()) {
				stateMap.put(state,
						LANChaosKiller.class.getMethod(state.name()));
			}

			while (!quitting) {
				stateMap.get(getState()).invoke(null);
				sleep(General.random(40, 80));
			}
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// Stop messing with my method/enum names!
			General.println(e);
			// todo: logout
		}
	}

	private State getState() {
		if (Inventory.isFull() || (foodCount > 0 && Inventory.find(foodID).length == 0 && (player.getMaxHealth() / 2) >= player.getHealth())) {
			if (Player.getPosition().distanceTo(bankTile) <= 2) {
				// We are infront of at the bank and in need of some banking
				// action.
				return State.BANKING;
			}
			// Inventory is full, we should move to bank to empty it.
			return State.GO_TO_BANK;
		}
		
		if (Player.getPosition().distanceTo(druidTowerCenter) <= 2) {
			// We are at the druids (in the tower).
			return State.PROCESS_DRUIDS;
		}
		// We have free space in inventory, so lets go to the druids and fill it
		// up!
		return State.GO_TO_DRUIDS;
	}

	public static void BANKING() {
		if (Player.getPosition().distanceTo(bankTile) >= 3) {
			// failsafe.
			GO_TO_BANK();
		}

		statusText = "Banking..";

		while (!Banking.isBankScreenOpen() || Banking.isPinScreenOpen()) {
			Banking.openBankBooth();
			General.sleep(200, 250);
		}

		if (Banking.isPinScreenOpen()) {
			Banking.inPin();
		}

		while (!Banking.isBankScreenOpen()) {
			General.sleep(100, 150);
		}

		Banking.depositAll();
		General.sleep(1000, 1200);

		if (foodCount > 0) {
			Banking.withdraw(foodCount, foodID);
			General.sleep(1000, 1300);
		}
		Banking.close();
	}

	public static void PROCESS_DRUIDS() {
		if (foodCount > 0 && (player.getMaxHealth() / 3) >= player.getHealth() && Inventory.find(foodID).length > 0) {
			// health is below 33,3%. we have food, we should eat.
			statusText = "Eating..";

			// Double inventory.find call but since 99% of the time we wont
			// reach these conditions it's better performance to do the double
			// call now.
			RSItem[] food = Inventory.find(foodID);
			if (food.length > 0) {
				food[0].click("Eat");
			}
		}

		if (!player.isInCombat()) {
			// todo: looting.

			statusText = "Killing druids..";

			RSNPC[] druids = NPCs.find(NPCs
					.generateFilterGroup(new Filter<RSNPC>() {
						@Override
						public boolean accept(RSNPC npc) {
							return !npc.isInCombat()
									&& npc.getName() == "Chaos druid";
						}
					}));

			druids = NPCs.sortByDistance(player.getPosition(), druids);

			for (int i = 0; i < druids.length; i++) {
				if (PathFinding.canReach(druids[i], false)) {
					// We got a potential druid we can kill.
					if (!druids[i].isOnScreen()) {
						Camera.turnToTile(druids[i].getPosition());
					}

					int failsafe = 0;
					while (!player.isInCombat() && druids[i].isValid() && failsafe <= 20) {
						if (!druids[i].isInCombat()) {
							druids[i].click("Attack");
							General.sleep(200, 300);
							failsafe++;
						}
					}
				}
			}
		} else {
			// While we attack a druid we'll prepare for the next one.(If there
			// are more)

			// todo: test & debug

			RSNPC[] druids = NPCs.find(NPCs
					.generateFilterGroup(new Filter<RSNPC>() {
						@Override
						public boolean accept(RSNPC npc) {
							return !npc.isInCombat()
									&& npc.getName() == "Chaos druid";
						}
					}));

			druids = NPCs.sortByDistance(player.getPosition(), druids);

			for (int i = 0; i < druids.length; i++) {
				if (PathFinding.canReach(druids[i], false)) {
					// We got a new potential druid we can hover over.
					if (!druids[i].isOnScreen()) {
						Camera.turnToTile(druids[i].getPosition());
					}

					while (player.isInCombat() && druids[i].isValid()
							&& !druids[i].isInCombat()) {
						if (!druids[i].isInCombat()) {
							druids[i].hover();
							General.sleep(200, 300);
						}
					}

					if (!player.isInCombat()) {
						// We killed the current druid, and next druid isn't in
						// combat yet.
						druids[i + 1].click("Attack");

						// No retrying here, if it fails the states will catch
						// back up from the start
					}
				}
			}
		}
	}

	public static void GO_TO_BANK() {
		statusText = "Going to bank..";

		if (Player.getPosition().distanceTo(druidTowerCenter) <= 2) {
			// We are in the tower, first open the door.
			RSObject door = Objects.findNearest(6, towerDoorID)[0];
			if (door != null) {
				while (!Player.getPosition().equals(druidTowerDoor)) {
					door.click("Open");
					General.sleep(500, 800);
				}
			}
		}
		while (!Player.getPosition().equals(log_west)) {
			General.println("Walking");
			Walking.walkPath(Walking.invertPath(logToTower));
			General.sleep(2000, 2500);
		}

		// Arrived at the log crossing.
		statusText = "Crossing log..";

		RSObject log = Objects.findNearest(3, logFromTowerID)[0];
		if (log != null) {
			while (!Player.getPosition().equals(log_east) && !Player.getPosition().equals(failed_log_walk[1])) {
				log.click("Walk-across");
				General.sleep(800, 1000);
			}

			statusText = "Going to bank..";

			while (Player.getPosition().distanceTo(bankTile) >= 3) {
				Walking.walkPath(Walking.invertPath(bankToLog));
				General.sleep(2000, 2500);
			}
			// Arrived at the bank
		}
	}

	public static void GO_TO_DRUIDS() {
		if (!(Player.getPosition().distanceTo(druidTowerCenter) <= 2)) {
			statusText = "Going to the druids..";

			while (!Player.getPosition().equals(log_east)) {
				Walking.walkPath(bankToLog);
				General.sleep(2000, 2500);
			}

			// Arrived at the log crossing.
			statusText = "Crossing log..";

			RSObject log = Objects.findNearest(3, logFromBankID)[0];
			if (log != null) {
				while (!Player.getPosition().equals(log_west) && !Player.getPosition().equals(failed_log_walk[0])) {
					log.click("Walk-across");
					General.sleep(800, 1000);
				}

				statusText = "Going to the druids..";

				while (!Player.getPosition().equals(druidTowerDoor)) {
					Walking.walkPath(logToTower);
					General.sleep(2000, 2500);
				}

				// Arrived at the tower, lets picklock the door
				statusText = "Picklocking door..";

				RSObject door = Objects.findNearest(10, towerDoorID)[0];
				if (door != null) {
					while ((!(Player.getPosition().distanceTo(druidTowerCenter) <= 2)) && Player.getPosition().equals(druidTowerDoor)) {
						door.click("Pick-lock");
						General.sleep(500, 800);
					}
				}
			}
		}
	}

	// Here be paint stuff.

	private final Color colorOrange = new Color(215, 131, 0);
	private final Font font = new Font("Arial", 1, 13);
	private final Image paint = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/Paints/chaoskiller.png");
	private final Image paintShow = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/Paints/chaoskillertoggle.png");
	private final Rectangle paintToggle = new Rectangle(493, 345, 20, 20);
	private boolean showPaint = true;

	@Override
	public void onPaint(Graphics g) {
		if (showPaint) {
			g.drawImage(paint, 3, 345, null);
			g.setFont(font);
			g.setColor(colorOrange);
			g.drawString("Status: " + statusText, 25, 416);

			// todo: add more stats

		} else
			g.drawImage(paintShow, paintToggle.x, paintToggle.y, null);
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void mouseReleased(Point p, int arg1, boolean arg2) {
		if (paintToggle.contains(p.getLocation()))
			showPaint = !showPaint;
	}

	@Override
	public void mouseClicked(Point arg0, int arg1, boolean arg2) {
	}

	@Override
	public void mouseDragged(Point arg0, int arg1, boolean arg2) {
	}

	@Override
	public void mouseMoved(Point arg0, boolean arg1) {
	}

}
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
import java.net.URL;

import javax.imageio.ImageIO;

import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.MouseActions;
import org.tribot.script.interfaces.Painting;

enum State {
	BANKING {
		@Override
		void run() { LANChaosKiller.doBanking(); }
	},
	GO_TO_DRUIDS {
		@Override
		void run() { LANChaosKiller.goToDruids(); }
	},
	PROCESS_DRUIDS {
		@Override
		void run() { LANChaosKiller.doProcessDruids(); }
	},
	GO_TO_BANK {
		@Override
		void run() { LANChaosKiller.goToBank(); }
	};
	
	abstract void run();
}

@ScriptManifest(authors = { "Laniax" }, category = "Combat", name = "LAN's Chaos Killer", description = "Flawless Ardougne Chaos Druid Killer.")
public class LANChaosKiller extends Script implements Painting, MouseActions {
	// Global defines
	private static boolean quitting = false;
	private static String statusText = "Starting..";
	private static final RSPlayer player = Player.getRSPlayer();

	// Script defines
	private static int foodID = 379; // todo: grab from settings
	private static int foodCount = 1; // todo: grab from settings
	private static final int LOOT_IDS[] = { 
			563, // law rune
			199, // guam leaf
			201, // marrentill
			203, // tarromin
			205, // harralander
			207, // ranarr
			209, // irit
			211, // avantoe
			213, // kwuarm
			215, // cadantine
			217, // dwarf weed
			219, // torsol
			2485, // lantadyme
			3051, // snapdragon

	};// todo: grab from settings
	
	private static final int LOG_EAST_MODEL_POINT_COUNT = 54;
	private static final int LOG_WEST_MODEL_POINT_COUNT = 114;
	
	private static final int TOWER_DOOR_MODEL_POINT_COUNT = 168;
	private static final int MAX_FAILSAFE_ATTEMPTS = 20;
	
	private static final RSTile POS_OUTSIDE_DRUID_TOWER_DOOR = new RSTile(2565, 3356);
	private static final RSTile POS_DRUID_TOWER_CENTER = new RSTile(2562, 3356);
	private static final RSTile POS_BANK_CENTER = new RSTile(2617, 3332);
	private static final RSTile POS_LOG_EAST = new RSTile(2602, 3336);
	private static final RSTile POS_LOG_WEST = new RSTile(2598, 3336);

	private static final RSTile[] POS_LOG_WALK_FAILED = new RSTile[] {
			new RSTile(2598, 3329), // east (tower side)
			new RSTile(2604, 3330) // west (bank side)
	};
	
	private final static RSTile[] PATH_BANK_TO_LOG = new RSTile[] {
			new RSTile(2617, 3332, 0), 
			new RSTile(2616, 3337, 0),
			new RSTile(2613, 3339, 0), 
			new RSTile(2610, 3339, 0),
			new RSTile(2602, 3336, 0) 
	};

	private final static RSTile[] PATH_LOG_TO_TOWER = new RSTile[] {
			new RSTile(2598, 3336, 0), 
			new RSTile(2588, 3340, 0),
			new RSTile(2587, 3344, 0), 
			new RSTile(2576, 3350, 0),
			new RSTile(2565, 3356, 0) 
	};

	@Override
	public void run() {
		while (!quitting) {
			getState().run();
			sleep(General.random(40, 80));
		}
	}

	private State getState() {
		if (Inventory.isFull() || (foodCount > 0 && Inventory.find(foodID).length == 0 && (player.getMaxHealth() / 2) <= player.getHealth())) {
			if (Player.getPosition().distanceTo(POS_BANK_CENTER) <= 2) {
				// We are at the bank and in need of some banking action.
				return State.BANKING;
			}
			// Inventory is full, we should move to bank to empty it.
			return State.GO_TO_BANK;
		}
		
		if (Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2) {
			// We are at the druids (in the tower).
			return State.PROCESS_DRUIDS;
		}
		// We have free space in inventory, so lets go to the druids and fill it
		// up!
		return State.GO_TO_DRUIDS;
	}

	public static void doBanking() {
		// failsafe.
		if (Player.getPosition().distanceTo(POS_BANK_CENTER) >= 3) {
			goToBank();
		}

		statusText = "Banking..";

		int failsafe = 0;
		while ((!Banking.isBankScreenOpen() || Banking.isPinScreenOpen()) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			if (Banking.openBankBooth())
				General.sleep(200, 250);
			failsafe++;
		}

		if (Banking.isPinScreenOpen()) {
			Banking.inPin();
		}

		while (!Banking.isBankScreenOpen() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			General.sleep(100, 150);
			failsafe++;
		}

		Banking.depositAll();
		General.sleep(1000, 1200);

		if (foodCount > 0) {
			Banking.withdraw(foodCount, foodID);
			General.sleep(1000, 1300);
		}
		Banking.close();
	}

	public static void doProcessDruids() {
		if (foodCount > 0 && (player.getMaxHealth() / 3) >= player.getHealth() && Inventory.find(foodID).length > 0) {
			// health is below 33,3%. we have food, we should eat.
			statusText = "Eating..";

			// Double inventory.find call but since 99% of the time we wont
			// reach these conditions it's better performance to do the double call now.
			RSItem[] food = Inventory.find(foodID);
			if (food.length > 0) {
				food[0].click("Eat");
			}
		}

		if (!player.isInCombat()) {

			//Lets do some looting.
			RSGroundItem lootItems[] = GroundItems.findNearest(LOOT_IDS);
			if (lootItems.length > 0)
			{
				statusText = "Looting..";
				for (RSGroundItem item : lootItems)
				{
					// Apparently just 'Take' would causes issues with multiple items on 1 tile.
					if (item.click("Take "+ item.getDefinition().getName()))
						General.sleep(1000,2000);
				}
			}
			

			statusText = "Killing druids..";
			
			RSNPC[] druids = Utilities.findNearest(5, "Chaos druid", true);
			druids = NPCs.sortByDistance(player.getPosition(), druids);

			for (int i = 0; i < druids.length; i++) {
				if (PathFinding.canReach(druids[i], false)) {
					// We got a potential druid we can kill.
					if (!druids[i].isOnScreen()) {
						Camera.turnToTile(druids[i]);
					}

					int failsafe = 0;
					while (!player.isInCombat() && druids[i].isValid() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
						if (!druids[i].isInteractingWithMe() && !druids[i].isInCombat()) {
							if (druids[i].click("Attack"))
								General.sleep(50, 150);
							
							failsafe++;
						}
					}
					
					if (druids[i].isInteractingWithMe())
					{
						// we are in combat with current druid.
						// while we do that, prepare for next druid
						if (druids.length > i+1)
						{
							if (PathFinding.canReach(druids[i+1], false)) {
								// We got a new potential druid we can hover over.
								if (!druids[i+1].isOnScreen()) {
									Camera.turnToTile(druids[i+1]);
								}

								failsafe = 0;
								while (player.isInCombat() && druids[i+1].isValid() && !druids[i+1].isInCombat() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
									if (!druids[i+1].isInCombat()) {
										if (druids[i+1].hover())
											General.sleep(200, 300);
										failsafe++;
									}
								}

								if (!player.isInCombat() && !druids[i+1].isInCombat() && !druids[i].isInteractingWithMe()) {
									// We killed the current druid, and next druid isn't in combat yet.
									if (druids[i+1].click("Attack"))
										General.sleep(2000, 3000);
									// No retrying here, if it fails the states will catch back up from the start
								}
							}
						}
					}
				}
			}
		}
	}

	public static void goToBank() {
		statusText = "Going to bank..";

		if (Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2) {
			// We are in the tower, first open the door.
			RSObject[] doors = Utilities.findNearest(5, TOWER_DOOR_MODEL_POINT_COUNT);

			if (doors.length > 0) {
				Camera.turnToTile(doors[0]); // always turn the camera, otherwise it has a hard time clicking
				int failsafe = 0;
				
				while (!Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
					if (doors[0].click("Open"));
						General.sleep(500, 800);
					failsafe++;
				}
			}
		}		
		
		int failsafe = 0;
		while (!Player.getPosition().equals(POS_LOG_WEST) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			Walking.walkPath(Walking.invertPath(PATH_LOG_TO_TOWER));
			General.sleep(2000, 2500);
			failsafe++;
		}

		// Arrived at the log crossing.
		statusText = "Crossing log..";

		RSObject[] logs = Utilities.findNearest(5, LOG_WEST_MODEL_POINT_COUNT);
		
		if (logs.length > 0) {
			if (!logs[0].isOnScreen())
				Camera.turnToTile(logs[0]);
			
			failsafe = 0;
			while (!Player.getPosition().equals(POS_LOG_EAST) && !Player.getPosition().equals(POS_LOG_WALK_FAILED[1]) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
				logs[0].click("Walk-across");
				General.sleep(4000, 5000);
				failsafe++;
			}

			statusText = "Going to bank..";

			failsafe = 0;
			while (Player.getPosition().distanceTo(POS_BANK_CENTER) >= 3 && failsafe < MAX_FAILSAFE_ATTEMPTS) {
				Walking.walkPath(Walking.invertPath(PATH_BANK_TO_LOG));
				General.sleep(2000, 2500);
				failsafe++;
			}
			// Arrived at the bank
		}
	}

	public static void goToDruids() {
		if (!(Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2)) {
			statusText = "Going to the druids..";

			int failsafe = 0;
			while (!Player.getPosition().equals(POS_LOG_EAST) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
				Walking.walkPath(PATH_BANK_TO_LOG);
				General.sleep(2000, 2500);
				failsafe++;
			}

			// Arrived at the log crossing.
			statusText = "Crossing log..";
			
			RSObject[] logs = Utilities.findNearest(5, LOG_EAST_MODEL_POINT_COUNT);
			
			if (logs.length > 0) {
				if (!logs[0].isOnScreen())
					Camera.turnToTile(logs[0]);
				
				failsafe = 0;
				while (!Player.getPosition().equals(POS_LOG_WEST) && !Player.getPosition().equals(POS_LOG_WALK_FAILED[0]) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
					if (logs[0].click("Walk-across"))
						General.sleep(800, 1000);
					failsafe++;
				}

				statusText = "Going to the druids..";

				failsafe = 0;
				while (!Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
					Walking.walkPath(PATH_LOG_TO_TOWER);
					General.sleep(2000, 2500);
				}

				// Arrived at the tower, lets picklock the door
				statusText = "Picklocking door..";

				RSObject[] doors = Utilities.findNearest(5, TOWER_DOOR_MODEL_POINT_COUNT);
				
				if (doors.length > 0) {
					Camera.turnToTile(doors[0]); // always turn the camera, otherwise it has a hard time clicking
					
					failsafe = 0;
					while ((!(Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2)) && Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
						if (doors[0].click("Pick-lock"));
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
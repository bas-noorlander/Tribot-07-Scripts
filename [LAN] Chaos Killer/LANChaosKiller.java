package scripts;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.EventBlockingOverride;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.RandomEvents;

import scripts.Defines.ItemIDs;
import scripts.Defines.State;
import scripts.Managers.AntibanMgr;
import scripts.Managers.CombatMgr;
import scripts.Managers.GeneralMgr;
import scripts.Managers.MovementMgr;
import scripts.Managers.PaintMgr;
import scripts.Managers.ThreadingMgr;

/**
 * [LAN] Chaos Killer
 *   Kills druids in the tower above ardougne for combat exp + herbs.
 *   Supports tribot's ABC system.
 *   
 * @author Laniax
 */

@ScriptManifest(authors = { "Laniax" }, category = "Combat", name = "[LAN] Chaos Killer")
public class LANChaosKiller extends Script implements Painting, EventBlockingOverride, RandomEvents, Ending{

	public static String statusText = "Starting..";
	public static String foodName = "Lobster";
	
	private static boolean quitting = false;
	public static boolean waitForGUI = true;
	public static boolean isDoingRandom = false;
	private static boolean useLogCrossing = true;
	private static boolean wasIdle = false;

	public static int foodCount = 0;
	public static int eatBelowPercent = 50;

	public static ArrayList<Integer> lootIDs = new ArrayList<Integer>();
	private final static ArrayList<Integer> dropItemIds = new ArrayList<Integer>();

	public static final RSTile POS_STAIRS_DOWNSTAIRS_TOWER = new RSTile(2563, 9756);
	public static final RSArea AREA_DOWNSTAIRS_TOWER = new RSArea(new RSTile(2561, 9757), new RSTile(2592, 9730));
	private static final RSTile POS_OUTSIDE_DRUID_TOWER_DOOR = new RSTile(2565, 3356, 0);
	public static final RSTile POS_DRUID_TOWER_CENTER = new RSTile(2562, 3356, 0);
	public static final RSTile POS_BANK_CENTER = new RSTile(2617, 3332);
	private static final RSTile POS_LOG_EAST = new RSTile(2602, 3336);
	private static final RSTile POS_LOG_WEST = new RSTile(2598, 3336);

	private static final int COORD_X_LEFT_RIVER = 2599;
	private static final int COORD_X_RIGHT_RIVER = 2601;

	private static GUI gui;

	// singleton
	public static GUI getGUI() {
		return gui = gui == null ? new GUI() : gui;
	}

	public LANChaosKiller() {
		dropItemIds.add(526);  // bones
		dropItemIds.add(227);  // vial of water
		dropItemIds.add(1971); // kebab
		dropItemIds.add(1917); // beer
		dropItemIds.add(558);  // mind rune
		dropItemIds.add(231);  // snape grass
		dropItemIds.add(1291); // bronze longsword
		dropItemIds.add(117);  // 2 dose strength potion
	}

	@Override
	public void run() {

		// wait until login bot is done.
		while (Login.getLoginState() != Login.STATE.INGAME)
			sleep(250);

		useLogCrossing = Skills.getActualLevel(SKILLS.AGILITY) >= 33;
		
		// We do this after making sure we logged in, otherwise we can't get the xp.
		PaintMgr.startSkillInfo.put(SKILLS.ATTACK, Skills.getXP(SKILLS.ATTACK));
		PaintMgr.startSkillInfo.put(SKILLS.STRENGTH, Skills.getXP(SKILLS.STRENGTH));
		PaintMgr.startSkillInfo.put(SKILLS.DEFENCE, Skills.getXP(SKILLS.DEFENCE));
		PaintMgr.startSkillInfo.put(SKILLS.HITPOINTS, Skills.getXP(SKILLS.HITPOINTS));
		PaintMgr.startSkillInfo.put(SKILLS.RANGED, Skills.getXP(SKILLS.RANGED));
		PaintMgr.startSkillInfo.put(SKILLS.MAGIC, Skills.getXP(SKILLS.MAGIC));

		PaintMgr.showPaint = true;
		
		General.println("[LAN] ChaosKiller: *WARNING* this is an EXPERIMENTAL version. EATING and RANDOM EVENTS are NOT tested! Babysit this script!");
		
		if (!useLogCrossing)
			General.println("[LAN ChaosKiller]: Detected that you are lower then 33 agility. We will walk over the bridge instead of the log.");

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				getGUI().setVisible(true);
			}});

		while (waitForGUI)
			sleep(250);

		General.useAntiBanCompliance(true);
		ThreadingMgr.start();

		while (!quitting) {
			State.getState().run();
			sleep(50);
		}

		onEnd();
	}

	@Override
	public void onEnd() {
		//GeneralMgr.sendSignatureData((System.currentTimeMillis() - PaintMgr.startTime) / 1000, Skills.getXP(SKILLS.ATTACK) - PaintMgr.startSkillInfo[0], Skills.getXP(SKILLS.STRENGTH) - PaintMgr.startSkillInfo[1], Skills.getXP(SKILLS.DEFENCE) - PaintMgr.startSkillInfo[2], PaintMgr.itemsLooted);
	}

	/**
	 * Handles overriding of combat random events when we are in the tower.
	 * It will run downstairs and wait there until we are no longer being attacked.
	 */
	@Override
	public void onRandom(RANDOM_SOLVERS random) {
		if (random.equals(RANDOM_SOLVERS.COMBATRANDOM)) {
			if (State.getState().equals(State.KILL_DRUIDS)) {

				// the failsafe thread will run us up.
				if (AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition()))
					return;

				isDoingRandom = true;

				statusText = "Yikes! a nasty combat random!";

				// Climb down the stairs
				if (GeneralMgr.interactWithObject(new Condition() {
					public boolean active() {
						return AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition());
					}}, "Climb-down")) {

					// Okay we should be good to go again, lets go up the stairs and continue :)
					GeneralMgr.interactWithObject(new Condition() {
						public boolean active() {
							return Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) < 3;
						}}, "Climb-up");
				}

				isDoingRandom = false;
			}
		}
	}

	/**
	 * Handles the banking logic when at the bank.
	 */
	public static void doBanking() {

		statusText = "Banking";

		if (Banking.openBank()) {

			// Pin is handled by Tribot.

			GeneralMgr.waitFor(new Condition() {
				public boolean active() {
					return Banking.isBankScreenOpen();
				}});


			Banking.depositAll();

			GeneralMgr.waitFor(new Condition() {
				public boolean active() {
					return Inventory.getAll().length == 0;
				}});

			if (foodCount > 0) {
				Banking.withdraw(foodCount, foodName);

				GeneralMgr.waitFor(new Condition() {
					public boolean active() {
						return Inventory.getCount(new String[]{foodName}) >= foodCount;
					}});
			}

			Banking.close();
		}
	}

	/**
	 * Checks if there are items if we want on the ground, and if so, loot them.
	 * Drops items we accidently picked up.
	 */
	public static void doLooting() {

		if (lootIDs != null && lootIDs.size() > 0) {

			final int ids[] = GeneralMgr.buildIntArray(lootIDs);
			final RSGroundItem[] lootItems = GroundItems.find(ids);

			if (lootItems.length > 0) {

				for (int i = 0; i < 2; i++) {
					for (final RSGroundItem item : lootItems) {

						if (Inventory.isFull())
							return;

						final ItemIDs itemID = ItemIDs.valueOf(item.getID());
						final int stackSize = item.getStack();
						final int haveAmount = Inventory.getCount(item.getID());
						final RSItemDefinition itemDef = item.getDefinition();

						if (itemDef != null) {

							statusText = "Looting "+stackSize+" "+(itemID != null ? itemID.toString() : itemDef.getName());

							if (Clicking.click("Take "+ itemDef.getName(), item)) {

								if (GeneralMgr.waitFor(new Condition() {
									public boolean active() {
										return Inventory.getCount(item.getID()) > haveAmount;
									}}, General.random(1200, 2200))) {
								}
							}
						}
					}
				}

				// Drop shit we didn't want.
				for (ItemIDs itemID : ItemIDs.values()) {
					if (!lootIDs.contains(itemID.getID()) && !dropItemIds.contains(itemID.getID()) && Inventory.find(itemID.getID()).length > 0)
						dropItemIds.add(itemID.getID());
				}

				Inventory.drop(GeneralMgr.buildIntArray(dropItemIds));
			}
		}
	}

	/**
	 * Handles the finding & killing logic of chaos druids when we are in the tower.
	 */
	public static void doKillDruids() {

		if (!CombatMgr.isUnderAttack()) {
			
			doLooting();
			
			statusText = "Finding druids";
			
			final RSNPC npcs[] = AntibanMgr.orderOfAttack(NPCs.findNearest("Chaos druid"));
			
			if (npcs == null || npcs.length == 0) {
				// Nothing to attack, idling
				AntibanMgr.doIdleActions();
				wasIdle = true;
				return;
			}
			
			// NPCs to attack are found.
			
			// if we didn't have to turn the run on, we have to wait a small 'reaction time' before attacking the new npc.
			// The reason why i do this only if run wasn't set is because run takes time itself to set (longer then our delays), so it would wait double and be un-humanlike.
			if (!AntibanMgr.doActivateRun()){
				
				if (wasIdle) {
					// If we were idle (waiting for spawn)
					// we should see this as a 'new' action and wait an appropriate amount of time.
					AntibanMgr.doDelayForNewObject();
				} else {
					// if we were not idling (new npc was already spawned while fighting old)
					// we should see this as a 'switch' action and wait an appropriate amount of time. 
					AntibanMgr.doDelayForSwitchObject();
				}
			}
			
			statusText = "Killing druids";
			
			if (CombatMgr.attackNPCs(npcs, true) != null) {
				// succesfully attacked npc
			//	AntibanMgr.doCheckXP();
				wasIdle = false;
			}
		}
	}

	/**
	 * Handles the movement logic towards the bank.
	 */
	public static void goToBank() {

		if (AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition()))
			return;

		if (Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2) {

			statusText = "Opening door";

			// We are in the tower, first open the door.
			while (!GeneralMgr.interactWithObject(new Condition() {
				public boolean active() {
					return Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) >= 3;
				}}, "Open", General.random(2000, 3000))) {}
		}

		// if we are left from river and should use the log crossing
		if (useLogCrossing && Player.getPosition().getX() < COORD_X_LEFT_RIVER) {

			statusText = "Going to log";

			if (MovementMgr.walkTo(POS_LOG_WEST)) {

				if (GeneralMgr.waitFor(new Condition() {
					public boolean active() {
						return Player.getPosition().distanceTo(POS_LOG_WEST) < 5 && !Player.isMoving();
					}}, General.random(14000, 18000))) {

					statusText = "Crossing log";

					for (int i = 0; i < 20; i++) {
						if (GeneralMgr.interactWithObject(new Condition() {
							public boolean active() {
								return Player.getPosition().getX() > COORD_X_RIGHT_RIVER;
							}}, "Walk-across")) 
							break;
					}
				}
			}
		}

		statusText = "Going to bank";

		if (MovementMgr.walkTo(POS_BANK_CENTER)) {

			GeneralMgr.waitFor(new Condition() {
				public boolean active() {
					return Player.getPosition().distanceTo(POS_BANK_CENTER) <= 2 && !Player.isMoving();
				}}, General.random(14000, 18000));
		}
	}

	/**
	 * Handles the movement logic towards the tower with druids.
	 */
	public static void goToDruids() {

		if (AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition()))
			return;

		if (useLogCrossing && Player.getPosition().getX() > COORD_X_RIGHT_RIVER) {

			statusText = "Going to log";

			if (MovementMgr.walkTo(POS_LOG_EAST)) {

				if (GeneralMgr.waitFor(new Condition() {
					public boolean active() {
						return Player.getPosition().distanceTo(POS_LOG_EAST) < 5 && !Player.isMoving();
					}}, General.random(14000, 18000))) {

					statusText = "Crossing log";

					for (int i = 0; i < 20; i++) {
						if (GeneralMgr.interactWithObject(new Condition() {
							public boolean active() {
								return Player.getPosition().getX() < COORD_X_LEFT_RIVER;
							}}, "Walk-across", General.random(2000, 3000)))
							break;
					}
				}
			}
		}

		statusText = "Going to tower";

		MovementMgr.walkTo(POS_OUTSIDE_DRUID_TOWER_DOOR);

		GeneralMgr.waitFor(new Condition() {
			public boolean active() {
				return Player.getPosition().distanceTo(POS_OUTSIDE_DRUID_TOWER_DOOR) < 7;
			}});

	}

	/**
	 * Handles the logic to pick-lock the tower door.
	 */
	public static void doPicklockDoor() {

		statusText = "Picklocking door";

		// Always rotate camera when picklocking, else it might have a hard time clicking.
		Camera.turnToTile(POS_OUTSIDE_DRUID_TOWER_DOOR);

		if (GeneralMgr.interactWithObject(new Condition() {
			public boolean active() {
				return Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2;
			}}, "Pick-lock", General.random(50, 90))) {

			Camera.setCameraAngle(General.random(80, 100));
		}
	}

	// Paint is handled in different file for better readability

	public void onPaint(Graphics g) { PaintMgr.onPaint(g); }

	// Mouse actions below are for hiding/showing paint.
	
	@Override
	public OVERRIDE_RETURN overrideMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			if (PaintMgr.paintToggle.contains(e.getPoint())) {
				
				PaintMgr.showPaint = !PaintMgr.showPaint;
				
				e.consume();
				return OVERRIDE_RETURN.DISMISS;
			} else if (PaintMgr.settingsToggle.contains(e.getPoint())) {

				LANChaosKiller.getGUI().setVisible(!LANChaosKiller.getGUI().isVisible());
				
				e.consume();
				return OVERRIDE_RETURN.DISMISS;
			} else if (PaintMgr.herbToggle.contains(e.getPoint())) {
				
				PaintMgr.showHerbIdentifier = !PaintMgr.showHerbIdentifier;
				
				e.consume();
				return OVERRIDE_RETURN.DISMISS;
			}
		}
		
		return OVERRIDE_RETURN.PROCESS;
	}

	// unused overrides
	public OVERRIDE_RETURN overrideKeyEvent(KeyEvent e) {return OVERRIDE_RETURN.SEND;}
	public boolean randomFailed(RANDOM_SOLVERS random) { isDoingRandom = false; return true; }
	public void randomSolved(RANDOM_SOLVERS random) {isDoingRandom = false;}
}
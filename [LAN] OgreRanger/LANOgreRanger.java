package scripts;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Equipment.SLOTS;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.EventBlockingOverride;
import org.tribot.script.interfaces.Painting;

import scripts.Definitions.GUI;
import scripts.Definitions.ItemIDs;
import scripts.Definitions.Location;
import scripts.Definitions.State;
import scripts.Mgrs.AntibanMgr;
import scripts.Mgrs.CombatMgr;
import scripts.Mgrs.MovementMgr;
import scripts.Mgrs.PaintMgr;

/**
 * @author Laniax
 */
@ScriptManifest(authors = { "Laniax" }, category = "Ranged", name = "[LAN] OgreRanger")
public class LANOgreRanger extends Script implements Painting, EventBlockingOverride{
	
	public static boolean isQuitting = false;
	public static boolean waitForGUI = true;
	
	public static boolean pickUpArrows = false;
	public static int arrowId = 0;
	public static boolean pickUpArrowsAlways = false;
	public static int pickUpArrowCount = 10;
	public static boolean pickUpArrowsOnlyAboveAmount = false;
	
	public static boolean useSpec = false;
	public static int foodCount = 0;
	public static String foodName = "Lobster";
	public static String statusText = "Starting up..";
	public static ArrayList<Integer> lootIDs = new ArrayList<Integer>();

	public static Location scriptLocation;
	public static RSTile POS_SAFESPOT;
	public static RSArea AREA_BANK;
	
	private static boolean wasIdle = false;
	private static long idleSince;
	
	private static GUI gui;

	// singleton
	public static GUI getGUI() {
		return gui = gui == null ? new GUI() : gui;
	}

	@Override
	public void run() {
		
		// wait until login bot is done.
		while (Login.getLoginState() != Login.STATE.INGAME)
			General.sleep(250);
		
		PaintMgr.showPaint = true;
		
		PaintMgr.startRangeXP = Skills.getXP(SKILLS.RANGED);

		if (Combat.isAutoRetaliateOn())
			Combat.setAutoRetaliate(false);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				getGUI().setVisible(true);
			}});

		while (waitForGUI)
			sleep(250);
		
		switch (scriptLocation) {
		
			case CASTLE_WARS:
				
				POS_SAFESPOT = new RSTile(2496, 3096, 0);
				
				AREA_BANK = new RSArea(new RSTile(2437, 3088, 0), new RSTile(2443, 3082, 0));
				
				break;
				
			case NORTH_OF_ARDOUGNE:
				
				POS_SAFESPOT = new RSArea(new RSTile(0, 0, 0), new RSTile(0, 0, 0)).getRandomTile();
				
				AREA_BANK = new RSArea(new RSTile(0, 0, 0), new RSTile(0, 0, 0));
				
				break;
		}
		
		General.useAntiBanCompliance(true);
		
		while (!isQuitting) {
			
			State.getState().run();
			
			General.sleep(100);
		}
		
		General.println("Thank you for using [LAN] OgreRanger =) Have a nice day.");
	}
	
	/**
	 * Handles the banking logic when at the bank.
	 */
	public static void doBanking() {

		statusText = "Banking";

		if (Banking.openBank()) {

			// Pin is handled by Tribot.

			Timing.waitCondition(new Condition() {
				public boolean active() {
					return Banking.isBankScreenOpen();
				}}, General.random(7000, 9000));


			Banking.depositAll();

			Timing.waitCondition(new Condition() {
				public boolean active() {
					return Inventory.getAll().length == 0;
				}}, General.random(7000, 9000));
			
			RSItem arrowsEquiped = Equipment.getItem(SLOTS.ARROW);
			if (arrowsEquiped == null || arrowsEquiped.getStack() < 100) {
				
				General.println("We are low on arrows! Checking bank for more");
				RSItem[] arrows = Banking.find(arrowId);
				
				if (arrows.length == 0 || arrows[0].getStack() < 50) {
					General.println("There are less than 50 arrows (or none at all) in your bank =( Safely logging out here..");
					
					if (Banking.close()) {
						if (Login.logout()) {
							isQuitting = true;
							General.println("Logged out. You gained "+PaintMgr.xpGained+" ranged xp over "+Timing.msToString(System.currentTimeMillis() - PaintMgr.startTime)+" hours.");
							return;
						}
					}
				}
		
				General.println("There are some more arrows in bank =)");
				Banking.withdrawItem(arrows[0], General.random(2000, 4000));
						
				Timing.waitCondition(new Condition() {
					public boolean active() {
						return Inventory.getCount(arrowId) >= 50;
					}}, General.random(7000, 9000));
			}
			
		if (foodCount > 0) {
			Banking.withdraw(foodCount, foodName);

			Timing.waitCondition(new Condition() {
				public boolean active() {
					return Inventory.getCount(new String[]{foodName}) >= foodCount;
				}}, General.random(7000, 9000));
		}
		Banking.close();
		}
	}
	
	/**
	 * Handles the finding & killing logic of the ogres.
	 */
	public static void doKillOgres() {
		
		CombatMgr.eatIfNecessary();

		if (!CombatMgr.isUnderAttack()) {
			
			doLooting();
			
			// we may NEVER attack an NPC if we are NOT in our safezone!
			if (!Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
				return;
			}
			
			statusText = "Finding Ogre";
			
			final RSNPC npcs[] = AntibanMgr.orderOfAttack(NPCs.find("Ogre"));
			
			if (npcs == null || npcs.length == 0) {
				// Nothing to attack, idling
				AntibanMgr.doIdleActions();
				wasIdle  = true;
				idleSince = System.currentTimeMillis();
				return;
			}
			
			// NPCs to attack are found.
			
			// if we didn't have to turn the run on, we have to wait a small 'reaction time' before attacking the new npc.
			// The reason why i do this only if run wasn't set is because run takes time itself to set (longer then our delays), so it would wait double and be un-humanlike.
			if (!AntibanMgr.doActivateRun()){
				
				if (wasIdle && idleSince > (System.currentTimeMillis() + General.random(8000, 12000))) {
					// If we were idle (waiting for spawn) for more then 8-12 sec
					// we should see this as a 'new' action and wait an appropriate amount of time.
					AntibanMgr.doDelayForNewObject();
				} else {
					// if we were not idling (new npc was already spawned while fighting old, or within the 8-12sec after death)
					// we should see this as a 'switch' action and wait an appropriate amount of time.
					AntibanMgr.doDelayForSwitchObject();
				}
			}
			
			// we may NEVER attack an NPC if we are NOT in our safezone!
			if (!Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
				return;
			}
			
			statusText = "Killing Ogre";
			
			if (CombatMgr.attackNPCs(npcs, true)) {
				// succesfully attacked/killed npc
				
				CombatMgr.eatIfNecessary();
				
				if (!CombatMgr.isUnderAttack()){
					doLooting();
					AntibanMgr.doIdleActions();
				}
				wasIdle = false;
			}
		}
	}
	
	/**
	 * Handles the movement logic towards the bank.
	 */
	public static void goToBank() {

		statusText = "Going to bank";

		if (MovementMgr.walkTo(AREA_BANK.getRandomTile())) {

			Timing.waitCondition(new Condition() {
				public boolean active() {
					return AREA_BANK.contains(Player.getPosition()) && !Player.isMoving();
				}}, General.random(5000, 7000));
		}
	}

	/**
	 * Handles the movement logic towards the ogres.
	 */
	public static void goToOgres() {

		statusText = "Going to safespot";

		MovementMgr.walkTo(POS_SAFESPOT);
		
		// possibly open door when location == north of ardougne

		Timing.waitCondition(new Condition() {
			public boolean active() {
				return Player.getPosition().equals(POS_SAFESPOT);
			}}, General.random(2000, 4000));

	}
	
	/**
	 * Checks if there are items if we want on the ground, and if so, loot them.
	 * Drops items we accidently picked up.
	 */
	public static void doLooting() {

		if (lootIDs != null && lootIDs.size() > 0) {

			final int ids[] = buildIntArray(lootIDs);
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

								if (Timing.waitCondition(new Condition() {
									public boolean active() {
										return Inventory.getCount(item.getID()) > haveAmount;
									}}, General.random(1200, 2200))) {
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Converts an Arraylist full of Integers into a int[]
	 * 
	 * @param integers
	 * @return
	 */
	public static int[] buildIntArray(ArrayList<Integer> integers) {
		int[] ints = new int[integers.size()];
		int i = 0;
		for (Integer n : integers) {
			ints[i++] = n;
		}
		return ints;
	}

	@Override
	public void onPaint(Graphics g) {
		PaintMgr.onPaint(g);
	}

	@Override
	public OVERRIDE_RETURN overrideMouseEvent(MouseEvent e) {
		if (e.getID() == MouseEvent.MOUSE_CLICKED) {
			if (PaintMgr.paintToggle.contains(e.getPoint())) {
				
				PaintMgr.showPaint = !PaintMgr.showPaint;
				
				e.consume();
				return OVERRIDE_RETURN.DISMISS;
			} else if (PaintMgr.settingsToggle.contains(e.getPoint())) {

				LANOgreRanger.getGUI().setVisible(!LANOgreRanger.getGUI().isVisible());
				
				e.consume();
				return OVERRIDE_RETURN.DISMISS;
			}
		}
		
		return OVERRIDE_RETURN.PROCESS;
	}

	// unused overrides
	public OVERRIDE_RETURN overrideKeyEvent(KeyEvent e) {return OVERRIDE_RETURN.SEND;}

}

package scripts.Managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSNPC;
import scripts.LANChaosKiller;

/**
 * 
 * Helper class for Tribot's ABC system.
 * 
 * @author Laniax
 *
 */
public class AntibanMgr {

	private static ABCUtil abcUtil;

	// singleton
	public static ABCUtil getUtil() {
		return abcUtil = abcUtil == null ? new ABCUtil() : abcUtil;
	}


	/**
	 * Do all the antiban actions we are supposed to do while idling.
	 */
	public static void doIdleActions() {
		
		// the time trackers's next() functions are only returning 0.
		// disabled these antiban's until this is fixed/figured out.
		/*doCheckXP();
		doRotateCamera();
		doExamineObject();*/
		
	}
	
	/**
	 * Checks if our run energy is above a random threshold and toggles run on if it isn't already.
	 * 
	 * @return if run was activated or not.
	 */
	public static boolean doActivateRun() {

		if (!Game.isRunOn() && Game.getRunEnergy() >= getUtil().INT_TRACKER.NEXT_RUN_AT.next()) {
			
			getUtil().INT_TRACKER.NEXT_RUN_AT.reset();
			LANChaosKiller.statusText = "Antiban - Activate Run";
			
			return Options.setRunOn(true);
		}
		return false;
	}

	/**
	 * Checks what npc in the array should be attacked.
	 * 
	 * Does canReach and isInCombat checks as well.
	 * 
	 * @param npcs
	 * @return the npc to attack, or null if input array was null.
	 */
	public static RSNPC[] orderOfAttack(RSNPC[] npcs) {

		if (npcs.length > 0) {

			npcs = NPCs.sortByDistance(Player.getPosition(), npcs);

			List<RSNPC> orderedNPCs = new ArrayList<RSNPC>();

			for (RSNPC npc : npcs) {

				if (npc.isInCombat() || !npc.isValid() || !MovementMgr.canReach(npc))
					continue;

				orderedNPCs.add(npc);
			}

			if (orderedNPCs.size() > 1) {

				if (getUtil().BOOL_TRACKER.USE_CLOSEST.next()) {

					// if the 2nd closest npc is within 3 tiles of the closest npc, attack the 2nd one first.
					if (orderedNPCs.get(0).getPosition().distanceTo(orderedNPCs.get(1)) <= 3) 
						Collections.swap(orderedNPCs, 0, 1);
				}

				getUtil().BOOL_TRACKER.USE_CLOSEST.reset();
			}

			return orderedNPCs.toArray(new RSNPC[orderedNPCs.size()]);
		}

		return null;
	}

	/**
	 * Checks with the antiban if we are alowed to hover over the next object.
	 * @return
	 */
	public static boolean mayHoverNextObject() {
		getUtil().BOOL_TRACKER.HOVER_NEXT.reset();
		return getUtil().BOOL_TRACKER.HOVER_NEXT.next();
	}
	
	/**
	 * Checks the xp in the GameTab.Skills of one of the skills we are currently training.
	 * @return true if succesfully checked a skill
	 */
	public static boolean doCheckXP() {
		
		General.println("Current time: "+System.currentTimeMillis()+". Next XP_CHECK at: "+(System.currentTimeMillis() - getUtil().TIME_TRACKER.CHECK_XP.next())+". (next(): "+getUtil().TIME_TRACKER.CHECK_XP.next()+")");
		if (getUtil().TIME_TRACKER.CHECK_XP.next() > 0L && System.currentTimeMillis() < getUtil().TIME_TRACKER.CHECK_XP.next())
			return false;
		
		General.println("Checking XP");
		
		List<SKILLS> gainedXPInSkill = new ArrayList<SKILLS>();
		
		for (Entry<SKILLS, Integer> set : PaintMgr.startSkillInfo.entrySet()) {
			if (Skills.getXP(set.getKey()) > set.getValue())
				gainedXPInSkill.add(set.getKey());
		}
		
		if (gainedXPInSkill.isEmpty())
			return false;
		
		LANChaosKiller.statusText = "Antiban - Check XP";
		
		TABS oldTab = GameTab.getOpen();
		GameTab.open(TABS.STATS);
		
		SKILLS skillToCheck = gainedXPInSkill.get(General.random(1, gainedXPInSkill.size()) - 1);
		
		int index = 0;
		
		switch (skillToCheck) {
			case ATTACK: index = 1; break;
			case STRENGTH: index = 2; break;
			case DEFENCE: index = 3; break;
			case HITPOINTS: index = 9; break;
			case RANGED: index = 4; break;
			case MAGIC: index = 6; break;
		default:
			break;
		}
		
		if (index > 0) {
			
			final RSInterfaceChild skillInterface = Interfaces.get(320, index);
			
			if (skillInterface != null) {
				
				if (Clicking.hover(skillInterface)) {
					
					General.sleep(2500,4000);
					GameTab.open(oldTab);
					
					return true;
				}
			}
		}
		
		getUtil().TIME_TRACKER.CHECK_XP.reset();
		
		return false;
	}
	
	/**
	 * Rotates the camera for a random duration
	 * @return true if succesfully rotated, false if not.
	 */
	public static boolean doRotateCamera() {
		
		General.println("Current time: "+System.currentTimeMillis()+". Next CAM_ROTATE at: "+(System.currentTimeMillis() - getUtil().TIME_TRACKER.ROTATE_CAMERA.next())+". (next(): "+getUtil().TIME_TRACKER.ROTATE_CAMERA.next()+")");
		if (getUtil().TIME_TRACKER.ROTATE_CAMERA.next() > 0L && System.currentTimeMillis() < getUtil().TIME_TRACKER.ROTATE_CAMERA.next())
			return false;
		
		LANChaosKiller.statusText = "Antiban - Rotate Camera";
		
		General.println("Rotating Camera");
		
		long startTime = System.currentTimeMillis();
		long duration  = General.random(500, 2000);
		
		while (System.currentTimeMillis() < (startTime + duration)) {
			
			Camera.setCameraRotation(General.random(10, 360));

		}
		
		getUtil().TIME_TRACKER.ROTATE_CAMERA.reset();
		
		return true;
	}
	
	/**
	 * Does a delay for a random amount of time.
	 * Should be used when idling and a new object has spawned.
	 */
	public static void doDelayForNewObject() {
		
		LANChaosKiller.statusText = "Antiban - Delay";
		
		General.sleep(getUtil().DELAY_TRACKER.NEW_OBJECT.next());
		getUtil().DELAY_TRACKER.NEW_OBJECT.reset();
	}
	
	/**
	 * Does a delay for a random amount of time.
	 * Should be used when an object has been drained and a new object is already available.
	 */
	public static void doDelayForSwitchObject() {
		
		LANChaosKiller.statusText = "Antiban - Delay";

		General.sleep(getUtil().DELAY_TRACKER.SWITCH_OBJECT.next());
		getUtil().DELAY_TRACKER.SWITCH_OBJECT.reset();
	}
	
	/**
	 * Examines a random object
	 * @return true if an object was examined.
	 */
	public static boolean doExamineObject() {
		
		// TODO: debug Filters, only getting 0-1 results.
		//final RSObject[] objs = Objects.find(15, Filters.Objects.actionsContains("Examine"));
		
		return false;
	}
};
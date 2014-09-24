package scripts.Mgrs;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;

import scripts.LANOgreRanger;
import scripts.Managers.AntibanMgr;

/**
 * @author Laniax
 *
 */
public class CombatMgr {
	
	private static int eatAtPercent = AntibanMgr.getUtil().INT_TRACKER.NEXT_EAT_AT.next();

	/**
	 * Checks if there is anything (or anyone) that is attacking us.
	 * Does not require the healthbar to be above our head.
	 * 
	 * @return true if we are under attack
	 */
	public static boolean isUnderAttack() {
		return Combat.getAttackingEntities().length > 0;
	}
	
	/**
	 *  Eats if health low (determined by antiban) And continues eating if it is still low afterwards.
	 * 
	 */
	public static void eatIfNecessary() {
			if (Combat.getHPRatio() <= eatAtPercent) {
				RSItem[] food = Inventory.find(Filters.Items.nameEquals(LANOgreRanger.foodName));
				if (food.length > 0) {
					if (food.length > 0) {
						if (food[0].click("Eat"))
							AntibanMgr.getUtil().INT_TRACKER.NEXT_EAT_AT.reset();
						
						// recursion call for if we lose a lot of health fast.
						eatIfNecessary();
					}
				}
			}
		}

	/**
	 * Attacks the npc based on his name, can hover and attack the next npc.
	 * 
	 * @param npcs to attack (in order)
	 * @param hoverAndAttackNextTarget - True if we should hover our cursor over the next npc while fighting, however if it's true then the AntibanMgr may still override it.
	 * @return True if an npc was killed, false if not.
	 */
	public static boolean attackNPCs(final RSNPC[] npcs, final boolean hoverAndAttackNextTarget) {
		
		eatIfNecessary();
		
		// there is no npc available.
		if (npcs == null || npcs.length == 0)
			return false;

		for (int i = 0; i < npcs.length; i++) {
			
			eatIfNecessary();
			
			final RSNPC attackNPC = npcs[i];
			
			// we may NEVER attack an NPC if we are NOT in our safezone!
			if (!Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
				LANOgreRanger.goToOgres();
			}

			if (attackNPC.isInCombat() && !CombatMgr.isUnderAttack())
				continue;
			
			if (!attackNPC.isOnScreen())
				Camera.turnToTile(attackNPC);

			// as long as we both are alive and not in combat, we try to attack him.
			for (int it = 0; it < 20; it++) {
				if (!attackNPC.isInCombat()  && !CombatMgr.isUnderAttack() && attackNPC.isValid()) {
					if (Clicking.click("Attack", attackNPC)) {
						General.sleep(250,320);
					}
				} else break;
			}
			
			eatIfNecessary();
			
			// we may NEVER attack an NPC if we are NOT in our safezone!
			if (!Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
				LANOgreRanger.goToOgres();
			}

			// someone stole our npc =(
			if (attackNPC.isInCombat() && !CombatMgr.isUnderAttack()) {
				continue;
			}
			
			if (!hoverAndAttackNextTarget || !AntibanMgr.mayHoverNextObject())
				return true;

			if (npcs.length > i+1) {

				final RSNPC hoverNPC = npcs[i+1];

				while (attackNPC.isInteractingWithMe()) {
					
					eatIfNecessary();

					if (!hoverNPC.isInCombat() && hoverNPC.isValid() && MovementMgr.canReach(hoverNPC)) {

						if (!hoverNPC.isOnScreen())
							Camera.turnToTile(hoverNPC);

						Clicking.hover(hoverNPC);

						General.sleep(190, 310);
					} else {
						return true;
					}
				}
				
				AntibanMgr.doDelayForSwitchObject();
				
				// Here we killed our current npc, lets check if our hover target is still alive and out of combat
				if (!hoverNPC.isOnScreen())
					Camera.turnToTile(hoverNPC);
				
				// we may NEVER attack an NPC if we are NOT in our safezone!
				if (!Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
					LANOgreRanger.goToOgres();
				}

				for (int it = 0; it < 20; it++) {
					if (!hoverNPC.isInCombat() && hoverNPC.isValid() && !CombatMgr.isUnderAttack()) {
						if (Clicking.click("Attack", hoverNPC)) {
							return true;
						}
					} else break;

					General.sleep(250,320);
				}
			}
		}

		return false;
	}
}

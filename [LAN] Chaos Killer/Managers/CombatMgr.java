package scripts.Managers;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.types.RSNPC;

/**
 * Helper class that manages combat logic.
 * @author Laniax
 *
 */
public class CombatMgr {

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
	 * Attacks the npc based on his name, can hover and attack the next npc.
	 * 
	 * @param npcName
	 * @return last NPC that was attacked through this method. or null if nothing was attacked.
	 */
	public static RSNPC attackNPCs(final RSNPC[] npcs, final boolean hoverAndAttackNextTarget) {
		
		// there is no npc available.
		if (npcs == null || npcs.length == 0)
			return null;

		for (int i = 0; i < npcs.length; i++) {

			final RSNPC attackNPC = npcs[i];

			if (attackNPC.isInCombat() && !CombatMgr.isUnderAttack())
				continue;

			if (!attackNPC.isOnScreen())
				Camera.turnToTile(attackNPC);

			// as long as we both are alive and not in combat, we try to attack him.
			for (int it = 0; it < 20; it++) {
				if (!attackNPC.isInCombat()  && !CombatMgr.isUnderAttack() && attackNPC.isValid()) {
					Clicking.click("Attack", attackNPC);
					General.sleep(250,320);
				} else break;
			}

			// someone stole our npc =(
			if (attackNPC.isInCombat() && !CombatMgr.isUnderAttack()) {
				continue;
			}
			
			if (!hoverAndAttackNextTarget || !AntibanMgr.mayHoverNextObject())
				return attackNPC;

			if (npcs.length > i+1) {

				final RSNPC hoverNPC = npcs[i+1];

				while (attackNPC.isInteractingWithMe()) {

					if (!hoverNPC.isInCombat() && hoverNPC.isValid() && MovementMgr.canReach(hoverNPC)) {

						if (!hoverNPC.isOnScreen())
							Camera.turnToTile(hoverNPC);

						Clicking.hover(hoverNPC);

						General.sleep(190, 310);
					} else {
						return attackNPC;
					}
				}
				
				AntibanMgr.doDelayForSwitchObject();
				
				// Here we killed our current npc, lets check if our hover target is still alive and out of combat
				if (!hoverNPC.isOnScreen())
					Camera.turnToTile(hoverNPC);

				for (int it = 0; it < 20; it++) {
					if (!hoverNPC.isInCombat() && hoverNPC.isValid() && !CombatMgr.isUnderAttack()) {
						if (Clicking.click("Attack", hoverNPC))
							return hoverNPC;
					} else break;

					General.sleep(250,320);
				}
			}
		}

		return null;
	}
}

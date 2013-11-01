package scripts;

import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Combat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;

public class Utilities {
	
	public static RSObject[] findNearest(final int DISTANCE, final int MODEL_POINTS) {
		return Objects.find(DISTANCE, new Filter<RSObject>() {
			@Override
			public boolean accept(RSObject obj) {
				if (obj != null && obj.getModel() != null && obj.getDefinition() != null)
					return obj.getModel().getPoints().length == MODEL_POINTS;
				return false;
			}});
	}
	
	public static RSNPC[] findNearest(final int DISTANCE, final String NPC_NAME, final boolean IN_COMBAT_CHECK) {
		return NPCs.find(NPCs.generateFilterGroup(new Filter<RSNPC>() {
					@Override
					public boolean accept(RSNPC npc) {
						return IN_COMBAT_CHECK ?  !npc.isInCombat() && npc.getName().equals(NPC_NAME) : npc.getName().equals(NPC_NAME);
					}
				}));
	}
	
	public static boolean isUnderAttack() {
		return Combat.getAttackingEntities().length > 0;
	}
}

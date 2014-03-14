package scripts.Managers;

import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSObject;

/**
 * Helper class that manages movement logic. 
 * @author Laniax
 *
 */
public class MovementMgr {

	public MovementMgr() {

		// Needed for unstucking underneath the tower. (caves are not mapped with WebWalking)
		WebWalking.setUseAStar(true);

	}

	/**
	 * Walks to the position.
	 * 
	 * Checks if run can be toggled.
	 * 
	 * @param posToWalk
	 * @return if succesfully reached destination or not.
	 */
	public static boolean walkTo(final Positionable posToWalk) {

		AntibanMgr.doActivateRun();

		return WebWalking.walkTo(posToWalk);

	}

	/**
	 * Checks if we can reach the specified Positionable (RSTile/RSObject/RSPlayer/RSGroundItem etc)
	 * 
	 * @param toReach
	 * @return if we can reach the positionable or not.
	 */
	public static boolean canReach(final Positionable toReach) {

		return PathFinding.canReach(toReach, toReach instanceof RSObject);

	}
}

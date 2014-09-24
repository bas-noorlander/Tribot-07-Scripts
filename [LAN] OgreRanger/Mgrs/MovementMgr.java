package scripts.Mgrs;

import org.tribot.api.interfaces.Positionable;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Projection;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.util.DPathNavigator;

/**
 * @author Laniax
 *
 */
public class MovementMgr{
	
	private static DPathNavigator nav = new DPathNavigator();
	
	/**
	 * Walks to the position using either DPathNavigator for close by precision or WebWalking for greater lengths.
	 * 
	 * Checks if run can be toggled.
	 * 
	 * @param posToWalk
	 * @return if succesfully reached destination or not.
	 */
	public static boolean walkTo(final Positionable posToWalk) {

		AntibanMgr.doActivateRun();
		
		boolean faraway = !Projection.isInMinimap(Projection.tileToMinimap(posToWalk));
		
		if (faraway)
			return WebWalking.walkTo(posToWalk);

		return nav.traverse(posToWalk);

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

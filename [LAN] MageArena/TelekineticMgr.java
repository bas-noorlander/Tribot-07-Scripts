package scripts;

import scripts.Defines.Puzzles;

/**
 * @author Laniax
 *
 */
public  class TelekineticMgr {
	
	public static Puzzles currentPuzzle = null;

	public static boolean SolveRoom() {
		
		currentPuzzle = Puzzles.getCurrent();
		
		if (currentPuzzle != null)
			return currentPuzzle.Solve();
		
		return false;
	}
}

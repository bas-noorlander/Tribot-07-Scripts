package scripts.Telekinetic;

import java.util.ArrayList;
import java.util.Map.Entry;

import scripts.Defines.IPuzzleSolver;

/**
 * @author Laniax
 *
 */

public class CrossSolver extends IPuzzleSolver{
	
	 ArrayList<Entry<Integer, Integer>> _steps = new  ArrayList<Entry<Integer, Integer>>();
	
	public CrossSolver() {
		
		//todo: fill with steps.
		//_steps.add(new MapEntry<Integer, Integer>(0, 0));
	}


	/* Returns a user friendly name for this puzzle.
	 */
	@Override
	public String puzzleName() {
		return "Cross-Swirly Puzzle";
	}

	@Override
	public ArrayList<Entry<Integer, Integer>> steps() {
		return _steps;
	}

}

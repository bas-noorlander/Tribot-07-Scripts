package scripts.Telekinetic;

import java.util.ArrayList;
import java.util.Map.Entry;

import scripts.Defines.IPuzzleSolver;
import scripts.Defines.MapEntry;

/**
 * @author Laniax
 *
 */

public class QuarterCakeSolver extends IPuzzleSolver{
	
	 ArrayList<Entry<Integer, Integer>> _steps = new ArrayList<Entry<Integer, Integer>>();
	
	public QuarterCakeSolver() {
		
		// NOTE: THE MAZE VIEW IS POINTED TO THE EAST!
		// THESE VALUES ARE RELATIVE FROM THE NORTH!
		_steps.add(new MapEntry<Integer, Integer>(4, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, -5));
		_steps.add(new MapEntry<Integer, Integer>(-5, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 5));
		_steps.add(new MapEntry<Integer, Integer>(-4, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 4));
		_steps.add(new MapEntry<Integer, Integer>(4, 0));
	}


	/* Returns a user friendly name for this puzzle.
	 */
	@Override
	public String puzzleName() {
		return "Quarter Cake Puzzle";
	}

	@Override
	public ArrayList<Entry<Integer, Integer>> steps() {
		return _steps;
	}

}

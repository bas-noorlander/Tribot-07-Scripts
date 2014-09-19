package scripts.Telekinetic;

import java.util.ArrayList;
import java.util.Map.Entry;

import scripts.Defines.IPuzzleSolver;
import scripts.Defines.MapEntry;

/**
 * @author Laniax
 *
 */

public class SpiralSolver extends IPuzzleSolver{
	
	 ArrayList<Entry<Integer, Integer>> _steps = new ArrayList<Entry<Integer, Integer>>();
	
	public SpiralSolver() {
		
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, 0));
	}


	/* Returns a user friendly name for this puzzle.
	 */
	@Override
	public String puzzleName() {
		return "Spiral Puzzle";
	}

	@Override
	public ArrayList<Entry<Integer, Integer>> steps() {
		return _steps;
	}

}

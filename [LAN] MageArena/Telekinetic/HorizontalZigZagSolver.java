package scripts.Telekinetic;

import java.util.ArrayList;
import java.util.Map.Entry;

import scripts.Defines.IPuzzleSolver;
import scripts.Defines.MapEntry;

/**
 * @author Laniax
 *
 */

public class HorizontalZigZagSolver extends IPuzzleSolver{
	
	 ArrayList<Entry<Integer, Integer>> _steps = new ArrayList<Entry<Integer, Integer>>();
	
	public HorizontalZigZagSolver() {

		_steps.add(new MapEntry<Integer, Integer>(9, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, -3));
		_steps.add(new MapEntry<Integer, Integer>(-8, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, -3));
		_steps.add(new MapEntry<Integer, Integer>(8, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, -1));
		_steps.add(new MapEntry<Integer, Integer>(-9, 0));
		_steps.add(new MapEntry<Integer, Integer>(0, -2));
		_steps.add(new MapEntry<Integer, Integer>(9, 0));
	}

	/* Returns a user friendly name for this puzzle.
	 */
	@Override
	public String puzzleName() {
		return "Horizontal ZigZag Puzzle";
	}

	@Override
	public ArrayList<Entry<Integer, Integer>> steps() {
		return _steps;
	}

}

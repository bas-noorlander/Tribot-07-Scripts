package scripts.Defines;

import java.util.HashMap;
import java.util.Map;

import org.tribot.api2007.Game;

import scripts.Telekinetic.CrossSolver;
import scripts.Telekinetic.HorizontalZigZagSolver;
import scripts.Telekinetic.QuarterCakeSolver;
import scripts.Telekinetic.SpiralSolver;


public enum Puzzles {
	
	PUZZLE_SWIRLY_CROSS(8342) {
		public IPuzzleSolver getSolver() {
			return new CrossSolver();
		}
	},
	PUZZLE_SPIRAL(805314710) {
		public IPuzzleSolver getSolver() {
			return new SpiralSolver();
		}
	},
	PUZZLE_VERTICAL_ZIGZAG(671096982) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_HORIZONTAL_ZIGZAG(1207967894) {
		public IPuzzleSolver getSolver() {
			return new HorizontalZigZagSolver();
		}
	},
	PUZZLE_USELESSNESS(1073750166) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_BUNGLE(536879254) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_TRIFORCE(402661526) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_MINIMALISTIC(939532438) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_BOXMAN(134226070) {
		public IPuzzleSolver getSolver() {
			return null;
		}
	},
	PUZZLE_QUARTERCAKE(268443872) {
		public IPuzzleSolver getSolver() {
			return new QuarterCakeSolver();
		}
	};
	
	Puzzles(int roomId){
		this.roomId = roomId;
	}
	
	public int getRoomID(){
		return this.roomId;
	}
	
	private int roomId;
	
	public abstract IPuzzleSolver getSolver();
	
	public boolean Solve() {
		return this.getSolver().Solve();
	}
	
	private static Map<Integer, Puzzles> map = new HashMap<Integer, Puzzles>();

	static {
		for (Puzzles puzzle : Puzzles.values()) {
			map.put(puzzle.getRoomID(), puzzle);
		}
	}

	public static Puzzles valueOf(int roomId) {
		return map.get(roomId);
	}
	
	public static Puzzles getCurrent() {
		return valueOf(Game.getSetting(624));
	}
}

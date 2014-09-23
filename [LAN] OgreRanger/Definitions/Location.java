package scripts.Definitions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Laniax
 *
 */
public enum Location {

	CASTLE_WARS(0),
	NORTH_OF_ARDOUGNE(1);
	
	private int index;
	
	Location(int index){ 
		this.index = index; 
	}
	
	public int getIndex() { 
		return this.index; 
	}
	
	private static Map<Integer, Location> map = new HashMap<Integer, Location>();

	static {
		for (Location loc : Location.values()) {
			map.put(loc.getIndex(), loc);
		}
	}

	public static Location valueOf(int index) {
		return map.get(index);
	}
}

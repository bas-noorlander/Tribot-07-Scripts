/**
 * 
 */
package scripts.Definitions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Laniax
 * 
 */
public enum ItemIDs {

	SEED_RANARR(5295) {
		@Override
		public String toString() {
			return "Ranarr seed";
		}
	},
	SEED_SNAPDRAGON(5300) {
		@Override
		public String toString() {
			return "Snapdragon seed";
		}
	};
	
	private final int id;
	ItemIDs(int id) { this.id = id; }
	public int getID() { return id; }

	private static Map<Integer, ItemIDs> map = new HashMap<Integer, ItemIDs>();

	public abstract String toString();

	static {
		for (ItemIDs id : ItemIDs.values()) {
			map.put(id.getID(), id);
		}
	}

	public static ItemIDs valueOf(int itemID) {
		return map.get(itemID);
	}
}
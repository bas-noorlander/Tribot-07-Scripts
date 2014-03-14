package scripts.Defines;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Laniax
 * 
 */
public enum ItemIDs {
	// Herbs
	GUAM_LEAF(199) {
		@Override
		public String toString() {
			return "Guam leaf";
		}
	},
	MARRENTILL(201) {
		@Override
		public String toString() {
			return "Marrentill";
		}
	},
	TARROMIN(203) {
		@Override
		public String toString() {
			return "Tarromin";
		}
	},
	HARRALANDER(205) {
		@Override
		public String toString() {
			return "Harralander";
		}
	},
	RANARR(207) {
		@Override
		public String toString() {
			return "Ranarr";
		}
	},
	IRIT(209) {
		@Override
		public String toString() {
			return "Irit";
		}
	},
	AVANTOE(211) {
		@Override
		public String toString() {
			return "Avantoe";
		}
	},
	KWUARM(213) {
		@Override
		public String toString() {
			return "Kwuarm";
		}
	},
	CADANTINE(215) {
		@Override
		public String toString() {
			return "Cadantine";
		}
	},
	DWARF_WEED(217) {
		@Override
		public String toString() {
			return "Dwarf weed";
		}
	},
	LANTADYME(2485) {
		@Override
		public String toString() {
			return "Lantadyme";
		}
	},

	// Misc
	LAW_RUNE(563) {
		@Override
		public String toString() {
			return "Law rune";
		}
	},
	NATURE_RUNE(561) {
		@Override
		public String toString() {
			return "Nature rune";
		}
	},
	RUNE_JAVELIN(830) {
		@Override
		public String toString() {
			return "Rune javelin";
		}
	},
	MITHRIL_BOLTS(9142) {
		@Override
		public String toString() {
			return "Mithril bolts";
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
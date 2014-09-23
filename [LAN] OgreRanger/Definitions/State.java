package scripts.Definitions;

import org.tribot.api2007.Equipment;
import org.tribot.api2007.Equipment.SLOTS;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;

import scripts.LANOgreRanger;

/**
 * @author Laniax
 *
 */
public enum State {
	GO_TO_BANK {
		@Override
		public void run() { LANOgreRanger.goToBank(); }
	},
	BANKING {
		@Override
		public void run() { LANOgreRanger.doBanking(); }
	},
	GO_TO_OGRES {
		@Override
		public void run() { LANOgreRanger.goToOgres(); }
	},
	KILL_OGRES {
		@Override
		public void run() { LANOgreRanger.doKillOgres(); }
	},
	WORLDHOPPING {
		@Override
		public void run() { /*todo*/ }
	};

	public abstract void run();

	public static State getState() {
		
		//if (LANChaosKiller.shouldEat)
		//	CombatMgr.doEat();

		if (((Inventory.isFull() || (LANOgreRanger.foodCount > 0) && Inventory.find(LANOgreRanger.foodName).length == 0)) || Equipment.getItem(SLOTS.ARROW).getStack() < 50) {

			if (LANOgreRanger.AREA_BANK.contains(Player.getPosition())) {
				// We are at the bank and in need of some banking action.
				return State.BANKING;
			}

			// Inventory is full, food is gone, or out of arrows.. we should move to bank.
			return State.GO_TO_BANK;
		}

		if (Player.getPosition().equals(LANOgreRanger.POS_SAFESPOT)) {
			// We are on the exact safe spot and in no need for banking.
			return State.KILL_OGRES;
		}

		// We are not on the safespot and not in need of any banking!
		return State.GO_TO_OGRES;
	}
}
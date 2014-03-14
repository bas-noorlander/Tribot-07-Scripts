package scripts.Managers;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Combat;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.ext.Filters;

import scripts.LANChaosKiller;

/**
 * This helper class manages threads that run separately from the script thread.
 * @author Laniax 
 */

public class ThreadingMgr {

	public static final Thread scriptThread = Thread.currentThread();
	final static Thread stuckChecker = new Thread(new StuckChecker());
	final static Thread statsChecker = new Thread(new StatsChecker());

	public static void start() {
		stuckChecker.setDaemon(true);
		stuckChecker.start();

		statsChecker.setDaemon(true);
		statsChecker.start();
	}

	public static void stop() {
		stuckChecker.interrupt();
		statsChecker.interrupt();
	}
}

class StuckChecker implements Runnable{

	/***
	 * Asynchronously (from the script thread) checks if we are stuck.
	 * Since *if* we are stuck, the script thread does nothing and thus we can safely run this in this thread.
	 */

	@Override
	public void run() {
		while (ThreadingMgr.scriptThread.isAlive()) {

			// Check if we might be upstairs
			if (Player.getPosition().getPlane() > 0) {
				LANChaosKiller.statusText = "Unstucking";
				General.println("We are upstairs - unstucking");
				GeneralMgr.interactWithObject(new Condition() {
					public boolean active() {
						return Player.getPosition().distanceTo(LANChaosKiller.POS_DRUID_TOWER_CENTER) < 15;
					}}, "Climb-down");
			}

			// Check if we are downstairs
			if (LANChaosKiller.AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition())) {
				LANChaosKiller.statusText = "Unstucking";
				// we are trapped at the bottom of the tower.
				General.println("We are downstairs - unstucking");

				PathFinding.aStarWalk(LANChaosKiller.POS_STAIRS_DOWNSTAIRS_TOWER);

				GeneralMgr.interactWithObject(new Condition() {
					public boolean active() {
						return !LANChaosKiller.AREA_DOWNSTAIRS_TOWER.contains(Player.getPosition());
					}}, "Climb-up");
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class StatsChecker implements Runnable {

	/***
	 * Asynchronously (from the script thread) checks if we are below a certain HP threshold.
	 * This may pause the script thread in order to properly eat food.
	 */

	@Override
	public void run() {
		while (ThreadingMgr.scriptThread.isAlive()) {

			/**
			 * Check if we have food and are in need of eating
			 */
			if (LANChaosKiller.foodCount > 0) {
				//	General.println("HP Ratio: "+Combat.getHPRatio()+". Percent to eat below: "+LANChaosKiller.eatBelowPercent);
				if (Combat.getHPRatio() <= LANChaosKiller.eatBelowPercent || Inventory.isFull()) {
					try {

						General.println("Stopping scriptthread");
						// To prevent 2 threads battling each other for control. we will pause the script thread while we eat.
						synchronized(ThreadingMgr.scriptThread) {
							ThreadingMgr.scriptThread.wait();
						}

						LANChaosKiller.statusText = "Eating food";
						GameTab.open(TABS.INVENTORY);
						if (Clicking.click(Inventory.find(Filters.Items.nameEquals(LANChaosKiller.foodName)))) {

							// And resume the script thread.
							synchronized(ThreadingMgr.scriptThread) {
								ThreadingMgr.scriptThread.notify();
							}
						}

					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
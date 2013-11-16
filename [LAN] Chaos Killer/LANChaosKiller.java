package scripts;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.JCheckBox;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.interfaces.Ending;
import org.tribot.script.interfaces.MouseActions;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.RandomEvents;

enum ItemIDs {
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
	TORSOL(219) {
		@Override
		public String toString() {
			return "Torsol";
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
	private int lootAmount;
	ItemIDs(int id) { this.id = id; }
    public int getID() { return id; }
    public int getAmountLooted() { return lootAmount; }
    public void incredimentUpAmountLooted() { lootAmount++; }
    public void setAmountLooted(int i) { lootAmount = i; }
    public void incredimentDownAmountLooted() { --lootAmount; }
    
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

enum State {
	BANKING {
		@Override
		void run() { LANChaosKiller.doBanking(); }
	},
	GO_TO_DRUIDS {
		@Override
		void run() { LANChaosKiller.goToDruids(); }
	},
	PROCESS_DRUIDS {
		@Override
		void run() { LANChaosKiller.doProcessDruids(); }
	},
	GO_TO_BANK {
		@Override
		void run() { LANChaosKiller.goToBank(); }
	};
	
	abstract void run();
}

@ScriptManifest(authors = { "Laniax" }, category = "Combat", name = "[LAN] Chaos Killer")
public class LANChaosKiller extends Script implements Painting, MouseActions, RandomEvents, Ending{
	// Global defines
	private static boolean quitting = false;
	private static String statusText = "Starting..";
	private static final RSPlayer player = Player.getRSPlayer();
	private static boolean waitForGUI = true;
	private static boolean isDoingRandom = false;

	// Script defines
	private static int foodID = 0;
	private static int foodCount = 0;
	private static int eatBelowPercent = 50;
	private static int druidsKilledSinceLastLoot = 0;
	public static final ArrayList<Integer> LOOT_IDS = new ArrayList<Integer>();
	
	//private static final int LOG_EAST_MODEL_POINT_COUNT = 54;
	//private static final int LOG_WEST_MODEL_POINT_COUNT = 114;
	//private static final int TOWER_LADDER_TO_DOWNSTAIRS_MODEL_POINT_COUNT = 270;
	//private static final int TOWER_LADDER_FROM_DOWNSTAIRS_MODEL_POINT_COUNT = 288;
	//private static final int TOWER_LADDER_FROM_UPSTAIRS_MODEL_POINT_COUNT = 204;
	//private static final int TOWER_DOOR_MODEL_POINT_COUNT = 168;
	private static final int MAX_FAILSAFE_ATTEMPTS = 20;
	
	private static final int MINIMUM_RUN_ENERGY = 25;
	
	private static final RSTile POS_DOWNSTAIRS_TOWER = new RSTile(2563, 9756);
	private static final RSTile POS_OUTSIDE_DRUID_TOWER_DOOR = new RSTile(2565, 3356, 0);
	private static final RSTile POS_DRUID_TOWER_CENTER = new RSTile(2562, 3356, 0);
	private static final RSTile POS_BANK_CENTER = new RSTile(2617, 3332);
	private static final RSTile POS_LOG_EAST = new RSTile(2602, 3336);
	private static final RSTile POS_LOG_WEST = new RSTile(2598, 3336);
	
	private GUI gui;
	
	public GUI getGUI() {
		if (gui == null)
			gui = new GUI();
		return gui;
		
	}

	private static final RSTile[] POS_LOG_WALK_FAILED = new RSTile[] {
			new RSTile(2598, 3329), // east (tower side)
			new RSTile(2604, 3330) // west (bank side)
	};
	
	private final static RSTile[] PATH_BANK_TO_LOG = new RSTile[] {
			new RSTile(2617, 3332, 0), 
			new RSTile(2616, 3337, 0),
			new RSTile(2613, 3339, 0), 
			new RSTile(2610, 3339, 0),
			new RSTile(2602, 3336, 0) 
	};

	private final static RSTile[] PATH_LOG_TO_TOWER = new RSTile[] {
			new RSTile(2598, 3336, 0), 
			new RSTile(2588, 3340, 0),
			new RSTile(2587, 3344, 0), 
			new RSTile(2576, 3350, 0),
			new RSTile(2565, 3356, 0) 
	};

	@Override
	public void run() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() { 
				GUI gui = getGUI();
				gui.setVisible(true);
		}});
		
		LANAntiBan.startAntiBan();
		
		while (waitForGUI)
			sleep(250);
		
		while (!quitting) {
			getState().run();
			sleep(General.random(40, 80));
		}
		
		LANAntiBan.stopAntiBan();
	}
	
	@Override
	public void onEnd() {
		LANAntiBan.stopAntiBan();
	}

	@Override
	public void onRandom(RANDOM_SOLVERS random) {
		if (random.equals(RANDOM_SOLVERS.COMBATRANDOM)) {
			if (getState().equals(State.PROCESS_DRUIDS)) {
				isDoingRandom = true;
				statusText = "Yikes! a nasty random!";
				
				RSObject[] stairs = Utilities.findNearest(6, "Climb-down");
				if (stairs != null && stairs.length > 0) {
					if (!stairs[0].isOnScreen())
						Camera.turnToTile(stairs[0]);
					
					while (!Player.getPosition().equals(POS_DOWNSTAIRS_TOWER) && stairs[0].isOnScreen()) {
						if (stairs[0].click("Climb-down"));
							sleep(250,300);
					}
					
					statusText = "Waiting till it's safe!";
					
					// Okay, we're downstairs, away from the nasty swarm/evil chicken.. let's wait a bit here, shall we?
					sleep(8000,9000);
					
					// Okay we should be good to go again, lets go up the stairs and continue :)
					stairs = Utilities.findNearest(10, "Climb-up");
					if (stairs != null && stairs.length > 0) {
						if (!stairs[0].isOnScreen())
							Camera.turnToTile(stairs[0]);
						while (stairs[0].isOnScreen()  && Player.getPosition().distanceTo(POS_DOWNSTAIRS_TOWER) < 15) {
							if (stairs[0].click("Climb-up"));
								sleep(250,300);
						}
						isDoingRandom = false;
					} else {
						// Well.. shit.
						isDoingRandom = false;
						quitting = true;
						General.println("Couldn't find the stairs back up! :( quitting script.");
					}
				}
			}
		}
	}

	@Override
	public boolean randomFailed(RANDOM_SOLVERS arg0) {
		return false;
	}

	@Override
	public void randomSolved(RANDOM_SOLVERS random) { General.sleep(2500);}
	
	public static void checkStuck() {
		if (Player.getPosition().getPlane() > 0) {
			// we are upstairs
			General.println("We are upstairs - unstucking");
			RSObject[] stairs = Utilities.findNearest(10, "Climb-down");
			if (stairs != null && stairs.length > 0) {
				while (Player.getPosition().getPlane() > 0) {
					if (!stairs[0].isOnScreen())
						Camera.turnToTile(stairs[0]);
					
					if (stairs[0].click("Climb-down"));
						General.sleep(250,300);
				}
			}
		}
		
		if (Player.getPosition().distanceTo(POS_DOWNSTAIRS_TOWER) < 25 && !isDoingRandom) {
			// we are trapped at the bottom of le tower.
			General.println("We are downstairs - unstucking");
			RSObject[] stairs = Utilities.findNearest(10, "Climb-up");
			if (stairs != null && stairs.length > 0) {
				if (Player.getPosition().distanceTo(POS_DOWNSTAIRS_TOWER) > 5)
					WebWalking.walkTo(POS_DOWNSTAIRS_TOWER);
				
				while (Player.getPosition().distanceTo(POS_DOWNSTAIRS_TOWER) < 15) {
					if (!stairs[0].isOnScreen())
						Camera.turnToTile(stairs[0]);
					
					if (stairs[0].click("Climb-up"));
						General.sleep(250,300);
				}
			}
		}
	}

	private static State getState() {
		checkStuck();
		
		if (Inventory.isFull() || (foodCount > 0 && Inventory.find(foodID).length == 0)) {
			if (Player.getPosition().distanceTo(POS_BANK_CENTER) <= 2) {
				// We are at the bank and in need of some banking action.
				return State.BANKING;
			}
			// Inventory is full, or food is gone.. we should move to bank.
			return State.GO_TO_BANK;
		}
		
		if (Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) < 3 && !Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR)) {
			// We are at the druids (in the tower).
			return State.PROCESS_DRUIDS;
		}
		// We have free space in inventory, so lets go to the druids and fill it up!
		return State.GO_TO_DRUIDS;
	}

	public static void doBanking() {

		if (Player.getPosition().distanceTo(POS_BANK_CENTER) >= 3) {
			goToBank();
		}

		statusText = "Banking..";

		int failsafe = 0;
		while ((!Banking.isBankScreenOpen() || Banking.isPinScreenOpen()) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			if (Banking.openBankBooth())
				General.sleep(200, 250);
			failsafe++;
		}

		if (Banking.isPinScreenOpen()) {
			Banking.inPin();
		}

		while (!Banking.isBankScreenOpen() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			General.sleep(100, 150);
			failsafe++;
		}

		Banking.depositAll();
		General.sleep(1000, 1200);

		if (foodCount > 0) {
			Banking.withdraw(foodCount, foodID);
			General.sleep(1000, 1300);
		}
		Banking.close();
	}
	
	private static void eatIfNecessary() {
		if (foodCount > 0) {
			double currLevel = Skills.getCurrentLevel(SKILLS.HITPOINTS);
			int hitpointsLevel = Skills.getActualLevel(SKILLS.HITPOINTS);
			double decimal = currLevel / (double)(hitpointsLevel);
			int percent = (int)(decimal * 100);
			
			if (percent <= eatBelowPercent) {
				GameTab.open(TABS.INVENTORY);
				RSItem[] food = Inventory.find(foodID);
				if (food.length > 0) {
					statusText = "Eating..";
					if (food.length > 0) {
						food[0].click("Eat");
						
						// recursion call for if we lose a lot of health fast.
						eatIfNecessary();
					}
				}
			}
		}
	}

	public static void doProcessDruids() {
		eatIfNecessary();
		checkStuck();
		
		if (!Utilities.isUnderAttack()) {
			if (LOOT_IDS != null) {
				//Lets do some looting.
				int[] ids = new int[LOOT_IDS.size()];
				for (int i=0; i< ids.length; i++)
					ids[i] = LOOT_IDS.get(i).intValue();
				
				RSGroundItem lootItems[] = GroundItems.findNearest(ids);
				if (lootItems.length > 0) {
					statusText = "Looting..";
					druidsKilledSinceLastLoot = 0;
					for (RSGroundItem item : lootItems) {
						if (Inventory.isFull())
							return;
						
						// Apparently just 'Take' would causes issues with multiple items on 1 tile.
						if (item.click("Take "+ item.getDefinition().getName())) {
							ItemIDs i = ItemIDs.valueOf(item.getID());
							if (i != null) {
								i.incredimentUpAmountLooted();
							}
							itemsLooted++;
							General.sleep(1000,2000);
						}
					}
				}
				
				// Drop junk we didn't want.
				ArrayList<Integer> dropItemIds = new ArrayList<Integer>();
				dropItemIds.add(526); // bones
				dropItemIds.add(227); // vial of water
				dropItemIds.add(1971); // kebab
				dropItemIds.add(1917); // beer
				dropItemIds.add(558); // mind rune
				dropItemIds.add(231); // snape grass?
				dropItemIds.add(1291); // bronze longsword
				dropItemIds.add(117); // 2 dose strength potion
				
				for (ItemIDs itemID : ItemIDs.values()) {
					if (!LOOT_IDS.contains(itemID.getID()) && Inventory.find(itemID.getID()).length > 0)
						dropItemIds.add(itemID.getID());
				}
				
				int[] temp = new int[dropItemIds.size()];
				  for(int i = 0;i < temp.length;i++)
					  temp[i] = dropItemIds.get(i);
				
				int i = Inventory.drop(temp);
				if (i > 0) {
					itemsLooted -= i;
					for (int t : temp){
						ItemIDs item = ItemIDs.valueOf(t);
						if (item != null)
							item.incredimentDownAmountLooted();
					}
				}
			}
			
			if (Game.getRunEnergy()> MINIMUM_RUN_ENERGY && !Game.isRunOn()) {
				Options.setRunOn(true);
				GameTab.open(TABS.INVENTORY);
			}

			statusText = "Killing druids..";
			
			RSNPC[] druids = Utilities.findNearest(6, "Chaos druid", true);
			if (druids != null)
				druids = NPCs.sortByDistance(player.getPosition(), druids);

			for (int i = 0; i < druids.length; i++) {
				
				checkStuck();
				
				if (druidsKilledSinceLastLoot > 5)
					break;

				if (PathFinding.canReach(druids[i], false)) {
					// We got a potential druid we can kill.
					if (!druids[i].isOnScreen()) {
						Camera.turnToTile(druids[i]);
					}

					int failsafe = 0;
					while (!druids[i].isInteractingWithMe() && druids[i].isValid() && !druids[i].isInCombat() && !Utilities.isUnderAttack() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
						if (druids[i].click("Attack"))
							General.sleep(250, 300);
						
						failsafe++;
						
						checkStuck();
					}
					
					if (druids[i].isInteractingWithMe()) {
						// we are in combat with current druid.
						// while we do that, prepare for next druid
						if (druids.length > i+1)
						{
							checkStuck();
							if (PathFinding.canReach(druids[i+1], false)) {
								// We got a new potential druid we can hover over.
								if (!druids[i+1].isOnScreen()) {
									Camera.turnToTile(druids[i+1]);
								}

								failsafe = 0;
								while (druids[i].isInteractingWithMe() && druids[i+1].isValid() && !druids[i+1].isInCombat() && failsafe < MAX_FAILSAFE_ATTEMPTS) {
									eatIfNecessary();
									if (!druids[i+1].isInCombat()) {
										if (druids[i+1].hover())
											General.sleep(200, 300);
										
										checkStuck();
										failsafe++;
									}
								}
								

								if (!druids[i+1].isInCombat() && !Utilities.isUnderAttack()) {
									// We killed the current druid, and next druid isn't in combat yet.
									druidsKilledSinceLastLoot++;
									if (druids[i+1].click("Attack"))
										General.sleep(2000, 3000);
									// No retrying here, if it fails the states will catch back up from the start
								}
							}
						}
					}
				}
			}
		}
	}

	public static void goToBank() {
		statusText = "Going to bank..";
		
		if (getState() != State.GO_TO_BANK)
			return;

		if (Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 3) {
			// We are in the tower, first open the door.
			RSObject[] doors = Utilities.findNearest(6, "Open");
			
			if (doors.length > 0) {
				Camera.turnToTile(doors[0]); // always turn the camera, otherwise it has a hard time clicking
					
				int failsafe = 0;
				while (!Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR) && failsafe < MAX_FAILSAFE_ATTEMPTS) {
					if (DynamicClicking.clickRSObject(doors[0], "Open"))
						General.sleep(500, 800);
					failsafe++;
					
					if (getState() != State.GO_TO_BANK)
						return;
				}
			}
		}
		
		// If the script gets started (or a dc, etc) with a full inventory on the east side of the river we get stuck.
		statusText = "Going to log..";
		
		while (Player.getPosition().distanceTo(POS_LOG_WEST) < Player.getPosition().distanceTo(POS_LOG_EAST)) {
			if (Game.getRunEnergy()> MINIMUM_RUN_ENERGY && !Game.isRunOn()) {
				Options.setRunOn(true);
				GameTab.open(TABS.INVENTORY);
			}
			
			Walking.walkPath(Walking.invertPath(PATH_LOG_TO_TOWER));

			if (getState() != State.GO_TO_BANK)
				return;
			
			// Arrived at the log crossing.
			statusText = "Crossing log..";

			RSObject[] logs = Utilities.findNearest(3, "Walk-across");
			if (logs != null && logs.length > 0) {
				logs = Objects.sortByDistance(Player.getPosition(), logs);
				if (!logs[0].isOnScreen())
					Camera.turnToTile(logs[0]);

					if (logs[0].click("Walk-across"))
						General.sleep(5000, 7000);

					if (getState() != State.GO_TO_BANK)
						return;
				}
		}
		
		if (getState() != State.GO_TO_BANK)
			return;
		
		statusText = "Going to bank..";

		int failsafe = 0;
		while (Player.getPosition().distanceTo(POS_BANK_CENTER) >= 3 && failsafe < MAX_FAILSAFE_ATTEMPTS) {
			if (Game.getRunEnergy()> MINIMUM_RUN_ENERGY && !Game.isRunOn()) {
				Options.setRunOn(true);
				GameTab.open(TABS.INVENTORY);
			}
			
			Walking.walkPath(Walking.invertPath(PATH_BANK_TO_LOG));
			General.sleep(2000, 2500);
			failsafe++;
			
			if (getState() != State.GO_TO_BANK)
				return;
		}
	}

	public static void goToDruids() {
		if (!(Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) < 3) || Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR)) {
			
			// If the script gets started (or a dc, etc) with an empty inventory on the west side of the river we would get stuck.
			if (getState() != State.GO_TO_DRUIDS)
				return;
				
				while (Player.getPosition().distanceTo(POS_LOG_WEST) > Player.getPosition().distanceTo(POS_LOG_EAST)) {
					
					statusText = "Going to log..";
					
					if (Game.getRunEnergy()> MINIMUM_RUN_ENERGY && !Game.isRunOn()) {
						Options.setRunOn(true);
						GameTab.open(TABS.INVENTORY);
					}
					
					if (getState() != State.GO_TO_DRUIDS)
						return;
					
					Walking.walkPath(PATH_BANK_TO_LOG);
				
				// Arrived at the log crossing.
				statusText = "Crossing log..";
				
				RSObject[] logs = Utilities.findNearest(3, "Walk-across");
				if (logs != null && logs.length > 0) {
					logs = Objects.sortByDistance(Player.getPosition(), logs);
					
					if (!logs[0].isOnScreen())
						Camera.turnToTile(logs[0]);
					
						if (logs[0].click("Walk-across"))
								General.sleep(5000, 7000);

						if (getState() != State.GO_TO_DRUIDS)
							return;
					}
				}
			}
		
			while (!Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR)) {
				
				statusText = "Going to tower..";
				
				if (Game.getRunEnergy()> MINIMUM_RUN_ENERGY && !Game.isRunOn()) {
					Options.setRunOn(true);
					GameTab.open(TABS.INVENTORY);
				}
				Walking.walkPath(PATH_LOG_TO_TOWER);
				General.sleep(2000, 2500);
				
				if (getState() != State.GO_TO_DRUIDS)
					return;
			}
			
			// Arrived at the tower, lets picklock the door
			statusText = "Picklocking door..";

			RSObject[] doors = Utilities.findNearest(5, "Pick-lock");
				
			if (doors.length > 0) {
				Camera.turnToTile(doors[0]); // always turn the camera, otherwise it has a hard time clicking
					
				// It can take a while to open the door due to not so precise clicking, therefore not failsafing this.
				while ((!(Player.getPosition().distanceTo(POS_DRUID_TOWER_CENTER) <= 2)) && Player.getPosition().equals(POS_OUTSIDE_DRUID_TOWER_DOOR)) {
					if (DynamicClicking.clickRSObject(doors[0], "Pick-lock"))
						General.sleep(500, 800);
					
					if (getState() != State.GO_TO_DRUIDS)
						return;
				}
			}
		}

	// Here be paint stuff.

	private final Color colorSilver = new Color(230, 230, 230);
	private final Color colorTransparentBG = new Color(0, 0, 0, 153);
	private final Font font = new Font("Arial", 1, 13);
	private final Image paint = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/paint.png");
	private final Image paintShow = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/painttoggle.png");
	private final Rectangle paintToggle = new Rectangle(495, 343, 21, 24);
	private final Rectangle settingsToggle = new Rectangle(203, 455, 109, 22);
	private final Rectangle lootedToggle = new Rectangle(379, 389, 126, 25);
	private static final long startTime = System.currentTimeMillis();
	private static int itemsLooted = 0;
	private boolean showPaint = true;
	private boolean showLootInfo = false;
	
	private static int startSkillInfo[] = {
		 	 Skills.getXP(SKILLS.ATTACK),
		 	 Skills.getXP(SKILLS.STRENGTH),
		 	 Skills.getXP(SKILLS.DEFENCE)
		};

	@Override
	public void onPaint(Graphics g) {
		if (showPaint) {
			long timeRan = System.currentTimeMillis() - startTime;
			double secondsRan = (int) (timeRan/1000);
			double hoursRan = secondsRan/3600;
			g.drawImage(paint, 0, 338, null);
			g.setFont(font);
			g.setColor(colorSilver);
			g.drawString(statusText, 262, 410); 
			g.drawString(Timing.msToString(timeRan), 94, 410);
			g.drawString(Integer.toString(itemsLooted), 428, 409);

			int attackXPGained = (Skills.getXP(SKILLS.ATTACK) - startSkillInfo[0]);
			String xp = NumberFormat.getNumberInstance().format(Math.round(attackXPGained / hoursRan));
			g.drawString(attackXPGained + " ("+xp+" XP/h)", 63, 433);
			
			int strengthXPGained = (Skills.getXP(SKILLS.STRENGTH) - startSkillInfo[1]);
			xp = NumberFormat.getNumberInstance().format(Math.round(strengthXPGained / hoursRan));
			g.drawString(strengthXPGained + " ("+xp+" XP/h)", 238, 433);
			
			int defenceXPGained = (Skills.getXP(SKILLS.DEFENCE) - startSkillInfo[2]);
			xp = NumberFormat.getNumberInstance().format(Math.round(defenceXPGained / hoursRan));
			g.drawString(defenceXPGained + " ("+xp+" XP/h)", 402, 433);
			
			if (showLootInfo) {
				g.setColor(colorTransparentBG);
				g.fillRoundRect(153, 72, 250, 170, 16, 16);
				g.setColor(colorSilver);
				g.drawRoundRect(153, 72, 250, 170, 16, 16);
				
				int y = 90;
				int x = 160;
				
				ItemIDs[] vals = ItemIDs.values();
				for (int i = 0; i < vals.length; i++) {
					if (i == 8) {
						y = 90;
						x = 280;
					}
				
					g.drawString(vals[i].toString()+": "+vals[i].getAmountLooted(), x, y);
					y += 20;
				}
			}
		} else
			g.drawImage(paintShow, paintToggle.x, paintToggle.y, null);
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void mouseReleased(Point p, int button, boolean isBot) {
		if (paintToggle.contains(p.getLocation()))
			showPaint = !showPaint;
		else if (settingsToggle.contains(p.getLocation())) {
			GUI gui = getGUI();
			gui.setVisible(!gui.isVisible());
		}
	}

	@Override
	public void mouseClicked(Point p, int button, boolean isBot) {}

	@Override
	public void mouseDragged(Point p, int button, boolean isBot) {}

	@Override
	public void mouseMoved(Point p, boolean isBot) {
		if (!isBot && lootedToggle.contains(p)) {
			showLootInfo = true;
		} else {
			showLootInfo = false;
		}
	}
	
	@SuppressWarnings("serial")
	class GUI extends JFrame {
		
		private HashMap<JCheckBox, ItemIDs> checkBoxes = new HashMap<JCheckBox, ItemIDs>();
		Point start_drag;
		Point start_loc;
		
		public GUI() {

			lootGuam = new JCheckBox();
			lootMarrentill = new JCheckBox();
			lootTarromin = new JCheckBox();
			lootHarralander = new JCheckBox();
			lootRanarr = new JCheckBox();
			lootIrit = new JCheckBox();
			lootAventoe = new JCheckBox();
			lootKwuarm = new JCheckBox();
			lootCadantine = new JCheckBox();
			lootDwarf = new JCheckBox();
			lootLantadyme = new JCheckBox();
			lootTorsol = new JCheckBox();

			lootLaw = new JCheckBox();
			lootNature = new JCheckBox();
			lootBolts = new JCheckBox();
			lootJavelin = new JCheckBox();
			backgroundLabel = new JLabel();

			foodCountSpinner = new JSpinner();
			foodIDSpinner = new JSpinner();
			eatBelowPctSpinner = new JSpinner();
			mouseSpeed = new JSlider();
			btnSave = new JButton();

			checkBoxes.put(lootGuam, ItemIDs.GUAM_LEAF);
			checkBoxes.put(lootMarrentill, ItemIDs.MARRENTILL);
			checkBoxes.put(lootTarromin, ItemIDs.TARROMIN);
			checkBoxes.put(lootHarralander, ItemIDs.HARRALANDER);
			checkBoxes.put(lootRanarr, ItemIDs.RANARR);
			checkBoxes.put(lootIrit, ItemIDs.IRIT);
			checkBoxes.put(lootAventoe, ItemIDs.AVANTOE);
			checkBoxes.put(lootKwuarm, ItemIDs.KWUARM);
			checkBoxes.put(lootCadantine, ItemIDs.CADANTINE);
			checkBoxes.put(lootDwarf, ItemIDs.DWARF_WEED);
			checkBoxes.put(lootLantadyme, ItemIDs.LANTADYME);
			checkBoxes.put(lootTorsol, ItemIDs.TORSOL);
			checkBoxes.put(lootLaw, ItemIDs.LAW_RUNE);
			checkBoxes.put(lootNature, ItemIDs.NATURE_RUNE);
			checkBoxes.put(lootBolts, ItemIDs.MITHRIL_BOLTS);
			checkBoxes.put(lootJavelin, ItemIDs.RUNE_JAVELIN);

			setResizable(false);
			setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			setBounds(new Rectangle(0, 0, 337, 495));
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setUndecorated(true);
			setPreferredSize(new Dimension(337, 495));
			getContentPane().setLayout(null);

			for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
				JCheckBox checkBox = entry.getKey();
				checkBox.setOpaque(false);
				getContentPane().add(checkBox);
			}

			lootKwuarm.setBounds(250, 100, 100, 27);
			lootGuam.setBounds(20, 70, 100, 27);
			lootMarrentill.setBounds(20, 100, 100, 27);
			lootTarromin.setBounds(130, 40, 100, 27);
			lootHarralander.setBounds(130, 70, 110, 27);
			lootRanarr.setBounds(130, 100, 70, 27);
			lootIrit.setBounds(250, 40, 100, 27);
			lootAventoe.setBounds(250, 70, 100, 27);
			lootDwarf.setBounds(20, 170, 100, 27);
			lootCadantine.setBounds(20, 140, 100, 27);
			lootLantadyme.setBounds(130, 140, 110, 27);
			lootTorsol.setBounds(130, 170, 100, 27);
			lootLaw.setBounds(20, 230, 70, 27);
			lootNature.setBounds(20, 260, 110, 27);
			lootBolts.setBounds(130, 230, 110, 27);
			lootJavelin.setBounds(130, 260, 110, 27);

			mouseSpeed.setOpaque(false);
			getContentPane().add(mouseSpeed);
			mouseSpeed.setBounds(70, 345, 200, 23);
			mouseSpeed.setMaximum(200);
			mouseSpeed.setMinimum(10);
			mouseSpeed.setValue(Mouse.getSpeed());

			foodCountSpinner.setOpaque(false);
			foodCountSpinner.setModel(new SpinnerNumberModel(1, 0, 28, 1));
			getContentPane().add(foodCountSpinner);
			foodCountSpinner.setBounds(280, 302, 40, 20);

			foodIDSpinner.setOpaque(false);
			foodIDSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(379), 0, null, Integer.valueOf(1)));
			getContentPane().add(foodIDSpinner);
			foodIDSpinner.setBounds(100, 302, 60, 20);
			
			eatBelowPctSpinner.setOpaque(false);
			eatBelowPctSpinner.setModel(new SpinnerNumberModel(eatBelowPercent, 10, 90, 5));
	        getContentPane().add(eatBelowPctSpinner);
	        eatBelowPctSpinner.setBounds(220, 390, 40, 20);

			btnSave.setText("Save Settings");
			btnSave.setOpaque(false);
			btnSave.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					btnSaveSettingsClicked(evt);
				}
			});
			getContentPane().add(btnSave);
			btnSave.setBounds(90, 415, 170, 40);

			try {
				backgroundLabel.setIcon(new ImageIcon(new URL("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/settings.png")));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			backgroundLabel.setText("Failed to load background :(");
			backgroundLabel.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent evt) { 
					backgroundMousePressed(evt);
				}
			});
			backgroundLabel.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent evt) {
					backgroundMouseDragged(evt);
				}
			});
			getContentPane().add(backgroundLabel);
			backgroundLabel.setBounds(0, 0, 337, 495);

			for (int i = 1; i <= LOOT_IDS.toArray().length; i++) {
				for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
					if (LOOT_IDS.toArray()[i].equals(entry.getValue().getID())) {
						entry.getKey().setSelected(true);
						break;
					}
				}
			}

			pack();
			this.setLocationRelativeTo(null);
			this.toFront();
		}
	    
		protected void backgroundMousePressed(MouseEvent evt) {
			this.start_drag = this.getScreenLocation(evt);
			this.start_loc = this.getLocation();
		}

		Point getScreenLocation(MouseEvent e) {
			Point cursor = e.getPoint();
			Point target_location = this.getLocationOnScreen();
			return new Point((int) (target_location.getX() + cursor.getX()), (int) (target_location.getY() + cursor.getY()));
		}

		protected void backgroundMouseDragged(MouseEvent evt) {
			Point current = this.getScreenLocation(evt);
			Point offset = new Point((int) current.getX() - (int) start_drag.getX(), (int) current.getY() - (int) start_drag.getY());
			Point new_location = new Point((int) (this.start_loc.getX() + offset.getX()), (int) (this.start_loc.getY() + offset.getY()));
			this.setLocation(new_location);

		}

		protected void btnSaveSettingsClicked(ActionEvent evt) {

			LOOT_IDS.clear();

			for (Entry<JCheckBox, ItemIDs> entry : checkBoxes.entrySet()) {
				if (entry.getKey().isSelected())
					LOOT_IDS.add(entry.getValue().getID());
			}

			Mouse.setSpeed(mouseSpeed.getValue());

			foodID = (int) foodIDSpinner.getValue();
			foodCount = (int) foodCountSpinner.getValue();
			eatBelowPercent = (int) eatBelowPctSpinner.getValue();

			waitForGUI = false;
			this.setVisible(false);
		}

		private JButton btnSave;
		private JLabel backgroundLabel;
		private JCheckBox lootAventoe;
		private JCheckBox lootBolts;
		private JCheckBox lootCadantine;
		private JCheckBox lootDwarf;
		private JCheckBox lootGuam;
		private JCheckBox lootHarralander;
		private JCheckBox lootIrit;
		private JCheckBox lootJavelin;
		private JCheckBox lootKwuarm;
		private JCheckBox lootLantadyme;
		private JCheckBox lootLaw;
		private JCheckBox lootMarrentill;
		private JCheckBox lootNature;
		private JCheckBox lootRanarr;
		private JCheckBox lootTarromin;
		private JCheckBox lootTorsol;
		private JSlider mouseSpeed;
		private JSpinner foodCountSpinner;
		private JSpinner foodIDSpinner;
		private JSpinner eatBelowPctSpinner;
	}
}
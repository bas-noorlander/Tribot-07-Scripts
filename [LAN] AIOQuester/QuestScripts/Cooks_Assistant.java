package scripts.questscripts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.tribot.api.General;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills.SKILLS;

import scripts.defines.Command;
import scripts.defines.Quests;
import scripts.defines.QuestData;
import scripts.defines.QuestStatus;
import scripts.questutilities.Manager;
import scripts.questutilities.QuestManager;

public class Cooks_Assistant extends QuestData{
	
	private final static int COOK_MODEL_POINT_COUNT = 2265;
	private final static int WINDMILL_HOPPER_MODEL_POINT_COUNT = 354;
	private final static int WINDMILL_BIN_FULL_MODEL_POINT_COUNT = 1071;
	
	private final static int ITEMID_POT = 1931;
	private final static int ITEMID_POT_FULL = 1933;
	private final static int ITEMID_BUCKET = 1925;
	private final static int ITEMID_BUCKET_OF_MILK = 1927;
	private final static int ITEMID_EGG = 1944;
	private final static int ITEMID_GRAIN = 1947;
	
	private final static RSTile POS_LUMB_CENTER = new RSTile(3217, 3219, 0);
	private final static RSTile POS_LUMB_KITCHEN = new RSTile(3207, 3214, 0);
	private final static RSTile POS_FARM = new RSTile(3229, 3297, 0);
	private final static RSTile POS_FARM_DOOR = new RSTile(3236, 3295, 0);
	private final static RSTile POS_WHEAT_DOOR = new RSTile(3162, 3290, 0);
	private final static RSTile POS_WHEAT = new RSTile(3161, 3295, 0);
	private final static RSTile POS_WINDMILL_DOOR = new RSTile(3166, 3302, 0);
	private final static RSTile POS_WINDMILL_BASE = new RSTile(3166, 3305, 0);
	private final static RSTile POS_COW = new RSTile(3253, 3274, 0);
	private final static RSTile POS_COW_DOOR = new RSTile(3253, 3267, 0);
	
	private static String _statusText = "";
	private static boolean _completed = false;

	@Override
	public final String questName() {
		return "Cook's Assistant";
	}
	
	@Override
	public final Quests quest() {
		return Quests.COOKS_ASSISTANT;
	}
	
	@Override
	public final HashMap<Integer, SKILLS> getSkillRequirements() {
		return null;
	}
	
	@Override
	public final int[] getItemRequirements() {
		QuestStatus queststatus = QuestManager.getQuestStatus(quest());
		if (queststatus.equals(QuestStatus.NOT_STARTED)) {
			int[] i = {
				ITEMID_POT,
				ITEMID_BUCKET,
			};
			return i;
		}
		return null;
	}
	
	@Override
	public LinkedHashMap<String, Object> getGuiElements() {
		LinkedHashMap<String, Object> map =  new LinkedHashMap<String, Object>();
		
		map.put("Start this script anywhere you'd like!", null);
		map.put("If you haven't started the quest yet or you haven't done the flour/milk part", null);
		map.put("you will need an empty bucket and pot in your inventory.", null);
		map.put("", null);
		map.put("If you haven't started the quest yet this script will attempt to do a home teleport or otherwise run to lumbridge.", null);
		
		return map;
	}
	
	@Override
	public final boolean onStart() {
		return true;
	}

	@Override
	public String statusText() {
		return _statusText;
	}
	
	@Override
	public boolean completed() {
		return _completed;
	}
	
	@Override
	public void completed(boolean set) {
		_completed = set;
	}

	@Override
	public final LinkedHashMap<String, Command> steps() {
		LinkedHashMap<String, Command> steps = new LinkedHashMap<String, Command>();

		steps.put("I need to find an egg.", new Command() { public void run() { findEgg(); } });
		steps.put("I have found an egg to give to the cook.", new Command() { public void run() { returnEgg(); } });
		steps.put("I need to find a pot of flour.", new Command() { public void run() { findFlour(); } });
		steps.put("I have found a pot of flour to give to the cook.", new Command() { public void run() { returnFlour(); } });
		steps.put("I need to find a bucket of milk.", new Command() { public void run() { findMilk(); } });
		steps.put("I have found a bucket of milk to give to the cook.", new Command() { public void run() { returnMilk(); } });

		return steps;
	}

	public void startQuest() {
		_statusText = "Moving to quest start..";
		
		if (!(new RSArea(POS_LUMB_CENTER, 30).contains(Player.getRSPlayer()))) {
			Manager.castLumbridgeHomeTeleport();
		}
		
		Manager.walkToAndTalkTo(POS_LUMB_KITCHEN, COOK_MODEL_POINT_COUNT);
		Manager.talkToOption("What's wrong?");
		Manager.talkToOption("I'm always happy to help a cook in distress.");
		Manager.talkContinue();
	}
	
	public static void findEgg() {
		_statusText = "Moving to Egg location..";
		
		Manager.walkTo(POS_FARM_DOOR);
		Manager.walkThroughDoor(POS_FARM, POS_FARM_DOOR);
		Manager.walkTo(POS_FARM);
		
		_statusText = "Searching for an egg..";
		
		while (Inventory.find(ITEMID_EGG).length == 0) {
			Manager.lootGroundItem(ITEMID_EGG);
		}
	}
	
	public static void returnEgg() {
		_statusText = "Returning egg to the cook..";
		Manager.walkToAndTalkTo(POS_LUMB_KITCHEN, COOK_MODEL_POINT_COUNT);
		
		Manager.talkContinue();
	}
	
	public static void findFlour() {
		if (Inventory.find(ITEMID_GRAIN).length == 0 && Player.getPosition().getPlane() == 0) {
			_statusText = "Moving to wheat location..";
			
			Manager.walkTo(POS_WHEAT_DOOR);
			Manager.walkThroughDoor(POS_WHEAT, POS_WHEAT_DOOR);
			Manager.walkTo(POS_WHEAT);
			
			while (Inventory.find(ITEMID_GRAIN).length == 0) 
				Manager.interactWithObject("Pick");
			
		}
		if (Player.getPosition().getPlane() != 2) {
			_statusText = "Moving to windmill..";
			
			Manager.walkTo(POS_WINDMILL_DOOR);
			Manager.walkThroughDoor(POS_WINDMILL_BASE, POS_WINDMILL_DOOR);
			Manager.walkTo(POS_WINDMILL_BASE);
			
			// if by chance there is already flour in the bin..
			Manager.interactWithObject("Empty", WINDMILL_BIN_FULL_MODEL_POINT_COUNT);

			// climb up
			while (Player.getPosition().getPlane() != 2) 
				Manager.interactWithObject("Climb-up");			
		}
		
		_statusText = "Operating hopper..";
		
		while (Inventory.find(ITEMID_GRAIN).length > 0) {
			_statusText = "Operating hopper..";
			Inventory.find(ITEMID_GRAIN)[0].click("Use");
			Manager.interactWithObject("Use", WINDMILL_HOPPER_MODEL_POINT_COUNT);
		}
		
		Manager.interactWithObject("Operate");
		General.sleep(5000,6500);
		
		while (Player.getPosition().getPlane() != 0) 
			Manager.interactWithObject("Climb-down");
		
		int failsafe = 0;
		while (Inventory.find(ITEMID_POT_FULL).length == 0 && failsafe < 20) {
			Manager.interactWithObject("Empty");
			failsafe++;
		}
	}

	public static void returnFlour() {
		_statusText = "Returning flour to the cook..";
		
		if (new RSArea(new RSTile(3166,3306), 3).contains(Player.getRSPlayer())) {
			Manager.walkThroughDoor(POS_WINDMILL_DOOR, POS_WINDMILL_DOOR);
			Manager.walkTo(POS_WINDMILL_DOOR);
		}
		
		Manager.walkToAndTalkTo(POS_LUMB_KITCHEN, COOK_MODEL_POINT_COUNT);
		Manager.talkContinue();
	}
	
	public static void findMilk() {
		Manager.walkTo(POS_COW_DOOR);
		
		Manager.walkThroughDoor(POS_COW, POS_COW_DOOR);
		Manager.walkTo(POS_COW);
		
		while (Inventory.find(ITEMID_BUCKET_OF_MILK).length == 0) {
			Manager.interactWithObject("Milk");
			General.sleep(1000,1500);
		}
	}
	
	public static void returnMilk() {
		_statusText = "Returning milk to the cook..";

		Manager.walkToAndTalkTo(POS_LUMB_KITCHEN, COOK_MODEL_POINT_COUNT);
		
		while(NPCChat.getOptions() == null && !QuestManager.isQuestCompleteScreenOpen()) {
			NPCChat.clickContinue(true);
		}
	}
}
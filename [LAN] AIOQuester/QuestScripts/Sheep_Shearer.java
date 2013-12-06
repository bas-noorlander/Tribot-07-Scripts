package scripts.questscripts;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;

import scripts.defines.Command;
import scripts.defines.QuestData;
import scripts.defines.Quests;
import scripts.questutilities.Manager;
import scripts.questutilities.QuestManager;

public class Sheep_Shearer extends QuestData {
	
	private final static RSTile POS_LUMB_CENTER = new RSTile(3217, 3219, 0);
	private final static RSTile POS_FRED_FARMER = new RSTile(3188, 3277, 0);
	private final static RSTile POS_FARM_DOOR = new RSTile(3188, 3279, 0);
	private final static RSTile POS_FARM_HOUSE_DOOR = new RSTile(3189, 3275, 0);
	private final static RSTile POS_FARM_HOUSE_INNER_DOOR = new RSTile(3188, 3272, 0);
	private final static RSTile POS_FARM_HOUSE_INSIDE_INNER = new RSTile(3186, 3272);
	private final static RSTile POS_FARM_INSIDE_HOUSE = new RSTile(3191, 3272, 0);
	
	private final static RSTile POS_SHEEP_STILE = new RSTile(3197, 3276, 0);
	private final static RSTile POS_SHEEP_GATE = new RSTile(3213, 3262, 0);
	private final static RSTile POS_SHEEP_OUTSIDE_GATE = new RSTile(3216, 3257, 0);
	
	private final static RSTile POS_SHEEP_NORTHWEST = new RSTile(3193, 3276, 0);
	private final static RSTile POS_SHEEP_SOUTHEAST = new RSTile(3212, 3258, 0);
	
	private final static RSTile POS_SPINNING_DOOR = new RSTile(3207, 3214, 1);
	private final static RSTile POS_SPINNING = new RSTile(3209, 3213, 1);
	private final static RSTile POS_SPINNING_ROOM_NORTHWEST = new RSTile(3212, 3212, 1);
	private final static RSTile POS_SPINNING_ROOM_SOUTHEAST = new RSTile(3209, 3213, 1);
	
	private final static RSArea AREA_SHEEPS = new RSArea(POS_SHEEP_NORTHWEST, POS_SHEEP_SOUTHEAST);
	private final static RSArea AREA_SPINNING_WHEEL_ROOM = new RSArea(POS_SPINNING_ROOM_NORTHWEST, POS_SPINNING_ROOM_SOUTHEAST);
	
	private final static int FRED_THE_FARMER_MODEL_POINT_COUNT = 2004;
	private final static int ITEM_ID_SHEARS = 1735;
	private final static int ITEM_ID_WOOL = 1737;
	private final static int ITEM_ID_BALL_OF_WOOL = 1759;
	
	private static boolean _completed = false;
	private static String _statusText = "";
	
	@Override
	public final String questName() {
		return "Sheep Shearer";
	}

	@Override
	public final Quests quest() {
		return Quests.SHEEP_SHEARER;
	}

	@Override
	public final HashMap<Integer, SKILLS> getSkillRequirements() {
		return null;
	}

	@Override
	public final int[] getItemRequirements() {
		return null;
	}

	@Override
	public boolean onStart() {
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
	public LinkedHashMap<String, Object> getGuiElements() {
		LinkedHashMap<String, Object> map =  new LinkedHashMap<String, Object>();
		map.put("Start this script anywhere you'd like!", null);
		map.put("It will attempt to do a home teleport and otherwise walk to Lumbridge.", null);
		map.put("It will pick up any required items during the quest.", null);
		map.put("Make sure that you have 21 or more free inventory spaces before starting!", null);
		return map;
	}
	
	@Override
	public final LinkedHashMap<String, Command> steps() {
		LinkedHashMap<String, Command> steps = new LinkedHashMap<String, Command>();

		steps.put("I need to collect", new Command() { public void run() { collectBallsofWool(); } });
		steps.put("money!", new Command() { public void run() { finishQuest(); } });

		return steps;
	}
	
	public void startQuest() {
		_statusText = "Moving to quest start";
		if (!(new RSArea(POS_LUMB_CENTER, 30).contains(Player.getRSPlayer())) && !(new RSArea(POS_FRED_FARMER, 20).contains(Player.getRSPlayer()))) 
			Manager.castLumbridgeHomeTeleport();
		
		Manager.walkTo(POS_FARM_DOOR);
		Manager.walkThroughDoor(POS_FRED_FARMER, POS_FARM_DOOR);
		if (!Manager.canReachNPC(FRED_THE_FARMER_MODEL_POINT_COUNT))
			Manager.walkThroughDoor(POS_FARM_INSIDE_HOUSE, POS_FARM_HOUSE_DOOR);
		
		if (!Manager.canReachNPC(FRED_THE_FARMER_MODEL_POINT_COUNT))
			Manager.walkThroughDoor(POS_FARM_HOUSE_INSIDE_INNER, POS_FARM_HOUSE_INNER_DOOR);
			
		Manager.walkToAndTalkTo(POS_FRED_FARMER, FRED_THE_FARMER_MODEL_POINT_COUNT);
		
		Manager.talkToOption("I'm looking for a quest.");
		Manager.talkToOption("Yes okay. I can do that.");
		Manager.talkContinue();
	}
	
	private static void collectShears() {
		while (Inventory.find(ITEM_ID_SHEARS).length == 0) {
			_statusText = "Gettings shears";
			if (!(new RSArea(POS_FRED_FARMER, 3).contains(Player.getRSPlayer())) && !(new RSArea(POS_FARM_INSIDE_HOUSE, 3).contains(Player.getRSPlayer()))) {
				Manager.walkTo(POS_FARM_DOOR);
				Manager.walkThroughDoor(POS_FRED_FARMER, POS_FARM_DOOR);
			}
			
			if (!(new RSArea(POS_FARM_INSIDE_HOUSE, 3).contains(Player.getRSPlayer()))) {
				Manager.walkTo(POS_FARM_HOUSE_DOOR);
				Manager.walkThroughDoor(POS_FARM_INSIDE_HOUSE, POS_FARM_HOUSE_DOOR);
			}
			
			Manager.walkTo(POS_FARM_INSIDE_HOUSE);
			Manager.lootGroundItem(ITEM_ID_SHEARS, 100);
		}
	}
	
	private static void collectWool() {
		_statusText = "Moving to sheep";
		
		if ((new RSArea(POS_FRED_FARMER, 3).contains(Player.getRSPlayer())) && (new RSArea(POS_FARM_INSIDE_HOUSE, 3).contains(Player.getRSPlayer()))) {
			Manager.walkThroughDoor(POS_FRED_FARMER, POS_FARM_HOUSE_DOOR);
			Manager.walkThroughDoor(POS_FARM_DOOR, POS_FARM_DOOR);
		}
		
		while (!AREA_SHEEPS.contains(Player.getRSPlayer()) && Inventory.find(ITEM_ID_WOOL).length < 20) {
			_statusText = "Crossing stile";
			Manager.walkTo(POS_SHEEP_STILE);
			Manager.interactWithObject("Climb-over");
		}
		
		while (Inventory.find(ITEM_ID_WOOL).length < 20) {
			
			_statusText = "Harassing sheep!";
			
			Manager.walkTo(AREA_SHEEPS.getRandomTile());
			
			RSNPC[] sheeps = Manager.findNearestNPC("Shear");
			if (sheeps != null)
				sheeps = NPCs.sortByDistance(Player.getPosition(), sheeps);
			
			for (int i = 0; i < sheeps.length; i++) {
				if (Inventory.find(ITEM_ID_WOOL).length >= 20)
					break;
				
				while (Player.isMoving())
					General.sleep(100,150);
					
				if (PathFinding.canReach(sheeps[i], false)) {
					if (!sheeps[i].isOnScreen()) 
						Camera.turnToTile(sheeps[i]);
					
					if (sheeps[i].click("Shear"))
						General.sleep(250,350);
				}
			}
		}
	}
	
	private static void spinWool() {
		
		_statusText = "Moving to spinning wheel in lumb castle.";
		while (AREA_SHEEPS.contains(Player.getRSPlayer())) {
			Manager.walkTo(POS_SHEEP_GATE);
			Manager.walkThroughDoor(POS_SHEEP_OUTSIDE_GATE, POS_SHEEP_GATE);
			Manager.walkTo(POS_SHEEP_OUTSIDE_GATE);
		}

		Manager.walkTo(POS_SPINNING_DOOR);
		Manager.walkThroughDoor(POS_SPINNING, POS_SPINNING_DOOR);
		Manager.walkTo(POS_SPINNING);
		
		while(Inventory.find(ITEM_ID_BALL_OF_WOOL).length < 20) {
			
			while (Interfaces.get(459) == null)
				Manager.interactWithObject("Spin");
			
			RSInterfaceChild ball = Interfaces.get(459, 69);
			if (ball != null) {
				//todo: find better way to do this (problems with interface positions)
				Mouse.click(127,114,3);
				
				General.sleep(800, 1300);
				Mouse.click(102,184,1);
				General.sleep(800, 1300);
				Keyboard.typeSend("20");
				General.sleep(5000, 6000);
				while (Player.getAnimation() == 894)
					General.sleep(1000,1500);
				}
		}
	}
	
	public static void collectBallsofWool() {
		if ((Inventory.find(ITEM_ID_WOOL).length + Inventory.find(ITEM_ID_BALL_OF_WOOL).length) >= 20)
			spinWool();
		
		if (Inventory.find(ITEM_ID_BALL_OF_WOOL).length >= 20)
			return;
		
		if (Inventory.find(ITEM_ID_WOOL).length < 20) {
			if (Inventory.find(ITEM_ID_SHEARS).length == 0)
				collectShears();
			else
				collectWool();
		}
	}

	public static void finishQuest() {
		if (AREA_SPINNING_WHEEL_ROOM.contains(Player.getPosition())) {
			Manager.walkThroughDoor(POS_SPINNING, POS_SPINNING_DOOR);
		}
		
		Manager.walkTo(POS_FARM_DOOR);
		Manager.walkThroughDoor(POS_FRED_FARMER, POS_FARM_DOOR);
		if (!Manager.canReachNPC(FRED_THE_FARMER_MODEL_POINT_COUNT))
			Manager.walkThroughDoor(POS_FARM_INSIDE_HOUSE, POS_FARM_HOUSE_DOOR);
		
		if (!Manager.canReachNPC(FRED_THE_FARMER_MODEL_POINT_COUNT))
			Manager.walkThroughDoor(POS_FARM_HOUSE_INSIDE_INNER, POS_FARM_HOUSE_INNER_DOOR);
			
		Manager.walkToAndTalkTo(POS_FRED_FARMER, FRED_THE_FARMER_MODEL_POINT_COUNT);
		
		while(NPCChat.getOptions() == null && !QuestManager.isQuestCompleteScreenOpen()) {
			NPCChat.clickContinue(true);
		}
	}
}

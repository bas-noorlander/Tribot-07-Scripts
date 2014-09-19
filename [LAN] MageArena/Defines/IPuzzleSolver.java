package scripts.Defines;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Game;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Magic;
import org.tribot.api2007.PathFinding;
import org.tribot.api2007.Player;
import org.tribot.api2007.Projection;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.DPathNavigator;
import org.tribot.api2007.util.PathNavigator;

import scripts.LANMageArena;

enum Direction {
	NORTH,
	EAST,
	SOUTH,
	WEST;
}

/**
 * @author Laniax
 */
public abstract class IPuzzleSolver {
	
	private boolean isInitialized = false;
	
	private RSGroundItem statue;
	
	private RSArea topEdge, bottomEdge, leftEdge, rightEdge;
	
	private DPathNavigator navigator = new DPathNavigator();
	
	RSTile walkTile = null;
	
	/***
	 * Gets the statue we have to move in this puzzle. Can be null if we are not in the room/etc.
	 */
	public RSGroundItem getStatue() {
		if (statue == null) {
			RSGroundItem[] items = GroundItems.find("Guardian statue");//find(Filters.GroundItems.actionsContains("Observe")); <- doesn't work as usual >_>
			if (items.length > 0) {
				statue = items[0];
			}
		}
		return statue;
	}
	
	/***
	 * Since rooms are dynamic, we have to calculate the maze from the statue.
	 */
	public void InitializeRoom() {
		
		statue = null;
		
		if (getStatue() != null) {
			
			General.println("Detected "+ puzzleName());
			
			// We check for the tiles one by one which are next to the statue until there are some we can reach.
			// This can not take longer then 10 checks since the maze is 10x10
			
			RSTile baseTile = getStatue().getPosition();
			RSTile leftEdgeTile = null;
			RSTile topEdgeTile = null;
			
			for (int i = 1; i <= 10; i++) {
				
				if (leftEdgeTile == null) {
					
					RSTile checkTileLeft = baseTile.translate(-i, 0);
					
					if (PathFinding.canReach(checkTileLeft, false)) {
						leftEdgeTile = checkTileLeft;
					}
				}
				if (topEdgeTile == null) {
					
					RSTile checkTileTop = baseTile.translate(0, i);
					
					if (PathFinding.canReach(checkTileTop, false)) {
						topEdgeTile = checkTileTop;
					}
				}
			}
			
			// Now that we have a tile on the top and left which are outside the box
			// We can calculate all the corners because we know all mazes are 10x10.
			int leftX = leftEdgeTile.getX();
			int rightX = leftX + 11;
			
			int topY = topEdgeTile.getY();
			int bottomY = topY - 11;
			
			topEdge = new RSArea(new RSTile(leftX+1, topY), new RSTile(rightX-1 ,topY));
			bottomEdge = new RSArea(new RSTile(leftX+1, bottomY), new RSTile(rightX-1 ,bottomY));
			
			leftEdge = new RSArea(new RSTile(leftX, topY-1), new RSTile(leftX, bottomY+1));
			rightEdge = new RSArea(new RSTile(rightX, topY-1), new RSTile(rightX, bottomY+1));
			
			isInitialized = true;
		}
	}
	
	public boolean isMazeView() {
		return Game.getSetting(629) == 1074601984;
	}
	
	public boolean isMazeComplete() {
		return Game.getSetting(629) == -1072881664;
	}
	
	public void doProjection(Graphics g) {
		if (getStatue() != null) {
			g.setColor(Colors.BlackTransparent);
			g.fillPolygon(Projection.getTileBoundsPoly(getStatue(), 0));
			g.setColor(Colors.Red);
			g.drawPolygon(Projection.getTileBoundsPoly(getStatue(), 1));
			
			RSTile lastPos = getStatue().getPosition();
			
			if (walkTile != null) {
				g.fillPolygon(Projection.getTileBoundsPoly(walkTile,0));
			}
			
			for (Entry<Integer, Integer> step : steps()) {
				
				RSTile newPos = lastPos.translate(step.getKey(), step.getValue());
				RSArea path = new RSArea(lastPos, newPos);
				g.setColor(Colors.BlackTransparent);
				
				for (RSTile tile : path.getAllTiles()) {
					g.fillPolygon(Projection.getTileBoundsPoly(tile, 0));
				}
				
				Integer i = 1;
				for (RSTile tile : leftEdge.getAllTiles()) {
					Polygon poly = Projection.getTileBoundsPoly(tile, 0);
					g.setColor(Colors.BlackTransparent);
					g.fillPolygon(poly);
					g.setColor(Color.WHITE);
					g.drawString(i.toString(), (int)poly.getBounds().getCenterX(), (int)poly.getBounds().getCenterY());
					i++;
				}
				
				g.setColor(Colors.Red);
				g.drawPolygon(Projection.getTileBoundsPoly(newPos, 1));
				
				lastPos = newPos;
				
			}
		}
	}
	
	/*
	 * Solves the puzzle, recursion based, solving one step each time it's called.
	 * With a maximum of ~10 steps per puzzle this should be safe for any stack overflows.
	 */
	public boolean Solve() {
		
		if (!isInitialized) {
			InitializeRoom();
		}
		
		if (!isMazeView()) {
			
			if (steps().size() > 0) {
				// Walk to maze and observe the statue
				
				//todo
				navigator.traverse(getStatue());
				Camera.turnToTile(getStatue());
				Camera.setCameraAngle(33);
				getStatue().click("Observe");
			}
			else
			{
				// Do completion process.
				
				return true;
			}
		}
		
		if (steps().size() > 0) {
			
			LANMageArena.currentPuzzleSolver = this;
			LANMageArena.doProjection = true;
			
			final Entry<Integer, Integer> step = steps().get(0);

			// Now some calcs to determine what direction we should pull the statue from.
			int x = step.getKey();
			int y = step.getValue();
			
			if (x != 0) {
				// we should either pull from left or right.
				if (x < 0) {
					// statue should move to the right.
					walkToAndCast(Direction.EAST);
				} else {
					// statue should move to the left.
					walkToAndCast(Direction.WEST);
				}
			}
			else if (y != 0){
				// we should either pull from top or bottom.
				if (y < 0) {
					// statue should move to the bottom.
					walkToAndCast(Direction.SOUTH);
				} else {
					// statue should move to the top.
					walkToAndCast(Direction.NORTH);
				}
			}
			
			final RSTile destinationTile = getStatue().getPosition().translate(step.getKey(), step.getValue());
			
			if (Timing.waitCondition(new Condition() {
				public boolean active() {
					return getStatue().getPosition().equals(destinationTile);}}, General.random(8000, 12000))) {
					// If the statue is on his destination position, we are done with this step!
					steps().remove(0);
			}
			return Solve();
		}

		return false;
	}
	
	private void walkToAndCast(Direction direction) {
		
		RSArea side = null;
		
		switch (direction) {
			case NORTH:
				side = topEdge;
				break;
			case EAST:
				side = rightEdge;
				break;
			case SOUTH:
				side = bottomEdge;
				break;
			case WEST:
				side = leftEdge;
				break;
		}
		
		// We are already on the correct side
		if (side == null || side.contains(Player.getRSPlayer().getPosition())){
			General.println("side was null");
			return;
		}
			
		
		// Check which side of the area is closest, and walk there.
		
		// apparently Pathfinding.distanceTo is desepracted. and we should use DPathNavigator instead...
		// ..which is not static, and does not have such methods, so we should take resources and generate 2 paths and check their lengths..
		
		General.println("topEdge: "+topEdge.getAllTiles().length);
		General.println("leftEdge: "+leftEdge.getAllTiles().length);
		General.println("rightEdge: "+rightEdge.getAllTiles().length);
		General.println("bottomEdge: "+bottomEdge.getAllTiles().length);
		
		
		RSTile[] sideTiles = side.getAllTiles();
		General.println("sideTiles: "+sideTiles.length);
		RSTile[] firstPath = navigator.findPath(sideTiles[0]);
		RSTile[] secondPath = navigator.findPath(sideTiles[9]);
		
		General.println("firstPath: "+firstPath.length);
		General.println("secondPath: "+secondPath.length);
		
		if (firstPath.length < secondPath.length) {
			walkTile = sideTiles[0];
			navigator.traverse(walkTile);
		} else {
			walkTile = sideTiles[9];
			navigator.traverse(walkTile);
		}
		
		final RSArea dest = side;
		
		Timing.waitCondition(new Condition() {
			public boolean active() {
				return !Player.isMoving() && dest.contains(Player.getPosition());
		}}, General.random(7000, 10000));
		
		walkTile = null;
		
		if (!Magic.isSpellSelected() || !Magic.getSelectedSpellName().equals("Telekinetic Grab")) {
			if (Magic.selectSpell("Telekinetic Grab")) {
				if (getStatue() != null) {
					//getStatue().click("Cast");
				}
			}
		}
	}

	public abstract String puzzleName();
	
	/**
	 * Should be an ordered list from first step to last, all defining their supposed destination position.
	 */
	public abstract ArrayList<Entry<Integer, Integer>> steps();
	

}

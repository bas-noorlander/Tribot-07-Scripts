package scripts.LANScriptTools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JFrame;
import org.tribot.api.General;
import org.tribot.api.Screen;
import org.tribot.api2007.types.RSTile;
import scripts.LanAPI.Projecting;
import scripts.LanAPI.Constants.Triplet;

/**
 * The thread that keeps running even after the script stops.
 * 
 * @author Laniax
 *
 */
public class ScriptToolsThread implements Runnable {
	
	

	private final Thread scriptThread;
	private Frame tribotFrame;
	private Component applet;
	public static Dock dock;
	public static boolean quitting = false;
	public static boolean doDock = true;
	
	public static RSTile selectedTile;

	public static ArrayList<Triplet<Polygon, Color, Boolean>> shapesToDraw = new ArrayList<Triplet<Polygon, Color, Boolean>>();
	public static ArrayList<RSTile> tilesToDraw = new ArrayList<RSTile>();

	public ScriptToolsThread(Thread scriptThread, Graphics g) {
		this.scriptThread = scriptThread;

		// Since the original paint thread died, create a new one.
		new Thread(new PaintThread((Graphics2D)g)).start();;
	}

	public void run() {
		// Wait until previous thread properly died down.
		// We do this because it has a nack of closing all script-made frames when stopping.
		while (scriptThread.isAlive()){
			General.sleep(1000);
		}

		// Apparently, people don't want me to use the Frame or Window classes to get the tribot window.
		// Sorry! you forgot the JFrame class!
		Frame[] frames = JFrame.getFrames();
		for (Frame frame : frames) {
			if (frame.getTitle().contains("TRiBot Old-School")) {
				tribotFrame = frame;
			}
		}

		// Boot up our GUI
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {
					dock = new Dock();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		// and lets try to attach to tribot now
		if (tribotFrame != null) {
			// set the Dock to the current tribot position/size
			Dimension dim = tribotFrame.getSize();
			dock.setPosition(tribotFrame.getLocation(), (int) dim.getWidth());

			// Listen to resize events on the tribot frame
			tribotFrame.addComponentListener(Listeners.getResizeListener());

			// Listen to move events on the tribot frame
			tribotFrame.addComponentListener(Listeners.getMoveListener());
			
			// Tribot's mouse interfaces only work when a script is running.
			// So we have to hook the mouse to the applet the old fashioned way.
			applet = tribotFrame.findComponentAt(new Point((int)Screen.getViewport().getCenterX(), (int)Screen.getViewport().getCenterY()));
			if (applet != null) {
				applet.addMouseListener(Listeners.getMouseListener());
			}
		}

		while(!quitting) {

			// Tribot closes all frames when a script stops.
			// We might get away with a sleep() on our script, but that won't work if the user runs another script and stops that.
			// So we have to check continuously 
			if (!dock.isVisible())
				dock.setVisible(true);

			// 'select' the tile where a double mouseclick happened
	

			General.sleep(1000);
		}

		dispose();
		dock.dispose();

		General.println("[LAN] ScriptTools closed.");
	}
	
	public static void setSelectedTile(RSTile tile) {
		if (tile == null)
			return;
		
		tilesToDraw.remove(selectedTile); // remove old selected tile from paint
		selectedTile = tile;
		tilesToDraw.add(selectedTile);
		
		
		switch (dock.getOpenTab()) {
		
			case INSPECT_TOOL:
				dock.refreshInspectTool();
				break;
		default:
			break;
			
		}
	}
	
	public static void onTileClick(Point point) {
		setSelectedTile(Projecting.getTileAtPoint(point));
	}

	public void dispose() {
		// Clean up with what we messed with!
		dock.removeWindowListener(Listeners.getCloseListener());

		if (tribotFrame != null) {
			tribotFrame.removeComponentListener(Listeners.getMoveListener());
			tribotFrame.removeComponentListener(Listeners.getResizeListener());
			applet.removeMouseListener(Listeners.getMouseListener());
		}
	}
}

package scripts.LANScriptTools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import org.tribot.api.General;
import org.tribot.api2007.Projection;
import org.tribot.api2007.types.RSTile;

import scripts.LanAPI.Constants.Triplet;

/**
 * @author Laniax
 *
 */
public class PaintThread implements Runnable {

	private final Graphics2D g;
	
	private final RenderingHints antialiasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	private Color blackTransparent = new Color(0, 0, 0, 120);
	private Color cyanSlightTransparent = new Color(0,246,255, 200);

	public PaintThread(Graphics2D g) {
		g.setRenderingHints(antialiasing);
		this.g = g;
	}

	@Override
	public void run() {

		// Quit this thread ASAP when the scripttools thread stops.
		while(!ScriptToolsThread.quitting) {

			for (Triplet<Polygon, Color, Boolean> shape : ScriptToolsThread.shapesToDraw) {
				
				g.setColor(shape.getB());
				g.fillPolygon(shape.getA());
				
				if (shape.getC()) {
					g.setColor(shape.getB().darker());
					g.drawPolygon(shape.getA());
				}
			}

			for (RSTile tile : ScriptToolsThread.tilesToDraw) {
				
				Polygon tilePoly = Projection.getTileBoundsPoly(tile, 0);
				g.setColor(blackTransparent);
				g.fillPolygon(tilePoly);
				g.setColor(cyanSlightTransparent);
				g.drawPolygon(tilePoly);
			}

			General.sleep(16);
		}
	}

}

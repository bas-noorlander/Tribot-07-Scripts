package scripts.Mgrs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.tribot.api.Timing;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSGroundItem;

import scripts.LANOgreRanger;

/**
 * @author Laniax
 *
 */
// I like to split my paint from the rest of my script, sue me.
public class PaintMgr {

	private final static Image paintImage = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/OrgeRanger/paint.png");
	private final static Font font = new Font("Arial", 1, 12);
	
	public static final long startTime = System.currentTimeMillis();
	public static boolean showPaint = true;
	public static boolean showArrowTracker = false;
	public static int startRangeXP = 0;
	public static int xpGained = 0;
	
	public static HashMap<RSGroundItem, Long> arrowLootList = new HashMap<RSGroundItem, Long>();

	public static void onPaint(Graphics g1) {
		
		if (showPaint && !LANOgreRanger.isQuitting) {
			
			Graphics2D g = (Graphics2D)g1;
			
			g.drawImage(paintImage, -4, 245, null);
			
			g.setColor(Color.WHITE);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.setFont(font);
			
			long timeRan = System.currentTimeMillis() - startTime;
			double secondsRan = (int) (timeRan/1000);
			double hoursRan = secondsRan/3600;
			xpGained = Skills.getXP(SKILLS.RANGED) - startRangeXP;
			String xpPerHour = NumberFormat.getNumberInstance().format(Math.round(xpGained / hoursRan));
			
			if (xpGained > 0) {
				String ttl = Timing.msToString((long)(Skills.getXPToNextLevel(SKILLS.RANGED) * 3600000D / Math.round(xpGained / hoursRan)));
				g.drawString("TTL: "+ttl, 285, 270);
			}
			g.drawString("Xp gained: "+xpGained+" ("+xpPerHour+" XP/h)", 90, 270);
			g.drawString("Runtime: "+Timing.msToString(timeRan), 260, 290);
			g.drawString("Status: "+LANOgreRanger.statusText, 90, 290);
		}
	}
	
	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

}

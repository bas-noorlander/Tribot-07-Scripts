package scripts.Managers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.tribot.api.Timing;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.GameTab.TABS;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Skills.SKILLS;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;

import scripts.LANChaosKiller;
import scripts.Defines.ItemIDs;

/**
 * Helper class that handles the script's paint logic.
 * 
 * @author Laniax
 *
 */
public class PaintMgr {

	private final static Font font = getFont("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/SF%20Electrotome.ttf", 22f);
	private final static Font fontLarge = font.deriveFont(33f);
	private final static Font fontSmall = font.deriveFont(18f);
	private final static Font fontHerb = new Font("Arial", 1, 7);

	private final static Image paint = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/scriptPaint.png");
	private final static Image paintShow = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/scriptPaintToggle.png");
	private final static Image herbToggleOn = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/herbToggleOn.png");
	private final static Image herbToggleOff = getImage("https://dl.dropboxusercontent.com/u/21676524/RS/ChaosKiller/Script/herbToggleOff.png");
	
	public final static Color colorTransparent = new Color(0, 0, 0, 128);
	public final static Rectangle paintToggle = new Rectangle(406, 465, 99, 26);
	public final static Rectangle settingsToggle = new Rectangle(406, 427, 99, 26);
	public final static Rectangle herbToggle = new Rectangle(704, 118, 45, 41);
	
	public static final long startTime = System.currentTimeMillis();
	public static boolean showPaint = false;
	public static boolean showLootInfo = false;
	public static boolean showHerbIdentifier = true;
	
	private static int[][] skillXPLocations = new int[][] {
		 {60, 433},
		 {60, 458},
		 {60, 483},
		 
		 {240, 433},
		 {240, 458},
		 {240, 483},
	};

	public static LinkedHashMap<SKILLS, Integer> startSkillInfo = new LinkedHashMap<SKILLS, Integer>();
	
	public static void onPaint(Graphics g1) {

		if (showPaint) {
			
			Graphics2D g = (Graphics2D)g1;

			long timeRan = System.currentTimeMillis() - startTime;
			double secondsRan = (int) (timeRan/1000);
			double hoursRan = secondsRan/3600;
			g.drawImage(paint, 0, 249, null);
			g.setFont(font);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			drawShadowedText(LANChaosKiller.statusText, fontLarge, 141, 372, g);
			drawShadowedText(Timing.msToString(timeRan), font, 115, 406, g);
			drawShadowedText(Integer.toString(LANChaosKiller.druidsKilled), font, 327, 406, g);
			
			int i = 0;
			for (Entry<SKILLS, Integer> entry : startSkillInfo.entrySet()) {
				int xpGained = Skills.getXP(entry.getKey()) - entry.getValue();
				String xp = NumberFormat.getNumberInstance().format(Math.round(xpGained / hoursRan));
				drawShadowedText(xpGained + " ("+xp+" XP/h)", fontSmall, skillXPLocations[i][0], skillXPLocations[i][1], g);
				i++;
			}
		} else {
			g1.drawImage(paintShow, paintToggle.x - 4 , paintToggle.y - 4, null);
		}
		
		if (showHerbIdentifier && GameTab.getOpen() == TABS.INVENTORY) {
			
			g1.drawImage(herbToggleOn, herbToggle.x - 8 , herbToggle.y - 13, null);
			
			for (RSItem item : Inventory.getAll()) {
				RSItemDefinition def = item.getDefinition();
				if (def != null) {
					
					if (!def.getName().equals("Herb")) 
						continue;
					
					ItemIDs itemID = ItemIDs.valueOf(item.getID());
					if (itemID != null) {
						Rectangle rect = item.getArea();
						g1.setFont(fontHerb);
						g1.setColor(colorTransparent);
						g1.fillRoundRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), 3, 3);
						g1.setColor(Color.WHITE);
						g1.drawString(itemID.toString(), (int)rect.getX(), (int)(rect.getY() + (rect.getHeight() / 2)));
					}
				}
			}
		} else {
			g1.drawImage(herbToggleOff, herbToggle.x - 8 , herbToggle.y - 13, null);
		}
	}
	
	private static void drawShadowedText(String text, Font font, int x, int y, Graphics2D g) {
		TextLayout textLayout = new TextLayout(text, font, g.getFontRenderContext());
		g.setColor(Color.DARK_GRAY);
		textLayout.draw(g, x+1, y);
		textLayout.draw(g, x, y+1);
		textLayout.draw(g, x-1, y);
		textLayout.draw(g, x, y-1);
		
		g.setColor(Color.WHITE);
		textLayout.draw(g, x, y);
	}

	public static Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	
	public static Font getFont(String url, float size) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream());
			return Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size);
		}
		catch(Exception e) {
			return null;
		}
	}
}

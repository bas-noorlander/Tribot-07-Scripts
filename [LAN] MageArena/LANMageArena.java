package scripts;

import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

import scripts.Defines.IPuzzleSolver;

/**
 * @author Laniax
 *
 */
@ScriptManifest(authors = { "Laniax" }, category = "Magic", name = "[LAN] MageArena")
public class LANMageArena extends Script implements Painting {

	private boolean isQuitting = false;
	
	//telekinetic
	public static boolean doProjection = false;
	public static IPuzzleSolver currentPuzzleSolver;
	
	@Override
	public void run() {
		

		while (!isQuitting) {
			// GUI determines what we should go do.
			General.sleep(100);
		}
		
		
		
	}

	@Override
	public void onPaint(Graphics g) {
		
		if (doProjection){
			currentPuzzleSolver.doProjection(g);
		}
		
	}

}

package scripts.LANScriptTools;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

/**
 * @author Laniax
 * 
 */

@ScriptManifest(authors = { "Laniax" }, category = "Tools", name = "[LAN] ScriptTools")
public class LANScriptTools extends Script implements Painting {

	Graphics g;
	
	@Override
	public void run() {

		// We pass a reference to the script thread to see if/when it closes properly.
		final Thread scriptThread = Thread.currentThread();

		// However, we also want to keep the ability to paint on the screen, so we pass the graphics object allong to the new thread.
		while (g == null)
			General.sleep(100);

		// Fire off a new thread from the EDT
		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					// Pass it a reference to the script thread to see if it properly closes.
					new Thread(new ScriptToolsThread(scriptThread, g)).start();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void onPaint(Graphics g) {
		this.g = g;
	}
}
package scripts.LANScriptTools;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

/**
 * @author Laniax
 *
 */
public class Listeners {
	
	private static final ResizeListener _resizeListener = new ResizeListener();
	private static final MoveListener _moveListener = new MoveListener();
	private static final CloseListener _closeListener = new CloseListener();
	private static final CursorListener _mouseListener = new CursorListener();
	
	
	public static ResizeListener getResizeListener() {
		return _resizeListener;
	}
	
	public static MoveListener getMoveListener() {
		return  _moveListener;
	}
	
	public static CloseListener getCloseListener() {
		return  _closeListener;
	}
	
	public static CursorListener getMouseListener() {
		return  _mouseListener;
	}
}

class CursorListener extends MouseAdapter {

	public void mousePressed(MouseEvent e) {
		
		if (SwingUtilities.isMiddleMouseButton(e))
			ScriptToolsThread.onTileClick(e.getPoint());
		
	}
}
/**
 * A ComponentAdapter which will keep track of the height of the frame it is listening to.
 * We use it to resize the Dock when the tribot window is resized.
 */
class ResizeListener extends ComponentAdapter {
	
	public void componentResized(ComponentEvent e) {
		if (ScriptToolsThread.doDock) {
			ScriptToolsThread.dock.setPosition(e.getComponent().getLocation(),(int) e.getComponent().getSize().getWidth());
		}
	}
}

/**
 * A ComponentAdapter which will keep track of the position of the frame it is listening to.
 * We use it to reposition the Dock when the tribot window is moved.
 */
class MoveListener extends ComponentAdapter {
	
	public void componentMoved(ComponentEvent e) {
			if (ScriptToolsThread.doDock)
			ScriptToolsThread.dock.setPosition(e.getComponent().getLocation(),(int) e.getComponent().getSize().getWidth());
	}
}

/**
 * A WindowAdapter which will fire if the frame it is attached to closes.
 * We use it to stop the script if the dock is closed.
 */
class CloseListener extends WindowAdapter {

	public void windowClosing(WindowEvent windowEvent) {
		ScriptToolsThread.quitting = true;
	}
}
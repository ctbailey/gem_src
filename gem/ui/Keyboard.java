package gem.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Keyboard implements KeyListener {
	Set<String> keysPressed = new LinkedHashSet<String>();
	public Set<String> getKeysPressed() {
		return Collections.unmodifiableSet(keysPressed);
	}
	@Override
	public void keyPressed(KeyEvent e) {
		keysPressed.add(KeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keysPressed.remove(KeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// Do nothing
	}
}

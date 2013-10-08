package gem.ui;

import gem.Debug;
import gem.Global;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class Keyboard {
	Set<String> keysPressed = new LinkedHashSet<String>();
	@SuppressWarnings("serial")
	public Keyboard() {
		Global.userInterface.mainFrame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("S"), "sPressed");
		Global.userInterface.mainFrame.getRootPane().getActionMap().put("sPressed",
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						keysPressed.add("S");
					}
		});
		Global.userInterface.mainFrame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("released S"), "sReleased");
		Global.userInterface.mainFrame.getRootPane().getActionMap().put("sReleased",
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						keysPressed.remove("S");
					}
		});
	}
	public Set<String> getKeysPressed() {
		return Collections.unmodifiableSet(keysPressed);
	}
}

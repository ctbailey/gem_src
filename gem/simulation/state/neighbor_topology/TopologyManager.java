package gem.simulation.state.neighbor_topology;

import static gem.Global.userInterface;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import gem.ui.UserDidNotConfirmException;

public class TopologyManager extends AbstractTopologyManager {
	private final TopologyManager.UserInterfaceManager uiManager;
	
	public TopologyManager() {
		uiManager = new TopologyManager.UserInterfaceManager();
	}

	public List<JMenuItem> getMenuItems() {
		return uiManager.getMenuItems();
	}
	
	// User interface
	private class UserInterfaceManager {
		private JCheckBoxMenuItem smallWorldMenuItem;
		private static final String smallWorldMenuItemText =  "Use small world neighbors";
		public List<JMenuItem> getMenuItems() {
			List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
			smallWorldMenuItem = new JCheckBoxMenuItem(smallWorldMenuItemText);
			smallWorldMenuItem.addActionListener(new SmallWorldMenuItemListener());
			menuItems.add(smallWorldMenuItem);
			return menuItems;
		}
		private float getSmallWorldProbabilityFromUser() throws UserDidNotConfirmException {
			SmallWorldProbabilityGrabber grabber = new SmallWorldProbabilityGrabber(userInterface.mainFrame);
			return grabber.getInput();
		}
		private class SmallWorldProbabilityGrabber {
			private JFrame frame;
			private SpinnerNumberModel smallWorldNeighborsPerCellModel;
			
			SmallWorldProbabilityGrabber(JFrame frame) {
				this.frame = frame;
			}
			float getInput() throws UserDidNotConfirmException {
				JPanel statePanel = new JPanel();
				smallWorldNeighborsPerCellModel = new SpinnerNumberModel();
				smallWorldNeighborsPerCellModel.setValue(0.3);
				smallWorldNeighborsPerCellModel.setStepSize(0.01);
				smallWorldNeighborsPerCellModel.setMinimum(0.0);
				smallWorldNeighborsPerCellModel.setMaximum(1.0);
				JSpinner neighborsPerCellSpinner = new JSpinner(smallWorldNeighborsPerCellModel);
				neighborsPerCellSpinner.setPreferredSize(new Dimension(75, 25));
				statePanel.add(new JLabel("Rewiring probability for each neighbor connection"));
				statePanel.add(neighborsPerCellSpinner);
				
				int userAction = JOptionPane.showConfirmDialog(frame, statePanel, "Probability to rewire a neighbor connection?", JOptionPane.OK_CANCEL_OPTION);
				if(userAction == JOptionPane.OK_OPTION) {
					double numberAsDouble = (Double)smallWorldNeighborsPerCellModel.getNumber();;
					return (float)numberAsDouble;
				} else {
					throw new UserDidNotConfirmException();
				}
			}
		}
		private class SmallWorldMenuItemListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					boolean isNowSmallWorld;
					float smallWorldProbability;
					if(smallWorldMenuItem.isSelected()) {
						isNowSmallWorld = true;
						smallWorldProbability = getSmallWorldProbabilityFromUser();
						smallWorldMenuItem.setText(appendProbabilityToSmallWorldMenuItemText(smallWorldProbability));
					} else {
						isNowSmallWorld = false;
						smallWorldProbability = 0;
						smallWorldMenuItem.setText(smallWorldMenuItemText);
					}
					setTopology(TopologyFactory.createTopology(isNowSmallWorld, smallWorldProbability));
				} catch(UserDidNotConfirmException ex) {
					// setSelected() doesn't trigger a new action event, so doesn't
					// result in another call to actionPerformed(),
					// which would spuriously call setTopology(),
					// resulting in a spurious board reset
					smallWorldMenuItem.setSelected(false);
				}
			}
			private String appendProbabilityToSmallWorldMenuItemText(Float p) {
				return smallWorldMenuItemText + " (p = " + Float.toString(p) + ")";
			}
		}
	}
}

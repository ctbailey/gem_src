package gem.simulation.state.neighbor_topology;

import static gem.Global.userInterface;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import gem.simulation.state.neighbor_topology.TopologySpecification.TopologyAlgorithm;
import gem.ui.UserDidNotConfirmException;

public class TopologyManager extends AbstractTopologyManager {
	private final TopologyManager.UserInterfaceManager uiManager;
	private TopologySpecification topologySpecification;
	
	public TopologyManager() {
		uiManager = new TopologyManager.UserInterfaceManager();
		topologySpecification = new TopologySpecification(false, TopologyAlgorithm.Moore);
	}

	public List<JMenuItem> getMenuItems() {
		return uiManager.getMenuItems();
	}
	
	// User interface
	private class UserInterfaceManager {
		private JMenu topologySubmenu;
		private JLabel currentTopologyLabel;
		private JCheckBoxMenuItem directedNeighborsMenuItem, rewireOnlySelectedCellsMenuItem;
		private JRadioButtonMenuItem mooreRadioButton, edgeSwapRadioButton, wattsStrogatzRadioButton;
		private JRadioButtonMenuItem buttonForPreviouslySelectedAlgorithm;
		
		// Menu item text
		private static final String useDirectedNeighborsMenuItemText = "Use directed (asymmetrical) neighbor relations";
		private static final String rewireOnlySelectedCellsMenuItemText = "Rewire only selected cells";
		private static final String currentTopologyLabelText = "Current topology";
		private static final String mooreTopologyRadioButtonText = "Moore topology";
		private static final String edgeSwapRadioButtonText = "Edge swap topology";
		private static final String wattsStrogatzRadioButtonText = "Watts-Strogatz topology";
		
		private void changeTopologySpecification(boolean isDirected, TopologyAlgorithm algorithm, boolean rewireOnlySelectedCells) throws UserDidNotConfirmException {
			topologySpecification.isDirected = isDirected;
			topologySpecification.algorithm = algorithm;
			topologySpecification.rewireOnlySelectedCells = rewireOnlySelectedCells;
			float rewiringProbability;
			if(algorithm.isSmallWorld()) {
				rewiringProbability = uiManager.getSmallWorldProbabilityFromUser();
			} else {
				rewiringProbability = -1;
			}
			setTopology(topologySpecification.createTopology(rewiringProbability));
			currentTopologyLabel.setText(currentTopologyLabelText + "(Rewiring probability: " + getCurrentTopology().getRewiringProbability() + ")");
		}
		
		public List<JMenuItem> getMenuItems() {
			topologySubmenu = new JMenu("Neighbor topology");
			List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
			menuItems.add(topologySubmenu);
			
			// Add items to the topology submenu
			populateTopologySubmenu();

			return menuItems;
		}
		private void populateTopologySubmenu() {
			// Items for selecting a topology
			ButtonGroup topologyButtonGroup = new ButtonGroup();
		
			currentTopologyLabel = new JLabel(currentTopologyLabelText + " (Rewiring probability: " + getCurrentTopology().getRewiringProbability() + ")");
			topologySubmenu.add(currentTopologyLabel);
			
			mooreRadioButton = new JRadioButtonMenuItem(mooreTopologyRadioButtonText);
			topologySubmenu.add(mooreRadioButton);
			topologyButtonGroup.add(mooreRadioButton);
			buttonForPreviouslySelectedAlgorithm = mooreRadioButton;
			mooreRadioButton.setSelected(true);
			mooreRadioButton.addActionListener(new SelectAlgorithmRadioButtonListener(TopologyAlgorithm.Moore));
			
			edgeSwapRadioButton = new JRadioButtonMenuItem(edgeSwapRadioButtonText);
			topologySubmenu.add(edgeSwapRadioButton);
			topologyButtonGroup.add(edgeSwapRadioButton);
			edgeSwapRadioButton.addActionListener(new SelectAlgorithmRadioButtonListener(TopologyAlgorithm.EdgeSwap));
			
			wattsStrogatzRadioButton = new JRadioButtonMenuItem(wattsStrogatzRadioButtonText);
			topologySubmenu.add(wattsStrogatzRadioButton);
			topologyButtonGroup.add(wattsStrogatzRadioButton);
			wattsStrogatzRadioButton.addActionListener(new SelectAlgorithmRadioButtonListener(TopologyAlgorithm.WattsStrogatz));
			
			topologySubmenu.addSeparator();
			
			directedNeighborsMenuItem = new JCheckBoxMenuItem(useDirectedNeighborsMenuItemText);
			topologySubmenu.add(directedNeighborsMenuItem);
			directedNeighborsMenuItem.addActionListener(new BinaryTopologyParameterListener(directedNeighborsMenuItem) {
				@Override
				protected void onStateChange(boolean isNowSelected)
						throws UserDidNotConfirmException {
					changeTopologySpecification(isNowSelected, topologySpecification.algorithm, topologySpecification.rewireOnlySelectedCells);
				}
			});
			rewireOnlySelectedCellsMenuItem = new JCheckBoxMenuItem(rewireOnlySelectedCellsMenuItemText);
			topologySubmenu.add(rewireOnlySelectedCellsMenuItem);
			rewireOnlySelectedCellsMenuItem.addActionListener(new BinaryTopologyParameterListener(rewireOnlySelectedCellsMenuItem) {
				@Override
				protected void onStateChange(boolean isNowSelected) throws UserDidNotConfirmException {
					changeTopologySpecification(topologySpecification.isDirected, topologySpecification.algorithm, isNowSelected);
				}
			});
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
		private abstract class BinaryTopologyParameterListener implements ActionListener {
			private final JCheckBoxMenuItem menuItem;
			public BinaryTopologyParameterListener(JCheckBoxMenuItem menuItem) {
				this.menuItem = menuItem;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					onStateChange(menuItem.isSelected());
				} catch(UserDidNotConfirmException ex) {
					// setSelected() doesn't trigger a new action event, so doesn't
					// result in another call to actionPerformed(),
					// which would spuriously call setTopology(),
					// resulting in a spurious board reset
					menuItem.setSelected(false);
				}
			}
			protected abstract void onStateChange(boolean isNowSelected) throws UserDidNotConfirmException;
		}
		private class SelectAlgorithmRadioButtonListener implements ActionListener {
			private final TopologyAlgorithm algorithm;
			public SelectAlgorithmRadioButtonListener(TopologyAlgorithm algorithm) {
				this.algorithm = algorithm;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					changeTopologySpecification(topologySpecification.isDirected, algorithm, topologySpecification.rewireOnlySelectedCells);
					buttonForPreviouslySelectedAlgorithm = getButtonForThisAlgorithm();
				} catch(Exception ex) {
					buttonForPreviouslySelectedAlgorithm.setSelected(true);
				}
			}
			private JRadioButtonMenuItem getButtonForThisAlgorithm() {
				JRadioButtonMenuItem button;
				switch(algorithm) {
					case WattsStrogatz:
						button = wattsStrogatzRadioButton;
						break;
					case EdgeSwap:
						button = edgeSwapRadioButton;
						break;
					case Moore:
						button = mooreRadioButton;
						break;
					default:
						throw new RuntimeException("Didn't recognize the algorithm for creating a new neighbor topology.");
				}
				return button;
			}
		}
	}
}

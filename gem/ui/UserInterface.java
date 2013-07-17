/*
	 * Copyright 2013, C. Thomas Bailey
	 * 
	 * This file is part of GEM: The Geographic Modeler.
	 *
     * GEM is free software: you can redistribute it and/or modify
     * it under the terms of the GNU General Public License as published by
     * the Free Software Foundation, either version 3 of the License, or
     * (at your option) any later version.
     * 
     * GEM is distributed in the hope that it will be useful,
     * but WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     * GNU General Public License for more details.
     * 
     * You should have received a copy of the GNU General Public License
     * along with GEM.  If not, see <http://www.gnu.org/licenses/>.
	 */

package gem.ui;

import static gem.AutomatonGlobal.*;

import gem.*;
import gem.simulation.*;
import gem.simulation.AbstractBoard.GameConfigurationKey;
import gem.simulation.ICell.CellState;
import gem.talk_to_outside_world.RandomDotOrgRandomNumberSource;
import gem.talk_to_outside_world.validation.JsonLogger;
import gem.talk_to_outside_world.validation.SimpleValidationBoardState;
import gem.ui.BoardPanel.BoardDisplayConfigurationKey;
import gem.ui.BoardPanel.MapDisplaySettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class UserInterface {
	public static final boolean IS_PUBLIC_BUILD = true;
	
	public JPanel controlPanel;
	public JPanel infoPanel;
	public JPanel westernPanel;
	public JButton zoomToFitButton;
	public JRadioButtonMenuItem pseudoRandomModeRB;
	public JRadioButtonMenuItem trueRandomModeRB;
	public SpinnerNumberModel randomizeSpinnerModel;
	public JCheckBox goForwardCheckBox;
	public BoardPanel boardPanel;
		
	public boolean showDebugArea = true;
	public File tallyRegionsFile;	
	
	public JFrame debugFrame;
	public JScrollPane debugScrollPane;
	
	public JMenuBar menuBar;
	
	public JTextArea debugArea; // Separate window where debugging messages appear; accessed through static methods in the Debug class
	
	public JFrame mainFrame; // The main window of the automaton that contains the board area, control area, etc.
		public JScrollPane boardScrollPane; // The scrolling panel; contains the board area
			public JViewport boardViewport; // The actual "port hole" through which one sees the underlying object when it won't fit in the boardScrollPane
		public JLabel sliderMaxLabel;
		public JLabel sliderMinLabel;
		public JCheckBoxMenuItem showMapMenuItem; // A check box in the "Board" menu which allows the user to toggle display of an already-in-memory map image.
		public JRadioButton stretchMapToFitBoardRB;
		public JRadioButton scaleMapToFitBoardRB;
	public JSlider playbackSlider;
	
	// Constants
	private static final String ABOUT_MESSAGE = 
			"GEM: The Geographic Modeler" + "\n" +
			"July, 2013" + "\n" +
			"Written by C. Thomas Bailey, University of Georgia" + "\n" +
			"ctbailey@uga.edu" + "\n" + "\n" +
			"GEM is free software: you can redistribute it and/or modify" + "\n" +
			"it under the terms of the GNU GPL v3, or" + "\n" +
			"(at your option) any later version." + "\n" + "\n" +
			"GEM is distributed in the hope that it will be useful," + "\n" +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of" + "\n" +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" + "\n" +
			"GNU General Public License for more details.";
	
	// Notifiers
	public GoForwardNotifier goForwardNotifier;
	public ShouldShowMapNotifier shouldShowMapNotifier;
	
	static final long serialVersionUID = 6;
	
	public UserInterface() {
		goForwardNotifier = new GoForwardNotifier();
		shouldShowMapNotifier = new ShouldShowMapNotifier();
	}
	
	// Primary method. Calls sub-methods which build individual sections of the UI
	public void buildUI() {
		
		/*
		 * Creates the primary JFrame (mainFrame) of the automaton.
		 * Also calls additional methods which in turn build other parts of the UI.
		 * 
		 */
		
		mainFrame = new JFrame();
		
		buildBoardArea();
		buildWesternArea();
		buildMenuArea();
		buildSouthernArea();
		
		mainFrame.setBounds(0,0,1000,800);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		
		if(!IS_PUBLIC_BUILD) {
			buildDebugArea();
		}
		
		// Make sure the main frame shows up on top of the debug area
		mainFrame.toFront();
		boardPanel.getBoardImageSource().refreshBoardImage();
	}
	
	// Methods which build individual sections of the UI
	void buildBoardArea() {
		
		/*
		 * Builds the board area, which is located in the central region of the main window.
		 * The board area contains two objects:
		 * 
		 * 	- The board itself, which allows manipulation/visualization
		 * 	of the state of the automaton
		 * 
		 * 	- The inset panel, which allows the board to retain its correct dimensions
		 * 	and provides empty space for padding when the board is smaller than
		 * 	the viewport of the scrollpane.
		 * 
		 */
		
		// Build the boardPanel, which actually displays all the cells,
		// and set its default size to 500,500
		boardPanel = new BoardPanel();
		boardPanel.setPreferredSize(new Dimension(500,500));
		
		// Build the inset panel, which simply holds the board panel in the center of the scrollpane
		// The InsetPanel class also handles zooming with the mouse wheel
		InsetPanel insetPanel = new InsetPanel(new GridBagLayout()); 
		insetPanel.add(boardPanel, new GridBagConstraints());
		
		// Create the scrollpane and add the inset panel to it
		boardScrollPane = new JScrollPane(insetPanel);
		boardScrollPane.setWheelScrollingEnabled(false);
		
		// Add the scrollpane to the main frame
		mainFrame.getContentPane().add(boardScrollPane,BorderLayout.CENTER);

		// Make a persistent reference to the JScrollPane's viewport to enable custom scrolling
		boardViewport = boardScrollPane.getViewport();
		
	}
	void buildWesternArea() {
				
		westernPanel = new JPanel();
		westernPanel.setLayout(new GridBagLayout());
			
	// Transport panel
		JPanel transportPanel = new JPanel();
		GridBagConstraints transportConstraints = new GridBagConstraints();
		transportConstraints.gridx = 0;
		transportConstraints.gridy = 0;
		transportConstraints.gridheight = 2;
		transportConstraints.weighty = 1;
		transportConstraints.fill = GridBagConstraints.BOTH;
		transportPanel.setLayout(new GridLayout(2,2));
			JButton startButton = new JButton("Start");
			startButton.addActionListener(new StartIteratingListener());
			transportPanel.add(startButton);
			goForwardCheckBox = new JCheckBox("Forward");
			goForwardCheckBox.addItemListener(goForwardNotifier);
			/*
			 * GO FORWARD CHECK BOX COMMENTED OUT
			 */
			//transportPanel.add(goForwardCheckBox);
			new StartButtonUpdater(startButton); // Automatically registers itself and updates the start button as necessary
			
			JButton slowerButton = new JButton("Slower");
			slowerButton.addActionListener(new SlowerListener());
			transportPanel.add(slowerButton);
			JButton fasterButton = new JButton("Faster");
			fasterButton.addActionListener(new FasterListener());
			transportPanel.add(fasterButton);
			
	// Rules panel
		JPanel rulesPanel = new JPanel();
		GridBagConstraints rulesConstraints = new GridBagConstraints();
		rulesConstraints.gridx = 0;
		rulesConstraints.gridy = 4;
		rulesConstraints.fill = GridBagConstraints.BOTH;
		rulesPanel.setLayout(new GridBagLayout());
		rulesPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JButton getRulesButton = new JButton("Input Rules");
			getRulesButton.addActionListener(new GetRulesListener());
			GridBagConstraints getRulesButtonConstraints = new GridBagConstraints();
				getRulesButtonConstraints.gridx = 0;
				getRulesButtonConstraints.gridy = 0;
				getRulesButtonConstraints.gridwidth = 2;
				getRulesButtonConstraints.gridheight = 2;
				getRulesButtonConstraints.weighty = 1;
				getRulesButtonConstraints.fill = GridBagConstraints.BOTH;
			rulesPanel.add(getRulesButton,getRulesButtonConstraints);
	
	// Clear panel
		JPanel clearPanel = new JPanel();
		GridBagConstraints clearConstraints = new GridBagConstraints();
		clearConstraints.gridx = 0;
		clearConstraints.gridy = 5;
		clearConstraints.weighty = 1;
		clearConstraints.fill = GridBagConstraints.BOTH;
		clearPanel.setLayout(new GridBagLayout());
		clearPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JButton clearAutomatonButton = new JButton("Clear playback history");
			clearAutomatonButton.addActionListener(new ClearPlaybackHistoryListener());
			GridBagConstraints clearButtonConstraints = new GridBagConstraints();
				clearButtonConstraints.gridx = 0;
				clearButtonConstraints.gridy = 0;
				clearButtonConstraints.gridwidth = 2;
				clearButtonConstraints.gridheight = 1;
				clearButtonConstraints.fill = GridBagConstraints.BOTH;
			clearPanel.add(clearAutomatonButton,clearButtonConstraints);
		
		JButton clearSelectedCellTypeButton = new JButton();
			clearSelectedCellTypeButton.addActionListener(new ClearSelectedCellTypeListener());
			GridBagConstraints clearSelectedCellTypeButtonConstraints = new GridBagConstraints();
				clearSelectedCellTypeButtonConstraints.gridx = 0;
				clearSelectedCellTypeButtonConstraints.gridy = 1;
				clearSelectedCellTypeButtonConstraints.gridwidth = 2;
				clearSelectedCellTypeButtonConstraints.gridheight = 1;
				clearSelectedCellTypeButtonConstraints.fill = GridBagConstraints.BOTH;
				new ClearCellTypeButtonUpdater(clearSelectedCellTypeButton);
			clearPanel.add(clearSelectedCellTypeButton,clearSelectedCellTypeButtonConstraints);
		
	// Board manipulate panel
		JPanel boardManipulatePanel = new JPanel();
		GridBagConstraints boardManipulateConstraints = new GridBagConstraints();
		boardManipulateConstraints.gridx = 0;
		boardManipulateConstraints.gridy = 6;
		boardManipulateConstraints.weighty = 1;
		boardManipulateConstraints.fill = GridBagConstraints.BOTH;
		boardManipulatePanel.setLayout(new GridBagLayout());
		boardManipulatePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JButton storeButton = new JButton("Store state");
				storeButton.addActionListener(new StoreListener());
				GridBagConstraints storeButtonConstraints = new GridBagConstraints();
					storeButtonConstraints.gridx = 0;
					storeButtonConstraints.gridy = 0;
					storeButtonConstraints.gridwidth = 1;
					storeButtonConstraints.gridheight = 1;
					storeButtonConstraints.fill = GridBagConstraints.BOTH;
				boardManipulatePanel.add(storeButton,storeButtonConstraints);
			
			JButton recallButton = new JButton("Recall state");
				recallButton.addActionListener(new RecallListener());
				GridBagConstraints recallButtonConstraints = new GridBagConstraints();
					recallButtonConstraints.gridx = 1;
					recallButtonConstraints.gridy = 0;
					recallButtonConstraints.gridwidth = 1;
					recallButtonConstraints.gridheight = 1;
					recallButtonConstraints.fill = GridBagConstraints.BOTH;
				boardManipulatePanel.add(recallButton,recallButtonConstraints);
			
			JButton randomizeButton = new JButton("Randomize");
				randomizeButton.addActionListener(new RandomizeListener());
				GridBagConstraints randomizeButtonConstraints = new GridBagConstraints();
					randomizeButtonConstraints.gridx = 0;
					randomizeButtonConstraints.gridy = 1;
					randomizeButtonConstraints.gridwidth = 1;
					randomizeButtonConstraints.gridheight = 1;
					randomizeButtonConstraints.fill = GridBagConstraints.BOTH;
				boardManipulatePanel.add(randomizeButton,randomizeButtonConstraints);
			
				randomizeSpinnerModel = new SpinnerNumberModel();
				randomizeSpinnerModel.setValue(50);
				randomizeSpinnerModel.setStepSize(1);
				randomizeSpinnerModel.setMinimum(0);
				randomizeSpinnerModel.setMaximum(100);
				JSpinner randomizeSpinner = new JSpinner(randomizeSpinnerModel);
				GridBagConstraints likelihoodComboBoxConstraints = new GridBagConstraints();
					likelihoodComboBoxConstraints.gridx = 1;
					likelihoodComboBoxConstraints.gridy = 1;
					likelihoodComboBoxConstraints.gridwidth = 1;
					likelihoodComboBoxConstraints.gridheight = 1;
					likelihoodComboBoxConstraints.fill = GridBagConstraints.BOTH;
				boardManipulatePanel.add(randomizeSpinner,likelihoodComboBoxConstraints);
		
	// Populate paint panel
		JPanel paintPanel = new JPanel();
		GridBagConstraints paintConstraints = new GridBagConstraints();
		paintConstraints.gridx = 0;
		paintConstraints.gridy = 7;
		paintConstraints.weighty = 1;
		paintConstraints.fill = GridBagConstraints.BOTH;
		paintPanel.setLayout(new GridLayout(0,1));
		paintPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JLabel selectATileLabel = new JLabel("Select a cell type to place:");
				paintPanel.add(selectATileLabel);
			
			ButtonGroup paintPanelRBGroup = new ButtonGroup();
			JRadioButton livingCellRB = new JRadioButton("Living cell");
				livingCellRB.addActionListener(new LivingCellRBListener());
				paintPanelRBGroup.add(livingCellRB);
				livingCellRB.setSelected(true);
			
			JRadioButton impassableCellRB = new JRadioButton("Impassable cell");
				impassableCellRB.addActionListener(new ImpassableCellRBListener());
				paintPanelRBGroup.add(impassableCellRB);
				
			paintPanel.add(livingCellRB);
			paintPanel.add(impassableCellRB);
		
		JPanel invisoPanel = new JPanel(); // Just used to take up space at the bottom of the control panel
		invisoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
	// Add sub-panels to the western panel
		westernPanel.add(transportPanel,transportConstraints);
		westernPanel.add(rulesPanel,rulesConstraints);
		westernPanel.add(clearPanel,clearConstraints);
		westernPanel.add(boardManipulatePanel,boardManipulateConstraints);
		westernPanel.add(paintPanel,paintConstraints);
		westernPanel.add(invisoPanel);
	
		mainFrame.add(westernPanel, BorderLayout.WEST);
		
	}
	void buildMenuArea() {
	
		/*
		 * Builds the menu area at the top of the mainFrame.
		 * It contains three menus: 
		 * 	
		 * 	- The File menu provides ways to save and load the
		 * 	state and rule set of the automaton.
		 * 	
		 * 	- The Board menu allows the user to manipulate the board
		 * 	in less common ways. (E.g. changing the number of cells on
		 * 	the board, displaying images behind the automaton)
		 * 
		 * 	- The Randomization menu controls randomization options such
		 * 	as whether to use the Java.lang.Math pseudo-random number generator
		 * 	or the truly random number service from random.org
		 * 
		 */
		
		// Create the menu bar and its menus
	
			menuBar = new JMenuBar();
			
			JMenu fileMenu = new JMenu("File");
			JMenu boardMenu = new JMenu("Board");
			JMenu randomizationMenu = new JMenu("Randomization");
			JMenu experimentalMenu = new JMenu("Experimental");
		
		// Populate the menu bar
		
			menuBar.add(fileMenu);
			menuBar.add(boardMenu);
			menuBar.add(randomizationMenu);
			if(!IS_PUBLIC_BUILD) { menuBar.add(experimentalMenu); }
		
		// Populate the File Menu
			JMenuItem aboutMenuItem = new JMenuItem("About GEM");
			aboutMenuItem.addActionListener(new ShowAboutListener());
			fileMenu.add(aboutMenuItem);
			
			JMenuItem saveAutomatonItem = new JMenuItem("Save automaton state to binary");
			saveAutomatonItem.addActionListener(new SaveAutomatonListener());
			fileMenu.add(saveAutomatonItem);
			
			JMenuItem loadAutomatonItem = new JMenuItem("Load automaton state from binary");
			loadAutomatonItem.addActionListener(new LoadAutomatonListener());
			fileMenu.add(loadAutomatonItem);
			
			JMenuItem writeCurrentStateToJsonItem = new JMenuItem("Write current state to JSON file");
			writeCurrentStateToJsonItem.addActionListener(new WriteCurrentStateToJsonFileListener());
			fileMenu.add(writeCurrentStateToJsonItem);
			
			JMenuItem readCurrentStateFromJsonItem = new JMenuItem("Read current state from JSON file");
			readCurrentStateFromJsonItem.addActionListener(new ReadCurrentStateFromJsonFileListener());
			fileMenu.add(readCurrentStateFromJsonItem);
			
		// Populate the Board menu
			
			JMenuItem userBoardSizeItem = new JMenuItem("Resize board");
			userBoardSizeItem.addActionListener(new ResizeBoardListener());
			boardMenu.add(userBoardSizeItem);
			
			JCheckBoxMenuItem smallWorldItem = new JCheckBoxMenuItem("Use small world neighbors");
			smallWorldItem.addItemListener(new SmallWorldCheckBoxItemListener());
			
			JCheckBoxMenuItem linkAgeAndOpacityMenuItem = new JCheckBoxMenuItem("Link age and opacity");
			linkAgeAndOpacityMenuItem.addItemListener(new LinkAgeAndOpacityListener());
			linkAgeAndOpacityMenuItem.setSelected(false);
			boardMenu.add(linkAgeAndOpacityMenuItem);
			
			/*
			 * MAP OPTIONS COMMENTED OUT
			 */
			/*
			JMenu mapDisplayOptionsMenu = new JMenu("Map display options:");
			
				ButtonGroup mapDisplayOptionsRBGroup = new ButtonGroup();
			
				stretchMapToFitBoardRB = new JRadioButton("Stretch map to fit board (may distort map)");
				stretchMapToFitBoardRB.addActionListener(new StretchMapToFitBoardListener());
				mapDisplayOptionsRBGroup.add(stretchMapToFitBoardRB);
				stretchMapToFitBoardRB.setSelected(true); // this radio button is selected by default
				
				scaleMapToFitBoardRB = new JRadioButton("Scale map to fill board (certain parts of the map may not show)");
				scaleMapToFitBoardRB.addActionListener(new ScaleMapToFillBoardListener());
				mapDisplayOptionsRBGroup.add(scaleMapToFitBoardRB);
				
				mapDisplayOptionsMenu.add(stretchMapToFitBoardRB);
				mapDisplayOptionsMenu.add(scaleMapToFitBoardRB);
			
			JMenuItem mapWizardItem = new JMenuItem("Load Map");
			mapWizardItem.addActionListener(new MapWizardListener());
			
			
			showMapMenuItem = new JCheckBoxMenuItem("Show Map");
				showMapMenuItem.addItemListener(shouldShowMapNotifier);
				showMapMenuItem.setSelected(false);
				*/
			
			boardMenu.add(smallWorldItem);
			//boardMenu.add(mapDisplayOptionsMenu);		
			//boardMenu.add(mapWizardItem);
			//boardMenu.add(showMapMenuItem);
			
			
		// Populate the Randomization menu
			
			pseudoRandomModeRB = new JRadioButtonMenuItem("Pseudo-random mode");
			pseudoRandomModeRB.setSelected(true);
			
			trueRandomModeRB = new JRadioButtonMenuItem("True random mode (uses random.org)");
			
			ButtonGroup randomizationModeButtonGroup = new ButtonGroup();
			randomizationModeButtonGroup.add(trueRandomModeRB);
			randomizationModeButtonGroup.add(pseudoRandomModeRB);
			
			randomizationMenu.add(new JLabel("Randomization mode:"));
			randomizationMenu.add(trueRandomModeRB);
			randomizationMenu.add(pseudoRandomModeRB);
			
		// Populate the Experimental menu
			
			if(!IS_PUBLIC_BUILD) {
				JMenuItem regionTallyFileItem = new JMenuItem("Decide Where to Log Moran's I");
				regionTallyFileItem.addActionListener(new RegionsFileListener());
				
				JMenuItem metadataItem = new JMenuItem("Edit metadata");
				metadataItem.addActionListener(new MetadataListener());
				
				JMenuItem geocodeItem = new JMenuItem("Geocode");
				geocodeItem.addActionListener(new GeocodeListener());
				
				JMenuItem tallyRegions = new JMenuItem("Tally Regions");
				tallyRegions.addActionListener(new TallyRegionsListener());
					
				JMenuItem calculateSpatialAutoCorrelationItem = new JCheckBoxMenuItem("Log spatial autocorrelation (Moran's I)");
					calculateSpatialAutoCorrelationItem.addItemListener(new CalculateSAItemListener());
					calculateSpatialAutoCorrelationItem.setSelected(false);
					
				experimentalMenu.add(regionTallyFileItem);
				experimentalMenu.add(tallyRegions);
				experimentalMenu.add(calculateSpatialAutoCorrelationItem);
				experimentalMenu.add(geocodeItem);
				experimentalMenu.add(metadataItem);
			}
		
		// Add the menu bar to the mainFrame
		
			mainFrame.setJMenuBar(menuBar);
			
	}
	void buildSouthernArea() {
		
		/*
		 * The southern area is (as you might expect) located in the southern part of the main frame.
		 * It provides details about the current state of the automaton (e.g.
		 * current rule set, current board dimensions) and also houses the
		 * zoom buttons.
		 * 
		 */
		
		// Create southPanel (contains infoPanel and zoomPanel)
			JPanel southPanel = new JPanel();
			southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.Y_AXIS));
			
		// Create and populate zoom panel
			JPanel zoomPanel = new JPanel();
			
			JButton zoomInButton = new JButton("Zoom In");
			JButton zoomOutButton = new JButton("Zoom Out");
			zoomToFitButton = new JButton("Zoom to Fit");
			
			zoomInButton.addActionListener(new ZoomInButtonListener());
			zoomOutButton.addActionListener(new ZoomOutButtonListener());
			zoomToFitButton.addActionListener(new ZoomToFitButtonListener());
			
			zoomPanel.add(zoomInButton);
			zoomPanel.add(zoomOutButton);
			zoomPanel.add(zoomToFitButton);
			
		// Create info panel
			infoPanel = new JPanel();
			
		// Create info panel labels
			JLabel generationNumberLabel = new JLabel();
			new GenerationNumberUpdater(generationNumberLabel);
			JLabel currentRulesLabel = new JLabel();
			new RulesLabelUpdater(currentRulesLabel);
			JLabel speedLabel = new JLabel();
			new PlaybackSpeedLabelUpdater(speedLabel);
			JLabel boardDimensionsLabel = new JLabel();
			new BoardDimensionsUpdater(boardDimensionsLabel);
		// Add labels to info panel
			infoPanel.add(generationNumberLabel);
			infoPanel.add(currentRulesLabel);
			infoPanel.add(speedLabel);
			infoPanel.add(boardDimensionsLabel);
			
		// Add panels to south panel
			southPanel.add(zoomPanel);
			southPanel.add(infoPanel);
			
		// Add southPanel to mainFrame
			mainFrame.add(southPanel,BorderLayout.SOUTH);
	}
	void buildDebugArea() {
	
		/*
		 * A separate JFrame (window) used to display debugging information.
		 * Accessed through the static methods of the Debug class.
		 * 
		 */
		
		// Builds the debug frame
	
		debugFrame = new JFrame("Debug Area");
	
		debugArea = new JTextArea();
		debugScrollPane = new JScrollPane(debugArea);
		debugScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		debugScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		debugFrame.getContentPane().add(debugScrollPane);

		debugFrame.setBounds(1020,0,250,800);
		debugFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		debugFrame.setVisible(true);
		
	}

	// Automatic display updaters
	class PlaybackSpeedLabelUpdater implements IPlaybackSpeedChangedListener {
		private JLabel playbackSpeedLabel;
		private static final String TEXT = "Delay between time steps (ms):";
		public PlaybackSpeedLabelUpdater(JLabel playbackSpeedLabel) {
			this.playbackSpeedLabel = playbackSpeedLabel;
			playbackSpeedLabel.setText(TEXT + automaton.getDelayBetweenIterations());
			automaton.addPlaybackSpeedChangedListener(this);
		}
		@Override
		public void playbackSpeedChanged(int newPlaybackSpeed) {
			playbackSpeedLabel.setText(TEXT + newPlaybackSpeed);			
		}
	}
	class StartButtonUpdater implements IAutomatonStartedListener, IAutomatonStoppedListener {
		JButton startButton;
		private static final String START_STRING = "Start";
		private static final String STOP_STRING = "Stop";
		public StartButtonUpdater(JButton startButton) {
			this.startButton = startButton;
			startButton.setText(START_STRING);
			automaton.addAutomatonStartedListener(this);
			automaton.addAutomatonStoppedListener(this);
		}
		@Override
		public void automatonStopped(IState finalBoardState) {
			startButton.setText(START_STRING);
		}
		@Override
		public void automatonStarted() {
			startButton.setText(STOP_STRING);
		}
	}
	class ClearCellTypeButtonUpdater implements IUserCellTypeSelectionChangedListener {
		JButton clearCellTypeButton;
		private static final String TEXT1 = "Clear all ";
		private static final String TEXT2 = " cells";
		
		public ClearCellTypeButtonUpdater(JButton clearCellTypeButton) {
			this.clearCellTypeButton = clearCellTypeButton;
			clearCellTypeButton.setText(TEXT1 + CellState.toAdjective(boardPanel.getUserCellTypeSelection()) + TEXT2);
			boardPanel.addUserCellTypeSelectionChangedListener(this);
		}
		@Override
		public void userCellTypeSelectionChanged(CellState newState) {
			clearCellTypeButton.setText(TEXT1 + CellState.toAdjective(newState) + TEXT2);
		}
	}
	class GenerationNumberUpdater implements IBoardStateChangedListener {
		private JLabel generationNumberLabel;
		private static final String TEXT = "Current generation: ";
		public GenerationNumberUpdater(JLabel generationNumberLabel) {
			this.generationNumberLabel = generationNumberLabel;
			generationNumberLabel.setText(TEXT + 0);
			automaton.getBoard().addBoardStateChangedListener(this);
		}
		@Override
		public void boardStateChanged(IState newCurrentState, int newGeneration) {
			generationNumberLabel.setText(TEXT + newGeneration);
		}
	}
	class RulesLabelUpdater implements IRulesChangedListener {
		private JLabel rulesLabel;
		public RulesLabelUpdater(JLabel rulesLabel) {
			this.rulesLabel = rulesLabel;
			rulesLabel.setText(automaton.getBoard().getRules().visualizeForUser());
			automaton.getBoard().addRulesChangedListener(this);
		}
		@Override
		public void rulesChanged(AbstractRuleSet rules) {
			rulesLabel.setText(rules.visualizeForUser());
		}
		
	}
	class BoardDimensionsUpdater implements IBoardSizeChangedListener {
		private JLabel boardDimensionsLabel;
		private static final String PREFIX = "Board dimensions: ";
		public BoardDimensionsUpdater(JLabel boardDimensionsLabel) {
			this.boardDimensionsLabel = boardDimensionsLabel;
			boardDimensionsLabel.setText(PREFIX + automaton.getBoard().getCurrentState().getDimensions().toString());
			automaton.getBoard().addBoardSizeChangedListener(this);
		}
		@Override
		public void boardSizeChanged(BoardDimensions newBoardDimensions) {
			boardDimensionsLabel.setText(PREFIX + newBoardDimensions.toString());
		}
	}
	
	// UI event notifiers
	public class ShouldShowMapNotifier implements ItemListener {
		private java.util.List<IShouldShowMapListener> shouldShowMapListeners = new ArrayList<IShouldShowMapListener>();
		
		public void addShouldShowMapListener(IShouldShowMapListener listener) {
			shouldShowMapListeners.add(listener);
		}
		public void removeShouldShowMapListener(IShouldShowMapListener listener) {
			shouldShowMapListeners.remove(listener);
		}
		
		private void notifyShouldShowMapListeners(boolean showMap) {
			for(IShouldShowMapListener listener : shouldShowMapListeners) {
				listener.shouldShowMapUpdated(showMap);
			}
		}
		
		public void itemStateChanged(ItemEvent ev) {
			if(ev.getStateChange() == ItemEvent.SELECTED
					&& boardPanel.map != null ) { // if the checkbox was filled by the user
				notifyShouldShowMapListeners(true);
			} else {
				notifyShouldShowMapListeners(false);
			}
		}
		
	}
	public class GoForwardNotifier implements ItemListener {
		private java.util.List<IGoForwardListener> goForwardListeners = new ArrayList<IGoForwardListener>();
		
		@Override
		public void itemStateChanged(ItemEvent ev) {
			JCheckBox goForwardBox = (JCheckBox)ev.getSource();
			notifyGoForwardListeners(goForwardBox.isSelected());
		}
		public void addGoForwardListener(IGoForwardListener listener) {
			goForwardListeners.add(listener);
		}
		public void removeGoForwardListener(IGoForwardListener listener) {
			goForwardListeners.remove(listener);
		}
		private void notifyGoForwardListeners(boolean goForward) {
			for(IGoForwardListener listener : goForwardListeners) {
				listener.setShouldGoForward(goForward);
			}
		}
	}
	
	// Button listeners
	class GetRulesListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.getBoard().updateRulesFromUserInput(mainFrame);
		}
	}
	class StartIteratingListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			if(automaton.isPlaying()) {
				automaton.stopSimulationIfRunning();
			} else {
				automaton.beginSimulationInSeparateThreadIfNotAlreadyRunning();
			}
		}
	}
	class IterateListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			AutomatonGlobal.automaton.tryToIterateAutomaton();
		}
	}
	class RandomizeListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			IRandomNumberSource randomNumberSource;
			if(trueRandomModeRB.isSelected()) {
				randomNumberSource = new RandomDotOrgRandomNumberSource();
			} else {
				randomNumberSource = new PseudoRandomNumberSource();
			}
			double threshold = getThreshold();
			CellState stateToRandomize = boardPanel.getUserCellTypeSelection();
			automaton.getBoard().randomizeBoard(randomNumberSource, threshold, stateToRandomize);
		}
		private double getThreshold() {
			int currentSpinnerValue = (Integer)randomizeSpinnerModel.getValue();
			return ((double)currentSpinnerValue)/100.0;
		}
	}
	class ClearPlaybackHistoryListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.stopSimulationIfRunning();
			AutomatonGlobal.automaton.clearBoard(); // then clear the automaton
		}
	}
	class ClearSelectedCellTypeListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			AutomatonGlobal.automaton.getBoard().clearCellTypeFromCurrentState(boardPanel.getUserCellTypeSelection());
		}
	}
	class FasterListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.incrementPlaybackSpeed();
		}
	}
	class SlowerListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.decrementPlaybackSpeed();
		}
	}
	class StoreListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			AutomatonGlobal.automaton.getBoard().copyStateToClipboard();
		}
	}
	class RecallListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.getBoard().pasteFromClipboard();
		}
	}
	class LivingCellRBListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.setUserCellTypeSelection(CellState.ALIVE);
		}
	}
	class ImpassableCellRBListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.setUserCellTypeSelection(CellState.IMPASSABLE);
		}
	}
	class ZoomInButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.zoomIn();
		}
	}
	class ZoomOutButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.zoomOut();
		}
	}
	class ZoomToFitButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.zoomToFit();
		}
	}
	
	// Menu item listeners
	class SaveAutomatonListener implements ActionListener {
	
		/*
		 * Writes the following objects (in this order) to a user-specified file:
		 * 
		 * 	- World array
		 * 	- Stored world array (from hitting the Store button)
		 * 	- Display array
		 * 	- Currently selected cell type (for drawing on board)
		 * 	- Playback list map
		 * 	- Input file (from virtual memory functionality)
		 * 	- Output file (from virtual memory functionality)
		 * 	- Generation number (as Integer object)
		 * 	- Playback location (as Integer object)
		 * 	- Birth conditions hash set
		 * 	- Life conditions hash set
		 * 	- Sleep time (as Integer object)
		 * 	- Map image
		 * 	- Show map on/off (as Boolean object)
		 * 	- Stretch/scale map setting 
		 * 	- Randomization mode
		 * 	- Randomization likelihood
		 * 
		 */
		
		public void actionPerformed(ActionEvent ev) {
		
			try {
		
				JFileChooser fileChooser = new JFileChooser();
				
				int userChose = fileChooser.showSaveDialog(mainFrame);
			
				if(userChose == JFileChooser.APPROVE_OPTION) {
				
					// Get the file selected by the user and set up the output stream
					File file = fileChooser.getSelectedFile();
					
					saveAutomaton(file);
					
				}
			
			} catch(Exception ex) {ex.printStackTrace();}
		
		}
	
	}
	class LoadAutomatonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try{
				JFileChooser fileChooser = new JFileChooser();
				int userChose = fileChooser.showOpenDialog(mainFrame);
				if(userChose == JFileChooser.APPROVE_OPTION) {
					// Get the file selected by the user and set up the input stream
					File file = fileChooser.getSelectedFile();
					loadAutomaton(file);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				showInvalidFileFormatDialog();
			}
		}
	}
	class ResizeBoardListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			automaton.getBoard().resizeBasedOnUserInput();
		}
	}
	class GeocodeListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			geography.buildWindow();
		}
	}
	class MetadataListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			metadata.editMetadata();
		}
	}
	class MapWizardListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			mapWizard.launch();
		}
	}
	class StretchMapToFitBoardListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.setMapDisplaySettings(MapDisplaySettings.STRETCH_MAP_TO_FIT_BOARD);
		}
	}
	class ScaleMapToFillBoardListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			boardPanel.setMapDisplaySettings(MapDisplaySettings.SCALE_MAP_TO_FILL_BOARD);
		}
	}
	class RegionsFileListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				/*				
				String regionSizeString = JOptionPane.showInputDialog(
						mainFrame,
						"Indicate below how many regions you would like the horizontal and " +
						"vertical space of the board to be divided into. \n\n" +
						"E.g., if you'd like four regions horizontally and five regions vertically," +
						"type: 4x5","Input Rule Dialog",JOptionPane.PLAIN_MESSAGE);
				
				int[] regionDimensions = parseRegionSizeString(regionSizeString);
				automaton.setRegionDimensions(regionDimensions[0],regionDimensions[1]);
				*/
				JFileChooser fileChooser = new JFileChooser();
				int userChose = fileChooser.showSaveDialog(mainFrame);
				if(userChose == JFileChooser.APPROVE_OPTION) {
					// Get the file selected by the user and set up the output stream
					File file = fileChooser.getSelectedFile();
					geography.spatialAutocorrelationLogFile = file;
				}
			} catch(Exception ex) {ex.printStackTrace();}
		}
		int[] parseRegionSizeString(String regionSizeString) {
			int[] regionDimensions = {-1,-1};
			try{
				String[] xAndY = regionSizeString.split("x");
				regionDimensions[0] = Integer.parseInt(xAndY[0]);
				regionDimensions[1] = Integer.parseInt(xAndY[1]);
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(mainFrame, "Invalid format. \n\nPlease enter something like 6x6.", "Region Dimensions Input Error", JOptionPane.ERROR_MESSAGE);
			}
			return regionDimensions;
		}
	}
	class TallyRegionsListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			geography.tallyRegions(tallyRegionsFile);
		}
	}
	class CalculateSAItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent ev) {
			if(ev.getStateChange() == ItemEvent.SELECTED) { // if the checkbox was filled by the user
				geography.calculateSpatialAutocorrelation = true;
				String cutOffString = JOptionPane.showInputDialog(mainFrame,"What is the cut-off distance? All cells beyond this distance will be given a weight of 0.","Cut-off distance dialog",JOptionPane.PLAIN_MESSAGE);
				double saDistance = Double.parseDouble(cutOffString);
				geography.saDistance = saDistance;
			} else { 
				geography.calculateSpatialAutocorrelation = false;
				try{
					geography.closeSpatialAutocorrelationLog();
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	class WriteCurrentStateToJsonFileListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				JFileChooser fileChooser = new JFileChooser();
				int userChose = fileChooser.showSaveDialog(mainFrame);
				if(userChose == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					JsonLogger.writeJsonToFile(file, new SimpleValidationBoardState(automaton.getBoard().getCurrentState()).toJson());
				}
		
			} catch(Exception ex) {ex.printStackTrace();}
		}
	}
	class ReadCurrentStateFromJsonFileListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				JFileChooser fileChooser = new JFileChooser();
				int userChose = fileChooser.showOpenDialog(mainFrame);
				if(userChose == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					automaton.getBoard().loadCurrentStateFromFile(file);
				}
		
			} catch(Exception ex) {ex.printStackTrace();}
			
		}
	}
	class SmallWorldCheckBoxItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent ev) {
			try {
				automaton.getBoard().setValueForConfigurationKey(GameConfigurationKey.SmallWorld, (ev.getStateChange() == ItemEvent.SELECTED));
			} catch(InvalidConfigurationException ex) {
				JOptionPane.showMessageDialog(mainFrame, "Current board cannot use small world neighbors.");
			}
		}
	}
	class LinkAgeAndOpacityListener implements ItemListener {
		public void itemStateChanged(ItemEvent ev) {
			try {
				boardPanel.setValueForConfigurationKey(BoardDisplayConfigurationKey.LinkAgeToAlpha, ev.getStateChange() == ItemEvent.SELECTED);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	class ShowAboutListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			JOptionPane.showMessageDialog(mainFrame, ABOUT_MESSAGE);
		}
	}
	
	// Save and load methods
	public void saveAutomaton(File file) {
		
		try{
		
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream output = new ObjectOutputStream(fileOut);
			
			automaton.save(output);
			boardPanel.save(output);
			geography.save(output);
			metadata.save(output);
			
			output.close();
		
		} catch(Exception ex) {
			
			ex.printStackTrace();
			showInvalidFileFormatDialog();
			
		}
	}
	public void loadAutomaton(File file) {
		
		try{
		
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream input = new ObjectInputStream(fileIn);
			
			automaton.load(input);
			boardPanel.load(input);
			geography.load(input);
			metadata.load(input);
			
			input.close();
		
		} catch(Exception ex) {
			ex.printStackTrace();
			showInvalidFileFormatDialog();
		}
		
	}
	public void showInvalidFileFormatDialog() {
		JOptionPane.showMessageDialog(mainFrame, 
				"File is in an invalid format.", "File Input Error", JOptionPane.ERROR_MESSAGE);
	}
}


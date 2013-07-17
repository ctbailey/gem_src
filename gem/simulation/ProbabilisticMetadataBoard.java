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

package gem.simulation;

public abstract class ProbabilisticMetadataBoard extends AbstractBoard {
/*
	public class ProbabilisticRuleSet extends AutomatonRuleSet implements Serializable {

		//TODO: Create a way to display the current rule set.
		
		boolean firstTime = true; //true until the user has applied a set of rules at least once.
		
		ArrayList<ProbabilisticRule> birthRules;
		ArrayList<ProbabilisticRule> birthRulesTemp;
		ArrayList<ProbabilisticRule> lifeRules;
		ArrayList<ProbabilisticRule> lifeRulesTemp;
		
		double birthMaxProbability = BIRTH_MAX_DEFAULT;
		double birthMinProbability = BIRTH_MIN_DEFAULT;
		static final double BIRTH_MAX_DEFAULT = 95.0;
		static final double BIRTH_MIN_DEFAULT = 5.0;
		
		double lifeMaxProbability = LIFE_MAX_DEFAULT;
		double lifeMinProbability = LIFE_MIN_DEFAULT;
		static final double LIFE_MAX_DEFAULT = 95.0;
		static final double LIFE_MIN_DEFAULT = 5.0;
		
		transient JFrame ruleInputFrame;
		transient JPanel birthPanel;
		transient JPanel lifePanel;
		transient HashMap<JButton,JPanel> panelMap;
		transient HashMap<JButton,ProbabilisticRule> deleteToRuleMap;
		transient HashMap<JComboBox,ProbabilisticRule> comparisonToRuleMap;
		transient HashMap<JComboBox,ProbabilisticRule> categoryToRuleMap;
		transient HashMap<JSpinner,ProbabilisticRule> likelihoodToRuleMap;
		
		static final int BIRTH_RULE = 1;
		static final int LIFE_RULE = 2;
		
		static final long serialVersionUID = 5;
		
		public ProbabilisticRuleSet() {
			
			birthRules = new ArrayList<ProbabilisticRule>();
			lifeRules = new ArrayList<ProbabilisticRule>();
			
		}
		
		public String toString() {
			
			StringBuffer s = new StringBuffer("Birth:");
			
			for(ProbabilisticRule birthRule : birthRules) {
				
				s.append("\n" + birthRule.toString());
				
			}
			
			s.append("\n\nLife:");
			
			for(ProbabilisticRule lifeRule : lifeRules) {
				
				s.append("\n" + lifeRule.toString());
				
			}
			
			return s.toString();
			
		}
		
		int applyRuleSet(int x, int y) {
				
			int value = Automaton.DEAD_CELL;
			double sumOfProbabilities = 0;
			
			if(automaton.worldArray[x][y] == Automaton.IMPASSABLE_CELL) { // If the cell is impassable
				
				return Automaton.IMPASSABLE_CELL;
				
			} else if(automaton.worldArray[x][y] == Automaton.LIVING_CELL) { // if the cell is alive
					
				for(ProbabilisticRule rule : lifeRules) {
					
					sumOfProbabilities += rule.findProbability(x, y);
					
				}
				
				if(sumOfProbabilities > lifeMaxProbability) {
					
					sumOfProbabilities = lifeMaxProbability;
					
				}
				
				if(sumOfProbabilities < lifeMinProbability) {
					
					sumOfProbabilities = lifeMinProbability;
					
				}
								
			} else if(automaton.worldArray[x][y] == Automaton.DEAD_CELL) { // if the cell is dead
				
				for(ProbabilisticRule rule : birthRules) {
					
					sumOfProbabilities += rule.findProbability(x, y);
					
				}
				
				if(sumOfProbabilities > birthMaxProbability) {
					
					sumOfProbabilities = birthMaxProbability;
					
				}
				
				if(sumOfProbabilities < birthMinProbability) {
					
					sumOfProbabilities = birthMinProbability;
					
				}
							
			}
			
			double randomNumber = 100*Math.random();

			if(randomNumber <= sumOfProbabilities) {
				
				value = Automaton.LIVING_CELL;
				
			}
			
			return value;
			
		}

		boolean shouldStop() {
			
			if(memory.currentPlaybackListHasBeenDumped()) {
				
				return true;
				
			} else {
				
				return false;
				
			}
			
		}
		
		public void buildInputRuleWindow() {
			
			ruleInputFrame = new JFrame("Input rules");
			
			birthRulesTemp = new ArrayList<ProbabilisticRule>();
			lifeRulesTemp = new ArrayList<ProbabilisticRule>();
			
			panelMap = new HashMap<JButton,JPanel>();
			deleteToRuleMap = new HashMap<JButton,ProbabilisticRule>();
			comparisonToRuleMap = new HashMap<JComboBox,ProbabilisticRule>();
			categoryToRuleMap = new HashMap<JComboBox,ProbabilisticRule>();
			likelihoodToRuleMap = new HashMap<JSpinner,ProbabilisticRule>();
			
			JPanel rulesPanel = new JPanel();
				rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
				
				JPanel birthContainerPanel = new JPanel();
					birthContainerPanel.setLayout(new BorderLayout());
					
					birthPanel = new JPanel();
						birthPanel.setLayout(new BoxLayout(birthPanel, BoxLayout.Y_AXIS));
						
						if(firstTime) {
							JPanel birthRulePanel = makeRulePanel(BIRTH_RULE);
							birthPanel.add(birthRulePanel);
						} else {
							for(ProbabilisticRule rule : birthRules) {
								
								JPanel panel = makeRulePanelFromRule(rule,BIRTH_RULE);
								birthPanel.add(panel);
								
							}
						}
						
					JPanel birthButtonPanel = new JPanel();
						birthButtonPanel.setLayout(new BoxLayout(birthButtonPanel,BoxLayout.Y_AXIS));
						
						JPanel addBirthRuleButtonPanel = new JPanel(new GridBagLayout());
							JButton addNewBirthRuleButton = new JButton("Add new birth rule");
							addNewBirthRuleButton.addActionListener(new AddNewBirthRuleListener());
							addBirthRuleButtonPanel.add(addNewBirthRuleButton);
							
						JPanel birthMinMaxPanel = new JPanel();
							JLabel birthMinLabel = new JLabel("Birth probability floor:");
								SpinnerNumberModel birthMinModel = new SpinnerNumberModel();
								birthMinModel.setValue(birthMinProbability);
								birthMinModel.setStepSize(1);
								JSpinner birthMinSpinner = new JSpinner(birthMinModel);
								birthMinSpinner.addChangeListener(new BirthMinListener());
								Dimension birthMinPreferredSize = birthMinSpinner.getPreferredSize();
								birthMinPreferredSize.width = 80;
								birthMinSpinner.setPreferredSize(birthMinPreferredSize);
							
							JLabel birthMaxLabel = new JLabel("Birth probability ceiling:");
								SpinnerNumberModel birthMaxModel = new SpinnerNumberModel();
								birthMaxModel.setValue(birthMaxProbability);
								birthMaxModel.setStepSize(1);
								JSpinner birthMaxSpinner = new JSpinner(birthMaxModel);
								birthMaxSpinner.addChangeListener(new BirthMaxListener());
								Dimension birthMaxPreferredSize = birthMaxSpinner.getPreferredSize();
								birthMaxPreferredSize.width = 80;
								birthMaxSpinner.setPreferredSize(birthMaxPreferredSize);
							
							birthMinMaxPanel.add(birthMinLabel);
							birthMinMaxPanel.add(birthMinSpinner);
							birthMinMaxPanel.add(birthMaxLabel);
							birthMinMaxPanel.add(birthMaxSpinner);
							
						birthButtonPanel.add(birthMinMaxPanel);
						birthButtonPanel.add(addBirthRuleButtonPanel);
						
					birthContainerPanel.add(birthPanel,BorderLayout.CENTER);	
					birthContainerPanel.add(birthButtonPanel,BorderLayout.SOUTH);
				
				JPanel lifeContainerPanel = new JPanel();
					lifeContainerPanel.setLayout(new BorderLayout());
					
					lifePanel = new JPanel();
						lifePanel.setLayout(new BoxLayout(lifePanel, BoxLayout.Y_AXIS));
						
						if(firstTime) {
							JPanel lifeRulePanel = makeRulePanel(LIFE_RULE);
							lifePanel.add(lifeRulePanel);
						} else {
							for(ProbabilisticRule rule : lifeRules) {
								
								JPanel panel = makeRulePanelFromRule(rule, LIFE_RULE);
								lifePanel.add(panel);
								
							}
						}
					
					JPanel lifeButtonPanel = new JPanel();
						lifeButtonPanel.setLayout(new BoxLayout(lifeButtonPanel,BoxLayout.Y_AXIS));
						
						JPanel addLifeRuleButtonPanel = new JPanel(new GridBagLayout());
							JButton addNewLifeRuleButton = new JButton("Add new life rule");
							addNewLifeRuleButton.addActionListener(new AddNewLifeRuleListener());
							addLifeRuleButtonPanel.add(addNewLifeRuleButton);
						
						JPanel lifeMinMaxPanel = new JPanel();
							JLabel lifeMinLabel = new JLabel("Life probability floor:");
								SpinnerNumberModel lifeMinModel = new SpinnerNumberModel();
								lifeMinModel.setValue(lifeMinProbability);
								lifeMinModel.setStepSize(1);
								JSpinner lifeMinSpinner = new JSpinner(lifeMinModel);
								lifeMinSpinner.addChangeListener(new LifeMinListener());
								Dimension lifeMinPreferredSize = lifeMinSpinner.getPreferredSize();
								lifeMinPreferredSize.width = 80;
								lifeMinSpinner.setPreferredSize(lifeMinPreferredSize);
							
							JLabel lifeMaxLabel = new JLabel("Life probability ceiling:");
								SpinnerNumberModel lifeMaxModel = new SpinnerNumberModel();
								lifeMaxModel.setValue(lifeMaxProbability);
								lifeMaxModel.setStepSize(1);
								JSpinner lifeMaxSpinner = new JSpinner(lifeMaxModel);
								lifeMaxSpinner.addChangeListener(new LifeMaxListener());
								Dimension lifeMaxPreferredSize = lifeMaxSpinner.getPreferredSize();
								lifeMaxPreferredSize.width = 80;
								lifeMaxSpinner.setPreferredSize(lifeMaxPreferredSize);
								
								lifeMinMaxPanel.add(lifeMinLabel);
								lifeMinMaxPanel.add(lifeMinSpinner);
								lifeMinMaxPanel.add(lifeMaxLabel);
								lifeMinMaxPanel.add(lifeMaxSpinner);
								
								
						lifeButtonPanel.add(lifeMinMaxPanel);
						lifeButtonPanel.add(addLifeRuleButtonPanel);
						
					lifeContainerPanel.add(lifePanel,BorderLayout.CENTER);
					lifeContainerPanel.add(lifeButtonPanel,BorderLayout.SOUTH);
						
				rulesPanel.add(birthContainerPanel);
				rulesPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
				rulesPanel.add(lifeContainerPanel);
				
			JPanel applyPanel = new JPanel();
				applyPanel.setLayout(new GridBagLayout());
				
				JButton applyRulesButton = new JButton("Apply rules");
				applyRulesButton.addActionListener(new ApplyRulesListener());
				
				applyPanel.add(applyRulesButton);
				
			ruleInputFrame.add(rulesPanel,BorderLayout.CENTER);
			ruleInputFrame.add(applyPanel,BorderLayout.EAST);
			ruleInputFrame.setBounds(	userInterface.mainFrame.getWidth()/10, 
					userInterface.mainFrame.getHeight()/4, 
					1000,
					300);

			ruleInputFrame.setVisible(true);
			
		}
		
		JPanel makeRulePanel(int ruleType) {
			
			JPanel rulePanel = new JPanel();
			rulePanel.setLayout(new GridBagLayout());
			
			ProbabilisticRule rule = new ProbabilisticRule(0, 1.0, true);
				/*
				 *  0 = index of metadata category selected by default in the comparisonCB
				 *  1.0 = default value of IntCB
				 *  true corresponds to default setting of comparisonCB ("the same")
				 */
	/*
			
			if(ruleType == BIRTH_RULE) {
			
				birthRulesTemp.add(rule);
				
				JPanel sentencePanel = new JPanel();
					String[] comparisonOptions = {"the same", "a different"};
					
					JLabel oneLabel = new JLabel("If a neighbor has");
					JComboBox comparisonCB = new JComboBox(comparisonOptions);
						comparisonToRuleMap.put(comparisonCB, rule);
						comparisonCB.addActionListener(new ComparisonCBListener());
					JLabel twoLabel = new JLabel("value for");
					JComboBox metadataCB = new JComboBox(metadata.categoryIdentifiers);
						categoryToRuleMap.put(metadataCB, rule);
						metadataCB.addActionListener(new MetadataCBListener());
					JLabel threeLabel = new JLabel("add a");
					SpinnerNumberModel likelihoodSpinnerModel = new SpinnerNumberModel();
						likelihoodSpinnerModel.setValue(0.0);
						likelihoodSpinnerModel.setStepSize(0.1);
						JSpinner likelihoodSpinner = new JSpinner(likelihoodSpinnerModel);
						likelihoodToRuleMap.put(likelihoodSpinner, rule);
						likelihoodSpinner.addChangeListener(new LikelihoodListener());
						Dimension ruleSpinnerSize = likelihoodSpinner.getPreferredSize();
						ruleSpinnerSize.width = 80;
						likelihoodSpinner.setPreferredSize(ruleSpinnerSize);
					JLabel fourLabel = new JLabel("percent likelihood to turn on.");
									
					sentencePanel.add(oneLabel);
					sentencePanel.add(comparisonCB);
					sentencePanel.add(twoLabel);
					sentencePanel.add(metadataCB);
					sentencePanel.add(threeLabel);
					sentencePanel.add(likelihoodSpinner);
					sentencePanel.add(fourLabel);
					
				JPanel deletePanel = new JPanel();
					deletePanel.setLayout(new GridBagLayout());
					JButton deleteButton = new JButton("Delete");
					deleteButton.addActionListener(new DeleteButtonListener());
					deletePanel.add(deleteButton);
					panelMap.put(deleteButton,rulePanel);
					deleteToRuleMap.put(deleteButton, rule);
					
				rulePanel.add(sentencePanel);
				rulePanel.add(deletePanel);
				
			} else {
				
				lifeRulesTemp.add(rule);
				
				JPanel sentencePanel = new JPanel();
				String[] comparisonOptions = {"the same", "a different"};
				
				JLabel oneLabel = new JLabel("If a neighbor has");
				JComboBox comparisonCB = new JComboBox(comparisonOptions);
					comparisonToRuleMap.put(comparisonCB, rule);
					comparisonCB.addActionListener(new ComparisonCBListener());
				JLabel twoLabel = new JLabel("value for");
				JComboBox metadataCB = new JComboBox(metadata.categoryIdentifiers);
					categoryToRuleMap.put(metadataCB, rule);
					metadataCB.addActionListener(new MetadataCBListener());
				JLabel threeLabel = new JLabel("add a");
				SpinnerNumberModel likelihoodSpinnerModel = new SpinnerNumberModel();
					likelihoodSpinnerModel.setValue(0.0);
					likelihoodSpinnerModel.setStepSize(0.1);
					JSpinner likelihoodSpinner = new JSpinner(likelihoodSpinnerModel);
					likelihoodToRuleMap.put(likelihoodSpinner, rule);
					likelihoodSpinner.addChangeListener(new LikelihoodListener());
					Dimension ruleSpinnerSize = likelihoodSpinner.getPreferredSize();
					ruleSpinnerSize.width = 80;
					likelihoodSpinner.setPreferredSize(ruleSpinnerSize);
				JLabel fourLabel = new JLabel("percent likelihood to remain on.");
				
				sentencePanel.add(oneLabel);
				sentencePanel.add(comparisonCB);
				sentencePanel.add(twoLabel);
				sentencePanel.add(metadataCB);
				sentencePanel.add(threeLabel);
				sentencePanel.add(likelihoodSpinner);
				sentencePanel.add(fourLabel);
				
			JPanel deletePanel = new JPanel();
				deletePanel.setLayout(new GridBagLayout());
				JButton deleteButton = new JButton("Delete");
				deleteButton.addActionListener(new DeleteButtonListener());
				deletePanel.add(deleteButton);
				panelMap.put(deleteButton,rulePanel);
				deleteToRuleMap.put(deleteButton, rule);
				
			rulePanel.add(sentencePanel);
			rulePanel.add(deletePanel);
				
			}
					
			return rulePanel;
			
		}
		
		JPanel makeRulePanelFromRule(ProbabilisticRule rule, int ruleType) {
			
			JPanel rulePanel = new JPanel();
			rulePanel.setLayout(new GridBagLayout());
			
			if(ruleType == BIRTH_RULE) {
			
				birthRulesTemp.add(rule);
				
				JPanel sentencePanel = new JPanel();
					String[] comparisonOptions = {"the same", "a different"};
					
					JLabel oneLabel = new JLabel("If a neighbor has");
					JComboBox comparisonCB = new JComboBox(comparisonOptions);
						comparisonToRuleMap.put(comparisonCB, rule);
						comparisonCB.addActionListener(new ComparisonCBListener());
						if(rule.areSame) {
							comparisonCB.setSelectedItem("the same");
						} else {
							comparisonCB.setSelectedItem("a different");
						}
						
					JLabel twoLabel = new JLabel("value for");
					JComboBox metadataCB = new JComboBox(metadata.categoryIdentifiers);
						categoryToRuleMap.put(metadataCB, rule);
						metadataCB.addActionListener(new MetadataCBListener());
						metadataCB.setSelectedIndex(rule.category);
					JLabel threeLabel = new JLabel("add a");
					SpinnerNumberModel likelihoodSpinnerModel = new SpinnerNumberModel();
						likelihoodSpinnerModel.setValue(rule.weight);
						likelihoodSpinnerModel.setStepSize(0.1);
						JSpinner likelihoodSpinner = new JSpinner(likelihoodSpinnerModel);
						likelihoodToRuleMap.put(likelihoodSpinner, rule);
						likelihoodSpinner.addChangeListener(new LikelihoodListener());
						Dimension ruleSpinnerSize = likelihoodSpinner.getPreferredSize();
						ruleSpinnerSize.width = 80;
						likelihoodSpinner.setPreferredSize(ruleSpinnerSize);
					JLabel fourLabel = new JLabel("percent likelihood to turn on.");
									
					sentencePanel.add(oneLabel);
					sentencePanel.add(comparisonCB);
					sentencePanel.add(twoLabel);
					sentencePanel.add(metadataCB);
					sentencePanel.add(threeLabel);
					sentencePanel.add(likelihoodSpinner);
					sentencePanel.add(fourLabel);
					
				JPanel deletePanel = new JPanel();
					deletePanel.setLayout(new GridBagLayout());
					JButton deleteButton = new JButton("Delete");
					deleteButton.addActionListener(new DeleteButtonListener());
					deletePanel.add(deleteButton);
					panelMap.put(deleteButton,rulePanel);
					deleteToRuleMap.put(deleteButton, rule);
					
				rulePanel.add(sentencePanel);
				rulePanel.add(deletePanel);
				
			} else {
				
				lifeRulesTemp.add(rule);
				
				JPanel sentencePanel = new JPanel();
					String[] comparisonOptions = {"the same", "a different"};
					
					JLabel oneLabel = new JLabel("If a neighbor has");
					JComboBox comparisonCB = new JComboBox(comparisonOptions);
						comparisonToRuleMap.put(comparisonCB, rule);
						comparisonCB.addActionListener(new ComparisonCBListener());
						if(rule.areSame) {
							comparisonCB.setSelectedItem("the same");
						} else {
							comparisonCB.setSelectedItem("a different");
						}
						
					JLabel twoLabel = new JLabel("value for");
					JComboBox metadataCB = new JComboBox(metadata.categoryIdentifiers);
						categoryToRuleMap.put(metadataCB, rule);
						metadataCB.addActionListener(new MetadataCBListener());
						metadataCB.setSelectedIndex(rule.category);
					JLabel threeLabel = new JLabel("add a");
					SpinnerNumberModel likelihoodSpinnerModel = new SpinnerNumberModel();
						likelihoodSpinnerModel.setValue(rule.weight);
						likelihoodSpinnerModel.setStepSize(0.1);
						JSpinner likelihoodSpinner = new JSpinner(likelihoodSpinnerModel);
						likelihoodToRuleMap.put(likelihoodSpinner, rule);
						likelihoodSpinner.addChangeListener(new LikelihoodListener());
						Dimension ruleSpinnerSize = likelihoodSpinner.getPreferredSize();
						ruleSpinnerSize.width = 80;
						likelihoodSpinner.setPreferredSize(ruleSpinnerSize);
					JLabel fourLabel = new JLabel("percent likelihood to remain on.");
									
					sentencePanel.add(oneLabel);
					sentencePanel.add(comparisonCB);
					sentencePanel.add(twoLabel);
					sentencePanel.add(metadataCB);
					sentencePanel.add(threeLabel);
					sentencePanel.add(likelihoodSpinner);
					sentencePanel.add(fourLabel);
					
				JPanel deletePanel = new JPanel();
					deletePanel.setLayout(new GridBagLayout());
					JButton deleteButton = new JButton("Delete");
					deleteButton.addActionListener(new DeleteButtonListener());
					deletePanel.add(deleteButton);
					panelMap.put(deleteButton,rulePanel);
					deleteToRuleMap.put(deleteButton, rule);
					
				rulePanel.add(sentencePanel);
				rulePanel.add(deletePanel);
				
			}
			
			return rulePanel;
			
		}
		
		double getProcessedSpinnerValue(JSpinner spinner) {
			
			SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
			
			if(model.getValue() instanceof Integer) {
				
				Integer value = (Integer) model.getValue();
				return value;
				
			} else {
				
				Double value = (Double) model.getValue();
				return value;
				
			}
		}
		
		class ProbabilisticRule implements Serializable {
			
			int category;
			/*
			 * Each rule is tied to a particular category of metadata.
			 * (E.g., the rule might begin, "If two cells have the same age...",
			 * age being the metadata category.)
			 * 
			 * This field indicates where that category is stored in
			 * metadata.categoryIdentifiers and the metadata array.
			 */
	/*
		
			boolean areSame;
			/*
			 * Rules can be specified to apply a weight if two cells 
			 * are of the same category, or if two cells are of a different
			 * category. (E.g., the rule might go, "If two cells have
			 * different ages..." or "If two cells have the same age..."). 
			 * 
			 * This field is true if the rule applies to cells having 
			 * the same value for a given metadata
			 * category (e.g., "If two cells are the same race..."), and 
			 * false if the rule has to do with cells having different values
			 * for a given metadata category (e.g. "If two cells are different races...").
			 * 
			 * Used in this kind of test:
			 * 
			 * if(
			 * 		(metadata[2][3][category] == metadata[2][4][category]) == areSame
			 * 	){
			 * 
			 * 		// Do something
			 * 
			 *  }
			 *  
			 *  The above IF statement says basically this: succeed if the two metadata values are equal
			 *  and areSame is true; also succeed if the two metadata values are NOT equal, and areSame is false.
			 * 
			 */
		/*
			double weight;
					
			final static long serialVersionUID = 5;
			
			public ProbabilisticRule(int c, double w, boolean a) {
				
				category = c;
				weight = w;
				areSame = a;
				
			}
			
			public String toString() {
				
				String turnOrRemain;
				String comparison;
				
				if(birthRules.contains(this)) {
					turnOrRemain = "turn";
				} else {
					turnOrRemain = "remain";
				}
				
				if(areSame) {
					comparison = "same";
				} else {
					comparison = "not same";
				}
				
				String s = new String("If " + comparison + " " + metadata.categoryIdentifiers[category] + " " + weight + "% " + turnOrRemain + " on");	
				return s;
				
			}
			
			double findProbability(int x, int y) {
				
				boolean[] neighborValues = findNeighborValues(x,y);
				/*
				 * Returns an array of boolean values with one entry
				 * for each living neighbor. Each entry tells whether
				 * that neighbor has the same or a different value
				 * for this rule's metadata category.
				 * (E.g., Suppose this rule's category is "3" - gender
				 * perhaps - and the cell at (x,y) is female. If the cell
				 * has two living neighbors, one of whom is female and
				 * one of whom is male, the neighborValues array will contain
				 * the values: {true,false}. (The order of the values depends on 
				 * the relative locations of the neighbors.)
				 */
				/*
				double probability = 0;
				
				if(neighborValues.length > 0) { // if the cell has at least one living neighbor
					
					double normalizer = 8 / neighborValues.length; 
					/* 
					 * Weights neighbors more heavily if there are fewer
					 * than 8 of them. (I.e., if some of the cell's
					 * would-be neighbors are outside the automaton or
					 * impassable.)
					 * 
					 * E.g., - if the cell has 4 neighbors,
					 * normalizer = 8/4 = 2, so each cell
					 * contributes twice its normal weight.
					 * Similarly, if the cell has 2 neighbors,
					 * normalizer = 8/2 = 4, so each cell
					 * contributes four times its normal weight.
					 * If a cell has all eight neighbors,
					 * normalizer = 8/8 = 1, so the weight is
					 * applied normally.
					 * 
					 * This helps to avoid edge effects; without
					 * this, the borders of the automaton would
					 * tend to have lower probabilities of applying
					 * the rules due to fewer neighbors.
					 */
					/*
					for(boolean value : neighborValues) {
						
						if(value == areSame) {
							
							probability += weight * normalizer;
							
						}
						
					}
				}
				
				return probability;
				
			}
			
			boolean[] findNeighborValues(int x, int y) {
				
				ArrayList<int[]> neighborsXY = getNeighbors(x,y);
				
				boolean[] neighborValues = new boolean[neighborsXY.size()];
				
				int neighborX;
				int neighborY;
				
				for(int i = 0; i < neighborsXY.size(); i++) {
					
					neighborX = neighborsXY.get(i)[0];
					neighborY = neighborsXY.get(i)[1];
					
					if(metadata.metadataArray[x][y][category] == metadata.metadataArray[neighborX][neighborY][category]) {
						
						neighborValues[i] = true;
						
					} else {
						
						neighborValues[i] = false;
						
					}
					
				}
				
				return neighborValues;
				
			}
			
			ArrayList<int[]> getNeighbors(int x, int y) {
				
				/* 
				 * Returns an ArrayList<int[]> containing the locations of any non-impassable neighbors.
				 * (X-coordinate stored as the first entry in the int[], Y-coordinate as the second entry.)
				 * "Non-impassable neighbors" means all cells not outside the bounds of the automaton
				 * that don't have a value of Automaton.IMPASSABLE_CELL;
				 */
				/*
				ArrayList<int[]> neighbors = new ArrayList<int[]>();
				
				for(int i = 0; i < 8; i++) {
					
					int[] neighborXY = countClockWise(i,x,y);
					int value = getCellValue(neighborXY[0],neighborXY[1]);
					
					if(value > 0) { // if the cell was alive
						
						neighbors.add(neighborXY);
						
					}
					
				}
				
				return neighbors;
			}
			
			int getCellValue(int x,int y) {
				
				/* 
				 * Returns the cell value of the cell located at (x,y).
				 * If (x,y) is beyond the bounds of the automaton, returns -1.
				 * 
				 */
				/*
				int cellValue;
					
				if(	(x < 0 || y < 0)   
					|| (((x + 1) > automaton.worldArray.length) 
					|| ((y + 1) > automaton.worldArray[x].length))
					) { // if (x,y) is outside the automaton
					
					cellValue = -1;
					
				} else {
					
					cellValue = automaton.worldArray[x][y];
					
				}

				return cellValue;
			
			}
			
			int[] countClockWise(int i, int x, int y) {
				
				/* 
				 * Used when counting clockwise, starting from the top, around the 8 immediate neighbors of (x,y).
				 * Takes i, how far you are in counting (starting from 0), and the original coordinates of the cell. 
				 * Returns the coordinates of the i-th cell as a 2 element array.
				 * E.g., the second cell to be counted, located at (x+1,y+1), has an i value of 1. If the original
				 * coordinates were (3,4), this method would return {4,5}.
				 * 
				 */
				/*
				int[] neighborXY = new int[2];
				
				switch(i) {
					case 0: neighborXY[0] = x;
							neighborXY[1] = y + 1;
							break;
					case 1: neighborXY[0] = x + 1;
							neighborXY[1] = y + 1;
							break;
					case 2: neighborXY[0] = x + 1;
							neighborXY[1] = y;
							break;
					case 3: neighborXY[0] = x + 1;
							neighborXY[1] = y - 1;
							break;
					case 4: neighborXY[0] = x;
							neighborXY[1] = y - 1;
							break;
					case 5: neighborXY[0] = x - 1;
							neighborXY[1] = y - 1;
							break;
					case 6: neighborXY[0] = x - 1;
							neighborXY[1] = y;
							break;
					case 7: neighborXY[0] = x - 1;
							neighborXY[1] = y + 1;
							break;
				}
				
				return neighborXY;

			}
			
		}
		
		class DeleteButtonListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				JButton sourceButton = (JButton) ev.getSource();
				JPanel sourcePanel = panelMap.get(sourceButton);
				JPanel parent = (JPanel) sourcePanel.getParent();
				ProbabilisticRule rule = (ProbabilisticRule) deleteToRuleMap.get(sourceButton);
				
				if(birthRulesTemp.contains(rule)) {
					
					birthRulesTemp.remove(rule);
					
				} else if(lifeRulesTemp.contains(rule)) {
					
					lifeRulesTemp.remove(rule);
					
				}
				
				parent.remove(sourcePanel);
				
				parent.validate();
				parent.repaint();
				
			}
			
		}
		
		class AddNewBirthRuleListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				JPanel birthRulePanel = makeRulePanel(BIRTH_RULE);
				birthPanel.add(birthRulePanel);
				
				birthPanel.revalidate();
				birthPanel.repaint();
				
			}
			
		}
		
		class AddNewLifeRuleListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				JPanel lifeRulePanel = makeRulePanel(LIFE_RULE);
				lifePanel.add(lifeRulePanel);
				
				lifePanel.revalidate();
				lifePanel.repaint();
				
			}
			
		}
		
		class ComparisonCBListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				JComboBox source = (JComboBox) ev.getSource();
				ProbabilisticRule rule = comparisonToRuleMap.get(source);
				String selected = (String) source.getSelectedItem();
				
				if(selected.equals("the same")) {
					
					rule.areSame = true;
					
				} else { // if selected.equals("a different")
					
					rule.areSame = false;
					
				}
						
			}
		}
		
		class MetadataCBListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				JComboBox source = (JComboBox) ev.getSource();
				ProbabilisticRule rule = categoryToRuleMap.get(source);
				int selectedIndex = source.getSelectedIndex();
				
				rule.category = selectedIndex;
				
			}
			
		}
		
		class LikelihoodListener implements ChangeListener {
			
			public void stateChanged(ChangeEvent ev) {
				
				JSpinner source = (JSpinner) ev.getSource();
				ProbabilisticRule rule = likelihoodToRuleMap.get(source);
				
				double value = getProcessedSpinnerValue(source);
				
				if(value > 100) {
					
					value = 100;
					source.getModel().setValue(value);
					
				} else if(value < -100) {
					
					value = -100;
					source.getModel().setValue(value);
					
				}
				
				rule.weight = value;
							
			}
			
		}
		
		class LifeMinListener implements ChangeListener {
			
			public void stateChanged(ChangeEvent ev) {
				
				JSpinner source = (JSpinner) ev.getSource();
				double value = getProcessedSpinnerValue(source);
				
				if(value > 100) {
					
					value = 100;
					source.getModel().setValue(value);
					
				} else if(value < 0) {
					
					value = 0;
					source.getModel().setValue(value);
					
				}
				
				lifeMinProbability = value;
				
			}
			
		}
		
		class LifeMaxListener implements ChangeListener {
			
			public void stateChanged(ChangeEvent ev) {
				
				JSpinner source = (JSpinner) ev.getSource();
				double value = getProcessedSpinnerValue(source);
				
				if(value > 100) {
					
					value = 100;
					source.getModel().setValue(value);
					
				} else if(value < 0) {
					
					value = 0;
					source.getModel().setValue(value);
					
				}
				
				lifeMaxProbability = value;
				
			}
			
		}
		
		class BirthMinListener implements ChangeListener {
			
			public void stateChanged(ChangeEvent ev) {
				
				JSpinner source = (JSpinner) ev.getSource();
				double value = getProcessedSpinnerValue(source);
				
				if(value > 100) {
					
					value = 100;
					source.getModel().setValue(value);
					
				} else if(value < 0) {
					
					value = 0;
					source.getModel().setValue(value);
					
				}
				
				birthMinProbability = value;
				
			}
			
		}
		
		class BirthMaxListener implements ChangeListener {
			
			public void stateChanged(ChangeEvent ev) {
				
				JSpinner source = (JSpinner) ev.getSource();
				double value = getProcessedSpinnerValue(source);
				
				if(value > 100) {
					
					value = 100;
					source.getModel().setValue(value);
					
				} else if(value < 0) {
					
					value = 0;
					source.getModel().setValue(value);
					
				}
				
				birthMaxProbability = value;
				
			}
		}
		
		class ApplyRulesListener implements ActionListener {
			
			public void actionPerformed(ActionEvent ev) {
				
				if(firstTime) {
					firstTime = false;
				}
				
				birthRules = birthRulesTemp;
				lifeRules = lifeRulesTemp;
				
				panelMap.clear();
				deleteToRuleMap.clear();
				comparisonToRuleMap.clear();
				categoryToRuleMap.clear();
				likelihoodToRuleMap.clear();
				
				ruleInputFrame.dispose();
				
				userInterface.refreshCurrentRulesLabel();
				
			}
		}
		
	}
*/
}
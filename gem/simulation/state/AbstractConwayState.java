package gem.simulation.state;

import static gem.Global.userInterface;
import gem.Global;
import gem.simulation.Utility;
import gem.simulation.board.BoardDimensions;
import gem.simulation.randomization.IRandomNumberSource;
import gem.simulation.randomization.NoRandomNumbersRemainingException;
import gem.simulation.state.ICell.CellState;
import gem.simulation.state.neighbor_topology.INeighborGraph;
import gem.ui.UserDidNotConfirmException;

import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public abstract class AbstractConwayState extends AbstractState {
	private static final long serialVersionUID = 1L;
	private AbstractConwayCell[][] cells;
	public static final CellState DEFAULT_CELL_STATE = CellState.DEAD;
	private static final boolean RANDOMIZE_ONLY_SELECTED_CELLS = true;
	
	public AbstractConwayState(BoardDimensions dimensions) {
		this(dimensions, Global.topologyManager.createNeighborGraphWithCurrentTopology(dimensions));
	}
	public AbstractConwayState(BoardDimensions dimensions, INeighborGraph neighborGraph) {
		this(createDefaultCells(dimensions), neighborGraph);
	}
	public AbstractConwayState(AbstractConwayCell[][] cells, INeighborGraph neighborGraph) {
		super(neighborGraph);
		for(AbstractConwayCell[] column : cells) {
			for(AbstractConwayCell c : column) {
				if(c == null) {
					throw new NullPointerException("Tried to initialize a board state with at least one null entry in the cell array.");
				}
			}
		}
		this.cells = cells;
	}
	
	public BoardDimensions getDimensions() {
		return new BoardDimensions(getWidth(), getHeight());
	}
	public int getWidth() {
		return cells.length;
	}
	public int getHeight() {
		return cells[0].length;
	}
	
	@Override
	public ICell getCell(int x, int y) {
		return cells[x][y];
	}
	public ICell getRandomCell() {
		Point randomLocation = getRandomCellLocation();
		return cells[randomLocation.x][randomLocation.y];
	}
	public Point getRandomCellLocation() {
		return Utility.getRandomIndexPair(cells);
	}
	
	protected AbstractConwayCell[][] copyCells() {
		AbstractConwayCell[][] cellsCopy = new AbstractConwayCell[getWidth()][getHeight()];
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				cellsCopy[x][y] = cells[x][y].deepCopy();
			}
		}
		return cellsCopy;
	}
	
	public int getNumberOfCells() {
		return getWidth()*getHeight();
	}
	public int getNumberOfCellsOfType(CellState kindOfCell) {
		int total = 0;
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cells[x][y].getState() == kindOfCell) { total++; }
			}
		}
		return total;
	}
	public int getNumberOfNeighborsInState(int x, int y, CellState targetState) {
		ICell[] neighbors = getCellsThatInfluenceCellAt(x, y);
		int neighborsInState = 0;
		for(ICell neighbor : neighbors) {
			if(neighbor.getState() == targetState) {
				neighborsInState++;
			}
		}
		return neighborsInState;
	}
	public int calculateCellTotalExcept(CellState stateToIgnore) {
		int total = 0;
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cells[x][y].getState() != stateToIgnore) { total++; }
			}
		}
		return total;
	}
	
	public ConwaySerializedState serialize() {
		return new ConwaySerializedState(this, neighborGraph);
	}
	public boolean equals(ConwayState otherState) {
		if(!otherState.getDimensions().equals(this.getDimensions())) {
			return false;
		}
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				if(cells[x][y].getState() != otherState.getCell(x, y).getState()) { return false; }
			}
		}
		return true;
	}
	protected static AbstractConwayCell[][] createDefaultCells(BoardDimensions dimensions) {
		AbstractConwayCell[][] cells = new AbstractConwayCell[dimensions.getWidth()][dimensions.getHeight()];
		for(int x = 0; x < dimensions.getWidth(); x++) {
			for(int y = 0; y < dimensions.getHeight(); y++) {
				cells[x][y] = ConwayCell.createDefaultCell(); // Puts the cells in their default state
			}
		}
		return cells;
	}
	
	public BoardDimensions getNewDimensionsFromUser() throws UserDidNotConfirmException {
		// Create user input panel
		SpinnerModel widthModel =
				new SpinnerNumberModel(	50, //initial value
										0, //min
										Integer.MAX_VALUE, //max
										1); //step
		SpinnerModel heightModel = new SpinnerNumberModel(50, 0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthModel);
		JSpinner heightSpinner = new JSpinner(heightModel);
		
		JPanel userInputPanel = new JPanel();
		userInputPanel.setLayout(new BoxLayout(userInputPanel,BoxLayout.X_AXIS));
		userInputPanel.add(new JLabel("Width: "));
		userInputPanel.add(widthSpinner);
		userInputPanel.add(new JLabel("Height: "));
		userInputPanel.add(heightSpinner);
		
		int userSelection = JOptionPane.showConfirmDialog(userInterface.mainFrame, userInputPanel, "New board dimensions", JOptionPane.OK_CANCEL_OPTION);
		if(userSelection == JOptionPane.OK_OPTION) {
			BoardDimensions newBoardDimensions = new BoardDimensions((Integer)widthSpinner.getModel().getValue(), (Integer)heightSpinner.getModel().getValue());
			return newBoardDimensions;
		} else {
			throw new UserDidNotConfirmException();
		}
	}
	
	protected Point getLocationOf(ICell c) {
		Point p = new Point(-1, -1);
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				ICell currentCell = getCell(x,y);
				if(currentCell == c) {
					p.x = x;
					p.y = y;
				}
			}
		}
		return p;
	}
	protected AbstractConwayCell[][] randomlyModifyCellState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		AbstractConwayCell[][] randomlyGeneratedCells = new AbstractConwayCell[getWidth()][getHeight()];
		
		for(int x = 0; x < getWidth(); x++) {
			for(int y = 0; y < getHeight(); y++) {
				ICell currentCell = getCell(x,y);
				CellState currentCellState = currentCell.getState();
				if((currentCellState == stateToRandomize
						|| currentCellState == CellState.DEAD)
					&& (currentCell.isSelected() 
						|| !RANDOMIZE_ONLY_SELECTED_CELLS)
					) {
					randomlyGeneratedCells[x][y] = (AbstractConwayCell) currentCell.getModifiedCopy((getRandomCellState(randomNumberSource, threshold, stateToRandomize)));
				} else {
					randomlyGeneratedCells[x][y] = (AbstractConwayCell) currentCell.deepCopy();
				}
			}
		}
		
		return randomlyGeneratedCells;
	}
	private CellState getRandomCellState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
			throws NoRandomNumbersRemainingException {
		if(randomNumberSource.getNextRandomNumber() < threshold) { return stateToRandomize; }
		else { return CellState.DEAD; }
	}
	
	// Methods that return a ConwayState (need to be overridden in subclasses)
	public abstract AbstractConwayState getNextIteration(AbstractConwayCell[][] newCells);
	public abstract AbstractConwayState getModifiedCopy(ICell newCell, int cellX, int cellY);
	public abstract AbstractConwayState getCopyWithClearedCellType(CellState toBeCleared);
	public abstract AbstractConwayState getCopyWithClearedSelection();
	public abstract AbstractConwayState createDefault(BoardDimensions dimensions);
	public abstract AbstractConwayState createDefault(BoardDimensions dimensions, INeighborGraph neighbor);
 	public abstract AbstractConwayState getCopyWithRandomizedState(IRandomNumberSource randomNumberSource, double threshold, CellState stateToRandomize) 
 		throws NoRandomNumbersRemainingException;
	public abstract AbstractConwayState getModifiedCopy(INeighborGraph g);
}

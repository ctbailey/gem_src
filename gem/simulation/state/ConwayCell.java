package gem.simulation.state;


public final class ConwayCell extends AbstractConwayCell {
	public ConwayCell(CellState state, boolean isSelected) {
		super(state, isSelected);
	}
	@Override
	public ICell getModifiedCopy(CellState newState) {
		return new ConwayCell(newState, super.isSelected());
	}

	@Override
	public AbstractConwayCell deepCopy() {
		return new ConwayCell(this.getState(), this.isSelected());
	}
	public static ConwayCell createDefaultCell() {
		return new ConwayCell(DEFAULT_STATE, false);
	}
	@Override
	public ICell getModifiedCopy(boolean isSelected) {
		return new ConwayCell(this.getState(), isSelected);
	}
	@Override
	public ICell getModifiedCopy(CellState newState, boolean isSelected) {
		return new ConwayCell(newState, isSelected);
	}
}

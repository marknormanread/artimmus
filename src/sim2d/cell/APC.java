package sim2d.cell;

import sim2d.TregSimulation;

public interface APC extends Cell
{
	/**
	 * Method called to induce the phagocytosis of the specified cell by this APC. 
	 * 
	 * This is the first point of call in the phagocytosis of a cell.
	 */
	public void phagocytoseCell(TregSimulation sim, Cell cell);
	
	/**
	 * Returns true when this APC is in a state of licensed. 
	 */
	public boolean getExpressing_CoStimulatory();
}

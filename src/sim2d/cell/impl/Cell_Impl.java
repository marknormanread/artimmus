package sim2d.cell.impl;


import java.util.Set;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.compartment.Compartment;
import sim2d.molecule.Molecule;

public abstract class Cell_Impl implements Cell 
{
	public Compartment compartment;			// the compartment where this cell resides.
	
		
	private Cell_Impl() {}					// a cell must have a compartment. No cell in the simulation can be instantiated without specifying a compartment. 
	/**
	 * Constructor places cell in a random location within the specified compartment. Intended to be used at setup of the simulation, not during its run. 
	 * @param placeCloseToFirstPick
	 */
	public Cell_Impl(Compartment location, boolean placeCloseToFirstPick)
	{
		if(location == null)
			throw new RuntimeException("null location!");
		
		scheduleCell();
				
		compartment = location;
		compartment.placeCellRandomlyInCompartmentCloseIfOccupied(this);
	}
	/**
	 * Constructor places the cell at the specified coordinates within the specified compartment. Intended to be used for the proliferation of cells.
	 */
	public Cell_Impl (TregSimulation sim, Compartment location, Cell parent)
	{
		scheduleCell();
		
		compartment = location;
		compartment.receiveDaugherCell(this, parent);
	}
	
	private void scheduleCell()
	{
		final TregSimulation sim = TregSimulation.sim;
		double time = sim.schedule.getTime() + sim.timeSlice;
		if(time < sim.schedule.EPOCH)
			time = sim.schedule.EPOCH;
		
		sim.addCellToSimulationScheduleRepeating(this, time);			// critical that we do this! Or the cell will not be stepped!
	}
	
	public void migrateIntoCompartment(Compartment newCompartment)
	{
		compartment = newCompartment;
	}
	
	/**
	 * At this high level abstract class we define which cells are chosen to interact with eachohter. Because this is at a high level the rules
	 * are the same for all cells.
	 * 
	 * Currently, a cell will attempt to interact with all its neighbours. 
	 * 
	 * The specifics of cell interactions are bespoke for each cell type. Thus, we have defined a protected abstract method which subtypes must implement
	 * to dictate the rules of which cells it can interact with.
	 */
	protected final void interactWithOtherCellsGeneric(TregSimulation simulation)
	{
		Cell[] neighbours = compartment.getNeighbours(this);
		if(neighbours.length <= 0)								// if there's nothing to interact with then we can return.
			return;					
		
		for(Cell otherCell : neighbours)						// iterate over neighbours, and perform cell specific interactions
		{
			interactWithOtherCell( simulation, otherCell );
		}
	}
	
	/**
	 * This method must be implemented in all concrete classes to indicate which cells types can interact with one another. 
	 * @param sim TODO
	 */
	protected abstract void interactWithOtherCell(TregSimulation sim, Cell cell);
	
	/**
	 * Method that performs apoptosis and phagocytosis of a cell, packaging up components and passing them to whatever cell is performing the phagocytosis. 
	 * 
	 * Note that in most cases the cells of the simulation simply disappear when they become apoptotic, rather than having to seek out an APC to be removed from the simulation. Where this is not the
	 * case this method should be overridden. 
	 */
	public Set<Molecule> bePhagocytosised(TregSimulation sim) 
	{		return null;	
	}
}

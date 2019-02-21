package sim2d.cell;


import java.util.Set;

import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.compartment.Compartment;
import sim2d.molecule.Molecule;

public interface Cell extends Steppable 
{
	/**
	 * Registers the cell as being resident within the new compartment. Updates the data local to this cell (as opposed to the compartment).
	 */
	public void migrateIntoCompartment(Compartment newCompartment);
	
	/**
	 * Returns true if this cell is apoptotic.
	 */
	public boolean isApoptotic();
	
	/**
	 * Called by an APC to phagocytose a cell. 
	 * @param sim 	The simulation object. Required to call a method that removes the cell from the simulation.
	 */
	public Set<Molecule> bePhagocytosised(TregSimulation sim);
	
	/**
	 * Due to the operation of the schedule, and that all cells are stepped and can interact with one another,
	 * this method is required to ensure that a cell that was phagocytosed in a particular 'step' does not get
	 * restepped.
	 */
	public abstract boolean isDead();
}

package sim2d.compartment;

import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.Steppable;
import sim2d.cell.Cell;
import sim2d.molecule.Molecule;

/**
 * As a convention, the flow of cells enters the top of a compartment and leaves out of the bottom. 
 *
 */
public abstract class Compartment implements Steppable
{
	public static boolean activatedTCellsFastTrackThroughSLOCompartments;
	
	/**
	 *  Indicates whether a cell of a certain type can enter this compartment. 
	 */
	public abstract boolean canEnter(Cell cell);
	
	/**
	 * Indicates whether the given cell is of a type that may leave this compartment.
	 */
	public abstract boolean canLeave(Cell cell);
	
	/**
	 * Method handles the receipt of cytokines into a specified location in the compartment from a cell. 
	 * 
	 * @param m				the type of the molecule that was secreted
	 * @param quantity		the quantity of the molecule that was secreted
	 * @param x				x coordinate of secreted molecules
	 * @param y				y coordinate of secreted molecules
	 */
	public abstract void receiveMolecules(Molecule m, double quantity, int x, int y);
	/**
	 * Same as above, but the location is deduced from the specified cell.
	 */
	public abstract void receiveSecretedMolecules(Molecule m, double quantity, Cell cell);
	
	/**
	 * The specified cell arrives in this compartment.
	 */
	public abstract void enterCompartment(Cell cell);
	
	/**
	 *  Places the specified cell into the specified compartment at a random location. 
	 */
	public abstract void placeCellRandomlyInCompartmentCloseIfOccupied(Cell cell);
	
	/**
	 * This method removes the cell from the compartment, regardless of its location. For example, when it is phagocytosed.
	 */
	public abstract void removeCellFollowingDeath(Cell cell);
	
	/**
	 * Places the daughter cell in the same location as the parent cell.
	 * @param daugher
	 * @param parent
	 */
	public abstract void receiveDaugherCell(Cell daughter, Cell parent);
	
	/**
	 * Called by a cell to retrieve any cells in neighbouring grid spaces.
	 */
	public abstract Cell[] getNeighbours(Cell cell);	
	
	/**
	 * Returns the quantity of the specified molecule at the location of the specified cell.
	 */
	public abstract double getConcentrationMolecule(Molecule m, Cell c);
	
	/**
	 * Used for visualisation stuff, for drawing graphs of the cell populations. 
	 */
	public abstract Collection<Cell> getAllCells();
	
	public abstract int totalCells();
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Compartment").item(0);
		
		activatedTCellsFastTrackThroughSLOCompartments = Boolean.parseBoolean(pE.getElementsByTagName("activatedTCellsFastTrackThroughSLOCompartments").item(0).getTextContent());
	}
}

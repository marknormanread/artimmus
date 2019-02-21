package sim2d.cell.impl;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.compartment.Compartment;
import sim2d.dataCollection.dataLoggers.NeuronsKilledDataLogger;
import sim2d.molecule.MBP;
import sim2d.molecule.Molecule;
import sim2d.molecule.SDA;

/**
 * The simulation's representation of a neuron.  
 * 
 * Neurons are randomly placed within the specified compartment (which should always be the CNS), and have an indefinite natural life span. They can be apoptosed through
 * the presence of a threshold level of SDA. Neurons are replaced upon apoptosis, with the new neuron being placed at the same location as the previous one. In vivo,
 * the neuron's need not die in order for MBP to be derived, this replacement of neurons is the simulation's representation of the MBP stripping process, but in a way that
 * there will be a constant supply of neurons (neurons are never a limiting factor in the mouse - the mouse may die, but it is assumed that this is not because all neurons
 * have been killed, rather that sufficient damage has been caused).
 * 
 * 
 * @author mark
 *
 */
public class Neuron extends Cell_Impl
{    
	private static double apoptosisSDAThreshold;					// how much SDA this CNS cell needs to perceive at any single point in time before it becomes apoptotic.
    
    private boolean isApoptotic = false;
	
	private boolean isDead = false;									// when the cell gets phagocytosed this is set to true to stop the 'step' method from re-stepping a cell that was phagocytosed by another in the same timeframe.
	
	public static NeuronsKilledDataLogger neuronsKilledDL;			// data logger that records how many neurons have been killed over time. Now part of the simulation's logic.
	
	/**
	 * Standard constructor to place a CNS cell in a compartment (should only be the CNS compartment). Used when initialising the simulation. The cell is placed randomly
	 * within the specified @location.  
	 * @param location
	 */
	public Neuron(Compartment location)
	{
		super(location, false);
	}
	/**
	 * Constructor used to replace a neuron that is phagocytosed. Rather than create a new neuron at a random location in the specified compartment ( @location ), 
	 * this constructor allows for a neuron to be placed at the location held by the specified cell.
	 *  
	 * @param location The compartment in which a new cell is to be placed. 
	 * @param placeAtThisCellsLocation The new neuron will be placed at the same location as that occupied by the specified cell. 
	 */
	public Neuron(Compartment location, Cell placeAtThisCellsLocation)
	{
		super(TregSimulation.sim, location, placeAtThisCellsLocation);
	}
	
	/**
	 * Method used to step the state of this neuron. 
	 */
	public void step(SimState state) 
	{
		if(isDead)														// do nothing if the cell was already phagocytosed earlier in this time frame.
			return;
	
		TregSimulation simulation = (TregSimulation)state;
		
		if(isApoptotic == false) {	// apoptotic neurons to not perceive cytokines, all that can happen is that they die, which they already are..  
			perceiveSDA(); 	
		}
		interactWithOtherCellsGeneric(simulation);						// handles interaction with other neighbouring cells.
	}

	/**
	 * Method perceives the level of SDA in the system. If this is above a threshold then the cell dies. 
	 * 
	 * Actually, CNS macrophages should be doing that intermittently anyway. 
	 */
	private void perceiveSDA()
	{
		// enter apoptosis if there is more SDA at the neuron's location than the threshold governing neuron death.
		if(apoptosisSDAThreshold < compartment.getConcentrationMolecule(SDA.instance, this)) {
			isApoptotic = true;
			neuronsKilledDL.logApoptosisEvent(this);							// log the fact that this neuron has just been killed. 
		}
	}
	
	/**
	 * Method handles cell-to-cell interactions between this CNS cell and other cells.
	 * 
	 * The only cell-to-cell interaction currently represented in the simulation is the phagocytosis of apoptotic neurons by APCs. 
	 */
	protected void interactWithOtherCell(TregSimulation sim, Cell cell) 
	{
		if(cell instanceof APC)
			((APC)cell).phagocytoseCell(sim, this);									// APC handles phagocytosis of this cell. It also checks if this CNS cell is apoptotic.
	}

	/**
	 * Handles the result of this neuron being phagocytosed. This method performs cleanup and tear down as the neuron is removed from the simulation and replaced by another. 
	 * Neurons contain MBP, and APCs are able to derive MBP peptides from phagocytosing them, also handled by this method. 
	 */
	public Set<Molecule> bePhagocytosised(TregSimulation sim) 
	{
		isDead = true;															// prevent further stepping of this cell after it is dead. 
		
		new Neuron(this.compartment, this);									// homeostatic replacement of dead cells with immature ones.
		
		sim.removeFromSimulationSchedule(this);									// critical, remove this cell from the simulation's schedule.
		compartment.removeCellFollowingDeath(this);								// remove cell from compartment. 
	
		Set<Molecule> contents = new HashSet<Molecule>();
		contents.add(MBP.instance);												// place MBP contents of the cell into a hashmap, and return to the APC.  

		return contents;
	}

	public boolean isApoptotic() 
	{
		return isApoptotic;
	}
	
	public boolean isDead() 
	{
		return isDead;
	}
	
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("CNSCell").item(0);
		
		apoptosisSDAThreshold = Double.parseDouble(pE.getElementsByTagName("apoptosisSDAThreshold").item(0).getTextContent());
	}
}

package sim2d.cell.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.cell.molecule.CD200;
import sim2d.cell.molecule.CD200R;
import sim2d.cell.molecule.MHC_I_CDR12;
import sim2d.compartment.Compartment;
import sim2d.dataCollection.dataLoggers.CD4Th1ApoptosisedDataLogger;
import sim2d.molecule.Type1;

/**
 * The simulation's representation of the CD8Treg cell. These cells are specific for Qa-1:CDR1/2 as expressed on (licensed) APCs, and on recently activated CD4Th1 cells. 
 * @author mark
 *
 */
public class CD8Treg extends TCell_Impl implements CD200
{
	/*
	 * General properties of all CD8Treg cells
	 */
	private static double type1SecretedPerHourWhenActivated;				// the quantity of type 1 cytokines secreted per hour by an activated CD4Th1 cell.
	private static double type1SecretedPerTimeslice;						// the quantity of type 1 cytokines secreted per simulation time slice by an activated CD4Th1 cell. This is dynamically calculated.
	
	private static double cd8TregToCD4ThelperSpecificityDropOff;			// 0.0 will remove all interaction between CD8Treg and Th1, 1.0 will have a normal interaction with no dropoff. 
	
	public static CD4Th1ApoptosisedDataLogger cd4Th1ApopDL;					// this is a data logger that collects data on where and when CD4Th1 cells are killed by CD8Treg cells.
																			// it is static, because we wish to know how many are being killed throughout the system, not how many are being killed by individual
																			// CD8Treg instances. Making this static provides an easy way for all CD8Treg instances to access the logger to record events. 
	private boolean expressingCD200 = false;								// does not express CD200 upon instantiation. 
	
	/**
	 * Constructor that places the cell in a random location within the specified compartment.
	 */
	public CD8Treg(Compartment location)
	{
		super(location);	
	}
	
	/**
	 * Constructor that places the cell within the specified compartment (location), at the same coordinates as the cell represented by parent. 
	 * This constructor is used to create new cells as a result of proliferation. The daughter cell will be placed in the same location as the parent assuming that the 
	 * location does not already contain the threshold maximum number of cell - see the Compartment classes for additional details. 
	 */
	public CD8Treg(TregSimulation sim, Compartment location, TCell_Impl parent)
	{
		super(sim, location, parent);
	}

	
	/**
	 * Method handles proliferation of this T cell.
	 */
	protected void spawnDaughterCell(TregSimulation sim)
	{
		new CD8Treg(sim, compartment, this);
	}
	
	/**
     * Called by Schedule to animate cells in the simulation. 
     */
	public void step(SimState state) 
	{
		super.step(state);										// make sure that all things in the super class are performed first. 
		
		if(getEffectorFunctionFromLocalActivation())			// local activation must take place before effector functions can be performed. 
			secreteCytokines();
	}
	
	/**
	 * This overridden method handles the specific cells that this T cell can interact with. 
	 */
	protected void interactWithOtherCell(TregSimulation sim, Cell otherCell)
	{
		if(otherCell instanceof APC)
			interactWithAPC( sim, (APC)otherCell );
		else if (otherCell instanceof CD4THelper)
			if( ((CD4THelper)otherCell).getPolarization() instanceof Th1Polarization )		// interaction is only with Th1 cells, not Th2 cells. 
				interactWithCD4Th1( (CD4THelper)otherCell );
		
		if(otherCell instanceof CD200R)
		{														// CD200 negative signalling, but only if this pathway has been activated in the simulation
			if( DendriticCell.cd200CytokineSwitching == true || DendriticCell.cd200GradualReductionPrimingCapacity == true)
				if( ((CD200R)otherCell).getExpressing_CD200R() && this.expressingCD200 )   // other cell must currently express CD200R, and this one must express CD200
					((CD200R)otherCell).receiveCD200RNegativeSignal();
		}
	}	
	
	/**
	 * What happens when an effector CD8Treg cell interacts with an APC. Since activated CD8Treg cells are interested in CD4THelper cells, rather than APCs, we don't perform any killing or anything here,
	 * just local activation in case is has not already occurred. 
	 */
	protected void interactWithAPCEffector(APC apc)
	{	
		effectorTCellStimulated(TregSimulation.sim);			// perform some housekeeping, since we are performing effector function here. 
	}
	
	/**
	 * Method handles the interaction between this CD8Treg and a CD4Th1 cell. Note that the CD4THelper has already been identified as being of a type 1 polarization at this point. 
	 */
	public void interactWithCD4Th1(CD4THelper cd4th1)
	{
		if(maturity != Maturity.Effector)						// only activated CD8Treg cells can apoptosise CD4Th1 cells
			return;
		
		if(cd4th1.isEffector() == false)						// only activated CD4Th1 cells can be apoptosised
			return;
						 
		if(attemptToInstigateSpecificityBasedBinding(cd4th1)) 	// probabilistic, specificity-determined attempt to form a binding with Qa-1 expressed on the CD4Th1 cell.
		{
			effectorTCellStimulated(TregSimulation.sim);		// perform some housekeeping, since we are performing effector function here. 
							
			cd4th1.beApoptosised();								// apoptosise the CD4Th1 cell
			cd4Th1ApopDL.logApoptosisEvent(this);				// record that this Th1 has been killed (this is data logging, not logic of the simulation)
		}
	}
	
	/**
	 * Overridden method. Upregulates CD200 expression upon maturation. 
	 */
	protected void becomeEffector(TregSimulation sim)
	{
		expressingCD200 = true;
		super.becomeEffector(sim);								// must still do all the things that a T cell normally would upon maturation.
	}

	/**
	 * Method handles the secretion of cytokines by this cell into the compartment that this cell occupies.
	 */
	private void secreteCytokines()
	{
		if(maturity == Maturity.Effector)						// if this cell is an effector cell.
		{	
			compartment.receiveSecretedMolecules(Type1.instance, type1SecretedPerTimeslice, this);	// then secrete type1 cytokine.
		}
	}

	public boolean isDead() {
		return isDead;
	}
	
	/**
	 * Method is called when this T cell collides with another cell, and attempts to instigate a binding with it. Binding instigation is specificity dependent, and that is what this method provides.
	 * 
	 * Overridden from TCell_Impl, because two cell types are bound by CD8Tregs, being CD4THelper cells and APCs. The common case implemented in TCell_Impl is for APCs. This overridden method
	 * permits investigation into the system-wide effects of altering the binding strength between a CD8Treg and a Th1. 
	 */
	protected boolean attemptToInstigateSpecificityBasedBinding(Cell cell)
	{			
		if( specificForThisCell(cell) )							// is cell expressing MHC-peptides for which this cell is specific?
		{
			double probabilityOfBinding = specificity;			// binding instigation with all cells is dependent on specificity
			
			if( cell instanceof CD4THelper ) 					// binding with a CD4Th1 can be further subject to a decreased number of adhesive molecules expressed on the T cell compared to the APC. 
				probabilityOfBinding *= cd8TregToCD4ThelperSpecificityDropOff;	// ... and that's what this represents. 

							// if CD200 negative signalling on DCs is active, then reduce probability of binding according to how suppressed DC is. 
			if( DendriticCell.cd200CytokineSwitching == true || DendriticCell.cd200GradualReductionPrimingCapacity == true)
				if(cell instanceof DendriticCell)
					probabilityOfBinding *= ((DendriticCell)cell).getCD200PrimingCapacity();
				
			
			return (TregSimulation.sim.random.nextDouble() <= probabilityOfBinding);	// probabilistically decide if the binding succeeds. 
				
		}
		
		return false;											// if any of the above fails, then the binding is not made.
	}
	
	/**
	 * This method will return whether the specified cell is expressing MHCpeptide for which this T cell is specific. 
	 * 
	 * THIS METHOD DOES NOT DECIDE WHETHER A BINDING WILL BE FORMED, THAT DEPENDS ON SPECIFICITY. 
	 */
	protected boolean specificForThisCell(Cell cell)
	{
		if(cell instanceof MHC_I_CDR12)
		{
			return ((MHC_I_CDR12)cell).getExpressing_MHC_I_CDR12();			// Qa-1 is not constitutionally expressed, this checks whether it is currently being expressed. 
		}
		return false;														// default behaviour if the specified cell does not express MHC-peptide for which this T cell is specific. 
	}
	

	/**
	 * Returns true when cell is expressing CD200 molecules.
	 */
	public boolean getExpressing_CD200() {
		return expressingCD200;
	}
	
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		
		Element pE = (Element) params.getElementsByTagName("CD8Treg").item(0);
		
		type1SecretedPerHourWhenActivated = Double.parseDouble(pE.getElementsByTagName("type1SecretedPerHourWhenActivated").item(0).getTextContent());
		type1SecretedPerTimeslice = type1SecretedPerHourWhenActivated * TregSimulation.timeSlice;
				
		cd8TregToCD4ThelperSpecificityDropOff = Double.parseDouble(pE.getElementsByTagName("cd8TregToCD4ThelperSpecificityDropOff").item(0).getTextContent());
	}

	
	/**
	 * A Steppable class that injects naive CD8Treg cells into the system at a basal rate, representing the generation of naive T cells by the thymus. 
	 * Despite variation due to stochasticity, the rate at which cells are inserted into the system is automatically determined based on other parameters, and results
	 * in a homeostatic level of cells in the system. 
	 */
	public static class NaiveCD8TregGenerator implements Steppable
	{
		// since naive cells will die without stimulation, this parameter defines the rate at which new cells are introduced into the system. It is automatically generated below
		// in the constructor. It represents how many cells should be introduced per time step. 
		public double probabilityNaiveTCellGenerated;	
		
		public NaiveCD8TregGenerator(double scheduledTimeSlice, int initialPopn)
		{
			/* cells are injected at roughly the same rate that they die of neglect (in absence of immunisation). For any one cell, the probability of death due to neglect
			 * is calculated as the mean time taken for death by neglect, divided by the time slice (the resolution in the temporal domain that the simulation runs at), 
			 * and then multiplied by the homeostatic number of cells that should be in the system (initialPopn). This gives the probability that a naive cell
			 * is to be placed into the simulation at each timestep. 
			 */
			probabilityNaiveTCellGenerated = (initialPopn / retrieveApoptosisNaiveMean() ) * scheduledTimeSlice;
		}
		
		/**
		 * Called by the Schedule. Handles the probabilistic insertion of naive CD8Treg cells into the simulation, representing the influx from the thymus. 
		 */
		public void step(SimState state)
		{
			final TregSimulation sim = (TregSimulation) state;
			
			if(sim.random.nextDouble() <= probabilityNaiveTCellGenerated)
			{
				new CD8Treg(sim.circulation);
			}
		}
	}
	
}

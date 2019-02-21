package sim2d.cell.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.cell.molecule.MHC_II_Fr3;
import sim2d.compartment.Compartment;
import sim2d.molecule.Type1;

/**
 * The simulation's representation of the CD4Treg cell, and the functions that are specific to it outside of what is already handled by the TCell_Impl class. 
 * This includes the secretion of cytokines and the licensing of APCs. 
 * 
 * A subclass that handles the homeostatic maintenance of the naive CD4Treg population 
 * is also contained herein. 
 * @author mark
 *
 */
public class CD4Treg extends TCell_Impl
{
	
	/*
	 * General properties of all CD4Treg cells. The difference between type1 secreted per hour and per time slice is to allow different length of time slice (simulation
	 * steps) to be run without affecting the dynamics of how much is secreted in the longer term. 
	 */	
	private static double type1SecretedPerHourWhenActivated;				// the quantity of type 1 cytokines secreted per hour by an activated CD4Th1 cell.
	private static double type1SecretedPerTimeslice;						// the quantity of type 1 cytokines secreted per simulation time slice by an activated CD4Th1 cell.

	
	/**
	 * Constructor that places the cell in a random location within the specified compartment.
	 */
	public CD4Treg(Compartment location)
	{
		super(location);
	}
	
	/**
	 * Constructor that places the cell within the specified compartment (location), at the same coordinates as the cell represented by parent. 
	 * This constructor is used to create new cells as a result of proliferation. The daughter cell will be placed in the same location as the parent assuming that the 
	 * location does not already contain the threshold maximum number of cell - see the Compartment classes for additional details. 
	 */
	public CD4Treg(TregSimulation sim, Compartment location, TCell_Impl parent)
	{
		super(sim, location, parent);
	}
	
	/**
	 * Method handles proliferation of this T cell. Overridden method such that TCell_Impl can call it. 
	 */
	protected void spawnDaughterCell(TregSimulation sim)
	{
		new CD4Treg(sim, compartment, this);
	}
	
	/**
     * Called by Schedule to animate cells in the simulation. This method allows for CD4Treg specific functions to be carried out, the step method in TCell_Impl is called
     * to carry out functions common to all T cells. 
     */
	public void step(SimState state) 
	{
		super.step(state);											// this method is overridden, a lot of function common to all T cells is carried out in the superclass method. 
		
		if(getEffectorFunctionFromLocalActivation())				// cytokine secretion is considered an effector function, and the cell must be locally activated to perform it.  
			secreteCytokines();
	}
		
	/**
	 * This overridden method handles cell-to-cell interactions specific to this CD4Treg. Currently this is only interaction with an APC.  
	 */
	protected void interactWithOtherCell(TregSimulation sim, Cell otherCell)
	{
		if(otherCell instanceof APC)
			interactWithAPC( sim, (APC)otherCell );
	}
	
	/**
	 * Handles interaction between an effector CD4Treg and an APC.  
	 */
	protected void interactWithAPCEffector(APC apc)
	{	
		/* The check here is for a Dendritic Cell. To be true to the biology, this should be any APC. However, since the only other type of APC in the simulation is
		 * the CNS Macrophage, which (currently) resides exclusively in the CNS compartment, there is no need for licensing there. Only effector T cells can enter
		 * the CNS compartment, and they can derive local activation from any cell expressing MHC:peptide of corresponding specificity, regardless of the state
		 * of co-stim expression on that APC. Hence, for run-time efficiency, we only attempt to license DCs.
		 * 
		 *  Likewise, only DCs in the simulation express Qa-1. Regardless of what might be the case in the real domain, in the simulation CNSMacrophages never leave the CNS compartment, 
		 *  and since CD8Tregs (the only cell that is specific for Qa-1) never enters the CNS, there is no reason to make the simulation less efficient by licensing CNSMs for Qa-1.  
		 */
		if(apc instanceof DendriticCell)											// probabilistic binding with APC has already been accomplished at this point. 
		{
			((DendriticCell)apc).becomeLicensedForCoStim();							// license the DC.
			((DendriticCell)apc).becomeLicensedForQa1();							// this CD4Treg licenses the APC to express Qa-1.
		}
	}
	
	/**
	 * Method handles the secretion of cytokines by this cell into the compartment that this cell occupies.
	 */
	private void secreteCytokines()
	{		
		if(maturity == Maturity.Effector)							// if the CD4Treg is an effector cell... 
		{	
			compartment.receiveSecretedMolecules(Type1.instance, type1SecretedPerTimeslice, this);	// then secrete type1 cytokine.
		}
	}
	
	
	public boolean isDead() {
		return isDead;
	}
	

	/**
	 * This method will return whether the specified cell is expressing MHCpeptide for which this T cell is specific. 
	 * 
	 * THIS METHOD DOES NOT DICTATE WHETHER A BINDING WILL BE FORMED, THAT DEPENDS ON SPECIFICITY, AND A PROBABILISTIC CHECK IS PERFORMED ELSEWHERE. 
	 */
	protected boolean specificForThisCell(Cell cell)
	{
		if(cell instanceof MHC_II_Fr3)										// checks whether the specified cell can express the MHC:peptide complex for which the CD4Treg is specific.
		{
			return ((MHC_II_Fr3)cell).getExpressing_MHC_II_Fr3();		
		}
		return false;														// default behaviour if the specified cell does not express MHC-peptide for which this T cell is specific. 
	}
		
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("CD4Treg").item(0);

		type1SecretedPerHourWhenActivated = Double.parseDouble(pE.getElementsByTagName("type1SecretedPerHourWhenActivated").item(0).getTextContent());
		type1SecretedPerTimeslice = type1SecretedPerHourWhenActivated * TregSimulation.sim.timeSlice;
		
	}
	
	
	/**
	 * A Steppable class that injects naive CD4Treg cells into the system at a basal rate, representing the generation of naive T cells by the thymus. 
	 * Despite variation due to stochasticity, the rate at which cells are inserted into the system is automatically determined based on other parameters, and results
	 * in a homeostatic level of cells in the system. 
	 */
	public static class NaiveCD4TregGenerator implements Steppable
	{
		// since naive cells will die without stimulation, this parameter defines the rate at which new cells are introduced into the system. It is automatically generated below
		// in the constructor. It represents how many cells should be introduced per time step. 
		public double probabilityNaiveTCellGenerated;	
		
		public NaiveCD4TregGenerator(double scheduledTimeSlice, int initialPopn)
		{
			/* cells are injected at roughly the same rate that they die of neglect (in absence of immunisation). For any one cell, the probability of death due to neglect
			 * is calculated as the mean time taken for death by neglect, divided by the time slice (the resolution in the temporal domain that the simulation runs at), 
			 * and then multiplied by the homeostatic number of cells that should be in the system (initialPopn). This gives the probability that a naive cell
			 * is to be placed into the simulation at each timestep. 
			 */
			probabilityNaiveTCellGenerated = (initialPopn / retrieveApoptosisNaiveMean() ) * scheduledTimeSlice;
		}
		
		/**
		 * Called by the Schedule. Handles the probabilistic insertion of naive CD4Treg cells into the simulation, representing the influx from the thymus. 
		 */
		public void step(SimState state)
		{
			final TregSimulation sim = (TregSimulation) state;
			
			if(sim.random.nextDouble() <= probabilityNaiveTCellGenerated)
			{
				new CD4Treg(sim.circulation) ;
			}
		}
	}
}

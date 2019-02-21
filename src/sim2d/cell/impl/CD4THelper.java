package sim2d.cell.impl;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.cell.molecule.MHC_II_MBP;
import sim2d.cell.molecule.MHC_I_CDR12;
import sim2d.compartment.Compartment;
import sim2d.molecule.CDR12;
import sim2d.molecule.Fr3;
import sim2d.molecule.Molecule;
import sim2d.molecule.Type1;
import sim2d.molecule.Type2;

public class CD4THelper extends TCell_Impl implements MHC_I_CDR12
{
	/*
	 * read-in parameters that affect all CD4THelper cells.  
	 */ 		
	private static double diff00;			// the probability that a Th cell will adopt a type 1 polarisation if type1 cytokine represents 0 to 80 percent of the local cytokine mix. 
	private static double diff08;			// the probability that a Th cell will adopt a type 1 polarisation if type1 cytokine represents 80 or more percent of the local cytokine mix.
	
	private Polarization wasPolarized = null;
	
	/*
	 * parameters specific to individual CD4Th instantiations
	 */
	private Polarization polarization = null;	// starts off null, but is replaced when the Th cell adopts a polarization. 
	
	
	/**
	 * Constructor that places the cell in a random location within the specified compartment.
	 */
	public CD4THelper(Compartment location)
	{
		super(location);
	}
	
	/**
	 * Constructor that places the cell at the coordinates of the parent cell within the specified compartment. Used for spawing of daughter cells in proliferation. 
	 */
	public CD4THelper(TregSimulation sim, Compartment location, TCell_Impl parent)
	{
		super(sim, location, parent);
	}


	/**
	 * Method handles proliferation of this T cell.
	 */
	protected void spawnDaughterCell(TregSimulation sim)
	{
		new CD4THelper(sim, compartment, this); 
	}
	
	/**
     * Called by Schedule to animate cells in the simulation. 
     */
	public void step(SimState state) 
	{
		super.step(state);
		
		if(polarization != null)										// the following activities cannot happen unless the cell has a polarization (ie, it is an effector).
		{
			polarization.updateMoleculeExpression(TregSimulation.sim);
			if(getEffectorFunctionFromLocalActivation() == true)		// only secrete cytokines if Th cell has been locally activated. 
			{
				polarization.secreteCytokines();						// perform effector functions (cytokine secretion)
			}
		}
	}
	
	/**
	 * This overridden method handles the specific cells that this T cell can interact with.
	 * 
	 * The CD8Treg class handles its interactions with this cell. This cell handles interactions with APCs. 
	 */
	protected void interactWithOtherCell(TregSimulation sim, Cell otherCell)
	{
		if(otherCell instanceof APC)
			interactWithAPC( sim, (APC)otherCell );
	}	
	
	/**
	 * What happens when an effector CD4Th1 cell interacts with an APC. At this point it has already been established that the APC expressed MHC:peptide complexes for which this cell is specific. 
	 */
	protected void interactWithAPCEffector(APC apc)
	{															
		/* The check here is for a Dendritic Cell. To be true to the biology, this should be any APC. However, since the only other type of APC in the simulation is
		 * the CNS Macrophage, which (currently) resides exclusively in the CNS compartment, there is no need for licensing there. Only effector T cells can enter
		 * the CNS compartment, and they can derive local activation from a cell expressing MHC:peptide of corresponding specificity, regardless of the state
		 * of co-stim expression on that APC. Hence, for run-time efficiency, we only attempt to license DCs. 
		 */
		if(apc instanceof DendriticCell)
			((DendriticCell)apc).becomeLicensedForCoStim();			// license the DC. 
	}
	
	/**
	 * Overridden method from superclass. When a Th cell enters a proliferative state it must determine which polarisation to adopt.  
	 */
	protected void becomeProliferating(TregSimulation sim) 
	{
		super.becomeProliferating(sim);
		determinePolarization(sim);
	}
		
	/**
	 * Override the method from the superclass. Note that we still call the method on the superclass.
	 */
	protected void becomeEffector(TregSimulation sim)
	{
		super.becomeEffector(sim);										// housekeeping specific to all T cells.
		polarization.becomeEffector();									// allow polarization to do polarization specific things when cell becomes effector. 
	}
	
	/**
	 * Overridden from TCell_Impl to provide THelper cell specific cleanup when the cell enters apoptosis. 
	 */
	protected void becomeApoptotic(TregSimulation sim)
	{
		super.becomeApoptotic(sim);										// perform parent class operations.
		polarization = null;											// apoptotic THelper cells lose their polarity. 
	}
	
	/**
	 * This method is overridden from the TCell_Impl class. Most T cells are simply removed from the simulation when they become apoptotic, however Th1 cells
	 * contain peptides (Fr3 and CDR1/2) that are of interest to Treg cells within the system, hence THelper cells should not be removed from the simulation
	 * upon entering apoptosis, they will eventually be phagocytosed by APCs, permitting the priming of Treg cells. 
	 * 
	 * This overridden method disables automatic removal from the simulation. 
	 */
	protected void removeCellFromSimulation(TregSimulation sim)
	{	/* do not remove cell from simulation, since it is to be phagocytosed */ }

	/**
	 * Method is called when a CD4THelper enters 'proliferating' state; different polarisations of Th cells may have different proliferation rates. 
	 * The choice of whether to become a Th1 or a Th2 is dependent on the ratio of type 1 to type2 cytokines in the cell's grid space. 
	 * Different probabilities for differentiation into Th1 or Th2 exist, and are grouped into bounds that the ratio falls in to.
	 * Note that the decision regarding which polarization to adopt is heavily influenced by cytokine balances, but there is still a probabilistic 
	 * element to the decision.   
	 */
	private void determinePolarization(TregSimulation sim)
	{
		boolean chosenPolarization;										// the eventual polarization that this cell will adopt. True -> Th1.
		final boolean Th1 = true;
		final boolean Th2 = false;
		 
		final double type1 = compartment.getConcentrationMolecule(Type1.instance, this);	// retrieve cytokine concentrations from the neighbourhood. 
		final double type2 = compartment.getConcentrationMolecule(Type2.instance, this);
		
		double chanceOfTh1;												// will store the probability that the CD4T cell differentates in a Th1 direction.

		if (type1 + type2 == 0)											// no quantity of either cytokine, favour type2 polarization. 
			chanceOfTh1 = diff00;					
		else 															// there are cytokines, so calculate probability based on ratios. 
		{
			/* stepwise probabilities dependent on the ratios of type1 to type2 cytokines */
			double t1PropTotal = type1 / (type1 + type2);
			if(t1PropTotal >= 0.8) 	{	chanceOfTh1 = diff08;}
			else 					{	chanceOfTh1 = diff00;}
		}
		
		chosenPolarization = (sim.random.nextDouble() <= chanceOfTh1) ? Th1 : Th2; // chosen polarisation is determined here. 
			
		// perform the actual differentiation
		if(chosenPolarization == Th1) 
			this.polarization = new Th1Polarization(this); 
		 else
			this.polarization = new Th2Polarization(this);
		
		/* when Th cells become apoptotic they lose their polarization. However, only Th1 cells should be phagocytosed, so a record of which polarisation a cell had is maintained. 
		 * This is in place of leaving the polarization variable assigned because there are tests regarding a cell's polarization based on it - a record is required, but not in a
		 * manner that interferes with those tests.  
		 */
		wasPolarized = polarization;		
	}
	
	/**
	 * Returns 'true' when this cell is in an 'effector' state of maturity. 
	 */
	public boolean isEffector()
	{
		return (maturity == Maturity.Effector);
	}
	
	/**
	 * Called by a CD8Treg to induce apoptosis in this CD4Th1
	 */
	public void beApoptosised()
	{
		becomeApoptotic(TregSimulation.sim);
	}
		
	
	/**
	 *  Called on a cell by an APC when it phagocytoses a cell. This method is responsible for removing the cell from the compartment. 
	 */
	public Set<Molecule> bePhagocytosised(TregSimulation sim)
	{
		isDead = true;												// to prevent any further activity with the cell in the current simulation step. 
		sim.removeFromSimulationSchedule(this);						// critical, remove this cell from the simulation's schedule.
		compartment.removeCellFollowingDeath(this);					// cell removes itself from the compartment.
		
		Set<Molecule> contents = new HashSet<Molecule>();		
		
		/* There is not a completely clear distinction between various Th cell lineages in the simulation. This ensures that instances of this generic 'Th' class that were at any point
		 * polarized in a Th2 direction do not release Vb8.2 derived peptides to APCs (Vb8.2 cells tend to be type1 polarized. see
		 * 'Menezes, J., van den Elzen, P., Thornes, J., Huffman, D., Droin, N., Maverakis, E., Sercarz, E. - A Public T Cell Clonotype within a Heterogeneous Autoreactive Repertoire of Dominant in Driving EAE'
		 * for more details.)
		 */
		if((wasPolarized instanceof Th2Polarization) == false)		// 'null' will return false here, so naive cells will return these values. 
		{
			contents.add(Fr3.instance);
			contents.add(CDR12.instance);
		}
		return contents;											// pass peptides to the APC. 
	}	
	
	/**
	 * This method will return whether the specified cell is expressing MHCpeptide for which this T cell is specific. 
	 * 
	 * THIS METHOD DOES NOT DECIDE WHETHER A BINDING WILL BE FORMED, THAT DEPENDS ON SPECIFICITY. 
	 */
	protected boolean specificForThisCell(Cell cell)
	{
		if(cell instanceof MHC_II_MBP)										// cell is capable of expressing MHC-II:MBP. 
		{
			return ((MHC_II_MBP)cell).getExpressing_MHC_II_MBP();			// cell is expressing MHC-II:MBP at the current time. 
		}
		return false;														// default behaviour if the specified cell does not express MHC-peptide for which this T cell is specific. 
	}
	
	/**
	 * Method returns whether this Th cell is currently expression MHC-I:CDR1/2. Only Th1 cells are capable of doing this.  
	 */
	public boolean getExpressing_MHC_I_CDR12()
	{
		if(this.polarization instanceof Th1Polarization)					// if the cell is of a type1 polarization, then defer the decision to the polarization object.  
		{
			return ((MHC_I_CDR12)this.polarization).getExpressing_MHC_I_CDR12();
		}
		return false;														// default behaviour otherwise.
	}
	
	/**
	 * java getters and setters (for use with mason GUI).
	 * @return
	 */
	public Polarization getPolarization()
	{	return polarization;	}
	
	public boolean isDead() 
	{	return isDead;		}
	
	public boolean getExpressingQa1()				// for use with the MASON gui ONLY. 
	{
		if(this.maturity == Maturity.Effector)													// only effector Th1 cells express Qa1. Note that naive Th1 cells do not have Polarization objects. 
			if(this.getPolarization() instanceof Th1Polarization)
			{
				Th1Polarization polarization = (Th1Polarization) this.getPolarization();
				return polarization.getExpressing_MHC_I_CDR12();
			}
		return false;
	}
		
	public double getTimeStopExpressingQa1()		// for use with the MASON gui only!
	{
		if(this.maturity == Maturity.Effector)													// only effector cells express Qa1 (and only Th1s do that). This is also safety in case a naive cell is quieried, since it does not have a Polarization object. 
			if(this.getPolarization() instanceof Th1Polarization)
			{
				Th1Polarization polarization = (Th1Polarization) this.getPolarization();
				return polarization.timeOfMHC_I_CDR12UnExpression;
			}
		return Double.MAX_VALUE;																// default behaviour for all things other than effector Th1 cells. 
	}
	
	
	/**
	 * Used by TCell_Impl, allows concrete T cell classes to define their own parameters for the mean time it takes to spawn a daughter cell. 
	 */
	protected double retrieveProliferationMean()
	{	
		if(polarization instanceof Th2Polarization)		
			return ((Th2Polarization)polarization).retrieveProliferationMean();
		
		return super.retrieveProliferationMean();												// if there is no polarization, then return whatever is default in the superclass. 
	}
	
	/**
	 * Used by TCell_Impl, allows concrete T cell classes to define their own parameters for the standard deviation surrounding the mean for the time it takes to spawn a daughter cell. 
	 */
	protected double retrieveProliferationStdDev()
	{	
		if(polarization instanceof Th2Polarization)		
			return ((Th2Polarization)polarization).retrieveProliferationStdDev();
		
		return super.retrieveProliferationMean();												// if there is no polarization, then return whatever is default in the superclass. 
	}
	
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{ 
		Element pE = (Element) params.getElementsByTagName("CD4THelper").item(0);
				
		diff00 = Double.parseDouble(pE.getElementsByTagName("diff00").item(0).getTextContent());
		diff08 = Double.parseDouble(pE.getElementsByTagName("diff08").item(0).getTextContent());
	}
	
	
	/**
	 * A Steppable class that injects naive CD8Treg cells into the system at a basal rate, representing the generation of naive T cells by the thymus. 
	 * Despite variation due to stochasticity, the rate at which cells are inserted into the system is automatically determined based on other parameters, and results
	 * in a homeostatic level of cells in the system. 
	 */
	public static class NaiveCD4THelperGenerator implements Steppable
	{
		// since naive cells will die without stimulation, this parameter defines the rate at which new cells are introduced into the system. It is automatically generated below
		// in the constructor. It represents how many cells should be introduced per time step. 
		public double probabilityNaiveTCellGenerated;
		
		public NaiveCD4THelperGenerator(double scheduledTimeSlice, int initialPopn)
		{
			/* cells are injected at roughly the same rate that they die of neglect (in absence of immunisation). For any one cell, the probability of death due to neglect
			 * is calculated as the mean time taken for death by neglect, divided by the time slice (the resolution in the temporal domain that the simulation runs at), 
			 * and then multiplied by the homeostatic number of cells that should be in the system (initialPopn). This gives the probability that a naive cell
			 * is to be placed into the simulation at each timestep. 
			 */ 
			probabilityNaiveTCellGenerated = (initialPopn / retrieveApoptosisNaiveMean() ) * scheduledTimeSlice;
		}
		
		/**
		 * Called by the Schedule. Handles the probabilistic insertion of naive CD4Thelper cells into the simulation, representing the influx from the thymus. 
		 */
		public void step(SimState state)
		{
			final TregSimulation sim = (TregSimulation) state;
			
			if(sim.random.nextDouble() <= probabilityNaiveTCellGenerated)
			{
				new CD4THelper(sim.circulation);
			}
		}
	}
}

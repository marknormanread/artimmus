package sim2d.cell.impl;

import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sim.engine.SimState;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.molecule.CD200;
import sim2d.cell.molecule.CD200R;
import sim2d.cell.molecule.MHC_II_Fr3;
import sim2d.cell.molecule.MHC_II_MBP;
import sim2d.cell.molecule.MHC_I_CDR12;
import sim2d.compartment.CNS2D;
import sim2d.compartment.Compartment;
import sim2d.molecule.CDR12;
import sim2d.molecule.Fr3;
import sim2d.molecule.MBP;
import sim2d.molecule.Molecule;
import sim2d.molecule.Type1;
import sim2d.molecule.Type2;

/**
 * MHC-II expression is constitutive on DCs. 
 * @author mark
 * @version updated by Richard Alun Williams for DC Mutual Exclusion logic and parameter loading 
 *
 */
public class DendriticCell extends APC_Impl implements MHC_I_CDR12, MHC_II_Fr3, MHC_II_MBP, CD200R
{
	
	/*
	 * general properties of all dendritic cells. 
	 */
	private static double phagocytosisProbabilityImmature;			// the probability that an APC will phagocytose an apoptotic cell if the APC is immature.
	private static double phagocytosisProbabilityMature;				// the probability that an APC will phagocytose an apoptotic cell if the APC is mature.
	
	private static double type1RequiredForActivation;			// the quanitity of type 1 cytokines that must be perceived for (an MHC-expressing) APC to express costim molecules.   	
	private static double type1SecretedPerTimesliceImmunized;	// immunized DCs secrete type1 cytokines, this determines how many.  
		
	private static double cytokineType2PolarizationRatio;		// the ratio of type2 to type1 cytokines required for the DC to become polarized in a type2 direction 
	
	private static boolean mutualExclusivePeptidePresentation;					// whether DC Mutual Exclusive presentation of MBP & Type1 is ON
	private static boolean costimRequiredForCytokineSecretion = false;	// whether co-stimulatory molecule expression is a pre-requisite for cytokine secretion (and hence type 1 polarization)
	
	public static boolean cd200CytokineSwitching = false;					// whether or not CD200 negative signaling results in cytokine switching on DCs.
	public static boolean cd200GradualReductionPrimingCapacity = false;    // whether or not CD200 negative signaling results in gradual reduction in DC priming capacity. 
	private static double cd200PrimingCapacityReductionFactor = 1.0;		// factor by which priming capacity is multiplied upon each receipt of CD200 negative signalling. 
	/*
	 * properties of dendritic cell instances. 
	 */
	protected boolean canExpressMBP = false;
	protected boolean canExpressFr3 = false;
	protected boolean canExpressCDR12 = false;
	protected boolean canExpressMHCII = false;								// this variety of Dendritic cell can always express MHCII. 
	protected boolean canExpressQa1 = false;
	protected boolean canExpressCoStim = false; 
	protected boolean expressingCD200R = false;								// upregulated when DC matures. 	

	protected boolean isDead = false;										// when the cell gets phagocytosed this is set to true to stop the 'step' method from restepping a cell that was phagocytosed by another in the same timeframe.	
	protected boolean immigrantFromPeriphery = false;						// when immigrants die we do not want to replace them in the SLO compartments. 

	protected boolean awaitingFirstPhagocytosisEvent = true;	// for use with mutually exclusive peptide presentation mechanism. Under that experimental setup, this variable is set to false following the first phagocytosis event. 
	
	private boolean immunizationDC = false;						// set to true when this DC is the result of immunization - used for secreting type1 cytokines. 
	
	public enum Polarization {None, Type1, Type2};
	protected Polarization polarization = Polarization.None;						// if this is true, then the DC secretes type 1 cytokines, and hence favours polarization of Th1 cells. 
	
	protected double cd200PrimingCapacity = 1.0;							// multiplier for DC priming capacity, starts at 1 and is gradually reduced by each CD200 negative signal. 
	
	/**
	 * Generic constructor for creating a dendritic cell during the course of the simulation. 
	 * 
	 * DO NOT use this method to directly create DCs with which to populate compartments at teh start of the simulation. Because DC maturation is periodic we need to do some
	 * clever timing manipulations to make some of the population (at initialisation time) immature and others mature. FOr that use the methods 'createInitialPopulationDendriticCellImmature'
	 * and 'createInitialPopulationDendriticCellMature' instead. 
	 * @param location
	 */
	public DendriticCell(Compartment location)
	{
		super(location);
	}
	
	/**
	 * Constructor is called when populating the simulation with Dendritic Cells. Some of these cells should be mature, and some should be immature, this constructor handles the creation 
	 * of immature dendritic cells. Should not be used during the simulation run. 
	 * @param sim
	 * @param location
	 * @return
	 */
	public static DendriticCell createInitialPopulationDendriticCellImmature(final TregSimulation sim, final Compartment location)
	{
		DendriticCell dc = new DendriticCell(location);
		
		// calculate a time at which this cell will cease to be immature. Note that to stop everything becoming mature at once we subtract some random proportion
		// of the mean value (to make it appear that this has been going on for a long time already) 
		dc.timeImmatureDurationEnds = sim.random.nextDouble() * calculateTimeImmatureDurationEnds();

		return dc;
	}
	
	/**
	 * Constructor is called when populating the simulation with Dendritic Cells. Some of these cells should be mature and some should be immature, this constructor handles the creation of 
	 * mature DCs. Should not be used during the simulation run. 
	 */
	public static DendriticCell createInitialPopulationDendriticCellMature(final TregSimulation sim, final Compartment location)
	{
		DendriticCell dc = new DendriticCell(location);
		
		dc.immigrantFromPeriphery = false;
		
		dc.timeImmatureDurationEnds = Double.MAX_VALUE;
				
		// choose a type 2 polarization, because we assume that there has been no inflammation present up till now. It does not really matter because the cell is not going to be presenting anything, but I would like for it to have a polarization to be consistent.  
		dc.polarization = DendriticCell.Polarization.Type2;	
		
		dc.canExpressMHCII = true;								// cell is going to become mature, so it will be able to express MHCII.  
		dc.timeOfDeath = sim.random.nextDouble() * calculateAbsoluteTimeOfDeath();									// the cell will expire some time after it migrates. 
		
		return dc;
	}
	
	/**
	 * Rerturns a DC that has been immunized. Hence, it expressed co-stimulatory molecules, and MHC-I/II-MBP. A compartment must still be specified for where this
	 * cell will initially reside.
	 */
	public static DendriticCell createImmunizedDendriticCell(TregSimulation sim, Compartment compartment)
	{
		DendriticCell cell = new DendriticCell(compartment);

		cell.immunizationDC = true;
		cell.polarization = Polarization.Type1;
		
		cell.canExpressMBP = true;					
		
		cell.canExpressCoStim = true;
		
		cell.canExpressMHCII = true;
		
		cell.timeImmatureDurationEnds = Double.MAX_VALUE;
		cell.timeOfDeath = calculateAbsoluteTimeOfDeath(); 
		
		cell.immigrantFromPeriphery = true;				// this cell should not be replaced when it dies. 
		
		return cell;
	}
	

	
	/**
	 * Implements any functionality required when a DC goes from an immature state to either tolerogenic or immunogenic. 
	 */
	protected void becomeNonImmature()
	{	
		canExpressMHCII = true;								// cell is going to become mature, so it will be able to express MHCII. 
		expressingCD200R = true;							// cell expresses CD200R upon maturation.
		determinePolarization();							// pick a polarisation. 
		setTimeToDeath();									// the cell will expire some time after it migrates.
		
		if(TregSimulation.cd4TregAbrogation == true)		// check conditions of CD4Treg abrogation experiment, if true, then Qa-1 expression by DCs is constitutive. 
			canExpressQa1 = true;
		else												// CD4Treg abrogation experiment is NOT set. Hence, Qa-1 expression must be induced. 
			canExpressQa1 = false;
	}
	
	protected void secreteCytokines()
	{
		if (polarization.equals(Polarization.Type1))
		{
			// if costimulatory molecules are required for cytokine secretion, and if co-stimulatory molecules are not expressed, then return immediately;
			if(costimRequiredForCytokineSecretion && (canExpressCoStim == false))
				return;
			
			// if costimulatory molecules are not required, or if they are and they are being expressed, then proceed.
			compartment.receiveSecretedMolecules(Type1.instance, type1SecretedPerTimesliceImmunized, this);	// then secrete type1 cytokine.
		}
	}
	
	/**
	 * Called by a CD4Treg to permit this APC to express Qa-1 molecules. 
	 * 
	 * there are the following options (taking costim expression as an example)
	 * 
	 * expressing costim? || awaiting expression of costim?
	 * no  || no   => set time to express
	 * no  || yes  => do nothing (definitely do not set back the time to expression!) 
	 * yes || no   => relicensing, set time to unexpression further back (reset it)
	 * yes || yes  => this should not be possible, time to expression is set to inf. when it passes.
	 * 
	 * 
	 */
	public void becomeLicensedForQa1()
	{ 	
		if(this.getExpressing_MHC_II_Fr3() == false)	// safety. DC cannot become licensed if it is not expressing the very molecules that CD4Th cells require to bind with it. 
			throw new RuntimeException("Cannot become licensed for Qa-1 expression is no MHC-II-Fr3 is being expressed to attract the attention of a CD4Treg");
		
		if(canExpressQa1 == false)							// if Qa1 is not already being expressed. 
		{
			canExpressQa1 = true;			
		}
	}
	
	public boolean getLicensedForQa1()
	{	return canExpressQa1;		};
	
	/**
	 * Method through which a CD4 T cell can license this APC. This can only be executed in relation to an APC that is tolerogenic. An immature APC
	 * does not express MHC-II, so no binding can be made. An immunogenic APC already expresses co-stim molecules, so the first guard will fail. 
	 * 
	 * This method is here because co-stim expression is transient (for apcsThatDie == false) and can be shorter than MHC expression
	 * 
	 * there are the following options:
	 * expressing costim? || awaiting expression of costim?
	 * no  || no   => set time to express
	 * no  || yes  => do nothing (definitely do not set back the time to expression!) 
	 * yes || no   => relicensing, set time to unexpression further back (reset it)
	 * yes || yes  => this should not be possible, time to expression is set to inf. when it passes.
	 * 
	 */
	public void becomeLicensedForCoStim() 
	{
		// not been licensed yet, and not expressing
		if(canExpressCoStim == false)
		{
			canExpressCoStim = true;
		}
	}
	
	
	/**
	 * Handles the actual phagocytosis of a cell. It is protected, so it cannot be an entry point for the phagocytosis behaviour; there are guards that need to be checked first, and these
	 * are handled in 'phagocytoseCell'. Note that DCMigrates overrides this method because phagocytosis of a cell in DCM does not necessarily lead to its immediate maturation, whereas it does
	 * in the vanilla DC (this this behaviour is likely to be amended to reflect that of the DCM). 
	 * @param presentable
	 */
	protected void performPhagocytosisOfCell(Set<Molecule> presentable)
	{
		// if mutual exclusive peptide presentation is on, and the first phagocytosis even has occurred, then return immediately, and do not perform any
		// peptide processing. 
		if(mutualExclusivePeptidePresentation == true && awaitingFirstPhagocytosisEvent == false)
			return;											
		
		// perform peptide processing. 
		if(presentable.contains(Fr3.instance))				// check if the cell contains Fr3		
			if(canExpressFr3 == false)			
				canExpressFr3 = true;				
					
		if(presentable.contains(CDR12.instance))			// check if the cell contains CDR1/2	
			if(canExpressCDR12 == false)			
				canExpressCDR12 = true;				
			
		
		if(presentable.contains(MBP.instance))				// check if the cell contains MBP. 		
			if(canExpressMBP == false)
				canExpressMBP = true;

		
		if(mutualExclusivePeptidePresentation == true)		// relevant only for mutually exclusive peptide presentation. 
			// if, by the end of this potential peptide derivation, one of the 3 peptides has been derived, then we set the state flag accordingly. 
			if(canExpressMBP || canExpressCDR12 || canExpressFr3)
				awaitingFirstPhagocytosisEvent = false;
	}

	/**
	 * This method handles the perception of cytokines within the compartment.
	 * 
	 * The method will upregulate costim expression (subject to some guards) if sufficient type1 cytokine is perceived. One of the guards ensures that DCMigrates will not become
	 * mature and start expressing co-stims before it is due to become mature, and this is ensured by behaviours that prevent MHCII/Qa1 being expressed on the DCM. 
	 * 
	 * In light of periodic maturation, a DC cannot use this method of perceiving type 1 cytokines to enter into maturity fast. The 'canExpressMHCII' guard, which is checked on in 
	 * 'expressingMHC()' (along with canExpressQa1) is only set by 'becomeNonImmature'. DCs become mature only on the basis of this periodic mechanism. 
	 */
	protected void perceiveMolecules(TregSimulation sim)
	{	
		double quantity = compartment.getConcentrationMolecule(Type1.instance, this);
		/*
		 * We enter into expressing co-stim molecules only if sufficient type 1 is around, and
		 * if MHC is expressed (no point having Co-stim expressing APCs around if T cells cant bind to them in the first place).
		 * 
		 * For DC class the guard below is executed all the time, because MHCII expression is constitutive. 
		 * For DCMigrates class it is not executed until the DCM becomes non-immature, since that is when MHCII is expressable; and the cell will not become licensed for Qa1 until 
		 * 	MHCII is expressed.  
		 */
		if(quantity >= type1RequiredForActivation && expressingMHC())	// if there are enough type1 cytokines, and if the APC is expressing MHC, then become licensed for costims. 				
		{	
			// receipt of type 1 cytokines allows cell to express co-stim molecules.
			if(canExpressCoStim == false)
			{
				/* this is the point where the DC becomes immunogenic */
				if(polarization == Polarization.None) {					// only do this once, else as soon as its in the SLO compartments its going to get booted into type1 direction. 
					determinePolarization();
				}
				canExpressCoStim = true;
				setTimeToDeath();
			}
		}
	}
	
	/**
	 * Called when a DC becomes immunogenic, based on the cytokine profiles that it sees around it, it will become either type1 or type2 polarizing. 
	 * This method handles that decision. 
	 */
	protected void determinePolarization()
	{
		final double type1 = compartment.getConcentrationMolecule(Type1.instance, this);
		final double type2 = compartment.getConcentrationMolecule(Type2.instance, this);
		
		
		if(type1 + type2 == 0.0) { polarization = Polarization.Type2; return; }			// safety. Type 2 because I dont want a bunch of cells secreting IL12 for no reason!
		final double proportion = type2 / (type1 + type2);								// proportion of the total cytokines that type 2 comprises.
		
		/* choose polarization based on the ratio of type2 to (type1 and 2) cytokines, and do some logging for data collection purposes. */
		if(proportion >= cytokineType2PolarizationRatio) {
			polarization = Polarization.Type2;
			if(compartment == TregSimulation.sim.cns)
				CNS2D.cumulativeCNSDCType2Polarized ++;	// record the polarization of a type2 DC.
		} else {
			polarization = Polarization.Type1;
			if(compartment == TregSimulation.sim.cns)
				CNS2D.cumulativeCNSDCType1Polarized ++;	// record the polarization of a type1 DC.
		}
	}
	

	/**
	 * Overriden method that implements the state change into a Dendritic cell becoming apoptotic. 
	 */
	protected void becomeApoptotic()
	{
		/* stop this cell from interacting with others */
		isApoptotic = true;
		isDead = true; 
		
		if(immunizationDC == false)							// assuming the cell is not the result of an immunization (and originates from the periphery), then replace it when it dies. 
		{
			new DendriticCell(this.compartment);	// homeostatic replacement of dead cells with immature ones.
		}
		
		TregSimulation.sim.removeFromSimulationSchedule(this);		// critical, remove this cell from the simulation's schedule.
		compartment.removeCellFollowingDeath(this);					// remove yourself from the compartment. 
	}
	
	/**
	 * Called when DC is negatively signalled through CD200R. 
	 */
	public void receiveCD200RNegativeSignal()
	{
		if(cd200CytokineSwitching)  								// CD200 induced cytokine switching
			polarization = polarization.Type2;	             
		
		if(cd200GradualReductionPrimingCapacity) {					// CD200 induced reduction in priming capacity
			cd200PrimingCapacity *= cd200PrimingCapacityReductionFactor;
		}
	}
	
	/**
	 * Method handles the interactions between this Dendritic cell and other cells within the simulation.
	 * 
	 * T cells handle interactions with the DC, so they are not considered here. 
	 */
	protected void interactWithOtherCell(TregSimulation sim, Cell otherCell) 
	{
		if(otherCell instanceof APC_Impl && otherCell.isApoptotic())		// only deal with APCs here, T cells instigate this though their own step functions. 
			phagocytoseCell(sim, otherCell);
	}


	public boolean isImmature()
	{
		return (expressingMHC() == false) && (getExpressing_CoStimulatory() == false) && (isApoptotic() == false);
	}
	
	public boolean isTolerogenic()
	{
		return expressingMHC() && (getExpressing_CoStimulatory() == false) && (isApoptotic() == false);
	}
	
	public boolean isImmunogenic()
	{
		return expressingMHC() && getExpressing_CoStimulatory() && (isApoptotic() == false);				
	}
	
	/**
	 * Returns true when this cell is apoptotic.
	 */
	public boolean isApoptotic() 
	{	return isApoptotic;		}									
	
	public boolean isDead() 
	{	return isDead;		}
	
	
	/**
	 * Method handles setting a time of death. We guard against picking the time of death more than once because it is possible for the dendritic cell to phagocytose more than one
	 * variety of peptides at a time (even from the same cell, as is the case with Th1.  
	 */
	protected void setTimeToDeath()
	{
		if(timeOfDeath != Double.MAX_VALUE)								// if a time of death is already set, then do nothing and return. 
			return;
		
		timeOfDeath = calculateAbsoluteTimeOfDeath();					// calculate the time of death. 
	}
	
	/**
	 * Returns true when this DC can express Qa-1-CDR1/2. This is when the cell both contains CDR1/2 and has been licensed to express Qa-1. 
	 */
    public boolean getExpressing_MHC_I_CDR12() 
    {
    	// the APC can express CDR1/2 and Qa1
    	return (canExpressCDR12 && canExpressQa1); 	  
   	}
    
	public boolean getExpressing_MHC_II_Fr3() 
	{
		//	the APC can express Fr3 and MHC-II
		return (canExpressFr3 && canExpressMHCII);  	
    }
	
	public boolean getExpressing_MHC_II_MBP() 
	{
		return (canExpressMBP && canExpressMHCII);    	
	}
	
	public boolean getExpressing_CoStimulatory()
	{
		return canExpressCoStim;
	}
	
	public boolean isExpressing_MHCPeptide()
	{
		return (getExpressing_MHC_II_MBP() || getExpressing_MHC_II_Fr3() || getExpressing_MHC_I_CDR12());
	}
	
	
	/**
	 * Returns true if this APC is expressing any MHC molecules.
	 */
	public boolean expressingMHC()
	{
		// return true if any of these MHC types are currently being expressed.
		if( canExpressMHCII || canExpressQa1)
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if this APC is expressing CD200R. 
	 */
	public boolean getExpressing_CD200R()
	{
		return expressingCD200R;	
	}


	/**
	 * Purely here for GUI interface, Do not use within the rest of the program. 
	 * 
	 * java getters and setters
	 */
	public boolean getMBP()
	{	return canExpressMBP;	}
	public boolean getFr3()
	{	return canExpressFr3;	}
	public boolean getCDR12()
	{ 	return canExpressCDR12;	}
	public boolean getMHCII()
	{	return canExpressMHCII;	}
	public boolean getQa1()
	{	return canExpressQa1;	}
	
	public double getTimeImmatureDurationEnds()
	{ 	return timeImmatureDurationEnds;	 }
	public double getTimeToDeath()
	{	return timeOfDeath;		}
	
	public boolean getImmunizationDC()
	{	return immunizationDC;	}
	public Polarization getPolarization()
	{	return polarization; 	}
	
	public double getCD200PrimingCapacity()
	{	return cd200PrimingCapacity;	}
	

	protected double getPhagocytosisProbabilityImmature()
	{	return phagocytosisProbabilityImmature;		}
	
	protected double getPhagocytosisProbabilityMature()
	{	return phagocytosisProbabilityMature;		}
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("DendriticCell").item(0);
		
		phagocytosisProbabilityImmature = Double.parseDouble(pE.getElementsByTagName("phagocytosisProbabilityImmature").item(0).getTextContent());
		phagocytosisProbabilityMature = Double.parseDouble(pE.getElementsByTagName("phagocytosisProbabilityMature").item(0).getTextContent());
			
		type1RequiredForActivation = Double.parseDouble(pE.getElementsByTagName("type1RequiredForActivation").item(0).getTextContent());
				
		final double type1SecretedPerHourImmunized = Double.parseDouble(pE.getElementsByTagName("type1SecretedPerHourImmunized").item(0).getTextContent());
		type1SecretedPerTimesliceImmunized = type1SecretedPerHourImmunized * TregSimulation.sim.timeSlice;
				
		cytokineType2PolarizationRatio = Double.parseDouble(pE.getElementsByTagName("cytokineType2PolarizationRatio").item(0).getTextContent());
		
		NodeList mutExPPNL = pE.getElementsByTagName("mutualExclusivePeptidePresentation");
		if(mutExPPNL.getLength() > 0)
		{
			mutualExclusivePeptidePresentation = Boolean.parseBoolean(mutExPPNL.item(0).getTextContent()); 
		}
		
		NodeList coStimReqSecNL = pE.getElementsByTagName("costimRequiredForCytokineSecretion");
		if(coStimReqSecNL.getLength() > 0)
		{
			costimRequiredForCytokineSecretion = Boolean.parseBoolean(coStimReqSecNL.item(0).getTextContent()); 
		}
		
		pE = (Element) params.getElementsByTagName("CD200").item(0);
		cd200CytokineSwitching = Boolean.parseBoolean(pE.getElementsByTagName("cd200CytokineSwitching").item(0).getTextContent());
		cd200GradualReductionPrimingCapacity = Boolean.parseBoolean(pE.getElementsByTagName("cd200GradualReductionPrimingCapacity").item(0).getTextContent());
		cd200PrimingCapacityReductionFactor = Double.parseDouble(pE.getElementsByTagName("cd200PrimingCapacityReductionFactor").item(0).getTextContent());
	}
}

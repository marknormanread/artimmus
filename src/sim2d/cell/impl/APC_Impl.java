package sim2d.cell.impl;

import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.cell.molecule.MHC_II_Fr3;
import sim2d.compartment.Compartment;
import sim2d.molecule.Molecule;
import sim2d.molecule.Type1;

public abstract class APC_Impl extends Cell_Impl implements APC
{

	/*
	 * General properties of all APCs 
	 */
	private static double immatureDurationMean;							// the mean length of time that a DC will be immature and stationary for before it migrates.
	private static double immatureDurationStdDev;						// the std dev of mean length of time that a DC will be immature and stationary for before it migrates. 
	
	private static double costimExpressionDelayStdDev;
	private static double costimExpressionDelayMean;
	
	private static double mhcExpressionDelayMean;						// following the phagocytosis of a peptide, this is the mean time before MHC-peptide appears.
	private static double mhcExpressionDelayStdDev;						// this is the standard deviation around the mean that defines when the MHC-peptide appears.

	private static double timeOfDeathMean;								// the mean length of time that an APC will remain in a mature state before dying. 
	private static double timeOfDeathStdDev;							// the standard deviation for the above distribution. 
	public static double retrieveTimeOfDeathMean()
	{	return timeOfDeathMean;		}
		
	
	protected static double probabilityPhagocytosisToPeptide;			// when a cell is phagocytosed, this variable dictates the probability that peptides will be displayed. 
	
	/*
	 * Properties and state specific to instances of APCs. 
	 */	
	protected double timeImmatureDurationEnds = Double.MAX_VALUE;	// the time at which this instance will become either tolerogenic or immunogenic
	protected double timeOfDeath = Double.MAX_VALUE;						// time at which this APC will die following differentiation away from immaturity. 

	
	protected boolean isApoptotic = false;
	
	
	public APC_Impl(Compartment location)
	{
		super(location, true);
		
		// calculate a time at which this cell will cease to be immature, and it will migrate. Note that to stop everything migrating at once we subtract some random proportion
		// of the mean value (to make it appear that this has been going on for a long time already) 
		timeImmatureDurationEnds = calculateTimeImmatureDurationEnds();
	}
	
	/**
	 * Abstract methods that concrete implementations of APC_Impl must provide. 
	 */
	public abstract boolean isImmature();
	public abstract boolean isTolerogenic();
	public abstract boolean isImmunogenic();
	public abstract boolean isExpressing_MHCPeptide();									// is the APC expressing both MHC and a peptide?
	public abstract boolean expressingMHC();											// is the APC expressing MHC molecules?
	
	protected abstract double getPhagocytosisProbabilityImmature();	
	protected abstract double getPhagocytosisProbabilityMature();
	
	public static double retrieveImmatureDurationMean()
	{	return immatureDurationMean;	}
	public static double retrieveImmatureDurationStdDev()
	{	return immatureDurationStdDev;	}
	
	protected abstract void perceiveMolecules(TregSimulation sim);
	protected abstract void secreteCytokines();
	protected abstract void becomeNonImmature();										// this is related to the periodic maturation of APCs. 
	protected abstract void becomeApoptotic();
	
	
	/**
	 * Method is called by the schedule to step this cell and provide simulation behaviour. Here, this method provides some housekeeping functionality that is common to all APC concrete implementations;
	 * though concrete classes may override it, it is highly recommended that you 'super' call this method before doing any conrete class implementation. 
	 */
	public void step(SimState state)
	{
		if(isDead())										// do nothing if the cell was already phagocytosed earlier in this timeframe.		
			return;
				
		if(isApoptotic == true)								// apoptotic cells should not exectute anything beyond this point. 	
			return;
		
		final TregSimulation simulation = (TregSimulation) state;
		
		perceiveMolecules(simulation);						// examine the cytokine mix in the APCs neighbourhood, and potentially upregulate co-stimulatory molecules. 
		interactWithOtherCellsGeneric(simulation);
		secreteCytokines();		
		checkStateMaintenanceTimers();						// this comes last because it handles this cell becoming apoptotic, and when that happens none of the other steps should be completed.	
	
	}

	/**
	 * Method check and handles the timers that dictate the transition between states in an APC (note that timers are not the only events that dictate state changes...)
	 *
	 */
	protected void checkStateMaintenanceTimers()
	{
		final double timeNow = TregSimulation.sim.schedule.getTime();
		
		if(timeNow >= timeImmatureDurationEnds)
		{
			timeImmatureDurationEnds = Double.MAX_VALUE;			// reset timer
			becomeNonImmature();									// handle change in state to migrating.
		}		
		if(timeNow >= timeOfDeath)
		{
			timeOfDeath = Double.MAX_VALUE;								// reset timer
			becomeApoptotic();
		}
	}
	
	/**
	 * publicly available entry point that handles the phagocytosis of another cell by this APC. This method performs a lot of guard checks, there are several conditions and
	 * probabilities in which phagocytosis will not happen. If all the guards and checks pass, then the method 'performPhagocytosisOfCell' handles the phagocytosis itself. 
	 * 
	 * Note that 'performPhagocytosisOfCell' is overridden in DCMigrates, because phagocytosis in DCM does not lead to immediate maturiation, whereas it does in the vanilla DC.  
	 */
	public void phagocytoseCell(TregSimulation sim, Cell cell) 
	{
		if( cell.isApoptotic() == false || cell.isDead())		// do nothing if the cell is not apoptotic, or if it has already been phagocytosed (but not yet removed from the simulation)
			return;

		if(isApoptotic)
			return;												// dead APCs can't phagocytose anything.
		
		/* APCs that are stimulated (and express MHC) are less likely to phagocytose other cells. */
		double probOfPhagocytosis = getPhagocytosisProbabilityImmature();
		if(expressingMHC())										// if DC is expressing MHC molecules (ie, it is stimulated)
			probOfPhagocytosis = getPhagocytosisProbabilityMature();
		
		
		if(sim.random.nextDouble() >= probOfPhagocytosis)		// if we are unstimulated (no MHC) then we will continue, if we are stimulated then there is a high chance that we will not phagocytose this cell.
			return;
		
		Set<Molecule> presentable = cell.bePhagocytosised(sim);
		if(presentable == null)
			return;												// nothing to be derived.
		
		/* anything UPTO 'probabilityPhagocytosisToPeptide' will result in peptides being derived. Else, save computation time and return now instead. */ 
		if(sim.random.nextDouble() >= probabilityPhagocytosisToPeptide)
			return;
			
		performPhagocytosisOfCell(presentable);
	}
	
	/**
	 * Once a cell has been phagocytosed, as handled by 'phagocytoseCell', this method will process the peptides that are derived from that cell. Different APCs, in different compartments, are interested in different
	 * peptides; overriding this method allows cell specific behaviours to be implemented. 
	 */
	protected abstract void performPhagocytosisOfCell(Set<Molecule> presentable);
	
	/**
	 * Returns a time at which an immature APC will become mature, based on the probability distribution parameters. 
	 * @return
	 */
	protected static double calculateTimeImmatureDurationEnds()
	{
		double interval = TregSimulation.sim.random.nextGaussian();
		interval *= (retrieveImmatureDurationStdDev()/ 2.0);	// one random.gaussian stdDev = 2* stdDevs for apoptosis in partial maturity.
		interval += retrieveImmatureDurationMean();				// shift the mean from 0.0 to what we desire.
		return interval + TregSimulation.sim.schedule.getTime();		// convert into absolute time and return.
	}
	
	
	protected static double calculateAbsoluteTimeOfDeath()
	{
		double interval = TregSimulation.sim.random.nextGaussian();
		interval *= (timeOfDeathStdDev / 2.0);
		interval += timeOfDeathMean;
		return interval + TregSimulation.sim.schedule.getTime();
		
	}
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("APC").item(0);
		
		immatureDurationStdDev = Double.parseDouble(pE.getElementsByTagName("immatureDurationStdDev").item(0).getTextContent());
		immatureDurationMean = Double.parseDouble(pE.getElementsByTagName("immatureDurationMean").item(0).getTextContent());
		
		costimExpressionDelayStdDev = Double.parseDouble(pE.getElementsByTagName("costimExpressionDelayStdDev").item(0).getTextContent());
		costimExpressionDelayMean = Double.parseDouble(pE.getElementsByTagName("costimExpressionDelayMean").item(0).getTextContent());
		
		mhcExpressionDelayMean = Double.parseDouble(pE.getElementsByTagName("mhcExpressionDelayMean").item(0).getTextContent());
		mhcExpressionDelayStdDev = Double.parseDouble(pE.getElementsByTagName("mhcExpressionDelayStdDev").item(0).getTextContent());
		
		timeOfDeathMean = Double.parseDouble(pE.getElementsByTagName("timeOfDeathMean").item(0).getTextContent());
		timeOfDeathStdDev = Double.parseDouble(pE.getElementsByTagName("timeOfDeathStdDev").item(0).getTextContent());
		
		probabilityPhagocytosisToPeptide = Double.parseDouble(pE.getElementsByTagName("probabilityPhagocytosisToPeptide").item(0).getTextContent());
	}
}


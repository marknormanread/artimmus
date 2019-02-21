package sim2d.cell.impl;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim.engine.SimState;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.compartment.Compartment;
import sim2d.dataCollection.dataLoggers.DCApoptosedPeptidePresentationDataLogger;
import sim2d.molecule.CDR12;
import sim2d.molecule.Fr3;
import sim2d.molecule.MBP;
import sim2d.molecule.Molecule;


/**
 * IMPORTANT: In this class we assume that APCs die always, there is not 'APCs persist' mechanism. 
 * 
 * @author mark
 *
 */
public class DendriticCellMigrates extends DendriticCell 
{
	public static DCApoptosedPeptidePresentationDataLogger peptidePresentationDL;					// this is a data logger that collects data on what antigenic peptides an apoptotic DCMigrates cell expresses.
	
	/*
	 * Static variables that are specific to all instances of DendriticCellMigrates
	 */
	private static double lengthOfTimeMovingFollowingMigration; 	// when a DC migrates, it moves around for a short while before becoming statically placed. This variable dictates how long it can move around for.
	
	/*
	 * local variables that are specific to individual instantiations of this class.
	 */
	private Compartment originalCompartment;							// where the DC was original set to reside. This is in case the DC migrates. In that case we want to replace the DC in its new compartment, not in the new one.
	
	 	
	// when a cell ceases to be non-immature it migrates, hence for a short period of time it can move in the compartment (and migrate to another one). This timer handles that movement. 
	// it is set some time into the future when the cell becomes non-immature, and when that time passes it is reset to infinity. 
	private double timeToStopMovingAfterMaturation = Double.MAX_VALUE;					
	private boolean isMobile = false;
	

	
	/**
	 * The general constructor, to be used when a new DCM is created during the progression of the simulation.
	 * 
	 * NOT to be used when initialising the various compartments with these cells. See 'createDendriticCellMigratesImmature' and 'createDendriticCellMigratesMigrated' for
	 * details on why. 
	 */
	public DendriticCellMigrates(Compartment location)
	{
		super(location);
		
		originalCompartment = location;								// note this cell's original location (where it is to be replaced in the event that this instance dies).
												
		canExpressMHCII = false;									// by default a DCMigrages cannot express MHCII molecules. 
	}
	
	/**
	 * Creates a DCM that is immature, and resides in the specified compartment. TregSimulation uses this to populate the initial CNS-DCM population because we do not want them all
	 * to migrate at the same time, so this adds some random variation and makes it appear that the cells have been there for a long period of time, and are out of phase with eachothers
	 * migratory patterns. 
	 * @param sim
	 * @param compartment
	 * @return
	 */
	public static DendriticCellMigrates createDendriticCellMigratesImmature(final TregSimulation sim, final Compartment compartment)
	{
		DendriticCellMigrates dcm = new DendriticCellMigrates(compartment);
		
		// calculate a time at which this cell will cease to be immature, and it will migrate. Note that to stop everything migrating at once we subtract some random proportion
		// of the mean value (to make it appear that this has been going on for a long time already) 
		dcm.timeImmatureDurationEnds = sim.random.nextDouble() * calculateTimeImmatureDurationEnds();
		
		return dcm;
	}
	
	/**
	 * Creates a DCM that is already non-immature and resides elsewhere (compartmentDestination), but will be replaced in the given original compartment (compartmentSource).
	 * 
	 * This is used to place some number of the CNS population (in addition to what the parameters.xml file states) of DCMs into the CLN compartment, to make the transition into 
	 * the simulation smooth. Called from within TregSimulation. 
	 * @param sim
	 * @param compartmentSource
	 * @param compartmentDestination
	 * @return
	 */
	private static int total = 0;
	public static DendriticCellMigrates createDendriticCellMigratesMigrated(final TregSimulation sim, final Compartment compartmentSource, final Compartment compartmentDestination)
	{
		DendriticCellMigrates dcm = new DendriticCellMigrates(compartmentDestination);
		
		dcm.immigrantFromPeriphery = true;					// DCM will have originated from the CNS
		dcm.originalCompartment = compartmentSource;
		
		dcm.timeImmatureDurationEnds = Double.MAX_VALUE;
		dcm.isMobile = false;
		
		// choose a random polarisation - it does not really matter because the cell is not going to be presenting anything, but I would like for it to have a polarization to be consistent.  
		dcm.polarization = DendriticCell.Polarization.Type2;	
		
		dcm.canExpressMHCII = true;								// cell is going to become mature, so it will be able to express MHCII.  
		dcm.timeOfDeath = sim.random.nextDouble() * calculateAbsoluteTimeOfDeath();									// the cell will expire some time after it migrates. 

		return dcm;
	}
	
	/**
	 * Overridden from DendriticCell, in order to add on migration functionality. 
	 */
	public void step(SimState state)
	{
		super.step(state);
		
		final TregSimulation sim = (TregSimulation) state;
		
		
		/*
		 * Handle timers specific to the DendriticCellMigrates class. 
		 */

		if(sim.schedule.getTime() >= timeToStopMovingAfterMaturation)
		{	
			timeToStopMovingAfterMaturation = Double.MAX_VALUE;
			isMobile = false;
		}
	}
	
	
	/**
	 * Method handles events that result of the DendriticCellMigrates becoming either immunogenic or tolerogenic - essentially when it migrates. 
	 *
	 */
	protected void becomeNonImmature()
	{
		super.becomeNonImmature();							// do whatever the DendriticCell class does first. 
		
		isMobile = true;									// for a short period of time the cell can move as it migrates.  
	}
	
	/**
	 * For a short time after this cell ceases to be immature, it becomes mobile, such that it can migrate into another compartment. Eventually it stops being mobile again. 
	 * This method returns whether the cell is mobile, and is called from the compartment implementations. 
	 */
	public boolean isMobile()
	{	return isMobile;	}
	
	/**
	 * Overridden method that allows specific behaviours to be implemented in subclasses in the event that this cell dies. 
	 */
	protected void becomeApoptotic()
	{
		timeOfDeath = Double.MAX_VALUE;
		/* stop this cell from interacting with others */
		isApoptotic = true;
		
		isDead = true;
		
		/* replace this cell in its original compartment */
		new DendriticCellMigrates(originalCompartment);				// homeostatic replacement of dead cells with immature ones.
		
		/* remove this cell from the simulation, and from the current compartment */
		TregSimulation.sim.removeFromSimulationSchedule(this);		// critical, remove this cell from the simulation's schedule.
		compartment.removeCellFollowingDeath(this);					// remove yourself from the compartment.
		
		peptidePresentationDL.logApoptoticAPEvent(this);			// record that this DCMigrates dies (this is data logging, not logic of the simulation).
	}
	
	
	private void setTimeToStopMovingFollowingMigration()
	{
		timeToStopMovingAfterMaturation = TregSimulation.sim.schedule.getTime();						// set to now.
		timeToStopMovingAfterMaturation += lengthOfTimeMovingFollowingMigration;						// add something akin to a mean. 
		timeToStopMovingAfterMaturation += ((TregSimulation.sim.random.nextDouble() - 0.5) * 4.0); 		// add some variation. 
	}
	
	/**
	 * Overridden from Cell_Impl. 
	 * Cell has migrated into a new compartment. For a dendritic cell this means that it has become either fully or partially mature, and hence must
	 * be given some time to settle into one place.
	 */
	public void migrateIntoCompartment(Compartment newCompartment)
	{
		compartment = newCompartment;
		setTimeToStopMovingFollowingMigration();
	}
	
	/*
	 * Javabean getters and setters, for use with the MASON GUI. 
	 */
	public double getTimeToStopMoving()
	{	return timeToStopMovingAfterMaturation;	}
	

	
	public String getOriginalCompartment()
	{
		if(originalCompartment == TregSimulation.sim.cns)
			return "cns";
		if(originalCompartment == TregSimulation.sim.cln)
			return "cln";
		if(originalCompartment == TregSimulation.sim.slo)
			return "slo";
		if(originalCompartment == TregSimulation.sim.spleen)
			return "spleen";
		else return "unknown";
	}
	
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("DendriticCellMigrates").item(0);

		lengthOfTimeMovingFollowingMigration = Double.parseDouble(pE.getElementsByTagName("lengthOfTimeMovingFollowingMigration").item(0).getTextContent());
	}
}

package sim2d.cell.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import sim.engine.SimState;
import sim2d.TregSimulation;
import sim2d.cell.APC;
import sim2d.cell.Cell;
import sim2d.cell.TCell;
import sim2d.compartment.Compartment;
import sim2d.dataCollection.dataLoggers.TCellPrimingLocationDataLogger;

/**
 * This class is a collection point for data and methods that are common to all T cells. Many of the methods here are intended to be overridden, and then explicitly
 * called from the subclass. This way generic functionality is performed, and functions specific to particular T cell species can be performed too. 
 * 
 * T cells start out their existence in a naive state. Naive T cells can die of neglect if they do not experience TCR stimulation with a certain period of time. 
 * This mechanism, and others of a similar nature, is represented by a timer that holds an absolute time in the future when apoptosis will occur at. Each time
 * TCR stimulation is achieved, the timer is reset to some further time in the future. The times themselves (for this mechanism, and others like it) are drawn from a probability
 * distribution, represented by two variables - one holds a mean value, the other represents a value of TWO standard deviations (NOT ONE). 
 * 
 * All T cell have a TCR specificity, which is inherited directly by daughter cells that arise from proliferation. Specificities are randomly determined in naive cells that are
 * introduced into the simulation (ie, those that do not arise from proliferation), upon time of creation.
 * 
 * T cells can die of neglect at any stage of their lifecycle, though the time required to die of neglect is different for each stage. 
 * 
 * T cells must be locally activated following their differentiation into effector cells in order to perform effector functions. This applies to all cells with one exception:
 * CD4Treg cells do not have to be locally activated in order to license an APC. All other functions of cytokine secretion, or apoptosis induction require local activation.
 * Following local activation, cells can still die of neglect: they must be activated repeatedly. It is only before the first local activation that that effector function is 
 * suppressed for. In order to prevent local activation happening in the secondary lymphoid organs, there is a timer that must expire before any local activation events can
 * have an effect. 
 * 
 * @author mark
 *
 */
public abstract class TCell_Impl extends Cell_Impl implements TCell
{
	/*
	 * General properties of all T cells
	 */	
	private static double apoptosisNaiveMean;			// the mean time after which a naive cell that has not received a signal 1 becomes apoptotic.
	private static double apoptosisNaiveStdDev;			// the std dev of mean time after which a naive cells that has not recieved a signal 1 becomes apoptotic. 
	
	private static double apoptosisPartialMaturityMean;		// stores the mean time in which a cell can remain partially mature before dying.
	private static double apoptosisPartialMaturityStdDev;	// stores the standard deviation of time in which a cell can remain partially mature before dying.
	 
	private static double proliferationMean;				// the mean value of proliferation.
	private static double proliferationStdDev;			// the value of a single standard deviation of proliferation.
		// small values for this result in all potential daughters being cut off when APC binding is lost. 
	private static double cutoffThresholdForProlifWhenBindingLost;	// if time spent proliferating a naive cell has passed this proportion of ProliferationMean when an APC binding is lost, then the naive cell will still be spawned. 
	
	private static double becomeEffectorMean;			// the mean time that it takes for a T cell in proliferating state to differentiate into an effector cell
	private static double becomeEffectorStdDev;			// the standard deviation of the time that it takes for a T cell in proliferating state to differentate into an effector cell. 
	
	private static double AICDMean;						// the mean value of AICD.
	private static double AICDStdDev;					// the value of a single standard deviation of AICD.
	
	private static int cellsPerGridspace;				// how many T cells can fit into a single grid space
	public static boolean spatialTestEquals = true;		// this is an implementation specific test, and does not form part of simulation logic. 
	public static int retrieveCellsPerGridspace()
	{	return cellsPerGridspace;	}
	
	private static double specificityUpperLimit;			// the specificity of the T cell will be chosen from this range
	private static double specificityLowerLimit;	
	
	private static double timeLocalActivationInducedEffectorFunctionFor;			// how long effector function is maintained for following a 'local activation' (subsequent activation when in effector state)
	private static double timeLocalActivationDelay;
	
	protected boolean isDead = false;						// when the cell gets phagocytosed this is set to true to stop the 'step' method from restepping a cell that was phagocytosed by another in the same timeframe.
	
	public static TCellPrimingLocationDataLogger primingDL;	// used for data logging purposes. Whenever a T cells is primed, it should be logged here. 
	
	/*
	 * Properties of T Cell instances
	 */
	public static enum Maturity { Naive, Partial, Proliferating, Effector, Apoptotic } // the states of maturity that a T cell can exist in.
	
	protected Maturity maturity;
	
	protected double specificity;							// this is a value between 0 (no specificity for peptide) and 1.0 (perfect binding with MHC-peptide every time)
	
	
	private double timeOfApoptotisNaiveMaturity = Double.MAX_VALUE;		// stores the time at which this T cell instance will die as a naive cell though neglect. 
	private double timeOfApoptosisPartialMaturity = Double.MAX_VALUE;	// stores the amount of time that this T cell has been in a state of partial maturity.


	private double timeOfAICD = Double.MAX_VALUE;			// the absolute time at which activation-induced cell death is to take place, following activation of a T cell.
	private double timeOfProliferation = Double.MAX_VALUE;	// the absolute time at which this cell will next proliferate.

	private double timeOfBecomeEffector = Double.MAX_VALUE;	// this timer will keep track of the absolute time at which this cell is to become an effector cell. 
	
	protected boolean boundToAPC = false;					// when in a proliferating state it is possible for a T cell to become attached to an APC. This will stop it from moving around the compartment.

	private boolean effectorFunctionFromLocalActivation	= false;			// if this flag is false, then an effector cell cannot perform any effector function.
	private double timeEffectorApoptotoisFromNeglect = Double.MAX_VALUE;	// during effector state, this timer points to the time at which the effector cell is to die from neglect. It is reset upon MHC:peptide binding. 
	private double timeEndLocalActivationDelay = Double.MAX_VALUE;			// only after the absolute time that this timer holds has expired can a (recent) effector cell receive local activation. Before this time binding events are ignored. 
	
	
	/**
	 * Getters and setters for the time-probability related values. By default we return those defined statically to this class, however concrete classes may 
	 * override to provide their own values (CD4Th2 cells die at a slower rate than CD4Th1 cells, for example).
	 * 
	 * Some of these methods are called 'retrieve' because they do not need to appear in the 
	 * GUI. 
	 */
	protected static double retrieveApoptosisNaiveStdDev()
	{	return apoptosisNaiveStdDev;	}
	
	protected static double retrieveApoptosisNaiveMean()
	{	return apoptosisNaiveMean;	}
	
	protected static double retrieveApoptosisPartialMaturityStdDev()
	{	return apoptosisPartialMaturityStdDev;	}
	
	protected static double retrieveApoptosisPartialMaturityMean()
	{	return apoptosisPartialMaturityMean;	}
	
	protected double retrieveProliferationStdDev()
	{	return proliferationStdDev;		}
	
	protected double retrieveProliferationMean()
	{	return proliferationMean;		}
	
	protected static double retrieveEffectorTimeMean()
	{	return becomeEffectorMean; 	}
	
	protected static double retrieveEffectorTimeStdDev()
	{	return becomeEffectorStdDev;	}
	
	protected double retrieveAICDStdDev()
	{	return AICDStdDev;		}
	
	protected double retrieveAICDMean()
	{	return AICDMean;		}
	
	protected static double retrieveSpecificityUpperLimit()
	{	return specificityUpperLimit;	}
	
	protected static double retrieveSpecificityLowerLimit()
	{	return specificityLowerLimit;	}
	
	public double getSpecificity()
	{	return specificity;		}
	
	protected boolean getEffectorFunctionFromLocalActivation()
	{	return effectorFunctionFromLocalActivation;		}
	
	
	
	/**
	 * Constructor places cell randomly in the specified compartment. This constructor is used to create new naive T cells. It should NOT be used to create
	 * daughter cells as a result of proliferation. The newly created cell is assigned a specificity, randomly determined. 
	 */
	public TCell_Impl(Compartment location)
	{
		super(location, true);
		maturity = Maturity.Naive;										// set the new T cell's maturity.
		timeOfApoptotisNaiveMaturity = calculateTimeOfApoptotisNaiveMaturity();
		
		specificity = TregSimulation.sim.random.nextDouble();			// between 0 and 1.0
					// between 0.0 and (upper limit - lower limit) = get correct range
		specificity *= retrieveSpecificityUpperLimit() - retrieveSpecificityLowerLimit();
					// between lower limit and upper limit = shift range
		specificity += retrieveSpecificityLowerLimit();
	}
	/**
	 * Constructor places cell in the specified compartment, in the same location as the indicated 'parent' cell. This constructor should be used when creating cells that
	 * are the result of proliferation. Specificity is inherited from the parent cell. The daughter cell starts off 'bound to apc' - this is an artifact of the order in
	 * which cells and compartments (where movement is handled) are scheduled. If the daughter T cell is not adjacent to an APC (because, for example, there may be no space,
	 * in which case the cell will be placed in a neighbouring grid space) then this parameter will be adjusted accordingly immediately.  
	 */
	public TCell_Impl(TregSimulation sim, Compartment location, TCell_Impl parent)
	{
		super(sim, location, parent);
		maturity = Maturity.Naive;
		timeOfApoptotisNaiveMaturity = calculateTimeOfApoptotisNaiveMaturity();		// set timer for death by neglect. 
		
		this.specificity = parent.specificity;						// copy across specificity. 
		this.boundToAPC = true;										// start off bound to APC, may immediately revert though. This is an artifact of the order in which cells and compartments are stepped.
	}
	
	/**
     * Called by Schedule to animate cells in the simulation. This method is overridden in the concrete T cell implementations, 
     * however it must still be called here because it provides some implementation common to all T Cells. 
     */
	public void step(SimState state) 
	{
		if(isDead)														// do nothing if the cell was already phagocytosed earlier in this timeslice.		
			return;
		
		TregSimulation simulation = (TregSimulation) state;
		
		stateMaintenance(simulation);									// maintenance relating to the state of activation.
		if(isDead)														// it is possible that cell became completely dead during 'stateMaintenance'. This is safety.
			return;
				
		boundToAPC = false;				/* this will be set to true again in the following method call if there is an APC for which this cell is specific in the neighbourhood */
		interactWithOtherCellsGeneric(simulation);						// potential interactions with other cells in the neighbourhood. 
	}
	
	
	/**
	 *  Update aspects of a cells state of maturity. 
	 */
	protected void stateMaintenance(TregSimulation sim)
	{
		switch (maturity)
		{
			case Naive:													// if this naive cell does not receive a signal 1 before some time, then it will become apoptotic
				if(sim.schedule.getTime() >= timeOfApoptotisNaiveMaturity) 
				{
					timeOfApoptotisNaiveMaturity = Double.MAX_VALUE;	// cell enters apoptosis, this timer is set to infinity. 
					becomeApoptotic(sim);								// handles the specifics of entering apoptosis. 
				}
				break;
		
			case Partial:												// if a partially mature cell does not receive signal two in time, it dies of neglect. 
				if(sim.schedule.getTime() >= timeOfApoptosisPartialMaturity)
				{ 
					timeOfApoptosisPartialMaturity = Double.MAX_VALUE;	// to avoid unnecessary computation be reentering this if statement every step.
					becomeApoptotic(sim);
				}
				break;
				
			case Proliferating:											// note that proliferative cells cannot die of neglect, they will eventually become effectors. 
				if(timeOfProliferation != Double.MAX_VALUE && boundToAPC == false)	// if the time to proliferation is set, but cell is not attached to an APC ... 
				{	
					/* binding to APC lost. A decision is made as to whether the daughter cell is to be spawned or lost, depending on how much of the required APC-parent contact time
					 * was completed before the loss of binding. 
					 * 
					 *  'cutoffBasedOnMean' below holds the 'safe' time for saving a naive daughter cell if the binding with the APC is lost. If the time to the spawning event is smaller
					 *  than this threshold time, then spawning will still take place. Otherwise, the daughter cell spawning event is cancelled. 
					 */
					final double timeRemaining = timeOfProliferation - sim.schedule.getTime();
					final double cutoffBasedOnMean = retrieveProliferationMean() * cutoffThresholdForProlifWhenBindingLost;
			
					if(timeRemaining > cutoffBasedOnMean) {				// if cutoff threshold has passed.
						timeOfProliferation = Double.MAX_VALUE;			// then reset the proliferation timer - we need to be continually attached to an APC!						
					}
					// otherwise, do not cancel the spawning event.					 
				}
				
				if(sim.schedule.getTime() >= timeOfProliferation)
				{
					spawnDaughterCell(sim);								// proliferate, hence releasing a new naive daughter cell
					timeOfProliferation = Double.MAX_VALUE;				// clear the timer.
				}				
				if(sim.schedule.getTime() >= timeOfBecomeEffector)
				{
					becomeEffector(sim);								// differentiate into an effector cell
				}
				break;
				
			case Effector:												// effector cells can die of antigen induced cell death (AICD) or neglect through lack of sufficient MHC:TCR interaction. 
				if(sim.schedule.getTime() >= timeOfAICD)				// if time of AICD has passed...
				{
					becomeApoptotic(sim);
				}
				/* if some period of time (defined elsewhere) passes since this activated T cell last received stimulation in the form of a local activation, then it will die */ 
				else if(sim.schedule.getTime() >= timeEffectorApoptotoisFromNeglect)
				{
					becomeApoptotic(sim);
				}
				break;
		}
	}

	/**
	 * Method handles the proliferation of a daughter cell. causes this T cell to proliferate. A daughter cell is produced in the compartment at the same location as the parent cell.
	 * 
	 * The spawning of daughter cells is instigated within this abstract class. Since there are multiple species of T cell, represented by concrete subclasses. Code within this
	 * abstract class cannot determine which concrete class it belongs to (at least, I have no found a satisfactory way of doing so), hence all concrete classes must override this 
	 * method. 
	 */
	protected abstract void spawnDaughterCell(TregSimulation sim);

	
	/**
	 * Method handles interaction between this T cell and the specified APC. The structure of this interaction is common to all T cells. Specific behaviours can be added through
	 * overriding of certain methods. 
	 */
	public void interactWithAPC(TregSimulation sim, APC apc)
	{
		 
		if(maturity == Maturity.Naive)
		{
			if(attemptToInstigateSpecificityBasedBinding(apc))						// if the APC is expressing the required MHC-peptide complexes. This method will conduct a specificity-considered attempt at binding (binding not necessarily 100% successful). 
			{
				/* the contents of this if statement allow for a naive cell interacting with an immunogenic apc to immediately begin proliferating rather
				 * to pass through the partially activated state. This is important in that the interaction with an apc when partially activated (in the if
				 * statement below) includes another specificity based check for binding with an APC. Executing this twice would mean that a naive cell can
				 * encounter an immunogenic apc and become partially activated rather than fully activated, which is deemed to be incorrect behaviour.  
				 */
				if(apc.getExpressing_CoStimulatory() == true)
				{	// this is an immunogenic APC. Therefore, bypass the partially activated state.
					becomeProliferating(sim);
				} else {
					// this APC is tolerogenic, become Partially activated. 
					maturity = Maturity.Partial;	
					timeOfApoptotisNaiveMaturity = Double.MAX_VALUE;											// timer for death by neglect on a naive T cell no longer applicable in partially activated state. 
					timeOfApoptosisPartialMaturity = calculateTimeOfApoptosisPartialMaturity();					// set timer for death by neglect for a partially activated T cell. 
				}
			}
		}
		if(maturity == Maturity.Partial)
		{
			if(apc.getExpressing_CoStimulatory() && attemptToInstigateSpecificityBasedBinding(apc))  			// if the APC is licensed, and expressing MHC for which we are specific																		
			{
				becomeProliferating(sim);					//then receive signal 2
			}
		}
		if(maturity == Maturity.Proliferating)				
		{	/* note that this code allows for a T cell to become proliferative, lose its binding with an APC (because the APC dies), and then move off to find another APC, then form a binding with that APC in a manner that is not probabilistically
		 	 * dependent on the specificity of the T cell. This is a very minor simulation artifact. 
			 */
			if( specificForThisCell(apc) )					// simply checks for the relevant MHC:peptide complexes, there is no specificity related chance that a binding won't form. 
				boundToAPC = true;	
			
			if(timeOfProliferation == Double.MAX_VALUE)					// if a proliferation time has not already been set (we check to avoid wiping out an existing proliferative activity.)
				timeOfProliferation = calculateTimeOfProliferation();	// then set one following this interaction with an MHC expressing cell.
		}
		
		/* we 'else if' here because we do not want to go all the way from Naive to performing effector function in one go. Actually, that is supremely unlikely to happen anyway, since there are timers governing the 
		 * transitions between states.
		 */ 
		else if(maturity == Maturity.Effector && attemptToInstigateSpecificityBasedBinding(apc))		// if we are activated, AND if this APC expressed MHC for which we are specific. (binding instigation is probabilistic based on specificity).		
		{	
			effectorTCellStimulated(sim);					// perform some housekeeping common to all effector T cells that interact with APC. 
			interactWithAPCEffector(apc);					// the rules for interaction with an APC differ between types of activated T cells.
		}
		if(maturity == Maturity.Apoptotic)					// note that for CD8Treg and CD4Tregs this code is not executed, since those cells are simply removed from the simulation when they become apoptotic. Th cells are phagocytosed since they contain peptides of interest. 
		{
			apc.phagocytoseCell(sim, this);					// APC phagocytoses this apoptotic T cell. 
		}
	}
	
	/**
	 * The operations that maintain the shift from a partially activated cell into a fully activated cell. Can be overridden if additional functionality 
	 * is to be provided - however, be sure to call super.becomeActivated(); first. These operations are essential.
	 */
	protected void becomeProliferating(TregSimulation sim)
	{
		maturity = Maturity.Proliferating;	
		boundToAPC = true;									// cell adheres to APC and does not move around the compartment as a result. 
		timeOfApoptosisPartialMaturity = Double.MAX_VALUE;	// cell has passed partial maturity, set this time to infinity. 
		timeOfProliferation = Double.MAX_VALUE;				// proliferation is induced only by subsequent MHC interaction on an already activated T cell.
		timeOfBecomeEffector = calculateTimeOfBecomeEffector(sim);	// calculate the absolute time at which this cell will differentiate into an effector cell.
		
		primingDL.logPrimingEvent(this);					// this cell is being primed, therefore, log the event. 
	}
	
	/**
	 * This method handles the transition from a proliferating cell into an effector cell. The effector cell begins migration out of the secondary lymphoid organ, and has AICD time set.
	 * This method can be overridden to provide additional T cell species-specific functionality, but this superclass method must still be called. 
	 */
	protected void becomeEffector(TregSimulation sim)
	{
		maturity = Maturity.Effector;						// reach effector status.
		boundToAPC = false;									// effector T cell loses adhesion molecules that bind it to APC and is free to migrate around compartment. 
		timeOfProliferation = Double.MAX_VALUE;				// proliferation does not occur once the cell has reached effector status.
		timeOfBecomeEffector = Double.MAX_VALUE;			// can only become an effector once, clear the timer. 
		timeOfAICD = calculateTimeOfAICD(sim);				// calculate time at which AICD will occur.
		effectorFunctionFromLocalActivation = false;		// effector function is disabled until the cell is locally activated. 
		timeEffectorApoptotoisFromNeglect = calculateTimeEffectorApoptotoisFromNeglect(sim);	// set timer governing death by neglect in effector cell state. 
		timeEndLocalActivationDelay = calculateTimeEndLocalActivationDelay(sim);	// there is a delay before a cell is susceptible to local activation, this prevents local activation from occuring in the secondary lymphoid organs. 
	}
	
	/**
	 * Method can be overridden to perform any state cleanup when a cell enters an apoptotic state. 
	 */
	protected void becomeApoptotic(TregSimulation sim)
	{	
		maturity = Maturity.Apoptotic;						// become apoptotic
		timeOfAICD = Double.MAX_VALUE;						// save unnecessary computation of reentering this if statement.
		timeOfProliferation = Double.MAX_VALUE;				// cell will no longer proliferate
		timeEffectorApoptotoisFromNeglect = Double.MAX_VALUE;	// resetting of timers (can save computational effort in later 'steps').
		timeEndLocalActivationDelay = Double.MAX_VALUE;		// resetting of timers (can save computational effort in later 'steps').
		
		removeCellFromSimulation(sim);
	}
	
	/**
	 * Method handles operations common to all T cells when performing their effector functions. It handles the case where an effector T cell is stimulated through its TCR complex, which might be through
	 * APC interaction, or with another MHC presenting cell (in the case of CD8 T cells).  
	 * This method can be overridden to provide additional T cell species-specific functionality, but this superclass method must still be called. 
	 */
	protected void effectorTCellStimulated(TregSimulation sim)
	{
		// the absolute time at which the effector cell is sensitive to local activation following its differentiation into an effector cell must have passed. Otherwise do nothing.
		// This prevents the T cells from receiving local activation whilst in the SLO (for example). There is a delay before local activation can take place. 
		if(TregSimulation.sim.schedule.getTime() >= timeEndLocalActivationDelay) 					
		{
			timeEffectorApoptotoisFromNeglect = calculateTimeEffectorApoptotoisFromNeglect(sim); 	// reset the time at which the cell will die from neglect.
			effectorFunctionFromLocalActivation = true;												// cell is capable of performing effector function (for now). 
		}
	}
	
	/**
	 * By default T cells are not phagocytosed. Because they do not (with exceptions) contain any peptides of significance to priming of other cells, we do not need to worry about
	 * phagocytosing them. If this is not the case (as in CD4Th cells), then this method should be overridden. 
	 */
	protected void removeCellFromSimulation(TregSimulation sim)
	{
		isDead = true;
		TregSimulation.sim.removeFromSimulationSchedule(this);		// critical, remove this cell from the simulation's schedule.
		compartment.removeCellFollowingDeath(this);					// cell removes itself from the compartment. 
	}
	
	/**
	 * Method returns an absolute time at which a naive T cells will enter apoptosis through neglect. The absolute time is placed some interval into the future from the current simulation time. The size of the interval is 
	 * stochastically governed based on a gaussian distribution. A mean and a standard deviation together describe the distribution, and are stored as class level parameters. Note that the parameter name indicates standard
	 * deviation, however, the parameter is actually TWICE the standard deviation. 
	 * @return
	 */
	private double calculateTimeOfApoptotisNaiveMaturity()
	{
		double interval = TregSimulation.sim.random.nextGaussian();
		interval *= (retrieveApoptosisNaiveStdDev()/ 2.0);			// one random.gaussian stdDev = 2* stdDevs for apoptosis in partial maturity.
		interval += retrieveApoptosisNaiveMean();					// shift the mean from 0.0 to what we desire.
		return interval + TregSimulation.sim.schedule.getTime();	// convert into absolute time and return.
	}
	
	/**
	 * Calculates the time at which this cell will die if it remains partial mature without receiving signal 2. Is determined probabilistically around a 
	 * gaussian distribution.
	 */
	private double calculateTimeOfApoptosisPartialMaturity()
	{
		double interval = TregSimulation.sim.random.nextGaussian();
		interval *= (retrieveApoptosisPartialMaturityStdDev()/ 2.0);	// one random.gaussian stdDev = 2* stdDevs for apoptosis in partial maturity.
		interval += retrieveApoptosisPartialMaturityMean();				// shift the mean from 0.0 to what we desire.
		return interval + TregSimulation.sim.schedule.getTime();		// convert into absolute time and return.
	}

	protected double calculateTimeOfProliferation()
	{
		TregSimulation sim = TregSimulation.sim;
		double interval = sim.random.nextGaussian();
		interval *= (retrieveProliferationStdDev() / 2.0);	// one random.gaussian stdDev = 2*standard deviations for proliferation.
		interval += retrieveProliferationMean();								// shift the mean from 0.0 to what we desire.
		return interval + sim.schedule.getTime();						// convert into absolute time, and return.
	}
	
	private double calculateTimeOfAICD(TregSimulation sim)
	{
		double interval = sim.random.nextGaussian();
		interval *= (retrieveAICDStdDev() / 2.0);
		interval += retrieveAICDMean();
		return interval + sim.schedule.getTime();
	}
	
	private double calculateTimeOfBecomeEffector(TregSimulation sim)
	{
		double interval = sim.random.nextGaussian();
		interval *= (retrieveEffectorTimeStdDev() / 2.0);
		interval += retrieveEffectorTimeMean();
		return interval + sim.schedule.getTime();
	}
	
	/**
	 * Returns an absolute time in the future at which an effector cell will die of neglect. There is no gaussian distribution governing the interval into the future. T cells must be (repeatedly) locally activated in order
	 * to escape death by neglect.   
	 */
	protected double calculateTimeEffectorApoptotoisFromNeglect(TregSimulation sim)
	{
		return sim.schedule.getTime() + timeLocalActivationInducedEffectorFunctionFor;
	}
	
	/**
	 * Returns an absolute time in the future at which an effector cell will be susceptible to local activation. There is no stochastic distribution for these times, the interval into the future is deterministically determined.   
	 */
	protected double calculateTimeEndLocalActivationDelay(TregSimulation sim)
	{
		return sim.schedule.getTime() + timeLocalActivationDelay;
	}
	
	
	/**
	 * Depending on whether the effector T cell is CD4 or CD8 the result of interaction with an APC will be slightly different.
	 * 
	 * This should be overridden in subclasses to provide specific effector functionality. It may be assumed that the given APC expresses MHC molecules for 
	 * which this T cell is specific.  
	 */
	protected abstract void interactWithAPCEffector(APC apc);
	
	/** 
	 * javabean getters and setters, so that these times appear in the inspectors 
	 */
	public double getTimeOfApopotisPartialMaturity()
	{	return timeOfApoptosisPartialMaturity;	}
	
	public double getTimeOfProliferation()
	{	return timeOfProliferation; }
	
	public double getTimeOfBecomeEffector()
	{	return timeOfBecomeEffector; 	}
	
	public double getTimeEffectorApoptotoisFromNeglect()
	{	return timeEffectorApoptotoisFromNeglect;	}
	
	/**
	 * return in absolute time the time at which AICD will take place. 
	 */
	public double getTimeOfAICD()
	{	return timeOfAICD;	}
	
	public Maturity getMaturity()
	{		return maturity;		}
	
	public boolean getBoundToAPC()
	{	return boundToAPC;		}
	

	/**
	 * Method is called when this T cell collides with another cell, and attempts to instigate a binding with it. Binding instigation is specificity dependent, meaning that there is a specificity-determined probability
	 * that the binding will not succeed.  
	 * Method can be overridden (as in CD8Treg) if there are other factors to be considered in the decision to make a successful binding. 
	 */
	protected boolean attemptToInstigateSpecificityBasedBinding(Cell cell)
	{
		if( specificForThisCell(cell) )	
		{
			double probabilityOfBinding = specificity;			// binding instigation with all cells is dependent on specificity
			
			// if CD200 negative signalng on DCs is active, then reduce probability of binding according to how suppressed DC is. 
			if( DendriticCell.cd200CytokineSwitching == true || DendriticCell.cd200GradualReductionPrimingCapacity == true)
				if(cell instanceof DendriticCell)
					probabilityOfBinding *= ((DendriticCell)cell).getCD200PrimingCapacity();
			
												// is it expressing these MHC-peptides at the moment?			
			return (TregSimulation.sim.random.nextDouble() <= probabilityOfBinding);	// probabilistically decide if the binding succeeds.
		}
		return false;														// default behaviour otherwise. 
	}
	
	/**
	 * This is only to be used in maintaining a binding to an APC, NOT in deciding if the binding is to be instigated. Different concrete T cells are specific for different MHC:peptide complexes, and as such this method
	 * must be overridden by each concrete T cell class in order to provide that function. 
	 */
	protected abstract boolean specificForThisCell(Cell cell);
	
	/**
	 * Returns true when this cell is in an apoptotic state. The rules are the same for all T cells.
	 */
	public boolean isApoptotic() 
	{
		return (maturity == Maturity.Apoptotic);
	}
	
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("TCell").item(0);
		
		proliferationStdDev = Double.parseDouble(pE.getElementsByTagName("proliferationStdDev").item(0).getTextContent());
		proliferationMean = Double.parseDouble(pE.getElementsByTagName("proliferationMean").item(0).getTextContent());
		cutoffThresholdForProlifWhenBindingLost = Double.parseDouble(pE.getElementsByTagName("cutoffThresholdForProlifWhenBindingLost").item(0).getTextContent());
		
		AICDStdDev = Double.parseDouble(pE.getElementsByTagName("AICDStdDev").item(0).getTextContent());
		AICDMean = Double.parseDouble(pE.getElementsByTagName("AICDMean").item(0).getTextContent());
		
		becomeEffectorMean = Double.parseDouble(pE.getElementsByTagName("becomeEffectorMean").item(0).getTextContent());
		becomeEffectorStdDev = Double.parseDouble(pE.getElementsByTagName("becomeEffectorStdDev").item(0).getTextContent()); 
		
		apoptosisNaiveMean = Double.parseDouble(pE.getElementsByTagName("apoptosisNaiveMean").item(0).getTextContent());
		apoptosisNaiveStdDev = Double.parseDouble(pE.getElementsByTagName("apoptosisNaiveMean").item(0).getTextContent());
		
		apoptosisPartialMaturityMean = Double.parseDouble(pE.getElementsByTagName("apoptosisPartialMaturityMean").item(0).getTextContent());
		apoptosisPartialMaturityStdDev = Double.parseDouble(pE.getElementsByTagName("apoptosisPartialMaturityStdDev").item(0).getTextContent());
		
		cellsPerGridspace = Integer.parseInt(pE.getElementsByTagName("cellsPerGridspace").item(0).getTextContent());
		
		specificityUpperLimit = Double.parseDouble(pE.getElementsByTagName("specificityUpperLimit").item(0).getTextContent());
		specificityLowerLimit = Double.parseDouble(pE.getElementsByTagName("specificityLowerLimit").item(0).getTextContent());
		
		timeLocalActivationInducedEffectorFunctionFor = Double.parseDouble(pE.getElementsByTagName("timeLocalActivationInducedEffectorFunctionFor").item(0).getTextContent());
		timeLocalActivationDelay = Double.parseDouble(pE.getElementsByTagName("timeLocalActivationDelay").item(0).getTextContent());
		
		NodeList eqSpatialTestNL = pE.getElementsByTagName("spatialTestEquals");
		if(eqSpatialTestNL.getLength() > 0)
		{
			spatialTestEquals = Boolean.parseBoolean(eqSpatialTestNL.item(0).getTextContent()); 
		} 

	}
	
}

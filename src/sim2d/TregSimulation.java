package sim2d;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.network.Network;
import sim2d.cell.impl.APC_Impl;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.CNSMacrophage;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;
import sim2d.compartment.CLN2D;
import sim2d.compartment.CNS2D;
import sim2d.compartment.Circulation2D;
import sim2d.compartment.Compartment;
import sim2d.compartment.SLO2D;
import sim2d.compartment.Spleen2D;
import sim2d.compartment.SpleenSplenectomy2D;
import sim2d.dataCollection.dataLoggers.CD4Th1ApoptosisedDataLogger;
import sim2d.dataCollection.dataLoggers.DCApoptosedPeptidePresentationDataLogger;
import sim2d.dataCollection.dataLoggers.NeuronsKilledDataLogger;
import sim2d.dataCollection.dataLoggers.TCellPrimingLocationDataLogger;
import sim2d.molecule.Molecule;

/**
 * This is the top level class that runs a simulation, called from the driver classes that are used for automation (such as can be found in the package sim2d.experiment).
 * 
 * The simulation is set-up and torn-down in this class. This class runs the simulation absent of any GUI related function - the reason that a lot of the variables here (and
 * elsewhere in the project) are public is so that the GUI can be placed on top of simulation logic without any need to change the logic to accommodate it. It is a MASON design pattern.   
 *  
 * @author mark
 * @version updated by Richard Williams for DC Mutual Exclusive antigen presentation and Removal of CD4Treg Functionality
 *
 */
public class TregSimulation extends SimState
{
	public static TregSimulation sim;							// static so that other objects can get hold of key items such as the schedule. 
	
	// TODO remove this, always two dimensional. 
	public static enum Dimension { TwoD }						// this is only used to set up the correct compartment types.
	public Dimension dimension;									// this is only used to set up the correct compartment types.
	
	public static Document parameters;							// java representation of the XML document holding parameters for this simulation run. 
	
	public static double timeSlice;								// how long, in hours, that a simulation timestep corresponds to.
	
	public static enum ImmunizationType {Linear, Exponential}	// The two immunization mechanisms that exist for the simulation. 
	
	/* 
	 * 'stoppables' keeps track of all the stoppable objects associated with steppable objects that are scheduled to repeat during the simulation.
	 * Methods are provided below so that new events (such as cells) can be scheduled, and those that die can be removed. Steppables and Stoppables are MASON housekeeping 
	 * artefacts. 
	 */
	private Map<Steppable, Stoppable> stoppables = new HashMap<Steppable, Stoppable>();
	public int getNumStoppables() {	return stoppables.size();	}
	
	/* We order compartments to be scheduled before cells. These figures relate to the Schedule MASON class. */
	public static int cellsOrdering = 0;						// the ordering at which cells scheduled. Cells first, makes the visuals match behaviour better (else they lag behind)
	public static int compartmentsOrdering = 1;					// the ordering at which compartments are scheduled.
	
	
	public Network compartmentsNetwork;
	
	public Compartment cns;										// central nervous system
	public Compartment circulation;								// circulatory system	
	public Compartment slo;										// secondary lymphoid organ
	public Compartment cln;										// cervical lymph node
	public Compartment spleen;									// the spleen
	
	private static boolean immunize;							// boolean value dictates whether an immunization is to take place at time zero. (this is mostly for testing, there is no reason for it not to be).
	
	private static boolean secondImmunization = false;			// default case is not to have a second immunization. 
	private static double secondImmunizationStartTime;			// if there is to be a second immunization, this variable dictates when it should happen. 
	
	
	/* linear immunization. An initial number of DCs are placed into the SLO compartment as the result of immunization, thereafter a certain (linearly decreasing) additional DCs
	 * appear in the SLO at certain periods of time. 
	 * For a more detailed view of how this immunization works, see @Immunization_Linear 
	 */
	private static double immunizationTime;                // time at which first immunization is to take place
	private static double immunizationLinearFreq;				// how frequently this steppable object will be scheduled. Equivalent to how frequently immunized DC influx occurs.
	private static int immunizationLinearInitial;				// the initial number of cells that will influx at time of immunization (time zero) - other cells may follow. 
	private static double immunizationLinearDC0;				// The intial number of influxing DCs - this is NOT the same as the initial influx.
	private static double immunizationLinearGradient;			// how quickly the number of DCs influxing will decrease. should be a negative number (else the number will increase)
			
	private static int numCD4Th;								// the basal level of CD4Th (naive - no polarization) cells in the simulation.
	private static int numCD4Treg;								// the basal level of naive CD4Treg cells in the simulation.
	private static int numCD8Treg;								// the basal level of naive CD8Treg cells in the simulation.
	private static int numCNS;									// the number of neurons in the CNS. This is homeostatically maintained. 
	private static int numCNSMacrophage;						// the number of CNS macrophages (microglia) in the CNS. This is homeostatically maintained. 
	private static int numDC;									// the number of DCs that reside in the SLO and CLN compartments. This is homeostatically maintained. 
	private static int numDCCNS;								// dendritic cells in the CNS compartment. This is homeostatically maintained. 
	private static int numDCSpleen;								// number of resident DCs in the spleen compartment. This is homeostatically maintained.  
	
	private static boolean splenectomy = false;					// when turned on, this turns simulation behaviour into a splenectomy experiment. 
																// all DCs are removed from the spleen, any cells that would enter it leave immediately. And if any of those cells
																// are apoptotic, they are simply removed from the simulation immediately. 
	public static boolean cd4TregAbrogation = false;			// represents an experimental setup in which CD4Treg cells are removed from the simulation. 
																// help to CD8Tregs is constitutive when this variable is set to true. 
	
	/*
	 * Java bean getters and setters, these are picked up by the Console and displayed on the 'Model' tab. They are used for GUI purposes only. 
	 */
	public boolean 		getImmunize() 					{	return immunize;	}
	public void 		setImmunize(boolean val)		{	immunize = val;		}
	
	public int 			getCD4Th()						{	return numCD4Th;	}
	public void 		setCD4Th(int val)				{ 	numCD4Th = val;		}	
	public int 			getCD4Treg()					{	return numCD4Treg;	}
	public void 		setCD4Treg(int val)				{ 	numCD4Treg = val;	}
	public int 			getCD8Treg()					{	return numCD8Treg;	}
	public void 		setCD8Treg(int val)				{ 	numCD8Treg = val;	}
	public int 			getCNS()						{	return numCNS;		}
	public void 		setCNS(int val)					{ 	numCNS = val;		}
	public int 			getCNSMacrophage()				{	return numCNSMacrophage;		}
	public void 		setCNSMacrophage(int val)		{ 	numCNSMacrophage = val;		}
	public int 			getDC()							{	return numDC;		}
	public void 		setDC(int val)					{ 	numDC = val;		}
	
	/* if a simulation run takes longer than timeout seconds to complete, then we halt the simulation and throw and exception. If this is caught by a driver, than the experiment/analysis can continue rather than
	 * the whole test being stopped. This might be necessary (for example) when running sensitivity analysis where parameter sets that cause the simulation to run out of physical space for cells can occur. 
	 * Under most conditions this should never be a problem, and as such, the timeout can be set to a very high value. 
	 */
	private int timeout = Integer.MAX_VALUE;							// as default there is no timeout. 									
	
	/**
	 * Constructors. Takes a specified seed. Specifying the seed is important - although java will seed experiments based on the internal clock, when running batch experiments on a cluster
	 * it is very possible that two machines will start with the same seed (I have explicitly confirmed this). When gathering data to form distributions representing experiments, it is critical 
	 * that you do not run simulations with the same seed, they skew the distributions. Hence, the seed must be explicitly provided. 
	 */
    public TregSimulation (long seed, Dimension d, Document params, int timeoutSeconds)
    {
        super(seed);
        sim = this;											// singleton pattern
        dimension = d;
        parameters = params;
    	timeout = timeoutSeconds * 1000;					// convert from seconds into miliseconds. 
    }
        
    /**
     * This is used for the @Treg2DSim_GUI. It allows a different parameters file to be set before running the simulation.
     * In the case of @Treg2DSim_GUI, it simply forces TregSimulation to read the same parameters file
     * again so that any chances made to the file by the user can be reflected in the simulation without having to 
     * restart the GUI.
     * @param params
     */
    public void setParametersDocument(Document params)
    {
    	parameters = params;
    }
    
    /**
     * Runs the simulation. This is the correct entry point into the simulation, the loading of parameters is done, and methods are called in the correct order.
     * 
     * @events though all @Steppable objects related to the simulations logic are set up internally, it is sometimes desirable to explicitly add others. As an example, the 
     * data stores that log simulation progression might be added; since they are not core to the simulations logic (but are instead observers that monitor and record simulation
     * progression for later analysis), they are not created or scheduled in the contents of this method. 
     * 
     * Note that some of the data loggers are created as part of the simulation logic. There is a separation made between the objects that extract information directly from simulation logic (such
     * as querying a cell type for some information), which can require the simulation logic to know about the loggers, and storing that information for however long the user of the system
     * requires (in this case, the simulation logic does not need to interact directly, so data stores are not instantiated at any point following this method call).  
     * 
     */
    public void run(double endTime, Steppable... events)
    {    	
        start();											// sets up the simulation. 
        
        // schedule the events into the schedule.
        for(Steppable event : events)
        {
        	addToSimulationScheduleRepeating(Schedule.EPOCH, 10, event, 1.0);
        }
        
        long steps;
        double time;
        do
        {            
            if (!sim.schedule.step(sim)) 			// performs the step, and if return is false, stops looping.
                break;
            steps = sim.schedule.getSteps();		// How many steps have been performed?
            time = sim.schedule.getTime();			// retrieve the current time in the simulation.  
            
        } while(time <= endTime);					// stopping condition. 
        
        finish();									// tears down the simulation
    }
    
    
    /**
     * Method is called to start the simulation. The bodily compartments are set up and connected together. 
     * The compartments are populated with cells. If an immunization is required, then a @Steppable object that performs the immunization at the 
     * correct time is created and scheduled.
     */
    public void start()
    {
    	super.start();														// call supertype's start method. 
		TregSimulation.sim.setupSimulationParameters();						// essential that we do this here.
    	compartmentsNetwork = new Network();								// compartments in the simulation are connected together as a MASON network. 

    	/* Set up the correct type of compartments, based on the requested dimensions. */
    	switch(dimension)
    	{
    	case TwoD:
    		circulation = new Circulation2D(this);
    		cln = new CLN2D(this);
    	    cns = new CNS2D(this);
    	    slo = new SLO2D(this);	
    	    if(splenectomy)													// if this is a splenectomy experiment, then create a splenectomy spleen with specialised functionality. 
    	    	spleen = new SpleenSplenectomy2D(this);
    	    else	
    	    	spleen = new Spleen2D(this);								// otherwise create a normal spleen compartment. 
    	}
    	     	
    	// treat our compartments as nodes in a network
    	compartmentsNetwork.addNode(cns);
    	compartmentsNetwork.addNode(cln);
    	compartmentsNetwork.addNode(circulation);
    	compartmentsNetwork.addNode(slo);    	
  		compartmentsNetwork.addNode(spleen);

   	
    	
    	/* connect up the network, as edges.
    	 * 
    	 * Circulation leads into Circulation, CNS, CLN, SLO, Spleen. NOTE, cells leaving the circulation can end up back in the circulation.
    	 * CNS leads only into CLN.
    	 * CLN leads only into the Circulation.
  		 * SLO leads only into the Circulation. 
  		 * Spleen leads only into the Circulation. 
    	 */
    	compartmentsNetwork.addEdge(circulation, circulation, null);	// circulation leads back into the circulation. 
    	
    	compartmentsNetwork.addEdge(circulation, cns, null); 
    	
    	compartmentsNetwork.addEdge(cns, cln, null);
    	compartmentsNetwork.addEdge(circulation, cln, null);
    	compartmentsNetwork.addEdge(cln, circulation, null);
    		
    	compartmentsNetwork.addEdge(circulation, slo, null);
    	compartmentsNetwork.addEdge(slo, circulation, null);

   		compartmentsNetwork.addEdge(circulation, spleen, null);
   		compartmentsNetwork.addEdge(spleen, circulation, null);

    	
    	/* schedule the compartments with the scheduler. That will handle things like cell movements and cytokine diffusions. */
    	addCompartmentToSimulationScheduleRepeatingEpoch(cns);
    	addCompartmentToSimulationScheduleRepeatingEpoch(cln);	
    	addCompartmentToSimulationScheduleRepeatingEpoch(circulation);		
    	addCompartmentToSimulationScheduleRepeatingEpoch(slo);
   		addCompartmentToSimulationScheduleRepeatingEpoch(spleen);
    	
    	
    	populateCompartments();									// populate the compartments with cells
    	    	
    	/* create and schedule a Steppable object to perform the immunization at the end of the user specified time */  	
    	if(immunize == true)										// if immunization is to take place. 
    	{
    		new Immunization_Linear(immunizationLinearInitial, immunizationLinearDC0, immunizationLinearGradient, immunizationTime, immunizationLinearFreq);    		
    	}
    	
    	/* Same as above, but this allows testing of the hypothesis that the persistence of Tregs in the system will allow for a faster secondary response to a second immunization with MBP */
    	if(secondImmunization == true)
    	{
    		new Immunization_Linear(immunizationLinearInitial, immunizationLinearDC0, immunizationLinearGradient, secondImmunizationStartTime, immunizationLinearFreq);    		
    	}
    		
    	/* create and schedule the naive T cell generators
    	 * 
    	 *  The naive T cell generators homeostatically maintain a minimum number of cells within the system. In absence of immunization there will by some basal level of these cells. 
    	 *  Of course, when immunization occurs, cells will proliferate etc, but these basal levels of naive cell generation and insertion into the simulation run at all times.  
    	 */
    	final double timesliceForNaiveTCellGenerators = timeSlice;	// in hours, how frequently the generators are to be run. 
    	final int orderingForNaiveTCellGenerators = 2;				// what ordering in the schedule the generators will be run at. 
    	addToSimulationScheduleRepeating(Schedule.EPOCH, orderingForNaiveTCellGenerators, new CD4THelper.NaiveCD4THelperGenerator(timesliceForNaiveTCellGenerators, numCD4Th), timesliceForNaiveTCellGenerators);
    	if(cd4TregAbrogation == false)								// homeostatic addition of CD4Tregs should not occur if CD4Treg abrogation experiment is being executed. 
    	{
    		addToSimulationScheduleRepeating(Schedule.EPOCH, orderingForNaiveTCellGenerators, new CD4Treg.NaiveCD4TregGenerator(timesliceForNaiveTCellGenerators, numCD4Treg), timesliceForNaiveTCellGenerators);
    	}
    	addToSimulationScheduleRepeating(Schedule.EPOCH, orderingForNaiveTCellGenerators, new CD8Treg.NaiveCD8TregGenerator(timesliceForNaiveTCellGenerators, numCD8Treg), timesliceForNaiveTCellGenerators);
    	
    	/* The mechanism that handles shutting down the simulation if a timeout has occurred. */
    	addToSimulationScheduleRepeating(Schedule.EPOCH, 3, 
    			/**
    			 * Anonymous class that checks for how long the simulation has been running for and throws a runtime exception if the timeout is exceeded. 
    			 */
    			new Steppable() 
    			{
					private long startTime = System.currentTimeMillis();
					
					@Override
					public void step(SimState state) 
					{
//						final long timeNow = System.currentTimeMillis();
//						if(timeNow > (startTime + timeout))
//						{
//							/* It is not proper use of java to throw a runtime exception here, since the simulation timing out is something that should
//							 * be dealt with more properly. However, in order to throw a more appropriate user-defined exception, I would need to change the
//							 * method signature of start(), and consequently would have to change the corresponding method signature of MASON's start() method.
//							 * I do not wish to change mason, since I want this simulation code to be independently compatible with any user's mason install,
//							 * not just the one that I have amended.   
//							 */ 
//							throw new RuntimeException("simulation run timed out. Timeout was set at " + timeout);
//						}
						if(totalSimulationCells() > 30000)
						{
							System.out.println("Killing simulation, > 30000 cells in simulation.");
							throw new RuntimeException("Killing simulation, > 30000 cells in simulation.");
						}
					}
				},
			timeSlice);
    	
    	/* create data loggers, and attach them to owner classes */
    	CD8Treg.cd4Th1ApopDL = new CD4Th1ApoptosisedDataLogger();
    	TCell_Impl.primingDL = new TCellPrimingLocationDataLogger();
    	Neuron.neuronsKilledDL = new NeuronsKilledDataLogger();
    	DendriticCellMigrates.peptidePresentationDL = new DCApoptosedPeptidePresentationDataLogger();   // logs which combinations of peptides DCMs have been presenting. 
    }
    
    private int totalSimulationCells()
    {
    	int total = 0;
    	total += circulation.totalCells();
    	total += cns.totalCells();
    	total += cln.totalCells();
    	total += slo.totalCells();
    	if (! splenectomy)
    		total += spleen.totalCells();
    	return total;
    }
    
    /**
     * Method populates the compartments with cells. The basal level of cells are created, since this is what we would expect to see had the simulation been running in absence of immunization
     * for some time. These cells are randomly assigned to the various compartments in the simulation. 
     */
    private void populateCompartments()
    {
    	for(int i = 0; i < numCD4Th; i++)
    		new CD4THelper(randomTCellLocation());    	
    	
    	for(int i = 0; i < numCD4Treg; i++)
    		new CD4Treg(randomTCellLocation());
    	
    	for(int i = 0; i < numCD8Treg; i++)
    		new CD8Treg(randomTCellLocation());
    	
    	for(int i = 0; i < numCNS; i++)
    		new Neuron(cns);
    	
    	for(int i = 0; i < numCNSMacrophage; i++) 
    	{
    		CNSMacrophage.createInitialCNSMacrophage(cns);										// create completely immature CNSM
    	}
    	
    	/* populate SLO and CLN compartments with dendritic cells.	 */
		double proportionImmatureOfTotal = (APC_Impl.retrieveImmatureDurationMean() / (APC_Impl.retrieveImmatureDurationMean() + APC_Impl.retrieveTimeOfDeathMean()));
		int numberImmatureInCompartment = (int) (numDC * proportionImmatureOfTotal);
		int numberMatureInCompartment = numDC - numberImmatureInCompartment;
    	
		for(int i = 0; i < numberImmatureInCompartment; i++) 
		{
			DendriticCell.createInitialPopulationDendriticCellImmature(this, slo) ;
			DendriticCell.createInitialPopulationDendriticCellImmature(this, cln) ;
		}
		for(int i = 0; i < numberMatureInCompartment; i++)
		{
			DendriticCell.createInitialPopulationDendriticCellMature(this, slo);
			DendriticCell.createInitialPopulationDendriticCellMature(this, cln);
		}

		/*
		 * populate the CNS with DCMigrates. This is a little different from the case with SLO and CLN. For starters, 'numDCCNS' is the number of immature DCs in the CNS, NOT the
		 * total number in AND originating from the compartment (as is the case with the other DC related variables). Some of the DCs that are in the CLN will have originated from the
		 * CNS and migrated. The simulation logic dictates that they are still registered with the CNS, even if they are not currently located there.
		 * 
		 * Some proportion of CNS originating cells must already be in the CLN compartments somewhere (to make it appear that the simulation has been running for ages - we do not
		 * want a sudden influx of cells). The number depends on the ratio between how long a DCM remains immature, and how long it remains non-immature.
		 * this variable holds how many DCM cells should be in the CLN comparment, given that the cells originate from the CNS.
		 */   	 
    	for(int i = 0; i < numDCCNS; i++) {
    		DendriticCellMigrates.createDendriticCellMigratesImmature(this, cns);	// schedule the DCMigrages.
    	}
    	
    	final int totalDCM = (int)  Math.round((double)numDCCNS / proportionImmatureOfTotal);	// based on ratio of immature to mature, and how many immature cells are in the CNS
    																							// calculate what the total population of CNS-originating cells is. 
    	final int numberDCMInCLN = totalDCM - numDCCNS;
    	for(int i = 0; i < numberDCMInCLN; i++)
    		DendriticCellMigrates.createDendriticCellMigratesMigrated(this, cns, cln);
    	
    	/* populate the Spleen with DCs. The number of DCs in the spleen is bigger than in ordinary lymph nodes. */ 
    	if(splenectomy) {										// if this is a splenectomy experiment, then remove all DCs from the spleen. 
    		numDCSpleen = 0;
    	}
    	
    	numberImmatureInCompartment = (int) (numDCSpleen * proportionImmatureOfTotal);
		numberMatureInCompartment = numDCSpleen - numberImmatureInCompartment;
		
		for(int i = 0; i < numberImmatureInCompartment; i++)
			DendriticCell.createInitialPopulationDendriticCellImmature(this, spleen);
	
		for(int i = 0; i < numberMatureInCompartment; i++)
			DendriticCell.createInitialPopulationDendriticCellMature(this, spleen);
    }
    /**
     * Method selects a random location in which to put a T cell. Note that we cannot put it in the CNS - naive T cells cannot migrate into the CNS. 
     * @return A randomly selected compartment. 
     */
    private Compartment randomTCellLocation()
    {
    	
    	if(splenectomy == false) {
	    	if(dimension == Dimension.TwoD) {
	    			
				final double rand = sim.random.nextInt(4);
	    		if(rand == 0)
	        		return circulation;
	        	else if (rand == 1)
	        		return cln;
	        	else if (rand == 2)
	        		return slo;
	        	else if (rand == 3)
	        		return spleen;
	        	else
	        		return null;			// should never happen.    		    		
	    	}
    	} else {
	    	final double rand = sim.random.nextInt(3);
    		if(rand == 0)
        		return circulation;
        	else if (rand == 1)
        		return cln;
        	else if (rand == 2)
        		return slo;        	
        	else
        		return null;			// should never happen.
	    }
	    return null;	
    	    	
    }
    
    /**
     * This method removes the provided @Steppable object from the simulation's schedule. Necessary for when (for example) cells are killed.
     */
    public void removeFromSimulationSchedule(Steppable event)
    {
    	if(stoppables.containsKey(event))
    	{
    		Stoppable stop = stoppables.remove(event);
    		stop.stop();											// stop the associated steppable from being scheduled in the schedule.
    	}
    }
    
    /**
     * This method adds the given event to the simulation's schedule, with the given ordering and interval. It keeps track of the event's stoppable object
     * such that the event can later be removed from the schedule. 
     * @param event			what is to be scheduled. 
     * @param ordering		lower orderings are scheduled before higher orderings, given the same time. 
     * @param interval		how frequent this event is to be scheduled. Should (pretty much) always be 1. 
     */
    public void addCellToSimulationScheduleRepeating(Steppable event, double time)
    {    	
    	Stoppable stoppable = schedule.scheduleRepeating(time, cellsOrdering, event, timeSlice);
    	stoppables.put(event, stoppable);							// store the stoppable so that we can get at it later.
    } 
    private void addCompartmentToSimulationScheduleRepeatingEpoch(Steppable event)
    {
    	Stoppable stoppable = schedule.scheduleRepeating(1.0, cellsOrdering, event, timeSlice);		// not sure why we add 1.0, but it is necessary.
    	stoppables.put(event, stoppable);							// store the stoppable so that we can get at it later.
    }
    /**
     * Use of the @Steppable is not a cell or a compartment.
     */
    public void addToSimulationScheduleRepeating(double time, int ordering, Steppable event, double interval)
    {
    	Stoppable stoppable = schedule.scheduleRepeating(time, ordering, event, interval);
    	stoppables.put(event, stoppable);
    }
    
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for the top level simulation.
     * @param params
     */
    private static void loadParameters(Document params)
    {
		
		Element pE = (Element) params.getElementsByTagName("Simulation").item(0);			// collect those items under 'Simulation'

		/* retrieve 'immunize' */
		NodeList immunizeNL = pE.getElementsByTagName("immunize");
		Node immunizeN = immunizeNL.item(0);
		immunize = Boolean.parseBoolean(immunizeN.getTextContent());
		
		NodeList secondImmunizeNL = pE.getElementsByTagName("secondImmunization");
		if(secondImmunizeNL.getLength() > 0)												// this if statement makes allowances for the case that there might not be a node of this description in the parameters xml file. 
		{						
			secondImmunization = Boolean.parseBoolean(secondImmunizeNL.item(0).getTextContent());
			secondImmunizationStartTime = Double.parseDouble(pE.getElementsByTagName("secondImmunizationStartTime").item(0).getTextContent());	
		}
		NodeList cd4TregAbrogationNL = pE.getElementsByTagName("cd4TregAbrogation");
		if(cd4TregAbrogationNL.getLength() > 0)
		{
			cd4TregAbrogation = Boolean.parseBoolean(cd4TregAbrogationNL.item(0).getTextContent());
		}					
				
		immunizationTime = Double.parseDouble(pE.getElementsByTagName("immunizationTime").item(0).getTextContent());
		immunizationLinearFreq = Double.parseDouble(pE.getElementsByTagName("immunizationLinearFreq").item(0).getTextContent());
		immunizationLinearInitial = Integer.parseInt(pE.getElementsByTagName("immunizationLinearInitial").item(0).getTextContent());
		immunizationLinearDC0 = Double.parseDouble(pE.getElementsByTagName("immunizationLinearDC0").item(0).getTextContent());
		immunizationLinearGradient = Double.parseDouble(pE.getElementsByTagName("immunizationLinearGradient").item(0).getTextContent());
		
		/* retrieve default cell populations */;		
		numCD4Th = Integer.parseInt(pE.getElementsByTagName("numCD4Th").item(0).getTextContent());
		if(cd4TregAbrogation == false)
		{
			numCD4Treg = Integer.parseInt(pE.getElementsByTagName("numCD4Treg").item(0).getTextContent());
		} else  {				// CD4Treg abrogation experiment is active. 
			numCD4Treg = 0;
		}		
		numCD8Treg = Integer.parseInt(pE.getElementsByTagName("numCD8Treg").item(0).getTextContent());
		numCNS = Integer.parseInt(pE.getElementsByTagName("numCNS").item(0).getTextContent());
		numCNSMacrophage = Integer.parseInt(pE.getElementsByTagName("numCNSMacrophage").item(0).getTextContent());
		numDC = Integer.parseInt(pE.getElementsByTagName("numDC").item(0).getTextContent());
		numDCCNS = Integer.parseInt(pE.getElementsByTagName("numDCCNS").item(0).getTextContent());
		numDCSpleen = Integer.parseInt(pE.getElementsByTagName("numDCSpleen").item(0).getTextContent());
			
		
		Node e = pE.getElementsByTagName("splenectomy").item(0);						// try and find a 'splenectomy' tag in the parameter file. 		
		if (e != null)																	// if there is one, then set the corresponding parameter in the simulation. 
			splenectomy = Boolean.parseBoolean(e.getTextContent());
		else
			splenectomy = false;
		
		/* retrieve the timeslice */
		timeSlice = Double.parseDouble(pE.getElementsByTagName("timeSlice").item(0).getTextContent());
    }
    
    /**
     * Loads the parameters.xml config file and loads the default parameters for all classes in the simulation. Should be the first thing that is done when
     * running the simulation, with GUI or without. Abstract classes must be called before concrete classes. 
     *
     */
    public void setupSimulationParameters()
    {
        
		/* read in the default parameters for the various classes in the simulation */
        loadParameters(parameters);
        
        TCell_Impl.loadParameters(parameters);				// load parameters from abstract before concrete
        CD4THelper.loadParameters(parameters);
        Th1Polarization.loadParameters(parameters);
        Th2Polarization.loadParameters(parameters);
        CD4Treg.loadParameters(parameters);
        CD8Treg.loadParameters(parameters);
        
        APC_Impl.loadParameters(parameters);				// load parameters from abstract before concrete
        CNSMacrophage.loadParameters(parameters);
        DendriticCell.loadParameters(parameters);
        DendriticCellMigrates.loadParameters(parameters);
        
        Neuron.loadParameters(parameters);
        Molecule.loadParameters(parameters);
        
        Compartment.loadParameters(parameters);
        
        switch(dimension)
        {
        case TwoD:
	        Circulation2D.loadParameters(parameters);
	        CNS2D.loadParameters(parameters);
	        CLN2D.loadParameters(parameters);
	        SLO2D.loadParameters(parameters);
	        Spleen2D.loadParameters(parameters);
	        break;
        }
    }
    
    /**
     * If the simulation runs for longer than a particular (real world, not simulation world) time, representing the strong possibility that a parameter set has been provided for which simulation
     * execution will not progress (for example, proliferation set so high that there is physically no space for cells to be place within the simulation anymore), then this exception is thrown. 
     * 
     * NOTE, that this exception is not actually used anywhere. Throwing a bespoke exception is better use of java, but would require changing mason method signatures, which
     * would require that this simulation be shipped with a specific mason that i have altered, rather than any mason installation. 
     * 
     * @author mark
     *
     */
    public class SimulationTimeoutException extends Exception
    {
    	private double timeout;
    	
    	public SimulationTimeoutException(double timeout)
    	{	
    		this.timeout = timeout;
    	}
    	
    	public String toString()
    	{
    		return "The simulation timed out, timeout was set to : " + timeout;
    	}
    }
}

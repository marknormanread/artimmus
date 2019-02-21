package sim2d.experiment;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.w3c.dom.Document;

import ec.util.MersenneTwisterFast;
import sim2d.TregSimulation;
import sim2d.dataCollection.MultipleRunDataStore;
import sim2d.dataCollection.SingleRunDataStore;
import sim2d.filesystem.FileSystemIO;

public class SingleRun 
{
	public static String topDir = "single_run";
		

	public static String defaultParamFile = "parameters.xml";	// the default file name containing parameters for the sensitivity analysis, should one not be given with the "-param" option. 
	public static String paramFile = defaultParamFile;			// the name of the file, found in 'single_run' directory, that contains the parameters for this simulation run. 						 
	
	private static Random rand = new Random(System.currentTimeMillis());

	
	private static boolean seedManuallySet = false;							// we only want to write experimental (as opposed to run) seeds to the file system if they were manually set, this flag controls that writing operation.
	private static long seed = 0;											// sometimes its necessary to manually specify the random number seed. This is where it is stored.
	private static int runs = 5;
	private static double time = 1000.0;
	private static boolean singleRunRawData = false;
	private static boolean compileMedians = false;							// whether or not java should try to compile median data from single runs. THis works fine for modest number of simulation runs, 
	private static int startNumber = 0;										// but it can run out of memory for very large numbers of long runs. There's a matlab script that will do the same computation though. 
	private static boolean startNumberSet = false;	
	private static int timeOut = 30;										// simulation is terminated after it has executed for this many real-world minutes. 
	
	public static void main(String[] args)
	{
		System.out.println("starting single run.");
		
		
		/* set up a directory structure into which the results of the single run are to be written */
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd'__'HH.mm.ss");	// this is how top level directories should be labelled 
		String timeNow = formatter.format(now);
		String expDir = topDir + File.separator + timeNow;					// build the path to output file. 
		
		/*
		 * Read command line arguments
		 */
		if(args.length != 0)
		{
			for(String s : args)
			{
				System.out.println("parameter = " + s);
			}
			
			for(int i = 0; i < args.length; i= i + 2)
			{
				String command = args[i];
				String arg = args[i+1];
				if(command.equals("-seed"))
				{
					System.out.println("manually setting the seed to : " + arg);
					seedManuallySet = true;
					seed = Long.valueOf(arg);
				}
				if(command.equals("-runs"))
				{
					System.out.println("will perform " + arg + " runs per parameter analysis.");
					runs = Integer.valueOf(arg);
				}
				if(command.equals("-rawData"))
				{
					singleRunRawData = Boolean.valueOf(arg);
					System.out.println("outputting the raw data from individual runs = " + singleRunRawData);
				}
				if(command.equals("-path"))
				{
					expDir = arg;
					System.out.println("working directory for experimentation set to " + arg);
				}
				if(command.equals("-time"))
				{
					time = Double.valueOf(arg);
					System.out.println("simulation runs will be terminated after " + time + " hours.");
				}
				if(command.equals("-param"))
				{
					paramFile = arg;
					System.out.println("singleRun parameters file = " + paramFile);
				}
				if(command.equals("-medians"))							// this option allows the user to turn off compilation of median data. For very large experiments this has crashed Java. 
				{														// there is a matlab script that can do the same functionality. 
					compileMedians = Boolean.parseBoolean(arg);
					System.out.println("compile median runs = " + compileMedians);
				}
				if(command.equals("-startNum"))							// this option allows the user to turn off compilation of median data. For very large experiments this has crashed Java. 
				{														// there is a matlab script that can do the same functionality. 
					startNumber = Integer.parseInt(arg);
					startNumberSet = true;
					System.out.println("simulation data numbering starting at = " + startNumber);
				}
				if(command.equals("-timeOut"))							// this option allows the user to dictate the real-world time limit, in minutes, for the simulation's execution. After this time the simulation will be temrinated. 
				{														
					timeOut = Integer.parseInt(arg);
					
					System.out.println("Real-world time limit for the simulation's execution, before forced termination = " + timeOut);
				}
			}			
		}
		
		boolean success = (new File(expDir)).mkdirs();
	    if (success) {
	      System.out.println("Directory: " + expDir + " created");
	    } else
	    	if(startNumberSet == false) 
	    	{  							
	    		throw new RuntimeException("Single Run: Exception raised when creating the directory structure : " + expDir);
	    	}	
	    	
	    // write configuration and execution files into the experiment directory (for future reference)
	    String paramFileName = paramFile;
	    if(paramFile.contains("/"))													// this code allows for parameter files to be stored in sub-directories. 
	    {
	    	paramFileName = paramFile.substring(paramFile.lastIndexOf("/") + 1);	// will pull out the substring from the character following the last slash to the end. 
	    }
	    String destName = expDir + File.separator + defaultParamFile;
	    File dest = new File(destName);
	    if (!dest.exists())		// don't try to copy the file if it already exists. 
	    	FileSystemIO.copyFile(paramFile, destName);	    	   
	    
	    // Create a MersenneTwister random number generator. This is used to obtain seeds for 
	    // for multiple run invocations of ARTIMMUS. If the user has instigated a single run, and
	    // supplied the seed manually, then this does nothing - the user supplied seed is used in 
	    // the simulation directly.
	    MersenneTwisterFast rand = new MersenneTwisterFast(seed);
	    if (runs == 1)
	    {
	    	if(! seedManuallySet)
	    	{
	    		rand = new MersenneTwisterFast();
		    	seed = rand.nextLong();	    		
	    	}
	    } else {		    
		    if(! seedManuallySet)
		    	rand = new MersenneTwisterFast(seed);
		    else
		    { 
		    	rand = new MersenneTwisterFast();
		    	seed = rand.nextLong();
		    }
	    }
	    	    
	    MultipleRunDataStore multiple = new MultipleRunDataStore();			// will store average results in this. 
	    
	    /* if you open this from within the 'TregSimulation' constructor, then chanses to the filesystem will be felt within the single run. And that would be BAD! */
	    Document params = FileSystemIO.openXMLFile(paramFile);
	    
	    /* perform multiple runs
	     * It is not recommended that large numbers of runs are executed using this mechanism, 
	     * it is more computationally efficient (and gives greater control of seeds) to run 
	     * each individual simulation execution on a cluster as a separate invocation of ARTIMMUS.   
	     */
	    for(int r = 0; r < runs; r++)
	    {
	    	int runNumber = r + startNumber;
	    	System.out.print("starting simulation run number " + (r + 1) + " corresponding to run number " + runNumber );
	    
	    	// if only performing one run, then use the user supplied seed directly in the simulation run. 
	    	// if there are multiple runs to be performed, then use the user supplied seed in the Mersenne Twister
	    	// instance, and pull seeds from that. 
	    	if (runs != 1)
	    	{	seed = rand.nextLong();	}
	    	
	    	SingleRunDataStore dataStore = new SingleRunDataStore();
	    	
	    	// write the seed for this simulation run to the filesystem. 
		    File singleRunSeed = new File(expDir + File.separator + "simRunSeed_" + runNumber);
		    try{
		    	PrintWriter dataOutput = new PrintWriter(singleRunSeed);
		    	dataOutput.print(seed);
		    	dataOutput.close();
		    } catch (Exception e)
		    {	System.out.println("SingleRun: exception whilst writing single run data to filesystem. " + e.getStackTrace());
		    }	
	    	

	    	TregSimulation simulation = new TregSimulation( seed, 
															TregSimulation.Dimension.TwoD,
															params,
															timeOut*60);	// convert timeout to minutes, not seconds.
		    // run the simulation, and pass in the datastore. 
		    long startTime = System.currentTimeMillis();
		    try{
		    	simulation.run(time, dataStore);
		    } catch (Exception e) {
		    	// if something goes wrong (probably a time-out), create this file on the filesystem. 
				File f = new File(expDir + File.separator + "simulationFAILED");
				try{
					f.createNewFile();
				} catch (Exception ee)
				{}
				
		    }
		    System.out.println(" which ran in " + ((System.currentTimeMillis() - startTime)/1000) + " seconds.");
		    
		    if(compileMedians)
		    {
			    // log the results of this single run.
			    multiple.logSingleRunResults(dataStore);
		    }
		    // only write the results of single runs if asked to. 
		    if(singleRunRawData)
		    {
			    // write the results of the single run to the filesystem.
			    File singleRunData = new File(expDir + File.separator + "simOutputData_" + runNumber + ".txt");
			    try{
			    	PrintWriter dataOutput = new PrintWriter(singleRunData);
			    	dataOutput.print(dataStore.compileTableToString());
			    	dataOutput.flush();		// try to ensure that the while output really is written
			    							// I've had some weird occurrences of the file getting lost.
			    							// this might be a problem with the SGE, but I'm not sure. 
			    	dataOutput.close();
			    } catch (Exception e)
			    {	System.out.println("SingleRun: exception whilst writing single run data to filesystem. " + e.getStackTrace());
			    }			    
			    
		    }
	    }

	    /* Not recommended that this be done for large numbers of simulation runs, since it requires 
	     * a lot of memory, and has a tendency to crash if too many runs are attempted. For larger
	     * scale experimentation, run each simulation individually (perhaps using a cluster) and use
	     * the supplied data analysis scripts. There is a matlab script that can handle large numbers
	     * of simulation executions. 
	     */
	    if(compileMedians)
	    {
		    /* Write the multiple data file to the filesystem. */
			File multipleRunData = new File(expDir + File.separator	+ "multipleDataOutput.txt");
			try {
				PrintWriter dataOutput = new PrintWriter(multipleRunData);
				dataOutput.print(multiple.compileTableToString());
				dataOutput.close();
			} catch (Exception e) {
				System.out.println("SingleRun: exception whilst writing multiple run data to filesystem. "	+ e.getStackTrace());
			}	    
	    
			/* print the key for the table */
			File tableKey = new File(expDir + File.separator + "tableKey.txt");
			try {
				PrintWriter dataOutput = new PrintWriter(tableKey);
				dataOutput.print(multiple.getTableKey());
				dataOutput.close();
			} catch (Exception e) {
				System.out.println("SingleRun: exception whilst writing table key to filesystem. " + e.getStackTrace());
			}
	    }
		System.out.println("finished single run.");
	}
}

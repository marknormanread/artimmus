package sim2d.experiment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sim2d.TregSimulation;
import sim2d.dataCollection.MedianDataDouble;
import sim2d.dataCollection.MultipleRunDataStore;
import sim2d.dataCollection.SingleRunDataStore;
import sim2d.filesystem.FileSystemIO;

/**
 * This class contains a main method that kicks of a sensitivity analysis experiment. 
 * @author mark
 *
 */
public class SensitivityAnalysis 
{
	public static String topDir = "sensitivity_analysis";					// directory name where sensitivity analysis related experiments are to be placed (writing of data and parameter files). 
	public static String expDir;											// is instantiated in the main method. 
	private static Random rand = new Random(System.currentTimeMillis());	// a seed is provided here by default, however there is a command line argument that can provide an explicit value. 

	public static String defaultParamFile = "sensitivity_parameters.xml";	// the default file name containing parameters for the sensitivity analysis, should one not be given with the "-param" option. 
	public static String paramFile = defaultParamFile;						// name of the file that contains the parameters for the sensitivity analysis. 
	
	public static String executingFileName = "run_sensitivity.rb";			// this is an example of what a command line call of this class looks like. For running on a cluster, this file is redundant. 
	public static String executingFile = executingFileName;
	
	public static String singleRunFilePrefix = "simOutputData_";			// files containing data collected from a SINGLE simulation execution are prefixed with this string. What follows is a run number, then ".txt". 
	
	private static boolean seedManuallySet = false;							// we only want to write experimental (as opposed to run) seeds to the file system if they were manually set, this flag controls that writing operation. 
	private static long seed = 0;											// sometimes its necessary to manually specify the random number seed. This is where it is stored. 
	private static int runs = 5;											// how many individual runs should be performed in each testcase.
	private static double time = 1000.0;									// how long individual runs should be run for, ie, time of execution
	private static boolean singleRunRawData = false;						// whether or not the raw data from individual runs should be output to filesystem.
	private static boolean runDefault = false;								// whether or not 'default' testcase should be executed
	private static boolean compileMedians = false;							// whether or not java should try to compile median data from single runs. THis works fine for modest number of simulation runs, 
	private static int startNumber = 0;										// but it can run out of memory for very large numbers of long runs. There's a matlab script that will do the same computation though. 
	private static boolean startNumberSet = false;
	private static int timeOut = 30;
	
	public static void main(String[] args)
	{
		System.out.println("starting sensitivity analysis.");
		
		
		/* set up a directory structure into which the results of the sensitivity analysis are to be written. It may be manually overwritten in the command line arguments. */
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd'__'HH.mm.ss");	// this is how top level directories should be labelled 
		String timeNow = formatter.format(now);
		expDir = topDir + File.separator + timeNow;					// build the path to output file. 
		
		/*
		 * Read command line arguments
		 */
		if(args.length != 0)
		{
			for(String s : args)
			{
				System.out.println("parameter = " + s);
			}
			System.out.println("\n\n");
			
			for(int i = 0; i < args.length; i= i + 2)						// increment i 2 at a time. 
			{
				String command = args[i];
				String arg = args[i+1];
				if(command.equals("-seed"))
				{
					System.out.println("manually setting the seed to : " + arg);
					seedManuallySet = true;
					seed = Long.valueOf(arg);
					rand = new Random(seed);
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
					expDir = topDir + File.separator + arg;
					System.out.println("working directory for experimentation set to " + arg);
				}
				if(command.equals("-time"))
				{
					time = Double.valueOf(arg);
					System.out.println("simulation runs will be terminated after " + time + " hours.");
				}
				if(command.equals("-runDefault"))
				{
					runDefault = Boolean.parseBoolean(arg);
					System.out.println("the default test case will be executed = " + runDefault);
				}
				if(command.equals("-param"))							// this option allows the user to specify the location of the sensitivity analysis parameters xml file to run. 
				{
					paramFile = arg;
					System.out.println("sensitivity parameters file = " + paramFile);
				}
				if(command.equals("-medians"))							// this option allows the user to turn off compilation of median data. For very large experiments this has crashed Java. 
				{														// there is a matlab script that can do the same functionality. 
					compileMedians = Boolean.parseBoolean(arg);
					System.out.println("compile median runs = " + compileMedians);
				}
				if(command.equals("-startNum"))							 
				{														 
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
		System.out.println("\n\n");
		
		
		boolean success = (new File(expDir)).mkdirs();									// create the directory for experimental data to be written into. 
	    if (success) {
	    	System.out.println("Directory: " + expDir + " created");
	    } else {
	    	// if a start number was given, then it is ok for experiments run in parallel to write to the same directory, however if it was not set
	    	// then there is a chance that the current simulation run would be overwriting existing data. So throw an exception. 
	    	if(startNumberSet == false) 
	    	{  							
	    		throw new RuntimeException("Exception raised when creating the directory structure : " + expDir);
	    	}
	    }
	    	
		
	    // write configuration and execution files into the experiment directory (for future reference)
	    FileSystemIO.copyFile("sensitivity_analysis" + File.separator + paramFile, expDir + File.separator + "sensitivity_parameters.xml");
	    if(paramFile.equals(defaultParamFile) == false) 								// only copy the file across if the sensitivity params file is not the default name. 
	    {
		    String paramFileName = paramFile;											// used to remove the path part of a name, and extract only the file name. 
		    if(paramFile.contains("/"))													// this code allows for parameter files to be stored in sub-directories. 
		    {
		    	paramFileName = paramFile.substring(paramFile.lastIndexOf("/") + 1);	// will pull out the substring from the character following the last slash to the end. 
		    }
	    	FileSystemIO.copyFile("sensitivity_analysis" + File.separator + paramFile, expDir + File.separator + paramFileName);
	    }
	    FileSystemIO.copyFile(executingFile, expDir + File.separator + executingFileName);
	    	    
	    // write the seed given for this experiment to the file system, but only if the experiment seed was manually set.  
	    if(seedManuallySet) {
		    File seedData = new File(expDir + File.separator + "experimentSeed");
		    PrintWriter dataOutput;
		    try{
		    	dataOutput = new PrintWriter(seedData);
		    	dataOutput.print(seed);
		    	dataOutput.close();
		    } catch (Exception e)
		    {	System.out.println("SensitivityAnalysis: exception whilst writing experimental seed to filesystem. " + e.getStackTrace());
		    }
	    }
	    
	    // open a DOM representation of the sensitivity analysis parameters XML file
	    Document sensParams = FileSystemIO.openXMLFile("sensitivity_analysis" + File.separator + paramFile);
		String pathToStandardParametersFile = "sensitivity_analysis" + File.separator + "parameters.xml";
	    
		/* find the default parameter value, and the path (in the XML file) to the parameter being analysed. Print both to the file system. These can be vital in subsequent analysis stages, where XML files can be
		 *  queried to gain additional information.
		 */
		DefaultValueAndPath paramDetails = retrieveDefaultParameterValue(sensParams);
		File defaultValueFile = new File(expDir + File.separator + "defaultParameterValue");
	    PrintWriter dataOutput;
	    try{
	    	dataOutput = new PrintWriter(defaultValueFile);
	    	dataOutput.print(paramDetails.defaultValue);
	    	dataOutput.close();
	    } catch (Exception e)
	    {	System.out.println("SensitivityAnalysis: exception whilst writing default value to filesystem. " + e.getStackTrace());
	    }
	    File pathToParamsFile = new File(expDir + File.separator + "pathToParameters");
	    try{
	    	dataOutput = new PrintWriter(pathToParamsFile);
	    	for(String path : paramDetails.pathToParam)	    							// iterate through each String in the path to parameters, and place on a new line. 	
	    		dataOutput.print(path + "\n");	    								
	    	dataOutput.close();
	    } catch (Exception e)
	    {	System.out.println("SensitivityAnalysis: exception whilst writing pathToParameters to filesystem. " + e.getStackTrace());
	    }
	    
	    /*
	     * Devise from the sensitivity analysis parameters file the parameter sets to be fed into the simulation.
	     * Each of the Document objects obtained from 'prepareParameters' represents a different parameter on which sensitivity analysis is to be performed.
	     * I call these different parameter sets 'TestCases'.  
	     */
	    TestCase[] testParameters = prepareParameters(sensParams, pathToStandardParametersFile);

	    /*
	     * Perform sensitivity analysis on each test case parameter
	     */
	    for(TestCase testCase : testParameters)
	    {
	    	// each test case is written into a unique directory, containing the name of the parameter file and its value, separated by reserved symbols that analysis scripts can query.
	    	String workingDir = expDir + File.separator + testCase.parameterName + "_-_" + testCase.parameterValue;		 
	    	
	    	System.out.println("Starting test case - " + workingDir);
	    	
	    	// create the working directory into which the results of sensitivity analysis are to be written
	    	if( new File(workingDir).mkdirs() == false )	
	    		if(startNumberSet == false)
	    		{
	    			throw new RuntimeException("Failed to create the working directory : " + workingDir);
	    		}
	    	
	    	performSensitivityAnalysisOnParameter(testCase.parameters, workingDir);
	    }
	     
		
		System.out.println("finished sensitivity analysis.");
	}
	
	
	/**
	 * Performs sensitivity analysis on a single parameter set.
	 * 
	 * @param parameters 
	 * @param workingDirectory
	 */
	private static void performSensitivityAnalysisOnParameter(Document parameters, String workingDirectory)
	{
		/* write current parameters.xml document file to the filesystem */
		File f = new File(workingDirectory + "/" +  "run_parameters.xml");
		if(f.exists() == false)													// only overwrite the file if it doesn't already exist. 
			FileSystemIO.writeXMLFile(parameters, workingDirectory, "run_parameters.xml");
	    
		
	    MedianDataDouble runTimes = new MedianDataDouble();						// will store all the runtimes for this this set of runs, and print out the median runtime at the end. 
	    
	    /* perform 'runs' single runs of the simulation and collect results */
	    for(int r = 0; r < runs; r++)
	    {
	    	int runNumber = r + startNumber;
	    	System.out.print("starting simulation run number " + (runNumber + 1) );
	    	
	    	// set up the single run.
		    long runSeed = rand.nextLong();
	    	
	    	// write the seed for this simulation run to the filesystem. 
		    File singleRunSeed = new File(workingDirectory + File.separator + "simRunSeed_" + runNumber);
		    try{
		    	PrintWriter dataOutput = new PrintWriter(singleRunSeed);
		    	dataOutput.print(runSeed);
		    	dataOutput.close();
		    } catch (Exception e)
		    {	System.out.println("SingleRun: exception whilst writing single run seed to filesystem. " + e.getStackTrace());		    	
		    }	
	    	
		    
		    SingleRunDataStore dataStore = new SingleRunDataStore();
		    
		    TregSimulation simulation = new TregSimulation(runSeed, TregSimulation.Dimension.TwoD, parameters, timeOut*60);
		    
		    // run the simulation, and pass in the datastore. 
		    long startTime = System.currentTimeMillis();
		    simulation.run(time, dataStore);
		    
		    long runTime = (System.currentTimeMillis() - startTime) / 1000;						// calculate how long this simulation execution took, and log it. 
		    runTimes.logValue(runTime);
		    
		    System.out.print(", which ran in " + runTime + " seconds. ");
		    System.out.println(prepareMemoryAnalysis());										// print out java memory usage. 
		    		    
		    // only write the results of single runs if asked to. 
		    if(singleRunRawData)
		    {
			    // write the results of the single run to the filesystem.
			    File singleRunData = new File(workingDirectory + File.separator + singleRunFilePrefix + runNumber + ".txt");
			    PrintWriter dataOutput;
			    try{
			    	dataOutput = new PrintWriter(singleRunData);
			    	dataOutput.print(dataStore.compileTableToString());
			    	dataOutput.close();
			    } catch (Exception e)
			    {	System.out.println("Sensitivity Analysis: exception whilst writing data to filesystem. " + e.getStackTrace());
			    }
		    }
		}

	    System.out.println("median time for runs = " + runTimes.findMedian());
	    
	    // very large jobs have caused memory related issues when compiling medians, hence median value compilation can be disabled. There is a matlab script that will do the same job without running out of memory.
	    if(compileMedians)
	    {
		    MultipleRunDataStore multiple = compileMedianValues(workingDirectory);
	
			/* Write the multiple data file to the filesystem. */
			File multipleRunData = new File(workingDirectory + File.separator + "multipleDataOutput.txt");
			try {
				PrintWriter dataOutput = new PrintWriter(multipleRunData);
				dataOutput.print(multiple.compileTableToString());
				dataOutput.close();
			} catch (Exception e) {
				System.out.println("Sensitivity Analysis: exception whilst writing data to filesystem. " + e.getStackTrace());
			}
	
			/* print the key for the table */
			File tableKey = new File(workingDirectory + File.separator + "tableKey.txt");
			try {
				PrintWriter dataOutput = new PrintWriter(tableKey);
				dataOutput.print(multiple.getTableKey());
				dataOutput.close();
			} catch (Exception e) {
				System.out.println("Sensitivity Analysis: exception whilst writing data to filesystem. " + e.getStackTrace());
			}
	    }	
	}
	
	/** 
	 * Method will search through the specified @workingDirectory, search for all files containing single run simulation data, and compile the median
	 * results across all variables, for all times, in all simulation runs. The @MultipleRunDataStore that contains this information is returned.  
	 */
	public static MultipleRunDataStore compileMedianValues(String workingDirectory)
	{
		File workingDir = new File(workingDirectory);											// gain access to the specified directory. 
		
		String[] files = workingDir.list( 														// find all files that contain the string which uniquely specifies files containing single run data. 
				new FilenameFilter()
				{
					public boolean accept(File arg0, String arg1) 
					{
						return (arg1.contains(singleRunFilePrefix));
					}				
				}
		);
		
		MultipleRunDataStore multiple = new MultipleRunDataStore();
		for(String fn : files)																	// iterate through each single run data file, and log the results with the median run data store. 
		{
			System.out.println("found single run data file : " + fn);
			SingleRunDataStore store = SingleRunDataStore.compileFromFile(new File(workingDirectory + File.separator + fn));
			multiple.logSingleRunResults(store);

		}
		
		return multiple;
	}
	
	/**
	 * Method queries the runtime and creates a String that contains text inidcating how much memory is left and how much has been allocated in total. 
	 * @return
	 */
	public static String prepareMemoryAnalysis()
	{
		StringBuffer report = new StringBuffer();
		report.append(" total mem: " + Runtime.getRuntime().totalMemory());
		report.append(" free mem: " + Runtime.getRuntime().freeMemory());
		
		return report.toString();
	}
	
	/**
	 * This method will construct the various parameter files required to perform a batch sensitivity analysis. An array of Document objects is returned, 
	 * each Document represents a standard parameter file that can be fed directly into the simulator.  
	 * 
	 * The file for setting up the sensitivity analysis contains the same parameter listing as that which the simulation takes. The difference is that the 
	 * sensitivity analysis parameter file contains multiple entries for each parameter: 
	 * 		a default value to be used in all test cases (the 'default' tag); 
	 * 		and test values that will replace the default value (the 'test' tag). 
	 * 
	 * Across the entire parameters file, only one test parameter will be used at a time - all other simulation parameters will take their default values. 
	 * 
	 * Note that an example of the standard parameters file that the simulation takes is required by this method, and its location should be passed in as
	 * 'pathToStandardParametersFile'. The values in this standard parameters file are overwritten by the default values contained within the sensitivity analysis parameter
	 * file. 
	 * 
	 * @param pathToStandardParametersFile
	 * @return
	 */
	private static TestCase[] prepareParameters(Document sensParams, String pathToStandardParametersFile)
	{
		ArrayList<TestCase> parameterFiles = new ArrayList<TestCase>();
			
		if(runDefault)
		{	// if required, add the default parameters to the list of simulation parameters to be run.		
			parameterFiles.add( 	new TestCase(populateDefaultParameters( sensParams, FileSystemIO.openXMLFile(pathToStandardParametersFile)),
													"defaultValues",
													""	
												)
							);
		}
		
		Element de = sensParams.getDocumentElement();
		
		// get all the nodes in the sensitivity analysis  parameter file that represent test cases to be run
		NodeList testNL = de.getElementsByTagName("test");
		int numberOfTests = testNL.getLength();
		
		// iterate through the nodes that represent test cases to be run
		for(int i = 0; i < numberOfTests; i++)
		{
			ArrayList<String> pathToTestParam = new ArrayList<String>();		// the path through the parameters XML file to the parameter being tested.
			
			Node testNode = testNL.item(i);
			Node parent = testNode;
			// compile path to the parameter being tested
			do
			{
				parent = parent.getParentNode();								// recurse up the tree
				pathToTestParam.add( parent.getNodeName() );					// note that Node names are added in reverse order, from leaf to root!
			} while (parent != testNode.getOwnerDocument().getDocumentElement());// whilst not looking at root node
			
			// get a document representing the default parameters.
			Document parameters = populateDefaultParameters(sensParams, FileSystemIO.openXMLFile(pathToStandardParametersFile));
			
			Node node = parameters.getDocumentElement();
			// note that node names were added in reverse order, from leaf to root, hence we go through the array backwards
			// we are going to replace the relevant parameter value in the parameters document structure with the current test parameter, using the path to test parameter derived above.
			for(int k = pathToTestParam.size() - 2; k >= 0; k--)				// ignore the top level element (hence, pathToTestParam.size - 2, instead of "- 1"
			{
				node = ((Element)node).getElementsByTagName(pathToTestParam.get(k)).item(0);
			}
			node.setTextContent(testNode.getTextContent());
			
			parameterFiles.add(new TestCase(parameters, constructTestCaseParameterName(pathToTestParam) , testNode.getTextContent() ));
		}
	
		TestCase[] testCaseArray = new TestCase[parameterFiles.size()];
		return parameterFiles.toArray(testCaseArray);
	}
	
	/**
	 * This method will retrieve the default parameter value, and path to that parameter, from a given sensitivity analysis parameters xml file.  
	 * 
	 * This method assumes that a given sensitivity analysis parameters file contains only one 'default' tag anywhere in it. If there is more than one found, then
	 * an exception is thrown. 
	 * @param sensParams a DOM Document representation of the sensitivity analysis parameters file. 
	 * @return A DefaultValueAndPath object that encapsulates a String representation of the default parameter value, and the path to it in the sensitivity analysis parameters xml file.  
	 */	
	private static DefaultValueAndPath retrieveDefaultParameterValue(Document sensParams)
	{
		DefaultValueAndPath details = new DefaultValueAndPath();					// we will return the default parameter value, and path to that parameter in the xml file in this object. 			
		
		/*
		 * First, find the default parameter tag, and ensure that only one exists in the Document. 
		 */
		Element rootElement = sensParams.getDocumentElement();						// get the root of the Document tree. 
		NodeList defaultNL = rootElement.getElementsByTagName("default");			// get all the nodes in the sensitivity analysis parameter file that are named 'default'.
		
		// this method requires that 1 tag be named "default" in the sensitivity analysis parameters xml file. If this is not the case, then throw an exception. 
		if(defaultNL.getLength() != 1)
			throw new RuntimeException("SensitivityAnalysis: incorrect number of 'default' tags in sensitivity analysis parameters xml file. 1 is required, but found " + defaultNL.getLength());
		
		Node defaultNode = defaultNL.item(0);										// retrieve the default node		
		details.defaultValue = defaultNode.getTextContent();						// and store its content.
		
		/*
		 * Secondly, find the path to that parameter. This is done by recursing up the Document tree to the root, starting from the default tag identified above. 
		 */
		ArrayList<String> pathToParam = new ArrayList<String>();					// will store the path to the parameter here. Note that, since we are recursing up the tree, the path will initially be stored in reverse order. 
		Node node = defaultNode;													// assigned repeatedly with the parent node at any point in the tree, until the root node is reached
		do
		{
			node = node.getParentNode();											// find and store the parent node
			pathToParam.add(node.getNodeName());									// store the parent node's name 
		} while (node != rootElement);								// we recurse upwards, but stop one step before the root node, since we do not wish to store its name (none of the scripts need it). 
		Collections.reverse(pathToParam);											// reverse the order of the paths, such that the higher nodes in the tree appear first, rather than last. 
		
		details.pathToParam = new String[pathToParam.size()];						// instantiate a String array to store the paths
		pathToParam.toArray(details.pathToParam);									// ... and assign them
		
		return details;
	}
	
	/**
	 * Helper class that allows 'retrieveDefaultParameterValue' to return both the default parameter value, and the path to find it in the parameters xml file, in one go. 
	 * @author mark
	 *
	 */
	private static class DefaultValueAndPath
	{
		public String defaultValue;													// the string value representation of the default parameter value
		public String[] pathToParam;												// the path to that parameter value as found in a parameters.xml file. 
	}
	
	
	
	/**
	 * Constructs a String containing the path within the XML document to the parameter being tested. The intention is for this string to be used in 
	 * the working directory structure. 
	 * @param pathToTestParam
	 * @return
	 */
	private static String constructTestCaseParameterName(ArrayList<String> pathToTestParam)
	{
		StringBuilder output = new StringBuilder();
		for(int k = pathToTestParam.size() - 2; k >= 0; k--) // by " - 2" we are ommiting 'input', because that is the same for all parameters.
		{
			output.append(pathToTestParam.get(k));
			if(k != 0)
				output.append("__");
		}
		return output.toString();
	}
	
	
	/**
	 * Entry point for the recursive method that populates the given 'params' Document with the default values held in 'sensParams'. 
	 * @param sensParams
	 * @param params
	 * @return
	 */
	private static Document populateDefaultParameters(Document sensParams, Document params)
	{
		Element sde = sensParams.getDocumentElement();
		Element pde = params.getDocumentElement();
		
		populateDefaultParametersRecursive(sde, pde);
		return params;
	}
	
	/**
	 * Recursive method (NOT the entry point) that populates a parameters document (that taken by the simulation) with the default values held in 
	 * the sensitivity analysis parameters file. This is a helper function, and should only be called through 'populateDefaultParameters' above. 
	 * @param spn
	 * @param pn
	 */
	private static void populateDefaultParametersRecursive(Node spn, Node pn)
	{
				 
		// get the child nodes of this params node. 
		NodeList pnl = pn.getChildNodes();
		if (pnl == null) return;						// safety, if there are no children, then return. 
		for(int i = 0; i < pnl.getLength(); i++)
		{
			Node pChild = pnl.item(i);					// get each child in turn
			/* if the current node is a Text node, then it is a leaf that contains data we wish to copy across */
			if(pChild instanceof Text)
			{	/* found a Text object in the params file. This means that at the same level in the sensParams file we will be looking at a 'default' of 'test' named node.
				 * We look at the parent and pull out the 'default', rather than reading directly, because the default may not be the first in the list. 
				 * Having found the default we get its text content and copy it across to the params Text object.
				 */
				NodeList defaultValueNL = ((Element)spn).getElementsByTagName("default");
				if(defaultValueNL.getLength() != 0)			// 'default' provided, pull out the value and assign. 
				{ 	
					Node defaultValue = defaultValueNL.item(0);
					((Text)pChild).setData( ((Node)defaultValue).getTextContent() );
				} else	 {								// no default provided, assume the same format as params.
					((Text)pChild).setData( ((Element)spn).getTextContent() );
				}
			} else {
				/* pChild is not a text node -> recurse down to the children that do contain text */
				NodeList sChildList = ((Element)spn).getElementsByTagName(pChild.getNodeName());		// list contains the sensParams node of the same name as the params node.
				if(false ==(sChildList.getLength() == 0 || sChildList.getLength() < 1))							// some safety, ensures that params and sensParams documents are consistent with one another
				{	//throw new RuntimeException("parameters file did not contain a node of the same name as the sensitivityParameters file. " + pChild.getNodeName() );
					
					Node sChild = sChildList.item(0);			// the corresponding params node should be the first in the list. 
					
					populateDefaultParametersRecursive(sChild, pChild);				// recurse down to the children and populate at that level.
				}
			}
		}
				
	}
	
	
	/*
	 * Testing method that prints out a DOM object (Document) tree, starting from the specified node. Prints name and text values of nodes. 
	 */
	private static void printDOMRecursive(int indent, Node n)
	{
		System.out.println();
		for(int i = 0; i < indent; i++)
			System.out.print("  ");
		
		System.out.print(n.getNodeName() + "  __  " + n.getTextContent());
		
		NodeList ns = n.getChildNodes();
		if (ns == null) return;
		for(int i = 0; i < ns.getLength(); i++)
		{
			Node child = ns.item(i);
			printDOMRecursive(indent + 1, child);
		}
	}
	
	
	
	/**
	 * Class encapsulates an individual test case: the parameter configuration file to be fed into the simulator; the path of the parameter being tested, and the value
	 * that that parameter is assuming in the test. (note that all over parameters assume their default values. 
	 * @author mark
	 *
	 */
	private static class TestCase
	{
		public Document parameters;									// the parameters file to be fed into the simulation. 
		public String parameterName;								// the parameter that is being tested.
		public String parameterValue;								// the value that the parameter being tested has assumed. 
		
		
		public TestCase(Document params, String paramName, String paramValue)
		{
			this.parameters = params;
			this.parameterName = paramName;
			this.parameterValue = paramValue;
		}
	}
	
}

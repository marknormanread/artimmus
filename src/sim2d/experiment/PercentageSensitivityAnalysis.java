package sim2d.experiment;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import sim2d.TregSimulation;
import sim2d.dataCollection.MultipleRunDataStore;
import sim2d.dataCollection.SingleRunDataStore;
import sim2d.filesystem.FileSystemIO;

public class PercentageSensitivityAnalysis 
{

	private Double[] percentagePerturbations;
	
	
	public static String topDir = "percentagePerturbation";
	private static String percentagesFileName = "percentages.xml";
	
	
	public static String executingFileName = "run_percentages.rb";
	public static String executingFile = executingFileName;
	
	private static Random rand = new Random(System.currentTimeMillis());
	
	private static int runs = 5;											// how many individual runs should be performed in each testcase.
	private static double time = 1000.0;									// how long individual runs should be run for, ie, time of execution
	private static boolean singleRunRawData = false;						// whether or not the raw data from individual runs should be output to filesystem.
	private static boolean runDefault = false;								// whether or not 'default' testcase should be executed
	
	public static void main(String[] args)
	{
		System.out.println("starting percentage perturbation sensitivity analysis.");
		
		
		/* set up a directory structure into which the results of the sensitivity analysis are to be written */
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
			System.out.println("\n\n");
			
			for(int i = 0; i < args.length; i= i + 2)
			{
				String command = args[i];
				String arg = args[i+1];
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
			}
			
		}
		System.out.println("\n\n");
		
		
		boolean success = (new File(expDir)).mkdirs();					// try to make the top level directory for the experiments to be run and recorded in. 
	    if (success) {
	      System.out.println("Directory: " + expDir + " created");
	    } else
	    	throw new RuntimeException("Exception raised when creating the directory structure : " + expDir);
		
	    // write configuration and execution files into the experiment directory (for future reference)
	    FileSystemIO.copyFile(topDir + File.separator + percentagesFileName, expDir + File.separator + percentagesFileName);
	    FileSystemIO.copyFile(executingFile, expDir + File.separator + executingFileName);			// copy the ruby command file that ran this experiment. 
	    
	    
	    /*
	     * Load the sensitivity analysis parameters, and from them devise the parameter files to be fed into the simulation.
	     * Each of the Document objects obtained from 'prepareParameters' represents a different parameter on which sensitivity analysis is to be performed. 
	     */
//		Document sensParams = FileSystemIO.openXMLFile("sensitivity_analysis" + File.separator + "sensitivity_parameters.xml");
		String pathToStandardParametersFile = topDir + File.separator + "parameters.xml";
		Document percentParams = FileSystemIO.openXMLFile(topDir + File.separator + percentagesFileName);
		
	    TestCase[] testParameters = prepareTestCases(percentParams, pathToStandardParametersFile);

	    /*
	     * Perform sensitivity analysis on each test case parameter
	     */
	    for(TestCase testCase : testParameters)
	    {
	    	String workingDir = expDir + File.separator + testCase.parameterName + "_-_" + testCase.parameterValue;
	    	
	    	System.out.println("Starting test case - " + workingDir);
	    	
	    	// create the working directory into which the results of sensitivity analysis are to be written
	    	if( new File(workingDir).mkdirs() == false )			
	    		throw new RuntimeException("Failed to create the working directory : " + workingDir);
	    	
	    	performSensitivityAnalysisOnParameter(testCase.parameters, workingDir);
	    }
	     
		
		System.out.println("finished percentage based sensitivity analysis.");
	}
	
	
	/**
	 * Performs sensitivity analysis on a single parameter.
	 * 
	 * @param parameters 
	 * @param workingDirectory
	 */
	private static void performSensitivityAnalysisOnParameter(Document parameters, String workingDirectory)
	{
		/* write current parameters.xml document file to the filesystem */
	    FileSystemIO.writeXMLFile(parameters, workingDirectory, "run_parameters.xml");
	    
	    MultipleRunDataStore multiple = new MultipleRunDataStore();			// will store average results in this. 
	    
	    /* perform 'runs' single runs of the simulation and collect results */
	    for(int r = 0; r < runs; r++)
	    {
	    	System.out.print("starting simulation run number " + (r + 1) );
	    	
	    	// set up the single run.
		    long seed = rand.nextLong();
		    SingleRunDataStore dataStore = new SingleRunDataStore();
		    TregSimulation simulation = new TregSimulation(seed, TregSimulation.Dimension.TwoD, parameters, 300);
		    
		    // run the simulation, and pass in the datastore. 
		    long startTime = System.currentTimeMillis();
		    try 
		    {
			    simulation.run(time, dataStore);
			    
			    System.out.println(", which ran in " + ((System.currentTimeMillis() - startTime) / 1000) + " seconds.");
			    
			    // lot the results of this single run.
			    multiple.logSingleRunResults(dataStore);
			    
			    // only write the results of single runs if asked to. 
			    if(singleRunRawData)
			    {
				    // write the results of the single run to the filesystem.
				    File singleRunData = new File(workingDirectory + File.separator + "simOutputData_" + r + ".txt");
				    PrintWriter dataOutput;
				    try{
				    	dataOutput = new PrintWriter(singleRunData);
				    	dataOutput.print(dataStore.compileTableToString());
				    	dataOutput.close();
				    } catch (Exception e)  {	
				    	System.out.println("PercentagePerturbation: exception whilst writing data to filesystem. " + e.getStackTrace());
				    }
			    }
		    } catch (Exception e) {
		    	System.out.println("PercentagePerturbation: caught an exception whilst running simulation, however will continue : " + e.getStackTrace().toString());
		    }
	    }
	    
	   /* Write the multiple data file to the filesystem. */
	   File multipleRunData = new File(workingDirectory + File.separator + "multipleDataOutput.txt");
	   try{
		   PrintWriter dataOutput = new PrintWriter(multipleRunData);
		   dataOutput.print(multiple.compileTableToString());
		   dataOutput.close();
	   } catch (Exception e) {
		   System.out.println("Sensitivity Analysis: exception whilst writing data to filesystem. " + e.getStackTrace());
	   }
	   
	   /* print the key for the table */
	   File tableKey = new File(workingDirectory + File.separator + "tableKey.txt");
	   try{
		   PrintWriter dataOutput = new PrintWriter(tableKey);
		   dataOutput.print(multiple.getTableKey());
		   dataOutput.close();
	   } catch (Exception e) {
		   System.out.println("Sensitivity Analysis: exception whilst writing data to filesystem. " + e.getStackTrace());
	   }
	}
	
	/**
	 * This method will construct the various parameter files required to perform a batch sensitivity analysis. An array of Document objects is returned, 
	 * each Document represents a standard parameter file that can be fed directly into the simulator.  
	 * 
	 * The file for setting up the sensitivity analysis contains the same parameter listing as that which the simulation takes. The difference is that the 
	 * sensitivity analysis parameter file contains multiple entries for each parameter: 
	 * 		a default value to be used in all test cases; 
	 * 		and test values that will replace the default value. 
	 * 
	 * Across the entire parameters file, only one test parameter will be used at a time - all other simulation parameters will take their default values. 
	 * 
	 * Note that an example of the standard parameters file that the simulation takes is required by this method, and its location should be passed in as
	 * 'parametersLocation'. The values in this standard parameters file are overwritten by the default values contained within the sensitivity analysis parameter
	 * file. 
	 * 
	 * @param parametersLocation
	 * @return
	 */
	private static TestCase[] prepareTestCases(Document percentParams, String pathToStandardParametersFile)
	{
		ArrayList<TestCase> testCases = new ArrayList<TestCase>();
		
		//Document params = FileSystemIO.openXMLFile("sensitivity_analysis" + File.separator + "parameters.xml");
				
		Element de = percentParams.getDocumentElement();
		
		// get all the nodes in the percentages.xml file that represent test cases to be run - these are percentage values, but only non-zero positive values. We will dynamically calculate the negatives. 
		NodeList testNL = de.getElementsByTagName("percent");
		ArrayList<Double> percentagesAL = new ArrayList<Double>();				
		
		for(int i = 0; i < testNL.getLength(); i++)								// compile the values in the xml file into a proper array
		{
			Node n = testNL.item(i);
			percentagesAL.add(Double.parseDouble( n.getTextContent() ));
		}
		
		// TODO use a set for this to remove duplicate values?!
		/* this array will hold all the possible percentage values, both positive and negative, and zero.
		 * Percentage vales (eg, xx%) are converted into proportions, such as 0.95, 1.10 etc. 
		 */
		ArrayList<Double> proportionsAL = new ArrayList<Double>();				 			
		for(Double d : percentagesAL)
		{
			final double alpha = d / 100;										// convert percentage (eg, 5%) into a proportion (0.05)
			proportionsAL.add(1.0 + alpha);									// add the value itself (as a proportion).
			proportionsAL.add(1.0 - alpha);									// add the negative value too (as a proportion). 
		}
		proportionsAL.add(1.0);												// add the zero percentage (default) value too. This is equivalent to a propotion of 1.0 
		Double[] proportions = new Double[proportionsAL.size()];				// normal array to store the proportions in. 
		proportionsAL.toArray(proportions);									// compile the proportions into a normal array. 
		Arrays.sort(proportions);												// sort the array
		
		/* find all the parameters in the parameters.xml file. Determine the path to each one within the file, its type, and its default value.  */
		ArrayList<NodeInfo> nodeInfos = depthFirstNodeFinder(FileSystemIO.openXMLFile(pathToStandardParametersFile).getDocumentElement());
		for(NodeInfo ni : nodeInfos)
		{
			LinkedList<String> ll = ni.path;
			for(String s : ll)
			{
				System.out.print(s + "\t");
			}
			System.out.println();
		}
		
		final String[] pathToImmunize = {"Simulation", "immunize"};
		for(NodeInfo ni : nodeInfos)
		{
			/*
			 * the root of the params Document is called 'input'. So for the recursions in the following code to work properly we need to remove all the 'input' parts from the paths, 
			 * because searching for a node called 'input' on the 'input' node will return nothing.
			 */
			ni.path.removeFirst();
			String[] pathArray = new String[ni.path.size()];							// put the path into an array, because iterating over it (apparently) removes items of an linked list. not what we want if we need to traverse the path multiple times!
			ni.path.toArray(pathArray);
			
			switch(ni.dataType)
			{
				case BooleanType:
				{
					boolean[] values = {true, false};
					for(boolean value : values) 
					{
						final String valueS = Boolean.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent(valueS);									// set the value of the parameter to what was decided upon.
						
						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("true");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "REG__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);
					}
										
					for(boolean value : values) 
					{
						final String valueS = Boolean.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent(valueS);									// set the value of the parameter to what was decided upon.
						
						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("false");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "EAE__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);
					}
				}
				break;
				
				case DoubleType:
				{
					double[] values = constructParameterPerturbations(Double.parseDouble(ni.defaultValue),  proportions);
					for(double value : values)
					{
						final String valueS = Double.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}						
						e.setTextContent(valueS);										// set the value of the parameter to what was decided upon.
						
						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("true");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "REG__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);
					}
					
					for(double value : values)
					{
						final String valueS = Double.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}						
						e.setTextContent(valueS);										// set the value of the parameter to what was decided upon.
												
						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("false");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "EAE__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);
					}
				}
				break;
				
				case IntegerType:
				{
					int[] values = constructParameterPerturbations(Integer.parseInt(ni.defaultValue),  proportions);
					for(int value : values)
					{
						final String valueS = Integer.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent(valueS);										// set the value of the parameter to what was decided upon.

						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("true");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "REG__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);						
					}
					
					for(int value : values)
					{
						final String valueS = Integer.toString(value);
						Document params = FileSystemIO.openXMLFile(pathToStandardParametersFile);	// open a Document representation of parameters.xml that we will perturb to create a test case.
						Element e = params.getDocumentElement();									// get the root of the document. Element e is used iteratively to borrow down to the parameter of interest. 
						for(String nodeName : pathArray)												// iterate down the parameter's path (by node name) until you get to the parameter node in the Document. 
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
																						// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent(valueS);										// set the value of the parameter to what was decided upon.

						e = params.getDocumentElement();
						for(String nodeName : pathToImmunize)
						{
							e = (Element) e.getElementsByTagName(nodeName).item(0);		// recursively find the element in 'params' that represents the current parameter variable for which test cases are being generated. 		
							// assumes that there are no duplicate names at any point in the parameters file.							 
						}
						e.setTextContent("false");										// no immunization, to induce heavy EAE.
						String[] eaePath = new String[pathArray.length + 1];
						eaePath[0] = "EAE__";
						System.arraycopy(pathArray, 0, eaePath, 1, pathArray.length);
						TestCase testCase = new TestCase(params, constructTestCaseParameterName(eaePath), valueS);						
						testCases.add(testCase);						
					}
				}	
				break;
				
				case CatagoryType:
					/* there is little we can do about Catagory types in a generic automation test; we need to know what the options are. You're going to have to run these yourself */
				break;
			}
		}
		TestCase[] tc = new TestCase[testCases.size()];									// normal array in which to store test cases
		testCases.toArray(tc);															// compile test cases into normal array
		return tc;
	}
	
	private static ArrayList<NodeInfo> depthFirstNodeFinder(final Node thisNode)
	{
		final String thisNodeName = thisNode.getNodeName();
		final NodeList childrenNL = thisNode.getChildNodes();
		ArrayList<NodeInfo> childrenNodeInfos = new ArrayList<NodeInfo>();						//  will store the paths of all children nodes (each one of which may contain multiple paths).
		
		if(thisNode instanceof Text)				// this node is a 'Text' node, which means it is a leaf, and we need to record the path to it. 
		{
			NodeInfo ni = new NodeInfo();														// create a new NodeInfo object to store information about this node
			ni.path = new LinkedList<String>();													// create a new linked list for this leaf node
			// hat we do not add this node's name to teh list, because it is '#text'. We just return the empty linked list, and the recursive call above this one will fill in the correct name.
			final String value = thisNode.getTextContent();
			ni.defaultValue = value;
			ni.dataType = queryDataType(value);
			childrenNodeInfos.add(ni);																				// add it to the array containing paths. 		
		} else {									// there are children, recurse through each one.		 
			for(int n = 0; n < childrenNL.getLength(); n++)			
			{
				final Node childN = childrenNL.item(n);													// get the child node.									
				childrenNodeInfos.addAll(depthFirstNodeFinder(childN));									// recurse for that child node, and retrieve all the information that it contains 
			}
			for(NodeInfo ni : childrenNodeInfos)														// recurse through all the child paths, and add this node to the start of that path. 
			{
				ni.path.addFirst(thisNodeName);																	// add this node's name to the start of the path
			}
		}
		return childrenNodeInfos;
	}
	
	
	private static enum DataType {IntegerType, DoubleType, BooleanType, CatagoryType};
	
	
	private static DataType queryDataType(String value)
	{
		if(value == null)	throw new RuntimeException("PercentageSensitivityAnalysis: null data values are not permitted.");
				
//		try{
//			Boolean.parseBoolean(value);	
//			return DataType.BooleanType;
//		}catch(NumberFormatException nex)
//		{	/* do nothing, move onto next test */ }		
//		try{
//			Integer.parseInt(value);	
//			return DataType.IntegerType;
//		}catch(NumberFormatException nex)
//		{	/* do nothing, move onto next test */ }
//		try{
//			Double.parseDouble(value);	
//			return DataType.DoubleType;
//		}catch(NumberFormatException nex)
//		{	/* do nothing, move onto next test */ }

		
		if(value.equals("true") || value.equals("True") || value.equals("false") || value.equals("False"))
			return DataType.BooleanType;
		if(value.contains(".") && containsOnlyNumbers(value))
			return DataType.DoubleType;
		if(value.contains(".") == false
				&& containsOnlyNumbers(value))
			return DataType.IntegerType;
		
		return DataType.CatagoryType;			// in all other cases
	}
	
    /**
     * This method checks if a String contains only numbers
     * 
     * this method was found online at 
     * http://www.javadb.com/validate-if-a-string-contains-only-numbers
     * on 14th September 2009
     */
    public static boolean containsOnlyNumbers(String str) {
        
        // it can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {

            // if we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }
	
	/**
	 * Constructs a String containing the path within the XML document to the parameter being tested. The intention is for this string to be used in 
	 * the working directory structure. 
	 * @param pathToTestParam
	 * @return
	 */
	private static String constructTestCaseParameterName(String[] path)
	{
		StringBuilder output = new StringBuilder();
						
		for(int i = 0; i < path.length; i++)
		{
			final String s = path[i];
			output.append(s);
			if(i != (path.length - 1))
				output.append("__");
		}		
		return output.toString();
	}
	
	private static double[] constructParameterPerturbations(final double origValue, final Double[] proportions)
	{
		double[] perturbations = new double[proportions.length];
		for(int i = 0; i < proportions.length; i++)
		{
			perturbations[i] = origValue * proportions[i];
		}
		return perturbations;
	}
	
	private static int[] constructParameterPerturbations(final int origValue, final Double[] proportions)
	{
		int[] perturbations = new int[proportions.length];
		final double tempOrig = (double) origValue;
		for(int i = 0; i < proportions.length; i++)
		{
			perturbations[i] = (int) Math.round(tempOrig * proportions[i]);
		}
		
		/*
		 * REALLY UGLY, but it works and i'm in one hellova hurry now. It removes duplicates from 'perturbations' that can arrise from rounding. 
		 * 
		 * TODO fix with something nicer when I get back from holiday. 
		 */
		ArrayList<Integer> set = new ArrayList<Integer>();
		for(int i : perturbations)
		{
			if(set.contains(i) == false)
				set.add(i);
		}
		
		Integer[] perturbationsI = new Integer[set.size()];
		set.toArray(perturbationsI);
		perturbations = new int[set.size()];
		for(int i = 0; i < perturbationsI.length; i++)
			perturbations[i] = perturbationsI[i];
			
		
		return perturbations;
	}
	
	/**
	 * This is a convenience data encapsulation class that we use when finding all the parameter nodes in the parameter file. It stores the path to a node (which is recursively determined by one of the methods above), 
	 * the data type of the node, and the node's default value as a string. 
	 * @author mark
	 *
	 */
	private static class NodeInfo
	{
		LinkedList<String> path;
		DataType dataType;
		String defaultValue;
	}
	
}

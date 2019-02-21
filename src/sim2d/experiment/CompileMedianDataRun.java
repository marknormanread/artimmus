package sim2d.experiment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Random;

import sim2d.dataCollection.MultipleRunDataStore;
import sim2d.dataCollection.SingleRunDataStore;

public class CompileMedianDataRun {

	
	public static String singleRunFilePrefix = "simOutputData_";
	
	
	public static void main (String[] args)
	{
		String workingDirectory = System.getProperty("user.dir");
		
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
				if(command.equals("-cwd"))
				{
					System.out.println("manually setting the current working directory to : " + arg);
					workingDirectory = arg;				
				}				
			}			
		}
		System.out.println("\n\n");
		
		
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
	
	
	
	public static MultipleRunDataStore compileMedianValues(String workingDirectory)
	{
		File workingDir = new File(workingDirectory);
		
		String[] files = workingDir.list( 
				new FilenameFilter()
				{
					public boolean accept(File arg0, String arg1) 
					{
						return (arg1.contains(singleRunFilePrefix));
					}				
				}
		);
		
		MultipleRunDataStore multiple = new MultipleRunDataStore();
		for(String fn : files)
		{
			System.out.println("found single run data file : " + fn);
			SingleRunDataStore store = SingleRunDataStore.compileFromFile(new File(workingDirectory + File.separator + fn));
			multiple.logSingleRunResults(store);
			System.gc();

		}
		
		return multiple;
	}
	
}

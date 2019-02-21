package sim2d.experiment;

import java.io.File;
import java.io.PrintWriter;
import java.util.Random;


/**
 * This simulator is frequently run on a cluster. It was pointed out that if a load of scripts were submitted simultaneously, and the cluster was not already in use, then all those
 * scripts may start executing on different machines at the same time. If this is exactly the same time, then there could be a problem with regard to seed generation based on the clocks.
 * I think it is very unlikely that all the machine's clocks would be at exactly the same time, to the graulatity required to create the same seeds in each simulation run. However, if that 
 * was happening, then I have a problem, because the results of simulation runs are often interpreted together, as a distribution. The same seeds means the same results, which will bias the 
 * distribution. 
 * 
 * Hence, this class. It it intended to be submitted to a cluster when the cluster load is low, and as a batch, such that a whole load of these scripts start at pretty much the same time. All that happens
 * is that the current system clock is written to a file. Thes files can then be compared to see if the same seeds are being generated. 
 * @author mark
 *
 */
public class TestSeedGeneration 
{
	public static String expDir = "tests" + File.separator;					// build the path to output file. 
	
	public static void main(String[] args)
	{
		long wouldBeSeed = System.currentTimeMillis();						// do this before you do anything else related to writing or reading from the file system. 
		
		if(args.length != 0)
		{	
			for(int i = 0; i < args.length; i= i + 2)
			{
				String command = args[i];
				String arg = args[i+1];
				if(command.equals("-name"))
				{
					expDir += arg;
				}
			}
		}
		
		
		
	    // write the current system clock time the filesystem.
	    File singleRunData = new File(expDir);
	    PrintWriter dataOutput;
	    try
	    {
	    	dataOutput = new PrintWriter(singleRunData);
	    	dataOutput.print(wouldBeSeed);
	    	dataOutput.close();
	    } catch (Exception e)	{
	    	System.out.println("SingleRun: exception whilst writing single run data to filesystem. " + e.getStackTrace());
	    }
	}
}

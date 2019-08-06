package sim2d;

import sim2d.filesystem.FileSystemIO;

public class Treg2DSim
{
    /**
     * Used if you wish to run the simulation without any GUI. This method is NOT used by the console if the gui is to be used. 
     */
	public static void main(String[] args)
	{
		System.out.println("Hello World, welcome to the Treg project.");
		
    	TregSimulation sim = new TregSimulation(0, 
    											TregSimulation.Dimension.TwoD,
    											FileSystemIO.openXMLFile("parameters.xml"),
    											180
    											);
    	TregSimulation.sim.setupSimulationParameters();						// essential that we do this here.
    	
        sim.start();
        long steps;
        do
        {
        //    System.out.print("circulation ");sim.circulation.print();
        //    System.out.print("cns         ");sim.cns.print();
        //    System.out.print("slo         ");sim.slo.print();

            
            if (!sim.schedule.step(sim)) 			// performs the step, and if return is false, stops looping
                break;
            steps = sim.schedule.getSteps();		// How many steps have been performed?
            if (steps % 500 == 0)					// print info every 500 steps
            {
                System.out.println("Steps: " + steps + " Time: " + sim.schedule.time());
            }
            //System.out.println(steps + " steps;  " + sim.getNumStoppables() + " cells in simulation.");
        } while(sim.schedule.time() < 1300.0);
        sim.finish();							
        System.exit(0);  // make sure any threads finish up
	}	
}

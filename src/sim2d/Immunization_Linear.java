package sim2d;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.cell.impl.DendriticCell;

public class Immunization_Linear implements Steppable
{
	private Immunization_Linear() {}

	private int dcsInserted = 0;
	
	private double dc0;													// how many DCs influx at the beginning
	private double gradient;											// How quickly DCs will cease to migrate. 
	private int initialInflux;
	private double dcInflux;											// the current level of DC influx. 
	
	private double immunizeTime;											// the number of DCs influxing at any point after the immunization started is based on the gradient, and how long the immunization process has been going. So we need to 
																		// keep a track of when it started. 
	
	private double interval;											// the time passed since the last immunization (used for decay)
	/**
	 * 
	 * The linear mechanism here consists of the following. An initial number of immunized DCs that migrate (influx) into the compartment, which happens only once at the start of the immunization
	 * process. Following that, DC influx is best described as following a linear curve that fits the equation influx(time) = dc0 * (gradient * time_since_immunize_start). Hence DC0 is the number migrating (NOT the initial
	 * migration) which is continuously reduced through a product of gradient and time. 
	 * 
	 * Clearly influx(time) is a continuous variable, and since we deal with discrete numbers of actual cells, we need to turn
	 * that continuous variable into a discrete number. Sampling of the above equation is used. If the sample time is very low, then there will be a great deal more DCs influxing than if the sample time
	 * is high. This is because the equation describes the number of DCs influxing (their rate), NOT the actual number present in the compartment. That in turn will depend on other factors like DC lifespan.   
	 * 
	 * @initialInflux - the number of DCs that initially influx
	 * @dc0 - the number of migrating DCs at time zero, this is altered each timestep by @gradient. 
	 * @gradient - should be a negative number, how quickly influx reduces
	 * @starttime - when immunization/DCinflux starts. Usually set to 1.0
	 * @interval - how frequently DCs actually influx in the simulation, since this immunization mechanism represents continuous variables, and we can not have fractions of DCs migrating
	 * 				at a time, so sampling of the continuous variables is used instead.    
	 */
	public Immunization_Linear(int initialInflux, double dc0, double gradient, double startTime, double interval)
	{
		this.gradient = gradient;
		this.dc0 = dc0;
		this.dcInflux = dc0;
		this.initialInflux = initialInflux;
		this.interval = interval;
		
		this.immunizeTime = startTime;			
		
		final int immunizationPriority = 2;								// the immunization should be stepped after all other things. 
		TregSimulation.sim.addToSimulationScheduleRepeating(immunizeTime, immunizationPriority, this, interval);
	}

	private boolean deliveredInitial = false;
	
	public void step(SimState state)
	{
		TregSimulation sim = (TregSimulation) state;
	
		if(deliveredInitial == false)
		{
			// deliver initial burst
			for(int i = 0; i < initialInflux; i++)
			{
				DendriticCell cell = DendriticCell.createImmunizedDendriticCell(sim, sim.slo);		// cells will be placed randomly in the SLO compartment.
			}
			
			deliveredInitial = true;
		}
		
		
		/* perform the influx, add the immunized DCs to the SLO compartment */
		final int dcInfluxInt = (int) Math.round(dcInflux);										// turn the continuous variable into a discrete number. 

		for(int i = 0; i < dcInfluxInt; i++)
		{
			DendriticCell cell = DendriticCell.createImmunizedDendriticCell(sim, sim.slo);		// cells will be placed randomly in the SLO compartment.
			dcsInserted++;
		}
		
		/* update the influx rate */
		double timeSinceStart = sim.schedule.getTime() - immunizeTime;								// this mechanism gives a consistent reduction in DC influx, independently of the interval at which DCs are added to the SLO. 
		dcInflux = dc0 + (gradient * timeSinceStart);
				
		/* if influx rate has decayed to such a level that nothing more influxes, then remove this steppable object from the simulation */
		if(dcInfluxInt == 0)
		{		
			sim.removeFromSimulationSchedule(this);												// remove the immunization handler (this) from the schedule.
			return;				// do add immunized DCs to compartment.
		}
	}
}

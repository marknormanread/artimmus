package sim2d.dataCollection.dataLoggers;

import sim2d.cell.impl.Neuron;


/**
 * This class is a data logger, it does not form part of the simulation's logic, but is used to collect data from simulation activity. It collects information on
 * the number of neurons killed over time, as a cumulative count. 
 * @author mark
 *
 */
public class NeuronsKilledDataLogger 
{
	private int cumulativeCountNeuronsApoptosised = 0;
	
	
	public void logApoptosisEvent(Neuron cell)
	{
		cumulativeCountNeuronsApoptosised++;	
	}
	
	
	public int getCumulativeCountNeuronsApoptosised()
	{	return cumulativeCountNeuronsApoptosised;	}
	
}

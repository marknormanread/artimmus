package sim2d.dataCollection.dataLoggers;

import sim2d.cell.impl.CD8Treg;
import sim2d.compartment.CLN2D;
import sim2d.compartment.CNS2D;
import sim2d.compartment.Circulation2D;
import sim2d.compartment.Compartment;
import sim2d.compartment.SLO2D;
import sim2d.compartment.Spleen2D;

/**
 * This class is a data logger, it does not form part of the simulation's logic, but is used to collect data from simulation activity. It collects information on
 * the locations in which CD4Th1 cells have been apoptosised by CD8Tregs. This class only collects information, it does not write it to the filesystem or perform 
 * any statistics on it. 
 * @author mark
 *
 */
public class CD4Th1ApoptosisedDataLogger 
{

	private int apoptosisedCirculatory;
	private int apoptosisedCNS;
	private int apoptosisedCLN;
	private int apoptosisedSLO;
	private int apoptosisedSpleen;
	private int apoptosisedTotal;
		
	public void logApoptosisEvent(CD8Treg cell)
	{
		apoptosisedTotal++;
		
		Compartment location = cell.compartment;
		if(location instanceof Circulation2D)
			apoptosisedCirculatory++;			 		 
		else if (location instanceof CLN2D)
			 apoptosisedCLN++;			
		else if (location instanceof CNS2D)
			 apoptosisedCNS++;	
		else if (location instanceof SLO2D)
			apoptosisedSLO++;		
		else if (location instanceof Spleen2D)
			apoptosisedSpleen++;				
	}
	
	
	public int getApoptosisedCirculatory()
	{	return apoptosisedCirculatory;	}
	
	public int getApoptosisedCNS()
	{	return apoptosisedCNS;			}

	public int getApoptosisedCLN()
	{	return apoptosisedCLN;			}
	
	public int getApoptosisedSLO()
	{	return apoptosisedSLO;			}
	
	public int getApoptosisedSpleen()
	{	return apoptosisedSpleen;		}
	
	public int getApoptosisedTotal()
	{	return apoptosisedTotal;		}
}

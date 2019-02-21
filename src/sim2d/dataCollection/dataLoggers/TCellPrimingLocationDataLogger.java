package sim2d.dataCollection.dataLoggers;

import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.TCell_Impl;
import sim2d.compartment.CLN2D;
import sim2d.compartment.CNS2D;
import sim2d.compartment.Circulation2D;
import sim2d.compartment.Compartment;
import sim2d.compartment.SLO2D;
import sim2d.compartment.Spleen2D;

/**
 * This class is a data logger, it does not form part of the simulation's logic, but is used to collect data from simulation activity. It collects information pertaining to which
 * compartments each species of T cell is being primed in.  
 * 
 * This class only collects information, it does not write it to the filesystem or perform any statistics on it. 
 * @author mark
 *
 */
public class TCellPrimingLocationDataLogger 
{

	private int cd4ThCirculation;
	private int cd4ThCLN;
	private int cd4ThCNS;
	private int cd4ThSLO;
	private int cd4ThSpleen;

	private int cd4TregCirculation;
	private int cd4TregCLN;
	private int cd4TregCNS;
	private int cd4TregSLO;
	private int cd4TregSpleen;
	
	private int cd8TregCirculation;
	private int cd8TregCLN;
	private int cd8TregCNS;
	private int cd8TregSLO;
	private int cd8TregSpleen;
	
	public void logPrimingEvent(TCell_Impl cell)
	{
		Compartment location = cell.compartment;
		if (cell instanceof CD4THelper)
		{
			
			if(location instanceof Circulation2D)
				cd4ThCirculation++;			 
			else if (location instanceof CLN2D)
				cd4ThCLN++;			 
			else if (location instanceof CNS2D)
				cd4ThCNS++;			
			else if (location instanceof SLO2D)
				cd4ThSLO++;		
			else if (location instanceof Spleen2D)
				cd4ThSpleen++;				
		} 
		else if( cell instanceof CD4Treg)
		{			
			if(location instanceof Circulation2D)
				cd4TregCirculation++;			 
			else if (location instanceof CLN2D)
				cd4TregCLN++;			 
			else if (location instanceof CNS2D)
				cd4TregCNS++;			
			else if (location instanceof SLO2D)
				cd4TregSLO++;		
			else if (location instanceof Spleen2D)
				cd4TregSpleen++;				
		} 
		else if (cell instanceof CD8Treg)
		{		
			if(location instanceof Circulation2D)
				cd8TregCirculation++;			 
			else if (location instanceof CLN2D)
				cd8TregCLN++;			 
			else if (location instanceof CNS2D)
				cd8TregCNS++;			
			else if (location instanceof SLO2D)
				cd8TregSLO++;		
			else if (location instanceof Spleen2D)
				cd8TregSpleen++;				
		}
	}
	
	public int getCD4ThCirculation() 
	{	return cd4ThCirculation;	}

	public int getCD4ThCLN() 
	{	return cd4ThCLN;	}

	public int getCD4ThCNS() 
	{	return cd4ThCNS;	}

	public int getCD4ThSLO() 
	{	return cd4ThSLO;	}

	public int getCD4ThSpleen() 
	{	return cd4ThSpleen;	}

	public int getCD4TregCirculation() 
	{	return cd4TregCirculation;	}

	public int getCD4TregCLN() 
	{	return cd4TregCLN;	}

	public int getCD4TregCNS() 
	{	return cd4TregCNS;	}

	public int getCD4TregSLO() 
	{	return cd4TregSLO;	}

	public int getCD4TregSpleen() 
	{	return cd4TregSpleen;	}

	public int getCD8TregCirculation() 
	{	return cd8TregCirculation;	}

	public int getCD8TregCLN() 
	{	return cd8TregCLN;	}

	public int getCD8TregCNS() 
	{	return cd8TregCNS;	}

	public int getCD8TregSLO() 
	{	return cd8TregSLO;	}

	public int getCD8TregSpleen() 
	{	return cd8TregSpleen;	}
}

package sim2d.dataCollection.dataLoggers;

import sim2d.cell.impl.DendriticCellMigrates;


/**
 * This class is a data logger, it does not form part of the simulation's logic, but is used to collect data from simulation activity. It collects information pertaining to which
 * peptides an apoptosed DCMIgrates cell presents, e.g. MBP, Type1, or both.
 * 
 * This class only collects information, it does not write it to the filesystem or perform any statistics on it. 
 * @author Richard
 * @version Modified by Mark Read when integrating Richard's code back into the trunk EAE simulation development. 
 *
 */
public class DCApoptosedPeptidePresentationDataLogger 
{
		
	private int dcApoptosedMBPTotal;
	private int dcApoptosedType1Total;
	private int dcApoptosedBothTotal;
	private int dcApoptosedNullTotal;
	private int dcApoptosedTotal;
	
	public void logApoptoticAPEvent(DendriticCellMigrates cell)
	{
		dcApoptosedTotal ++;
		
		// Check to see if DCMigrates is expressing MBP, but not CDR1/2 or Fr3
		if(cell.getExpressing_MHC_II_MBP() && !( cell.getExpressing_MHC_I_CDR12() || cell.getExpressing_MHC_II_Fr3()) )
		{
			dcApoptosedMBPTotal ++;
		}
		
		// Check to see if DCMigrates is expressing Type1 (CDR1/2 or Fr3) but not MBP
		if( (cell.getExpressing_MHC_I_CDR12() || cell.getExpressing_MHC_II_Fr3() ) && !cell.getExpressing_MHC_II_MBP())
		{
			dcApoptosedType1Total ++;
		}
		
		// Check to see if DCMigrates is expressing either Type1 (CDR1/2 or Fr3) AND MBP
		if( (cell.getExpressing_MHC_I_CDR12() || cell.getExpressing_MHC_II_Fr3()) && cell.getExpressing_MHC_II_MBP())
		{
			dcApoptosedBothTotal ++;
		}
		
		// Check to see if DCMigrates is not expressing any antigen peptides
		if( !(cell.getExpressing_MHC_I_CDR12() || cell.getExpressing_MHC_II_Fr3() || cell.getExpressing_MHC_II_MBP() ))
		{
			dcApoptosedNullTotal ++;
		}		
	}
	
	public int getApoptosedMBPTotal()
	{	return dcApoptosedMBPTotal;	}
	
	public int getApoptosedType1Total()
	{	return dcApoptosedType1Total;	}
	
	public int getApoptosedBothTotal()
	{	return dcApoptosedBothTotal;	}
	
	public int getApoptosedNullTotal()
	{	return dcApoptosedNullTotal;	}
	
	public int getApoptosedDCTotal()
	{	return dcApoptosedTotal;	}
	
}
	

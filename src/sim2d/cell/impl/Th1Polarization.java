package sim2d.cell.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.TCell_Impl.Maturity;
import sim2d.cell.molecule.MHC_I_CDR12;
import sim2d.molecule.Type1;

public class Th1Polarization extends Polarization implements MHC_I_CDR12
{

	/*
	 * General properties of all CD4Th1 cells.
	 */
	public static double mhcUnExpressionDelayMean;						// how long, on average, it takes for a CD4Th1 cell to stop expressing Qa-1 molecules following its activation.
	public static double mhcUnExpressionDelayStdDev;					// the standard deviation of how long it takes for a CD4Th1 cell to stop expressing Qa-1 molecules following its activation.

	public static double type1SecretedPerHourWhenActivated;				// the quantity of type 1 cytokines secreted per hour by an activated CD4Th1 cell.
	public static double type1SecretedPerTimeslice;						// the quantity of type 1 cytokines secreted per simulation time slice by an activated CD4Th1 cell.
	 
	
	
	/*
	 * Properties of individual CD4Th1 cell instances.
	 */
	public double timeToMHC_I_CDR12Expression = Double.MAX_VALUE;		// time at which this CD4Th1 should start expressing MHC-I-CDR1/2
	public double timeOfMHC_I_CDR12UnExpression = Double.MAX_VALUE;		// time at which this CD4Th1 should stop expressing MHC-I-CDR1/2
	public boolean mhc_i_cdr12_Expr = false;							// whether this CD4Th1 cell is expressing MHC-I-CDR1/2 or not. 

	public Th1Polarization(CD4THelper Thelper)
	{
		super(Thelper);		
	}
	
	/**
	 * called every 'step'. It updates Th1 specific molecule expressions. 
	 */
	public void updateMoleculeExpression(TregSimulation simulation)
	{
		if(simulation.schedule.getTime() >= timeToMHC_I_CDR12Expression)	// time to express?
		{
			mhc_i_cdr12_Expr = true;
			timeToMHC_I_CDR12Expression = Double.MAX_VALUE;
		}
		if(simulation.schedule.getTime() >= timeOfMHC_I_CDR12UnExpression)	// time to un-express?
		{
			mhc_i_cdr12_Expr = false;
			timeOfMHC_I_CDR12UnExpression = Double.MAX_VALUE;
		}
	}
	
	/**
	 * Set times to start and stop expressing Qa1. 
	 */
	public void becomeEffector()
	{	
		timeToMHC_I_CDR12Expression = TregSimulation.sim.schedule.getTime();						// immediate expression of MHC-I-CDR1/2 following activation
		timeOfMHC_I_CDR12UnExpression = calculateAbsoluteTimeToMHCUnExpression(timeToMHC_I_CDR12Expression);
			// times for expression and unexpression have just been calculated, do a check now, to cover the extreme case that unexpression should happen immediately. 
		updateMoleculeExpression(TregSimulation.sim);					
	}
	
	/**
	 * Method handles the secretion of cytokines by this cell into the compartment that this cell occupies.
	 */
	public void secreteCytokines()
	{
		if(Th.maturity == Maturity.Effector)							// if we are activated
		{	
			Th.compartment.receiveSecretedMolecules(Type1.instance, type1SecretedPerTimeslice, Th);	// then secrete type1 cytokine.
		}
	}
		
	
	/**
	 * Returns whether this Type1 polarised cell is expressing Qa1-CDR1/2. Note that if the querying cell is a CD8Treg,
	 * then we return a specificity based answer.  
	 */
	public boolean getExpressing_MHC_I_CDR12() 
	{	return mhc_i_cdr12_Expr;		}
	
	public double getMHC_I_CDR12UnExpressionTime()
	{	return timeOfMHC_I_CDR12UnExpression;	}
	
	
	/**
	 * Static method for calculating the time at which Qa1 will no longer be expressed. A period is picked from a distribution, and is added to the time that Qa1 was expressed to create an absolute time for Qa1 
	 * unexpression. 
	 * @param timeOfMHCExpression
	 * @return
	 */
	private static double calculateAbsoluteTimeToMHCUnExpression(double timeOfMHCExpression)
	{
		double interval = TregSimulation.sim.random.nextGaussian();			// mean = 0.0, stddev = 1.0
		interval*= (mhcUnExpressionDelayStdDev / 2);						// manipulate gradient of curve to match std dev time. we divide by two because the figures in parameters.xml are 2sigma, not sigma. See [PG-A.7 & 7]
		interval += mhcUnExpressionDelayMean;								// shift to the desired mean value.
		double absoluteTime = timeOfMHCExpression + interval;
		
		if(absoluteTime <= timeOfMHCExpression)								// since we are using a distribution, it is possible that we will pick a time before Qa1 was expressed! This is safety.
		{
			absoluteTime = timeOfMHCExpression + 0.0;						// safety.
		}
		return absoluteTime;												// return absolute time of event.
	}
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Th1Polarization").item(0);
		
		mhcUnExpressionDelayMean = Double.parseDouble(pE.getElementsByTagName("mhcUnExpressionDelayMean").item(0).getTextContent());
		mhcUnExpressionDelayStdDev = Double.parseDouble(pE.getElementsByTagName("mhcUnExpressionDelayStdDev").item(0).getTextContent());
		
		type1SecretedPerHourWhenActivated = Double.parseDouble(pE.getElementsByTagName("type1SecretedPerHourWhenActivated").item(0).getTextContent());
		type1SecretedPerTimeslice = type1SecretedPerHourWhenActivated * TregSimulation.sim.timeSlice;
	}
	
}

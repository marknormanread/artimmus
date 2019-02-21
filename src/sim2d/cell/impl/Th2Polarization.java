package sim2d.cell.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.impl.TCell_Impl.Maturity;
import sim2d.molecule.Type2;

public class Th2Polarization extends Polarization
{
	/*
	 * General properties for all CD4Th2 cells.
	 * 
	 * some of these properties are overridden from the standard T cell to provide specific behaviours for type 2 T cells. 
	 */
	private static double proliferationStdDev;			// the value of a single standard deviation of proliferation.
	private static double proliferationMean;				// the mean value of proliferation. 
	
	private static double type2SecretedPerHourWhenActivated;				// the quantity of type 2 cytokines secreted per house by and activated CD4Th2 cell.
	private static double type2SecretedPerTimeslice;						// the quantity of type 2 cytokines secreted per simulation timeslice when activated. 
	
	
	public double retrieveProliferationMean()
	{	return proliferationMean;		}
	
	public double retrieveProliferationStdDev()
	{	return proliferationStdDev;		}
	
	public Th2Polarization(CD4THelper Thelper)
	{
		super(Thelper);
	}
	
	public void updateMoleculeExpression(TregSimulation simulation)
	{	/* no molecules of significant expressed by a CD4Th2 cell */	}
	
	public void becomeEffector()
	{	/* no changes to Th2 state when a cell becomes an effector */ }
	
	/**
	 * Method handles the secretion of cytokines by this cell into the compartment that this cell occupies.
	 */
	public void secreteCytokines()
	{
		if(Th.maturity  == Maturity.Effector)							// if we are activated
		{
			Th.compartment.receiveSecretedMolecules(Type2.instance, type2SecretedPerTimeslice, Th);
		}
	}
		
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Th2Polarization").item(0);
		
		proliferationStdDev = Double.parseDouble(pE.getElementsByTagName("proliferationStdDev").item(0).getTextContent());
		proliferationMean = Double.parseDouble(pE.getElementsByTagName("proliferationMean").item(0).getTextContent());
		
		type2SecretedPerHourWhenActivated = Double.parseDouble(pE.getElementsByTagName("type2SecretedPerHourWhenActivated").item(0).getTextContent());
		type2SecretedPerTimeslice = type2SecretedPerHourWhenActivated * TregSimulation.sim.timeSlice;
	}
}

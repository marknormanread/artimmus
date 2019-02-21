package sim2d.molecule;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Abstract class from which molecules in the simulation inherit. It contains certain parameters common to all molecules, such as molecular half life, and
 * a decay threshold. If there are less of a particular molecule than the decay threshold in a grid space, then the remaining molecules will be deleted. 
 * 
 * This can save computational resource, and prevents 'weird' ODE based effects where you can have half a molecule.  
 * 
 * Subclassing molecules use the singleton pattern - There is only one instance of any particular molecule, and this is referred to wherever necessary. 
 * Molecules in the system, in the environment, are not explicitly represented as objects, they are held by the Compartment classes as discrete or continuous domained variables. 
 * 
 * @author mark
 *
 */
public abstract class Molecule 
{
	public static double molecularHalflife;
	public static double decayThreshold;
	
	/**
	 * Used for testing and IO.
	 */
	public abstract String getName();
	
	
    /**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Molecule").item(0);
		
		molecularHalflife = Double.parseDouble(pE.getElementsByTagName("molecularHalflife").item(0).getTextContent());
		decayThreshold  = Double.parseDouble(pE.getElementsByTagName("decayThreshold").item(0).getTextContent());
	}
}

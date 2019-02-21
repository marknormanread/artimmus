package sim2d.molecule;

/**
 * The simulation's representation of myelin basic protein. 
 * @author mark
 *
 */
public class MBP extends Molecule 
{
	public static final MBP instance = new MBP();
	
	public String getName()
	{
		return "MBP";
	}

}

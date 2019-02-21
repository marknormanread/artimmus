package sim2d.molecule;

/**
 * The simulation's representation of complete freund's adjuvant. Not currently used in the simulation. 
 * @author mark
 *
 */
public class CFA extends Molecule 
{
	public static final CFA instance = new CFA();
	
	public String getName()
	{
		return "CFA";
	}

}

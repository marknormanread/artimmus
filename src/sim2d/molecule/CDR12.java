package sim2d.molecule;

/**
 * The simulation's representation of complementarity determining region 1/2 as found on a Th1's Vb8.2 TCR.  
 * @author mark
 *
 */
public class CDR12 extends Molecule 
{
	public static final CDR12 instance = new CDR12();

	public String getName()
	{
		return "CDR1/2";
	}

}

package sim2d.molecule;

/**
 * The simulation's generic representation of a type 1 cytokine. 
 * @author mark
 *
 */
public class Type1 extends Molecule {

	public static final Type1 instance = new Type1();
	
	public String getName()
	{
		return "Type1";
	}

}

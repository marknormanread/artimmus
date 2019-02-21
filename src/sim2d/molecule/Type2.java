package sim2d.molecule;

/**
 * The simulation's generic representation of a type 2 cytokine.  
 * @author mark
 *
 */
public class Type2 extends Molecule {

	public static final Type2 instance = new Type2();
	
	public String getName()
	{
		return "Type2";
	}

}

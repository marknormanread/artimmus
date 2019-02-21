package sim2d.molecule;


/**
 * Class represents soluble demyelinating agent (including TNF-a, NO and ROS).  
 * @author mark
 *
 */
public class SDA extends Molecule {

	public static final SDA instance = new SDA();
	
	public String getName()
	{
		return "TNF-a";
	}
	
}

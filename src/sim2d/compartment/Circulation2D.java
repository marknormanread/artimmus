package sim2d.compartment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.Cell;

public class Circulation2D extends Compartment_Impl2D
{
	private static int width;
	private static int height;
	private static double timeToCrossOrgan;							// how long, on average, it will take for a cell to cross the compartment because of blood flow. 
	
	private static VerticalMovementBoundaries vmb;
	
	public int getWidth()
	{	return width; 	}
	public int getHeight()
	{	return height;	}
	
	
	public Circulation2D(TregSimulation sim)
	{	super(sim);		}
	
	public boolean canEnter(Cell cell)
	{
		return true;
	}
	
	public boolean canLeave(Cell cell)
	{
		return true;
	}
	
	/**
	 * In the circulatory system all cells follow blood flow. 
	 */
	protected int calculateMovementVertical(Cell cell)
	{
		//return calculateMovementVerticalGaussian();					// all other cell types and states follow blood flow.
		return vmb.getMovement();
	}
	
	protected int calculateMovementHorrizontal(Cell cell)
	{
		return calculateMovementHorrizontalUniform();
	}
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Circulation2D").item(0);
		
		width = Integer.parseInt(pE.getElementsByTagName("width").item(0).getTextContent());
		height = Integer.parseInt(pE.getElementsByTagName("height").item(0).getTextContent());
		timeToCrossOrgan = Double.parseDouble(pE.getElementsByTagName("timeToCrossOrgan").item(0).getTextContent());
		
		/* dynamically calculated static variables */
		vmb = calculateVerticalMovementBoundaries(height, timeToCrossOrgan);
	}
}

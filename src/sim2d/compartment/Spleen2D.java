package sim2d.compartment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;

/**
 * A 2 dimensional representation of the spleen compartment. The spleen contains many dendritic cells keyed into removing apopototic cells from the 
 * circulatory system. These remain static and do not move. 
 * @author mark
 *
 */
public class Spleen2D extends Compartment_Impl2D
{
	private static int width;
	private static int height;
	private static double timeToCrossOrgan;
	
	private static VerticalMovementBoundaries vmb;

	
	public int getWidth()
	{	return width; 	}
	public int getHeight()
	{	return height;	}

	
	
	public Spleen2D(TregSimulation sim)
	{	super(sim);		}
	
	/**
	 * Method handles entry grants/denies for the spleen compartment. All cells can enter the spleen.
	 */
	public boolean canEnter(final Cell cell)
	{

		return true;						// all cells can enter the spleen.
	}
	
	/**
	 * Method handles departure grants/denies for the spleen compartment. All cells cen leave the spleen. 
	 */
	public boolean canLeave(final Cell cell)
	{	
		return true;
	}
	
	/**
	 * Overridden method that handles the vertical movement of the cells in the compartment. Different cells may behave differently in different compartments,
	 * and overriding this method allows for cell-compartment specific behaviours to be programmed in. 
	 */
	protected int calculateMovementVertical(final Cell cell)
	{
		/* Calculate movements for migratory dendritic cells. Note that DCs immediately removed upon becoming apoptotic, so there is no behaviour required for apoptotic DCs. */ 	
		if( cell instanceof DendriticCellMigrates )
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;
			if(dcm.isMobile())
				return calculateMovementVerticalUniform();
			else 
				return 0;
		}
		
		/* handles generic DCs, rather than DCMs */
		if( cell instanceof DendriticCell )
			return 0;
		
		/* default behaviour */
		return vmb.getMovement();
	}
	
	/**
	 * Overridden method that allows movement of various cell types in the simulation to be tailored for this compartment. 
	 */
	protected int calculateMovementHorrizontal(final Cell cell)
	{
		if(cell instanceof DendriticCellMigrates)
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;
			if(dcm.isMobile())
				return calculateMovementHorrizontalUniform();
			else 
				return 0;
		}
		
		/* handles generic DCs, rather than DCMs */
		if( cell instanceof DendriticCell )
			return 0;												// vanilla dendritic cells never move. 
		
		/* default behaviour */
		return calculateMovementHorrizontalUniform();
	}
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("Spleen2D").item(0);
		
		width = Integer.parseInt(pE.getElementsByTagName("width").item(0).getTextContent());
		height = Integer.parseInt(pE.getElementsByTagName("height").item(0).getTextContent());
		timeToCrossOrgan = Double.parseDouble(pE.getElementsByTagName("timeToCrossOrgan").item(0).getTextContent());
		
		/* dynamically calculated static variables */		
		vmb = calculateVerticalMovementBoundaries(height, timeToCrossOrgan);
	}
}

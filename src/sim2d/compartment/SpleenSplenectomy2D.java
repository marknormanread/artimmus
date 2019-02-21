package sim2d.compartment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4THelper;

/**
 * This class represents the functionality of the spleen during a splenectomy experiment. Splenectomy entails the removal
 * of the spleen. This is represented in the simulation by removing all dendritic cells from the spleen compartment (handled elsewhere
 * in TregSimulation). This means that a lot of the apoptotic cells that would normally be phagocytosed in the spleen are now not removed
 * from the simululation, and cause huge slowdown. To deal with this, any apoptotic CD4THelper cell that enters the spleen 
 * compartment under a splenectomy experiment is simply removed from the simulation. 
 * @author mark
 *
 */
public class SpleenSplenectomy2D extends Spleen2D
{

	private static boolean splenectomyFastTrack = true;		// when set to true, cells will go straight through the spleen with immediate effect, they do not take any time to traverse the compartment. 
																// (as if the spleen were not really there!). Set to true by default. 
	
	public SpleenSplenectomy2D(TregSimulation sim)
	{	super(sim);		}
	
	
	/**
	 * Overridden method. Does everything that the super method does, but will remove any apoptotic CD4Th cell from the simulation
	 * upon entering this compartment. 
	 */
	public void enterCompartment(final Cell cell)
	{
		super.enterCompartment(cell);								// perform all normal functionality. 
	
		if(cell instanceof CD4THelper && ((CD4THelper)cell).isApoptotic())// the following checks identify CD4TH cells in apoptotic state. 
		{
			CD4THelper cd4th = (CD4THelper) cell;
			cd4th.bePhagocytosised(TregSimulation.sim);			// instruct the apoptotic CD4Th cell to remove itself from the simulation. 	
		}
		else {
			// if cells go through the spleen in zero time, then migrate all other cells (NOT cd4th cells though, they will be placed in another compartment as they are deleted from this one!). 
			if (splenectomyFastTrack)	migrateCell(cell);			
		}
	}
	
	
	
	/**
	 * This method calls the super method, but in addition to that will also search for any parameters relevant to splenectomy experiments. 
	 * @param params
	 */
	public static void loadParameters(Document params)
	{		
		Spleen2D.loadParameters(params);							// do everything that would normally be done.
	
		Element pE = (Element) params.getElementsByTagName("Spleen2D").item(0);
		
		Node n = pE.getElementsByTagName("splenectomyFastTrack").item(0);						// try and find a 'splenectomy' tag in the parameter file. 		
		if (n != null)																	// if there is one, then set the corresponding parameter in the simulation. 
			splenectomyFastTrack = Boolean.parseBoolean(n.getTextContent());
	}
	
}

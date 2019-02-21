package sim2d.compartment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.TCell;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;
import sim2d.cell.impl.TCell_Impl;

public class CLN2D extends Compartment_Impl2D 
{
	private static int width;
	private static int height;
	private static double timeToCrossOrgan;
	
	private static VerticalMovementBoundaries vmb;									// standard rate at which cells move through the lymph node, driven by blood flow.
	private static VerticalMovementBoundaries vmb_recentlyMigratedDCs;				// recently migrated DCs move a little slower.
	private static VerticalMovementBoundaries vmb_activatedTCell;					// activated/effector T cells migrate through the lymph node very quickly. 
	
	public int getWidth()
	{	return width; 	}
	public int getHeight()
	{	return height;	}
	
	
	public CLN2D(TregSimulation sim)
	{	super(sim);		}
	
	/**
	 * Overridden method that defines which cells can enter this compartment from elsewhere.  
	 */
	public boolean canEnter(Cell cell)
	{
		if(cell instanceof TCell_Impl) 
		{
			final TCell_Impl tcell = (TCell_Impl) cell;
			final TCell_Impl.Maturity maturity = tcell.getMaturity();
			
			if( maturity == TCell_Impl.Maturity.Effector) 			
				return false;														// effector T cells cannot migrate through the HEV.
			else if( maturity == TCell_Impl.Maturity.Apoptotic )
			{
				if( tcell.compartment instanceof CNS2D )							// if the apoptotic T cell is coming from the CNS compartment (through the afferent lymph)
					return true;													// ... then allow it entry.  
				else
					return false;													// apoptotic T cells cannot migrate through the HEV (the only other way into this lymph node)
			}
			return true;															// all other states of T cell can enter the lymph node.
		}
		
		return true;																// all other cells can enter the lymph node. 
	}
	
	/**
	 * Overridden method hat defines which cells can leave this compartment. 
	 */
	public boolean canLeave(Cell cell)
	{	
		return true;
	}
	
	/**
	 * Overridden method that allows movement of various cell types in the simulation to be tailored for this compartment. 
	 */
	protected int calculateMovementVertical(final Cell cell)
	{
		/* Calculate movements for migratory dendritic cells. Note that DCs immediately removed upon becoming apoptotic, so there is no behaviour required for apoptotic DCs. */ 		
		if(cell instanceof DendriticCellMigrates)
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;
			if(dcm.isMobile() == true)
				// lets DCs move downards before settling, but not as fast as other cells. We want movement downwards to stop them blocking the entrance.  
				return vmb_recentlyMigratedDCs.getMovement();
			else
				return 0;											// non-mobile DCMigrates do not move. 
		}
		
		/* handles generic DCs, rather than DCMs */
		if(cell instanceof DendriticCell)							// dendritic cells are static. 
			return 0;
		
		
		/* Usually T cell movement behaviour defaults to the default. However, activated T cells are encouraged to leave the compartment very quickly, and so there is a different
		 * movement mechanism for them, implemented through 'vmb_activatedTCell'. 
		 */
		if(cell instanceof TCell)
		{
			TCell_Impl tcell = (TCell_Impl) cell;
			if(tcell.getMaturity() == TCell_Impl.Maturity.Effector)
				return vmb_activatedTCell.getMovement();
		}
		
		/* the default */
		return vmb.getMovement();
	}
	
	/**
	 * Overridden method that allow cell specific movement behaviours to be tailored towards different compartments. For comments describing the behaviours and the reasoning for them
	 * see 'calculateMovementVertical' above. 
	 */
	protected int calculateMovementHorrizontal(final Cell cell)
	{	
		if(cell instanceof DendriticCellMigrates)
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;
			if(dcm.isMobile() == true)
				return calculateMovementHorrizontalUniform();		// mobile DCMigrates can move. Pick random direction in the horrizontal plane. 			
			else 
				return 0; 											// non-mobile DCMigrates do not move. 
		}
		
		/* handles generic DCs, rather than DCMs */
		if(cell instanceof DendriticCell)							// dendritic cells are static. 
			return 0;
		
		return calculateMovementHorrizontalUniform();
	}
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("CLN2D").item(0);
		
		width = Integer.parseInt(pE.getElementsByTagName("width").item(0).getTextContent());
		height = Integer.parseInt(pE.getElementsByTagName("height").item(0).getTextContent());
		timeToCrossOrgan = Double.parseDouble(pE.getElementsByTagName("timeToCrossOrgan").item(0).getTextContent());
		
		vmb = calculateVerticalMovementBoundaries(height, timeToCrossOrgan);
		
		/*
		 * Dendritic cells that have recently migrated into the CLN compartment have some time to move around and find a place to reside. During this time
		 * they can move (obviously). We do not want them to move for very long, since that would interfere with T cell dynamics (hard to be bound to something
		 * that keeps moving - this simulation doesnt work that way, yet anyway). And we don't want them to move around randomly because they all end up staying
		 * near the top of the compartment and clogging up the entrance. So we create this, blood flow bias probabilities that allow DCs to move 'mostly' downwards, 
		 * but not in any huge hurry. This way they should find somewhere in the middle to settle
		 */
		vmb_recentlyMigratedDCs = calculateVerticalMovementBoundaries(height, timeToCrossOrgan/2);
		
		/*
		 * This movement mechanism specific for activated T cells represents an attempt to have T cells that have become activated to vacate the SLO compartment as
		 * quickly as possible. Whilst Th1 cells reside here it is unlikely that they will be subject to regulation, and they are only subject to regulation for
		 * 8 hours anyway. 
		 * This is an attempt to investigate if their speedy migration (as opposed to the relatively leisurely departure that the standard vmb setup affords)
		 * will result in more regulation of their number. Hence we set the time to cross organ to 1 hour, this will ensure that they leave as fast as possible. 
		 */
		vmb_activatedTCell = calculateVerticalMovementBoundaries(height, 6.0);
		if(activatedTCellsFastTrackThroughSLOCompartments == false)
			vmb_activatedTCell = vmb;			// same movement dynamics apply to activated T cells. 
	}
	
	
	
}

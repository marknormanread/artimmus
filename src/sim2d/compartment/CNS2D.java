package sim2d.compartment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.CNSMacrophage;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.DendriticCellMigrates;
import sim2d.cell.impl.TCell_Impl;
import sim2d.compartment.Compartment_Impl2D.VerticalMovementBoundaries;

public class CNS2D extends Compartment_Impl2D
{
	private static int width;
	private static int height;
	private static double timeToCrossOrgan;

	private static VerticalMovementBoundaries vmb;
	private static VerticalMovementBoundaries vmb_recentlyMigratedDCs;						// DCMs that are migrating should not be hanging around. 

	private static boolean TCellActivatedCanLeave;

	
	/*
	 * these variables are for datacollection, they do not form part of the behaviour of the dendritic cells. They count how many CNS-originating DCs have adopted either a type1 or type2
	 * polarization, in a cumulative fashion. Remember to set these to zero in 'loadParameters', or their values will be preserved between simulation runs. 
	 */
	public static int cumulativeCNSDCType1Polarized = 0;
	public static int cumulativeCNSDCType2Polarized = 0;
	
	
	public int getWidth()
	{	return width; 	}
	public int getHeight()
	{	return height;	}

	
	public CNS2D(TregSimulation sim)
	{	super(sim);		}
	
	public boolean canEnter(final Cell cell)
	{
		if(cell instanceof CD4Treg)
			return false;
		if(cell instanceof CD8Treg)
			return false;
		if(cell instanceof TCell_Impl)
			if( ((TCell_Impl)cell).getMaturity() == TCell_Impl.Maturity.Effector)					// naive T cells cannot enter the CNS compartment. 
				return true;
			else
				return false;
		
		return true;
	}
	
	public boolean canLeave(final Cell cell)
	{
		if(cell instanceof TCell_Impl)
			if(((TCell_Impl)cell).getMaturity() == TCell_Impl.Maturity.Effector)
				return TCellActivatedCanLeave;
		
		if(cell instanceof CNSMacrophage)
			return false;					// CNS Macrophages cannot leave the CNS compartment.
		
		return true;
	}
	
	/**
	 * Overridden method allows for compartment specific migratory behaviours to be incorporated. 
	 */
	protected int calculateMovementVertical(final Cell cell)
	{
		if(cell instanceof CNSMacrophage)
		{
			if( ((CNSMacrophage)cell).isMobile() )							// if the cell is immature, AND NOT stimulated, then we do not move. 
				return calculateMovementVerticalUniform();					// all other CNS Macrophages move around randomly
			else
				return 0;
		}
		
		if(TCellActivatedCanLeave == false)
			if(cell instanceof TCell_Impl)
				if( ((TCell_Impl)cell).getMaturity() == TCell_Impl.Maturity.Effector )
						// Acticated T cells in the CNS compartment (CD4Th1 and CD4Th2) do not leave, they move around randomly. 
					return calculateMovementVerticalUniform();

		/* If immature DCs can move, then they move around randomly ignoring blood flow. If they cannot, then they will only move when they are mobile.  */
		if(cell instanceof DendriticCellMigrates)
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;	// only DCMs will be found in the CNS compartment. 

			if(dcm.isMobile())
				return vmb_recentlyMigratedDCs.getMovement();				// move with blood flow (quciky)
			else 
				return 0;													// do not move. 
		}	
		
		return vmb.getMovement();											// all other cell types and states follow blood flow. 
	}
	
	/**
	 * Overridden method allows for compartment specific migratory behaviours to be incorporated. 
	 */
	protected int calculateMovementHorrizontal(Cell cell)
	{
		if(cell instanceof CNSMacrophage)
		{
			if( ((CNSMacrophage)cell).isMobile() )
				return calculateMovementHorrizontalUniform();				// all other CNS Macrophages move around randomly
			else
				return 0;												// immature CNS macrophages do not move
		}
		
		/* If immature DCs can move, then they move around randomly ignoring blood flow. If they cannot, then they will only move when they are mobile.  */
		if(cell instanceof DendriticCellMigrates)
		{
			final DendriticCellMigrates dcm = (DendriticCellMigrates) cell;	// the only DCs that will reside in teh CNS are those that migrate - hence DCMigrates. 

			if(dcm.isMobile())
				return calculateMovementHorrizontalUniform();		// move randomly in horrizontal plane. 
			else 
				return 0;			
		}	
		
		return calculateMovementHorrizontalUniform();
	}
	
	/**
     * Given the parameters.xml file (represented as a 'Document') this method loads the relevant default values for this class.
     * @param params
     */
	public static void loadParameters(Document params)
	{
		Element pE = (Element) params.getElementsByTagName("CNS2D").item(0);
		
		width = Integer.parseInt(pE.getElementsByTagName("width").item(0).getTextContent());
		height = Integer.parseInt(pE.getElementsByTagName("height").item(0).getTextContent());
		timeToCrossOrgan = Double.parseDouble(pE.getElementsByTagName("timeToCrossOrgan").item(0).getTextContent());
		
		TCellActivatedCanLeave = Boolean.parseBoolean(pE.getElementsByTagName("TCellActivatedCanLeave").item(0).getTextContent());		
		/*
		 * dynamically calculated static variables
		 */
		vmb = calculateVerticalMovementBoundaries(height, timeToCrossOrgan);
		/*
		 * Dendritic cells that have recently become migratory have some time to move around and find a place to reside. During this time
		 * they can move (obviously). We do not want them to move for very long, since that would interfere with T cell dynamics (hard to be bound to something
		 * that keeps moving - this simulation doesnt work that way, yet anyway). And we don't want them to move around randomly because they all end up staying
		 * near the top of the compartment and clogging up the entrance. So we create this, blood flow bias probabilities that allow DCs to move 'mostly' downwards, 
		 * but not in any huge hurry. This way they should find somewhere in the middle to settle
		 */
		vmb_recentlyMigratedDCs = calculateVerticalMovementBoundaries(height, timeToCrossOrgan/2);
		
		
		/* these are not parameters, they are data collection variables */
		cumulativeCNSDCType1Polarized = 0;
		cumulativeCNSDCType2Polarized = 0;
	}
}

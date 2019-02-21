package sim2d.compartment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.TCell_Impl;
import sim2d.molecule.INFg;
import sim2d.molecule.Molecule;
import sim2d.molecule.SDA;
import sim2d.molecule.Type1;
import sim2d.molecule.Type2;
/**
 * 
 * @author mark
 * 
 * Reasoning behind using continuous representation of molecules as opposed to discrete (int).
 * 1) continuous is more efficient when it comes to calculating half life (to get decay right we would have to run the probability for every sinlge discrete molecules,
 * or find a probability distribution that made sense. Also more efficient when calculating diffusion - if you don't move the remainder (less than 8), 
 * then they never move, and this gives strange artefacts in the simulation. To be accurate we would have to move each discrete remaining molecule to a random
 * location. With doubles you can work more exactly. 
 * 
 *
 */
/*
 * NOTES ON HOW THE SPACE IS REPRESENTED. 
 * horrizontal: -1 = left. 1 = right
 * vertical 1 = down, -1 = up.
 */
public abstract class Compartment_Impl2D extends Compartment
{
	private TregSimulation simulation;							// we need to gain access to features (such as the random num generator) from time to time. 
		
	public abstract int getWidth();
	public abstract int getHeight();
	
	public SparseGrid2D cellsGrid;			// there may be a lot of empty space in our compartments, so this is more efficient than an ObjectGrid. 
	
	/* These grids contain the concentration of molecules in the compartment */
	public DoubleGrid2D infgGrid;
	public DoubleGrid2D sdaGrid;
	public DoubleGrid2D type1Grid;
	public DoubleGrid2D type2Grid;
	
	private Compartment_Impl2D() {}							// cannot instantiate a compartment without passing the sim.
	public Compartment_Impl2D(TregSimulation sim)
	{
		simulation = sim;
		
		if(getWidth() % 2 != 0 || getHeight() % 2 != 0)
			throw new RuntimeException("neither width nor height of this compartment may hold odd values.");	// because we use optimisations that require even numbers.
		
		/* create new fields for the molecules to be held in */
		infgGrid = new DoubleGrid2D(getWidth(), getHeight());
		sdaGrid = new DoubleGrid2D(getWidth(), getHeight());
		type1Grid = new DoubleGrid2D(getWidth(), getHeight());
		type2Grid = new DoubleGrid2D(getWidth(), getHeight());
		
		cellsGrid = new SparseGrid2D(getWidth(), getHeight());
		
	}
	
	
	/**
	 * Method used to diffuse cytokines around the compartment.
	 */
	public void step(SimState state) 
	{
		diffuseGridContinuous( (TregSimulation)state, infgGrid);
		diffuseGridContinuous( (TregSimulation)state, sdaGrid);
		diffuseGridContinuous( (TregSimulation)state, type1Grid);
		diffuseGridContinuous( (TregSimulation)state, type2Grid);
		
		/* perform halflife decay on the cytokine grids */
		infgGrid = halflifeDecay( (TregSimulation)state, infgGrid);
		sdaGrid = halflifeDecay( (TregSimulation)state, sdaGrid);
		type1Grid = halflifeDecay( (TregSimulation)state, type1Grid);
		type2Grid = halflifeDecay( (TregSimulation)state, type2Grid);
		
		cellsMovement();							// there is no replacement of the grid, it is updated serially cell by cell. 
	}
	
	/**
	 * Method performs halflife decay on the molecules held within a grid.
	 * 
	 * number of molecules remaining after a timestep calculated as follows:
	 * Nt+d = Nt * (1/2)^(d / halflife)   			where d is the duration of the timestep (in hours)
	 * 
	 * 
	 */
	private DoubleGrid2D halflifeDecay(final TregSimulation sim, DoubleGrid2D grid)
	{
		final double duration = sim.timeSlice;					// the duration of a timestep, in hours.
		final double halflife = Molecule.molecularHalflife;
		final double gamma = Math.pow(0.5, (duration / halflife) );
		
		moleculeDecayThresholding(grid);
		return grid.multiply(gamma);
		
		
	}
	
	private void moleculeDecayThresholding(DoubleGrid2D grid)
	{
		final int xMax = grid.getWidth();
		final int yMax = grid.getHeight();
		final double treshold = Molecule.decayThreshold;
		for(int x = 0; x < xMax; x++ )
		{
			for(int y = 0; y < yMax; y++)
			{
				// if the grid value is less than the threshold, then we set it to zero. 
				if(grid.field[x][y] < treshold)	grid.field[x][y] = 0.0;
			}
		}
	}
	
		
	/**
	 * This returns the vertical movement of the cell, be it down (1), stay (0), or up (-1). Assumes a uniform distribution.
	 */
	protected int calculateMovementVerticalUniform()
	{
		final int i = TregSimulation.sim.random.nextInt(3);						// returns uniform int in {0,1,2} 
		return i - 1;	
	}
	/**
	 * This returns the horrizontal movement, be it left (-1), stay (0), or right (1). Assumes a uniform distribution.
	 */
	protected int calculateMovementHorrizontalUniform()
	{
		final int i = TregSimulation.sim.random.nextInt(3);						// returns uniform int in {0,1,2} 
		return i - 1;														// return something in range {-1, 0, 1}
	}
	/**
	 * Moves all the cells in the grid, if possible. We iterate through all cells on the grid attempting movement. 
	 * 
	 * 'movements' stores all the possible  movements to neighbouring cells. 'movementsIndexes' stores the indexes for 'movements'. 
	 * We randomly select an index from 'movementsIndex' and use it to get a proposed movement for the cell. WE DO NOT ATTEMPT THE SAME MOVEMENT TWICE PER CELL.
	 * If the proposed movement is to an occupied cell space, then we attempt another movement. 
	 * Either a movement will succeed, or, if every neighbouring cell is occupied the cell stays where it is.
	 *
	 */
	private void cellsMovement()
	{
		final Iterator<Cell> cells = cellsGrid.getAllObjects().iterator();
				
		/* iterate through the cells in the compartment */
		while(cells.hasNext())
		{
			final Cell cell = cells.next();
			// go through special cases
			if(cell instanceof Neuron)				// we do not wish to move CNS cells, move onto the next cell
			{	/* do nothing */  }
			else if(cell instanceof TCell_Impl)
			{
				if( ((TCell_Impl) cell).getBoundToAPC() )
				{	/* do nothing */ 	}
				else
					moveCell(cell);
			}
			else
			{    
				moveCell(cell);						// all other cells			
			}
		}
	}
	
	
	/**
	 * Implements a cell's movement around the compartment. Checks for whether the cell should move around the compartment are done elsewhere. 
	 * 
	 * After several attempts to move the cell, the cell remains where it is. 
	 * @param cell
	 */
	private void moveCell(final Cell cell)
	{
		int attemptsAtMovement = 8;
		final Int2D loc = cellsGrid.getObjectLocation(cell);			// current location of cell.
		
		while(attemptsAtMovement > 0)
		{
			final int dx = calculateMovementHorrizontal(cell);	// calculate proposed horrizontal movement.
			final int dy = calculateMovementVertical(cell);		// calculate the vertical movement. This will either follow or ignore bloodflow. 
								
			final int newx = cellsGrid.stx(loc.x + dx);		// calculate the new proposed coordinates (toroidal through x axis).
			int newy = loc.y + dy;							// the proposed new y coordinate. 				
			if(newy < 0)
				newy = 0;									// cannot disappear up the top of the grid.
			
			if(newy >= cellsGrid.getHeight())				// cell trying to migrate.
				if(migrateCell(cell) == true)				// try to migrate, and if that is successful then we break from the loop
					break;									// break from this loop.
				else
					// migration failed. The cell is either allowed to stay where it is, else it is moved to teh top of the compartment.
					newy = verticalMovementLoopOrStay(cell);		
			
			if(spaceInGridSpace(newx, newy, cell))
			{
				cellsGrid.setObjectLocation(cell, newx, newy);		// place the cell in the grid space.
				break;										// break from the loop.
			} else
				attemptsAtMovement--;						// record this attempt, and try again.
		}
	}
	
	private boolean spaceInGridSpace(final int x, final int y, final Cell cell)
	{
		final Bag cells = cellsGrid.getObjectsAtLocation(x, y);	// get all cells at current location
		if (cells != null)
			for(Cell c : ((Iterable<Cell>)cells))		
				if(c instanceof TCell_Impl == false)			// all cells other than T cells are considered to be big cells. 			
					return false;								// any cell not a T cell is a bit cell, return false
		
		// at this point there are no big cells in the specified gridspace
		if(cell instanceof TCell_Impl)
		{
			final int otherCells = cellsGrid.numObjectsAtLocation(x, y);	// all these cells will be T cells, because of the loop above not returning. 

			if(TCell_Impl.spatialTestEquals == true)
			{
				if(otherCells == TCell_Impl.retrieveCellsPerGridspace())
				{
					return false;
				}
			}
			else
			{
				if(otherCells >= TCell_Impl.retrieveCellsPerGridspace())
				{
					return false;
				}
			}
//			if(otherCells >= 7)	
//				return false;
			
			return true;
		}
		/* all other cells.
		 * 
		 * no big cells, and if the cell we're dealing with isnt a T cell, then any cell occuping space here will mean
		 *  there is not space for another cell. Only T cells can occupy the same space.
		 */
		return cellsGrid.numObjectsAtLocation(x, y) == 0;	 		
	}
	
	/**
	 * Method returns a y-co-ordinate for the cell. It will either stay at the bottom of the compartment, or it will loop back to the top.
	 */
	private int verticalMovementLoopOrStay(final Cell cell)
	{
		if (this instanceof CNS2D)
			if(cell instanceof TCell_Impl)							// the reason that we do not check for whether T cell's can actually leave is because they would already have left if they could at this point in the code.
				if( ((TCell_Impl)cell).getMaturity() == TCell_Impl.Maturity.Effector)
					return 0;											// loops back to the top of the compartment.
		
		return cellsGrid.getHeight() - 1;							// in all other cases the cell stays at the bottom of the compartment
	}
	
	/**
	 * Some cells have different behaviours in different compartments. This method is overridden by concrete compartment implemenetations to provide that behaviour. 
	 */
	protected abstract int calculateMovementVertical(final Cell cell);
	
	/**
	 * Some cells have different behaviours in different compartments. This method is overridden by concrete compartment implemenetations to provide that behaviour. 
	 */
	protected abstract int calculateMovementHorrizontal(final Cell cell);
	
	/**
	 * When a cell leaves down the bottom of the grid (past y=height) it is deemed to have migrated to another location. This method handles that posibility. 
	 */
	protected boolean migrateCell(Cell cell)
	{	
		final Bag c = simulation.compartmentsNetwork.getEdgesOut(this);		// retrieve the compartments to which a cell can transit from the one it presently occupies.
		final int index = simulation.random.nextInt(c.size());				// index of the next compartment we are going to enter, randomly chosen.
		final Edge newCompartmentEdge = (Edge)c.get(index);
		final Compartment newCompartment = (Compartment) newCompartmentEdge.to();
		if(newCompartment.canEnter(cell) && this.canLeave(cell))			// check to see if this cell can actually enter the proposed new compartment, and if it may leave this one. 
		{			
			cellsGrid.remove(cell);											// remove this cell from this compartment.			
			newCompartment.enterCompartment(cell);							// and enter the new compartment.
			return true;													// cell successfully migrated.
		} else
			return false;													// cell did not migrate out of this compartment. 
	}
	
	/**
	 * Places the specified cell in this compartment. 
	 * 
	 * Method attempts to place cell in a grid space not already occupied, but after some number of attempts will place it in a space regardless of whether 
	 * that space is occupied or not. 
	 */
	public void enterCompartment(final Cell cell)
	{	 		
		int x, y;						// the location of the cell in the new compartment. 
		y = 0;							// cells enter at the top and leave through the bottom of the compartment
		int attempts = getWidth();		// it is conceivable that the entire top of the compartment is occupied, in which case the cell cannot enter. 
		do
		{
			x = simulation.random.nextInt(getWidth());						// random placement across width of compartment (but still at the top)
			attempts --;				// record the attempt
			if(attempts == 0)			// when we have attempted enough times we move to a different height and try there. 
				break;					// after so many attempts we will place the cell at x, regardless of whether that space is occupied or not. 
	
		} while (spaceInGridSpace(x, y, cell) == false);					// if there is no space for the cell in the specified gridspace, try again... 
		cellsGrid.setObjectLocation(cell, x, y);							// place the cell at the location  (x, y)
		
		cell.migrateIntoCompartment(this);									// record that the cell is now within a different comparmtent.
	}
			
	/**
	 * Given a grid (of molecules) this method will diffuse the molecules from each cell into the neighouring cells.  
	 * If the number of molecules in a cell does not divide equally amongst the neighbours, then the remainder stays where it is.
	 * Before diffusion takes place we take a snap shot of the original grid, and reference the snap shot in making changes to the orginal grid.  
	 *  
	 * TODO inherrent assumption about movement of molecules here. 
	 */
	private void diffuseGridDiscrete(TregSimulation sim, IntGrid2D grid)
	{	
		IntGrid2D newGrid = new IntGrid2D(grid);	// clone the existing grid, and use that snapshot to make alterations to the original 'grid'. 
		for(int x = 0; x < grid.getWidth(); x++)			// scan along x	
		{
			for(int y = 0; y < grid.getHeight(); y++)		// scal along y 
			{
				int here = newGrid.get(x, y);				// pull out the original value for this cell.
				if(here != 0)								// if this cell does not contain any molecules then we do not need to continue with this cell. 
				{
					int share = here / 8;								// 8 neighbours. The remainder will stay where it is. TODO consider changing this last part. 
				
					grid.field[x][y] -= (share * 8);			// reduce the number of this cell, in the new grid. 
					
					/* add share to the neighbours */ 
					grid.field[grid.stx(x - 1)][grid.sty(y - 1)] 	+= share;		// bottom left 		
					grid.field[grid.stx(x - 1)][y]				  	+= share;		// bottom
					grid.field[grid.stx(x - 1)][grid.sty(y + 1)] 	+= share;		// bottom right
					
					grid.field[grid.stx(x)][grid.sty(y - 1)] 	+= share;			// middle left
					grid.field[grid.stx(x)][grid.sty(y + 1)] 	+= share;			// middle right
							
					grid.field[grid.stx(x + 1)][grid.sty(y - 1)] 	+= share;		// top left		
					grid.field[grid.stx(x + 1)][y]				  	+= share;		// top
					grid.field[grid.stx(x + 1)][grid.sty(y + 1)] 	+= share;		// top right
					
					/* the remainder in each cell diffuse to random locations, because if they don't cells that contain less than 8 never go anywhere */
					final int remainder = here - (share * 8);
					for(int i = 0; i < remainder; i++)
					{
						final int xi = sim.random.nextInt(3) - 1;	// the randomly chosen x direction
						final int yi = sim.random.nextInt(3) - 1;	// the randomly chosen y direction
						grid.field[x][y] -= 1;						// take one away from the current cell
						grid.field[grid.stx(x + xi)][grid.sty(y + yi)] += 1;	// move it to the randomly determined neighbour cell. 
					}
				}
			}
		}
	}
	/**
	 * Given a grid (of molecules) this method will diffuse the molecules from each cell into the neighouring cells.  
	 * Before diffusion takes place we take a snap shot of the original grid, and reference the snap shot in making changes to the orginal grid.  
	 *  
	 * TODO inherrent assumption about movement of molecules here. 
	 */
	private void diffuseGridContinuous(final TregSimulation sim, DoubleGrid2D grid)
	{
		DoubleGrid2D newGrid = new DoubleGrid2D(grid);		// clone the existing grid, and use that snapshot to make alterations to the original 'grid'. 
		for(int x = 0; x < grid.getWidth(); x++)			// scan along x	
		{
			for(int y = 0; y < grid.getHeight(); y++)		// scal along y 
			{
				double here = newGrid.get(x, y);			// pull out the original value for this cell.
				if(here != 0.0)								// if this cell does not contain any molecules then we do not need to continue with this cell. 
				{
					double share = here / 8.0;				// 8 neighbours. The remainder will stay where it is. TODO consider changing this last part. 
				
					grid.field[x][y] -= (share * 8);		// reduce the number of this cell, in the new grid. 
					
					/* add share to the neighbours */ 
					grid.field[grid.stx(x - 1)][grid.sty(y - 1)] 	+= share;		// bottom left 		
					grid.field[grid.stx(x - 1)][y]				  	+= share;		// bottom
					grid.field[grid.stx(x - 1)][grid.sty(y + 1)] 	+= share;		// bottom right
					
					grid.field[grid.stx(x)][grid.sty(y - 1)] 	+= share;			// middle left
					grid.field[grid.stx(x)][grid.sty(y + 1)] 	+= share;			// middle right
							
					grid.field[grid.stx(x + 1)][grid.sty(y - 1)] 	+= share;		// top left		
					grid.field[grid.stx(x + 1)][y]				  	+= share;		// top
					grid.field[grid.stx(x + 1)][grid.sty(y + 1)] 	+= share;		// top right				
				}
			}
		}
	}



	/**
	 * Method through which molecules are secreted into spaces in the compartment.
	 */
	public void receiveMolecules(Molecule m, double quantity, int x, int y) 
	{
		DoubleGrid2D grid = null;
		
		/* identify the correct molecule type */
		if (m instanceof INFg)
			grid = infgGrid;
		else if (m instanceof SDA)
			grid = sdaGrid;
		else if (m instanceof Type1)
			grid = type1Grid;
		else if (m instanceof Type2)
			grid = type2Grid;
		
		grid.field[x][y] += quantity;		// add the quantity to the appropriate cell.
	}
	
	/**
	 * Receive molecules secreted into the compartment from the location of the specified cell.
	 */
	public void receiveSecretedMolecules(Molecule m, double quantity, Cell cell)
	{
		DoubleGrid2D grid = null;				// we identify the grid below. 
		Int2D location = cellsGrid.getObjectLocation(cell);
		
		/* identify the correct molecule type */
		if (m instanceof INFg)
			grid = infgGrid;
		else if (m instanceof SDA)
			grid = sdaGrid;
		else if (m instanceof Type1)
			grid = type1Grid;
		else if (m instanceof Type2)
			grid = type2Grid;
		
		grid.field[location.x][location.y] += quantity;		// add the quantity to the appropriate cell.
	}
	
	/**
	 * Places the daughter cell in the same location as the parent cell.
	 * @param daugher
	 * @param parent
	 */
	public void receiveDaugherCell(Cell daughter, Cell parent) 
	{
		final Int2D location = cellsGrid.getObjectLocation(parent);	// location of parent cell. Will try to place the daughter here first
		
		placeCellAsCloseToLocationAsPossible(daughter, location.x, location.y);
	}
	
	/**
	 * This method will place the given cell as close as (is reasonably) possible to the location (x,y). 
	 * 
	 * @param cell
	 * @param x
	 * @param y
	 */
	private void placeCellAsCloseToLocationAsPossible(final Cell cell, final int x, final int y)
	{
		int attempts = 8;											// after 5 attempts to place the cell, it will be placed in a location regardless of how many cells already occupy it (this stops endless loops)
		int x1 = x;													// temp x and y variables, we do not actually want to chance x and y. 
		int y1 = y;
		
		int distance = 1;
		while(spaceInGridSpace(x1, y1, cell) == false)
		{
			// assign x and y to either +- distance, randomly. 
			x1 = ( TregSimulation.sim.random.nextBoolean() ) ? ( x + distance ) : ( x - distance );
			y1 = ( TregSimulation.sim.random.nextBoolean() ) ? y + distance : y - distance ;
			
			// place x and y back into the grid, in case the last operations selected co-ordinates outside of the grid. 
			x1 = cellsGrid.stx(x);									// toroidal x
			if(y1 < 0) 				y1 = 0;
			if(y1 >= getHeight())	y1 = getHeight() - 1;
			attempts --;											// record this attempt at placement
			if(attempts == 0)										// after so many placement attempts we try a distance further away.
			{
				distance++;
				attempts = 8;
			}
		}
		cellsGrid.setObjectLocation(cell, x1, y1);				// place cell in grid
	}
	
	/**
	 * Places the specified cell in a random location within this compartment. 
	 * We pick the location only once, because during a hard immune response we do not want new cells (eg, replacement APCs in the CNS) to be pushed away from where inflammation is taking
	 * place because of space shortage. 
	 */
	public void placeCellRandomlyInCompartmentCloseIfOccupied(final Cell cell) 
	{	
		// pick a random location.
		final int x = simulation.random.nextInt(getWidth());
		final int y = simulation.random.nextInt(getHeight());
		
		placeCellAsCloseToLocationAsPossible(cell, x, y);			// place the cell as close to that random location as we can. 
	}
	
	
	/**
	 * Places the specified cell in a random location within this compartment. If the location is occupied, then
	 * another is picked, at random. 
	 */
	public void placeCellRandomlyInCompartment(Cell cell) 
	{
		int x, y;													// where the cell will be placed in the compartment.
		do
		{
			x = simulation.random.nextInt(getWidth());
			y = simulation.random.nextInt(getHeight());
		} while (spaceInGridSpace(x, y, cell) == false);			// if there is no space for an additional (specified) cell, then try again.  
		cellsGrid.setObjectLocation(cell, x, y);					// place the cell at that location
	}
	
	/**
	 * This method removes the cell from the compartment, regardless of its location. For example, when it is phagocytosed.
	 */
	public void removeCellFollowingDeath(final Cell cell)
	{
		cellsGrid.remove(cell);										// remove cell from field
		cell.migrateIntoCompartment(null);							// record that cell no longer occupies any compartment.
	}
	
	/**
	 * Method will pull all the cells that lie within some distance of the specified co-ordinates. 
	 * 
	 * We assume toroidal world along x axis, but not along y axis. 
	 */
	public Cell[] getNeighbours(final Cell cell) 
	{
		final int distance = 1;											// so that we can change it at a later date, if we choose. CANNOT EXCEED MIN(WIDTH, HEIGHT) 		
		final Int2D loc = cellsGrid.getObjectLocation(cell);			// location of the cell in the grid.
		ArrayList<Cell> neighbours = new ArrayList<Cell>();				// where we will store the neighbouring cells. 
		
		for(int x0 = loc.x - distance ; x0 <= loc.x + distance ; x0++)	// iterate over x coordinates
		{
			final int x1 = cellsGrid.stx(x0);							// toroidal world around the x axis. (but not the y axis)
			for(int y0 = loc.y - distance; y0 <= loc.y + distance; y0++)	// iterate over y coordinates
				if( (y0 < getHeight()) && (y0 >= 0) )					// if this is not the case then we skip examination of this grid space (off the edge of the field)
				{
					final Bag cellsBag = cellsGrid.getObjectsAtLocation(x1, y0);	
					if (cellsBag != null)								// cellsBag will be null if there were no objects in that location of the SparseGrid2D.				
						for(Object o : cellsBag)
							neighbours.add( (Cell) o );					// cast objects into Cells and place into array list.					
				}
		}		
		neighbours.remove(cell);										// remove the cell from being in its own neighbourhood. 
		Cell[] cells = new Cell[neighbours.size()];						// cast into array of Cell objects...
		return neighbours.toArray(cells);								// ... and return.
	}
	
	/**
	 * Returns the quantity of the specified molecule at the location of the specified cell.
	 */
	public double getConcentrationMolecule(final Molecule m, final Cell c)
	{
		Int2D location = cellsGrid.getObjectLocation(c);
		
		if(m instanceof INFg)
			return infgGrid.field[location.x][location.y];
		else if(m instanceof SDA)
			return sdaGrid.field[location.x][location.y];
		else if(m instanceof Type1)
			return type1Grid.field[location.x][location.y];
		else if(m instanceof Type2)
			return type2Grid.field[location.x][location.y];
		return 0;
	}
	
	/**
	 * Removes the specified quantity of the specified molecule from the location occupied by the spefied cell. This is used for phagocytosis of molecules by APCs.
	 */
	public void removeQuantityMolecule(Molecule m, int quantity, Cell cell)
	{
		Int2D location = cellsGrid.getObjectLocation(cell);

		if(m instanceof INFg)
			infgGrid.field[location.x][location.y] -= quantity;
		else if(m instanceof SDA)
			sdaGrid.field[location.x][location.y] -= quantity;
		else if(m instanceof Type1)
			type1Grid.field[location.x][location.y] -= quantity;
		else if(m instanceof Type2)
			type2Grid.field[location.x][location.y] -= quantity;
	}


	/**
	 * Returns an array of all cells in the compartment. Used for collecting data on cell populations
	 * and drawing graphs.
	 */
	public Collection<Cell> getAllCells()
	{
		Bag b = cellsGrid.allObjects;
		return new ArrayList<Cell>(b);			// do not return the bag itself, because modifying it is dangerous. 
	}
	
	public int totalCells()
	{
		return cellsGrid.allObjects.size();
	}
	
	/**
	 * See the documentation on 'VerticalMovementBoundaries' below for a detailed description of how this algorithm works. Alternatively, look in log book 'A', pages
	 * 64 - 72. 
	 * 
	 * 
	 * @param compartmentHeight
	 * @param timeToCrossCompartment
	 * @return
	 */
	public static VerticalMovementBoundaries calculateVerticalMovementBoundaries(int compartmentHeight, double timeToCrossCompartment)
	{		
		final int timestepsInCompartment = (int) (timeToCrossCompartment / TregSimulation.sim.timeSlice);
		final double alpha = (double) compartmentHeight / (double) timestepsInCompartment;
		
		final double x = (1.0 + (1.5 * alpha)) / 3.0;		// size of region representing 'down'
			
		final double z = x - alpha;							// size of region representing 'up'
		
		final double y = (x + z) / 2.0;						// size of region representing 'stay' (is half way between up and down)
		
		final double boundary1 = x;
		final double boundary2 = x + y;
		
		return new VerticalMovementBoundaries(boundary1, boundary2);
	}
	
	/**
	 * This class represents a mechanism for blood flow bias and migration through a compartment that replaces the previous gaussian based one. 
	 * 
	 * This one operates with a flat distribution, and defines three regions (two boundaries) within the space of 0.0 to 1.0. The firs region (x) represents the
	 * probability that the cell will move down, the second region (y) represents the probability that the cell will stay at the current horrizontal position, and the
	 * third region (z) represents the probability that the cell will move up. Hence, we draw random numbers from a uniform distribution, and the boundaires
	 * are placed in a way that implements the blood flow bias. 
	 * 
	 * There were a few constraints on the sizes of the boundaries. they must all add to 1.0. We can calculate from the number of steps that a cell can
	 * have in the compartment before it is meant to have moved, and the height of the compartment, the 'rate' at which the cell must move through the space, which we
	 * call 'alpha'. region X, after subtracting 'z' must equal alpha. Y is set to lie half way between x and z, since it would make sense that the cell stays where it
	 * is more than it moves up, given blood flow. With these three constraints the sizes of the regions can be calculated.
	 * 
	 * Note that strange things will happen if it is not physically possible for the cell to traverse the space in the time allocated. However, this is used
	 * deliberately to engineer a strong downwards movement in the Dendritic cells that have recently migrated to a new space. 
	 * 
	 * More information can be found in logbook A, pages 64 - 72.
	 * 
	 * 
	 * @author mark
	 *
	 */
	public static class VerticalMovementBoundaries
	{
		/*
		 * 0.0 -> bounary1 = move down
		 * boundary1 -> boundary2 = no vertical movement
		 * boundary2 -> 1.0 = move up
		 */
		private double boundary1;
		private double boundary2;
		
		public VerticalMovementBoundaries(double b1, double b2)
		{
			boundary1 = b1;
			boundary2 = b2;
		}
		
		public int getMovement()
		{
			final double rand = TregSimulation.sim.random.nextDouble();
			if(rand < boundary1)
				return 1;
			else if(rand < boundary2)
				return 0;
			else
				return -1;
		}
	}
	
}

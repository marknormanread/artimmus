package sim2d.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;
import sim2d.compartment.Compartment;
import sim2d.utils.DrawGraph.SeriesAttributes;

public class SpleenTCellDataLogger implements Steppable
{
	public DrawGraph graph;				// the graph of the cell type, differentiating the states. 
	
	private SeriesAttributes[] colsMap = new SeriesAttributes[4];
	
	private SpleenTCellDataLogger() {}
	public SpleenTCellDataLogger(String title)
	{
		
		colsMap[0] = new SeriesAttributes("CD4TregEffector", Color.blue, false);
		colsMap[1] = new SeriesAttributes("CD8TregEffector", Color.green, false);
		colsMap[2] = new SeriesAttributes("CD4Th1", Color.red, false);
		colsMap[3] = new SeriesAttributes("CD4Th2", Color.yellow, false);
				
		graph = new DrawGraph(title, "time", "cells", colsMap);
	}

	
	public void step(SimState state)
	{
		TregSimulation sim = (TregSimulation) state;
		
		/* create a data structure in which to store references to all of the cells in the simulation, and populate it */
		ArrayList<Cell> cells = new ArrayList<Cell>();
		cells.addAll(sim.spleen.getAllCells());
		
		
		/* c will store the string name of each cell type and an integer representing the number of this cells within the simulation */
		Map<String, Integer> data = new HashMap<String, Integer>();
		data.put("CD4TregEffector", 0);
		data.put("CD8TregEffector", 0);
		data.put("CD4Th1", 0);
		data.put("CD4Th2", 0);
		
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell c : cells)
		{
			int i;
	
			if(c instanceof TCell_Impl)
			{
				if(c instanceof CD8Treg)
				{
					final CD8Treg treg = (CD8Treg) c;
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
					{
						i = data.get("CD4TregEffector");
						data.put("CD4TregEffector", i + 1);
					}
				} else if(c instanceof CD4Treg) 
				{
					final CD4Treg treg = (CD4Treg) c;
					if(treg.getMaturity() == TCell_Impl.Maturity.Effector)
					{
						i = data.get("CD8TregEffector");
						data.put("CD8TregEffector", i + 1);
					}				
				} else if (c instanceof CD4THelper)
				{
					final CD4THelper th = (CD4THelper) c;
					if(th.getMaturity() == TCell_Impl.Maturity.Effector)
					{
						if(th.getPolarization() instanceof Th1Polarization)
						{	
							i = data.get("CD4Th1");
							data.put("CD4Th1", i + 1);
						}	
						else if(th.getPolarization() instanceof Th2Polarization)
						{
							i = data.get("CD4Th2");
							data.put("CD4Th2", i + 1);
						}
					}
				}
			}
		}
		
		/* finally, log all the values with the graph, against time */
		for(String key : data.keySet())
			graph.logValue(key, sim.schedule.getTime(), data.get(key));
	}
}

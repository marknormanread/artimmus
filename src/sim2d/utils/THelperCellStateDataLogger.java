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
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;
import sim2d.compartment.Spleen2D;
import sim2d.utils.DrawGraph.SeriesAttributes;

public class THelperCellStateDataLogger implements Steppable
{
	public DrawGraph graph;				// the graph of the cell type, differentiating the states. 
	private Class cellType;
	
	private SeriesAttributes[] colsMap = new SeriesAttributes[7];
	
	private THelperCellStateDataLogger() {}
	public THelperCellStateDataLogger(String title)
	{
		cellType = CD4THelper.class;										// this class works only on this variety of T cell. 
		
		colsMap[0] = new SeriesAttributes("Naive", Color.cyan, false);
		colsMap[1] = new SeriesAttributes("Partial", Color.yellow, false);
		colsMap[2] = new SeriesAttributes("Proliferating", Color.magenta, false);
		colsMap[3] = new SeriesAttributes("CD4Th1", Color.red, false);
		colsMap[4] = new SeriesAttributes("CD4Th2", Color.white, false);
		colsMap[5] = new SeriesAttributes("Apoptotic", Color.gray, false);
		colsMap[6] = new SeriesAttributes("Total", Color.green, false);
		
		graph = new DrawGraph(title, "time", "cells", colsMap);
	}
	
	public void step(SimState state)
	{
		TregSimulation sim = (TregSimulation) state;
		
		/* create a data structure in which to store references to all of the cells in the simulation, and populate it */
		ArrayList<Cell> cells = new ArrayList<Cell>();
		cells.addAll(sim.circulation.getAllCells());
		cells.addAll(sim.cns.getAllCells());
		cells.addAll(sim.cln.getAllCells());
		cells.addAll(sim.slo.getAllCells());
		cells.addAll(sim.spleen.getAllCells());
		
		/* c will store the string name of each cell type and an integer representing the number of this cells within the simulation */
		Map<String, Integer> data = new HashMap<String, Integer>();
		data.put("Naive", 0);
		data.put("Partial", 0);
		data.put("Proliferating", 0);
		data.put("CD4Th1", 0);
		data.put("CD4Th2", 0);
		data.put("Apoptotic", 0);
		data.put("Total", 0);
		
	
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell c : cells)
		{	
			if(c instanceof CD4THelper) 
			{
				CD4THelper cell = (CD4THelper) c;
				
				data.put("Total", data.get("Total") + 1);
				
				switch(cell.getMaturity())
				{
				case Naive:
					data.put("Naive", data.get("Naive") + 1);
					break;
				case Partial:
					data.put("Partial", data.get("Partial") + 1);
					break;
				case Proliferating:
					data.put("Proliferating", data.get("Proliferating") + 1);
					break;
				case Effector:
					if(cell.getPolarization() instanceof Th1Polarization)
					{
						data.put("CD4Th1", data.get("CD4Th1") + 1);
					} else if(cell.getPolarization() instanceof Th2Polarization) {
						data.put("CD4Th2", data.get("CD4Th2") + 1);						
					}
					break;
				case Apoptotic:
					data.put("Apoptotic", data.get("Apoptotic") + 1);
					break;
				}				
			}
		}
		
		/* finally, log all the values with the graph, against time */
		for(String key : data.keySet())
			graph.logValue(key, sim.schedule.getTime(), data.get(key));
	}
}

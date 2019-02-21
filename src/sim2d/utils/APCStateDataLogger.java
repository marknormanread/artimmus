package sim2d.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.APC_Impl;
import sim2d.cell.impl.TCell_Impl;
import sim2d.compartment.Compartment;
import sim2d.utils.DrawGraph.SeriesAttributes;

public class APCStateDataLogger implements Steppable 
{
	public DrawGraph graph;				// the graph of the cell type, differentiating the states. 
	private Class cellType;
	
	private Compartment compartment;		// the compartment containing the cells that we wish to monitor.
	
	private SeriesAttributes[] colsMap = new SeriesAttributes[5];
	
	private APCStateDataLogger() {}
	public APCStateDataLogger(String title, Class type, Compartment compartment)
	{
		cellType = type;
		this.compartment = compartment;
		
		colsMap[0] = new SeriesAttributes("Immature", Color.green, false);
		colsMap[1] = new SeriesAttributes("Tolerogenic", Color.yellow, false);
		colsMap[2] = new SeriesAttributes("Immunogenic", Color.red, false);
		colsMap[3] = new SeriesAttributes("Apoptotic", Color.gray, false);
		colsMap[4] = new SeriesAttributes("Total", Color.blue, false);
		
		graph = new DrawGraph(title, "time", "cells", colsMap);
	}
	
	public void step(SimState state)
	{
		TregSimulation sim = (TregSimulation) state;
		
		/* create a data structure in which to store references to all of the cells in the simulation, and populate it */
		ArrayList<Cell> cells = new ArrayList<Cell>();
		cells.addAll(compartment.getAllCells());
		
		
		/* c will store the string name of each cell type and an integer representing the number of this cells within the simulation */
		Map<String, Integer> data = new HashMap<String, Integer>();
		data.put("Immature", 0);
		data.put("Tolerogenic", 0);
		data.put("Immunogenic", 0);
		data.put("Apoptotic", 0);
		data.put("Total", 0);
		
		
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell c : cells)
		{
			int i;
	
			if(cellType.isInstance(c)) 
			{
				APC_Impl cell = (APC_Impl) c;
				i = data.get("Total");
				data.put("Total", i + 1);

				if(cell.isImmature())
				{
					i = data.get("Immature");
					data.put("Immature", i+1);
				}
				else if(cell.isTolerogenic() && cell.isExpressing_MHCPeptide())
				{
					i = data.get("Tolerogenic");
					data.put("Tolerogenic", i+1);
				}
				else if(cell.isImmunogenic() && cell.isExpressing_MHCPeptide())
				{
					i = data.get("Immunogenic");
					data.put("Immunogenic", i+1);
				}
				else if(cell.isApoptotic())
				{
					i = data.get("Apoptotic");
					data.put("Apoptotic", i+1);
				}
			}
		}
		
		/* finally, log all the values with the graph, against time */
		for(String key : data.keySet())
			graph.logValue(key, sim.schedule.getTime(), data.get(key));
	}
}

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
import sim2d.cell.impl.DendriticCell;
import sim2d.compartment.Compartment;
import sim2d.utils.DrawGraph.SeriesAttributes;

public class APCPolarizationDataLogger implements Steppable
{
	public DrawGraph graph;				// the graph of the cell type, differentiating the states. 
	public Class cellType;
	
	private Compartment compartment;		// the compartment containing the cells that we wish to monitor.
	
	private SeriesAttributes[] colsMap = new SeriesAttributes[2];
	
	private APCPolarizationDataLogger() {}
	public APCPolarizationDataLogger(String title, Compartment compartment)
	{
		cellType = DendriticCell.class;
		this.compartment = compartment;
		
		colsMap[0] = new SeriesAttributes("Type1", Color.red, false);
		colsMap[1] = new SeriesAttributes("Type2", Color.yellow, false);
				
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
		data.put("Type1", 0);
		data.put("Type2", 0);
		
		
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell c : cells)
		{
			int i;
	
			if(cellType.isInstance(c)) 
			{
				DendriticCell dc = (DendriticCell) c;

				if(dc.getPolarization() == DendriticCell.Polarization.Type1 && dc.isExpressing_MHCPeptide())
				{
					i = data.get("Type1");
					data.put("Type1", i+1);
				}
				else if(dc.getPolarization() == DendriticCell.Polarization.Type2 && dc.isExpressing_MHCPeptide())
				{
					i = data.get("Type2");
					data.put("Type2", i+1);
				}
			}
		}
		
		/* finally, log all the values with the graph, against time */
		for(String key : data.keySet())
			graph.logValue(key, sim.schedule.getTime(), data.get(key));
	}
}

package sim2d.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.TCell_Impl;
import sim2d.compartment.Spleen2D;
import sim2d.utils.DrawGraph.SeriesAttributes;

public class CD4TregStateDataLogger implements Steppable 
{
	public DrawGraph graph;				// the graph of the cell type, differentiating the states. 
	private Class cellType = CD4Treg.class;
	
	private SeriesAttributes[] colsMap;
	

	public CD4TregStateDataLogger()
	{
		
		ArrayList<SeriesAttributes> tempColsMap = new ArrayList<SeriesAttributes>();
		
		tempColsMap.add( new SeriesAttributes("Naive", Color.green, false) );
		tempColsMap.add( new SeriesAttributes("Partial", Color.yellow, false) );
		tempColsMap.add( new SeriesAttributes("Proliferating", Color.magenta, false) );
		tempColsMap.add( new SeriesAttributes("Effector", Color.red, false) );
		tempColsMap.add( new SeriesAttributes("Apoptotic", Color.gray, false) );
		tempColsMap.add( new SeriesAttributes("Total", Color.blue, false) );
		
		colsMap = new SeriesAttributes[tempColsMap.size()];
		
		tempColsMap.toArray(colsMap);
		
		graph = new DrawGraph("CD4Treg Population", "time", "cells", colsMap);
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
		data.put("Effector", 0);
		data.put("Apoptotic", 0);
		data.put("Total", 0);
		
		
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell c : cells)
		{
			if(cellType.isInstance(c)) 
			{
				TCell_Impl cell = (TCell_Impl) c;
				
				data.put("Total", data.get("Total") + 1);
				switch(cell.getMaturity())								// log the cell's state of activation at a population level.
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
						data.put("Effector", data.get("Effector") + 1);
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

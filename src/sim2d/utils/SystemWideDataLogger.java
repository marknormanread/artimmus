package sim2d.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim2d.TregSimulation;
import sim2d.cell.Cell;
import sim2d.cell.impl.CD4THelper;
import sim2d.cell.impl.CD4Treg;
import sim2d.cell.impl.CD8Treg;
import sim2d.cell.impl.Neuron;
import sim2d.cell.impl.CNSMacrophage;
import sim2d.cell.impl.DendriticCell;
import sim2d.cell.impl.TCell_Impl;
import sim2d.cell.impl.Th1Polarization;
import sim2d.cell.impl.Th2Polarization;
import sim2d.compartment.Spleen2D;

public class SystemWideDataLogger implements Steppable
{
	
	private DrawGraph graph;				// the graph of the cell populations. 
	
	
	private SystemWideDataLogger() {}
	public SystemWideDataLogger(String title, Map<String, Color> cols)
	{
		DrawGraph.SeriesAttributes[] attributes = new DrawGraph.SeriesAttributes[cols.size()];
		int i = 0;
		for(String key : cols.keySet())
		{
			attributes[i] = new DrawGraph.SeriesAttributes(key, cols.get(key), false);
			i++;
		}
		
		graph = new DrawGraph(title, "time", "cells", attributes);
		
	}
	
	/**
	 * This method will be used to collect data from the compartments. 
	 */
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
		Map<String, Integer> c = new HashMap<String, Integer>();
		c.put("CD4Th", 0);
		c.put("CD4Th1", 0);
		c.put("CD4Th2", 0);
		c.put("CD4Treg", 0);
		c.put("CD8Treg", 0);
		c.put("CNS", 0);
		c.put("CNSM", 0);
		c.put("DC", 0);
		
		/* iterate over each cell in the simulation and calculate its type */
		for(Cell cell : cells)
		{
			int i;
			if(cell instanceof CD4THelper)
			{
				final CD4THelper Th = (CD4THelper) cell;
				if(Th.getMaturity() == TCell_Impl.Maturity.Effector)	// need this check partially to ensure that we do not perform 'instanceof' when polarization is null valued.
				{
					if(Th.getPolarization() instanceof Th1Polarization)
					{
						i = c.get("CD4Th1");
						c.put("CD4Th1", i+1);
					} else if (Th.getPolarization() instanceof Th2Polarization) {					
						i = c.get("CD4Th2");
						c.put("CD4Th2", i+1);
					}
				} else	{								// must be non-polarized Th cell									
					i = c.get("CD4Th");
					c.put("CD4Th", i+1);	
				}	
			}
			else if(cell instanceof CD4Treg)
			{
				if( ((CD4Treg)cell).getMaturity() == TCell_Impl.Maturity.Effector )
				{	
					i = c.get("CD4Treg");
					c.put("CD4Treg", i+1);
				}
			}
			else if(cell instanceof CD8Treg)
			{
				if( ((CD8Treg)cell).getMaturity() == TCell_Impl.Maturity.Effector )
				{	
					i = c.get("CD8Treg");
					c.put("CD8Treg", i+1);
				}
			}
			else if(cell instanceof DendriticCell)
			{
				i = c.get("DC");
				c.put("DC", i+1);
			}
			else if(cell instanceof CNSMacrophage)
			{
				i = c.get("CNSM");
				c.put("CNSM", i+1);
			}
			else if(cell instanceof Neuron)
			{
				i = c.get("CNS");
				c.put("CNS", i+1);
			}
		}
		
		/* finally, log all the values with the graph, against time */
		graph.logValue("CD4Th", sim.schedule.getTime(), c.get("CD4Th"));
		graph.logValue("CD4Th1", sim.schedule.getTime(), c.get("CD4Th1"));
		graph.logValue("CD4Th2", sim.schedule.getTime(), c.get("CD4Th2"));
		graph.logValue("CD4Treg", sim.schedule.getTime(), c.get("CD4Treg"));
		graph.logValue("CD8Treg", sim.schedule.getTime(), c.get("CD8Treg"));
		graph.logValue("DC", sim.schedule.getTime(), c.get("DC"));
		graph.logValue("CNSM", sim.schedule.getTime(), c.get("CNSM"));
		graph.logValue("CNS", sim.schedule.getTime(), c.get("CNS"));
		
//		for(String key : c.keySet())
//			graph.logValue(key, sim.schedule.getTime(), c.get(key));
		
	}
	
	public JFrame compileGraph()
	{	
		return graph.compileGraph();
	}
}

package sim2d.utils;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

public class DrawGraph {
	
	JFreeChart chart;

	// data is stored in here. The String holds the key (and gets printed on the graph) for the TwoDimensionalMatrix objects that hold the data. 
	HashMap<String,TwoDimensionalMatrix> data = new HashMap<String,TwoDimensionalMatrix>();
	
	String title, xAxis, yAxis;
	Font font = new Font("titleFont", Font.PLAIN, 10);
	
	SeriesAttributes[] seriesAttributes = null;
	
	/**
	 * Constructors. When we create a DrawGraph object we need to specify the title and the axis labels. 
	 */
	private DrawGraph() {	}
	public DrawGraph(String title, String xAxis, String yAxis)
	{
		this.title = title;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	/**
	 * As above, but allows for the specification of how certain series are to be drawn on the graph.
	 * NOTE THAT ONLY THOSE SERIES INCLUDED IN 'seriesAttrs' WILL BE DRAWN ON THE GRAPH.
	 */
	public DrawGraph(String title, String xAxis, String yAxis, SeriesAttributes[] seriesAttrs)
	{
		this.title = title;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		seriesAttributes = seriesAttrs;
	}
	
	
	/**
	 * Thus must be called very soon after the DrawGraph has been created. 
	 */
	public JFrame init()
	{
		JFrame frame = new JFrame();
		frame.pack();
		frame.setVisible(true);
		return frame;
	}
	
	/**
	 *  Log a single value into the specified series. 
	 */
	public void logValue(String key, double time, double val)
	{
		if(data.containsKey(key) == false)
		{
			TwoDimensionalMatrix series = new TwoDimensionalMatrix();
			series.add(time, val);
			data.put(key, series);
			return;
		}
		data.get(key).add(time, val);
	}
	
/*	public void logValues(double time, double u1, double u2, double v, double s)
	{
		u1Data.add(time, u1);
		u2Data.add(time, u2);
		vData.add(time, v);
		sData.add(time, s);
	}
*/
	
	/**
	 * Will compile the graph itself, and will plot it. Works for any number of dataseries now,
	 */
	public JFrame compileGraph()
	{
		DefaultXYDataset dataset = new DefaultXYDataset();
		XYLineAndShapeRenderer renderer = null;
		
		if(seriesAttributes != null)							// there are user specified details of how to draw the series.
		{
			renderer = new XYLineAndShapeRenderer();			// renderer that will store user preferences
			for(int i = 0; i < seriesAttributes.length; i++)	// cycle through each user defined series data. 
			{
				SeriesAttributes s = seriesAttributes[i];	
				dataset.addSeries(s.name, data.get(s.name).toArray());	// add the series here so that we know the order in which they are being added
				renderer.setSeriesPaint(i, s.col);						// set the corresponding color for the series
				renderer.setSeriesShapesVisible(i, s.shapesVisible);	// set the corresponding shape style for the series
			}
		} else {													// no user defined series attributes.
			for(String key : data.keySet())
			{
				dataset.addSeries(key, data.get(key).toArray());	// construct the data series from the TwoDimensionalMatrix objects stored in data. 
			}			
		}

				
		chart = ChartFactory.createXYLineChart(title, xAxis, yAxis, dataset, PlotOrientation.VERTICAL, true, false, false);
		chart.getTitle().setFont(font);
		chart.getXYPlot().getDomainAxis().setLabelFont(font);
		chart.getXYPlot().getRangeAxis().setLabelFont(font);
		
		if(renderer != null)									// if there are user defined series attributes
			chart.getXYPlot().setRenderer(renderer);			// then set the renderer that contains them.
		
		return plotGraph(chart);
	}
	
	private JFrame plotGraph(JFreeChart chart)
	{
		JPanel chartPanel = new ChartPanel(chart);
		JFrame frame = new JFrame();
		
		frame.add(chartPanel);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setTitle(title + " Display");
		return frame;	
	}
	
	/**
	 * Used to associate a data series with the attributes that define how it is to be drawn on the graph.
	 */
	public static class SeriesAttributes
	{
		String name;
		Color col;
		boolean shapesVisible;
		
		public SeriesAttributes(String seriesName, Color seriesCol, boolean seriesShapesVisible)
		{
			name = seriesName;
			col = seriesCol;
			shapesVisible = seriesShapesVisible;
		}
		
	}
	

}


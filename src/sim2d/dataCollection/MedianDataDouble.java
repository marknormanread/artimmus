package sim2d.dataCollection;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 
 * Class will collect a series of results (as an array), and is capable of then finding the mean value in those results. This class works on Double valued data.
 * @author mark
 */
public class MedianDataDouble
{
	private ArrayList<Double> data = new ArrayList<Double>();
	
	public MedianDataDouble()
	{		}
	
	public void logValue(double value)
	{
		data.add(value);
	}
	public void logValue(int value)
	{
		data.add((double)value);
	}
	
	public Double findMedian()
	{
		Double[] doublearray = new Double[data.size()];
		data.toArray(doublearray);
		Arrays.sort(doublearray);
		Double median = 0.0;
		if(doublearray.length % 2 == 0)
		{
			// even number of items in array -- median = mean of middle two
			double x = doublearray[doublearray.length / 2];
			double y = doublearray[(doublearray.length / 2) - 1];
			median = (x + y) / 2;	
		} else {
			// odd number of items in array -- median = the one in the middle. 
			median = doublearray[doublearray.length / 2];
		}
		return median;
	}
	
	public int getQuantityValuesLogged()
	{	return data.size();	}
}
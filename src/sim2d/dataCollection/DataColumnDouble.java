package sim2d.dataCollection;

import java.util.ArrayList;
import java.util.Iterator;

public class DataColumnDouble
{
	String title;
	ArrayList<Double> data = new ArrayList<Double>();;	
	
	public DataColumnDouble(String name)
	{
		title = name;
	}
	
	public void logValue(double value)
	{
		data.add(value);
	}
	
	public Iterator getIterator()
	{
		return data.iterator();
	}
	
	/**
	 * Converts the data into a string format and returns an iterator on that data instead.
	 */
	public Iterator getStringIterator()
	{
		ArrayList<String> stringData = new ArrayList<String>();
		for(Double d : data)
			stringData.add(Double.toString(d));
		
		return stringData.iterator();
	}
			
	public String getTitle()
	{	return title;	}
	
	public int getLength()
	{	return data.size();		}
	
}

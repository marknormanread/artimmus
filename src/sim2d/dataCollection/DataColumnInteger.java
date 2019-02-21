package sim2d.dataCollection;

import java.util.ArrayList;
import java.util.Iterator;


public class DataColumnInteger
{
	String title;
	ArrayList<Integer> data = new ArrayList<Integer>();;	
	
	public DataColumnInteger(String name)
	{
		title = name;
	}
	
	public void logValue(int value)
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
		for(Integer i : data)
			stringData.add(Integer.toString(i));
		
		return stringData.iterator();
	}
			
	public String getTitle()
	{	return title;	}
	
	public int getLength()
	{	return data.size();		}
}

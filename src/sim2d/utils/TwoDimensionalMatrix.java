package sim2d.utils;

import java.util.ArrayList;
import java.util.List;

public class TwoDimensionalMatrix {

	private ArrayList<Double> firstDimension;
	private ArrayList<Double> secondDimension;
	
	public TwoDimensionalMatrix() 
	{
		firstDimension = new ArrayList<Double>();
		secondDimension = new ArrayList<Double>();
	}
	
	public void add(double firstValue, double secondValue) 
	{
		firstDimension.add(firstValue);
		secondDimension.add(secondValue);
	}
	

	public double[][] toArray() 
	{
		return new double[][]{listToArray(firstDimension), listToArray(secondDimension)};
	}
	
	
	private double[] listToArray(List<Double> list) 
	{
		final double[] array = new double[list.size()];
		
		for (int index = 0; index < array.length; index++) {
			array[index] = list.get(index);
		}
		
		return array;
	}
}

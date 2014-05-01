package utilities;

import java.util.LinkedList;

public class RollingAverage {
	private int length;
	private double rollingAvg = 0;
	private LinkedList<Double> pastVals = new LinkedList<>();

	
	public RollingAverage(int length) {
		this.length = length;
	}
	
	public double update(double value) {
		pastVals.addLast(value);
		if (pastVals.size() > length)
			rollingAvg -= pastVals.removeFirst()/pastVals.size();
		else
			rollingAvg *= (pastVals.size()-1.0)/pastVals.size();
		
		rollingAvg += value/pastVals.size();
		return rollingAvg;
	}
	
	public double getValue() {
		return rollingAvg;
	}
}

package utilities;

import java.util.Arrays;
import java.util.LinkedList;

public class RollingAverage {
	protected int length;
	protected double rollingAvg = 0;
	protected LinkedList<Double> pastVals = new LinkedList<>();
	
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
	
	public double getPercentile(double p) {	
		Double[] sorted = pastVals.toArray(new Double[pastVals.size()]);
		Arrays.sort(sorted);
		return sorted[(int)((p*(pastVals.size()-1)))];
	}
}

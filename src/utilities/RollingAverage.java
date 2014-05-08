package utilities;

import java.util.Arrays;
import java.util.LinkedList;

public class RollingAverage {
	protected int length;
	protected double rollingAvg = 0;
	protected LinkedList<Double> pastVals = new LinkedList<>();
	
	private Double[] sorted = new Double[1];
	private boolean updated;
	
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
		
		updated = true;
		return rollingAvg;
	}
	
	public double getValue() {
		return rollingAvg;
	}
	
	public double getPercentile(double p) {
		if (updated) {
			if (pastVals.size() != sorted.length) {
				sorted = new Double[pastVals.size()];
			}
			sorted = pastVals.toArray(sorted);
			Arrays.sort(sorted);
			updated = false;
		}
		
		int index = (int)((p*(pastVals.size()-1)));
		return sorted[Math.min(sorted.length-1, index)];
	}
}

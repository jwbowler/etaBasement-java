package main;

import java.util.Iterator;
import java.util.TreeSet;

import utilities.RollingAverage;

public class RollingMedian extends RollingAverage{
	TreeSet<Double> rollingTreeSet = new TreeSet<>();
	
	public RollingMedian(int length) {
		super(length);
	}
	
	public double update(double value) {
		pastVals.addLast(value);
		rollingTreeSet.add(value);
		
		if (pastVals.size() > length) {
			double removed = pastVals.removeFirst();
			rollingTreeSet.remove(removed);
			rollingAvg -= removed/pastVals.size();
		}
		else {
			rollingAvg *= (pastVals.size()-1.0)/pastVals.size();
		}
		
		rollingAvg += value/pastVals.size();
		return rollingAvg;
	}
	
	public double getPercentile(double p) {		
		Iterator<Double> iterator = rollingTreeSet.iterator();
		for (int i = 0; i < p*length; i++)
			iterator.next();
		
		return iterator.next();
	}
}

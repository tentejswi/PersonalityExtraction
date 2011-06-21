package com.personalityextractor.evaluation;

import java.util.HashMap;
import java.util.Set;

public class PerfMetrics {
	
	public enum Metric {
		TOTAL, RESOLUTION, EXTRACTION, SEARCHPAGE, GETCATEGORIES;
	}
	
	private static PerfMetrics instance = null;

	private HashMap<Metric, Double> metrics;
	
	private PerfMetrics() {
		metrics = new HashMap<Metric, Double>();
	}
	
	public static PerfMetrics getInstance() {
		if(instance == null) {
			instance = new PerfMetrics();
		}
		
		return instance;
	}
	
	public void addToMetrics(Metric m, double value) {
		if(metrics.containsKey(m)) {
			metrics.put(m, metrics.get(m) + value);
		} else {
			metrics.put(m, value);
		}
	}
	
	public Set<Metric> getMetrics() {
		return metrics.keySet();
	}
	
	public double getMetric(Metric m) {
		return metrics.get(m);
	}
	
}

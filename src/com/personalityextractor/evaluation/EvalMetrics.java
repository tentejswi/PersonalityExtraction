/**
 * 
 */
package com.personalityextractor.evaluation;

/**
 * Evaluation Metrics
 * @author akishore
 *
 */
public class EvalMetrics {

	double fp;
	double fn;
	double tp;
	double tn;
	
	public EvalMetrics() {
		fp = 0;
		fn = 0;
		tp = 0;
		tn = 0;
	}
	
	public void incrFP() {
		fp++;
	}
	
	public void incrFN() {
		fn++;
	}
	
	public void incrTP() {
		tp++;
	}
	
	public void incrTN() {
		tn++;
	}

	public double getFp() {
		return fp;
	}

	public double getFn() {
		return fn;
	}

	public double getTp() {
		return tp;
	}

	public double getTn() {
		return tn;
	}
}

/**
 * 
 */
package com.personalityextractor.evaluation;

/**
 * Evaluation Metrics
 * 
 * @author akishore
 * 
 */
public class EvalMetrics {

	double fp;
	double fn;
	double tp;
	double tn;
	double f1;
	double err;

	public EvalMetrics() {
		fp = 0;
		fn = 0;
		tp = 0;
		tn = 0;
		err = 0;
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

	public double calculateF1Measure() {
		double precision = calculatePrecision();
		double recall = calculateRecall();

		if ((precision + recall) == 0) {
			return 0;
		}

		f1 = 2 * (precision * recall) / (precision + recall);
		return f1;
	}
	
	public double calculateError() {
		if((tp + fp) == 0) {
			return 0;
		}
		
		err = (fp/(tp+fp));
		return err;
	}
	
	private double calculatePrecision() {
		try {
			if((tp + fp) == 0) {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
		
		return 1.0*tp/(1.0*tp + 1.0*fp);
	}
	
	private double calculateRecall() {
		try {
			if((tp + fn) == 0) {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}
		
		return 1.0*tp/(1.0*tp + 1.0*fn);
	}
}

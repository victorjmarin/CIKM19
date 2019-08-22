package edu.rit.goal.pdgalign.graphlet;

/**
 * Created by IntelliJ IDEA. User: cwhelan Date: 10/20/11 Time: 3:42 PM
 */
public abstract class BaseGraphletSignature implements HasGraphletSignature {

	int[] counts;

	public int[] getCounts() {
		return counts;
	}

	public void setCounts(int[] counts) {
		this.counts = counts;
	}

	/**
	 * Computes the weighted degree counts as described in Milenkovic and Przulj,
	 * Cancer Informatics 2008:6 257–273
	 * 
	 * @return
	 */
	public double[] getWeightedCounts() {
		double[] weightedCounts = new double[counts.length];
		for (int i = 0; i < counts.length; i++) {
			weightedCounts[i] = GraphletCounter.orbitWeights[i] * counts[i];
		}
		return weightedCounts;
	}

}
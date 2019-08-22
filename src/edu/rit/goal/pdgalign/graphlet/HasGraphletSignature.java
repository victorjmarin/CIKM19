package edu.rit.goal.pdgalign.graphlet;

/**
 * Created by IntelliJ IDEA. User: cwhelan Date: 10/20/11 Time: 12:15 PM
 */
public interface HasGraphletSignature {

	public int[] getCounts();

	public double[] getWeightedCounts();

	void setCounts(int[] counts);
}
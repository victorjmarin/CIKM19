package edu.rit.goal;

import java.util.LinkedList;
import java.util.List;

public class Exp1Generator {

	public static void main(String[] args) {

		List<String> configs = new LinkedList<>();

		double[] epss = new double[] { .85, .9, .95 };

		for (int i = 0; i < Assignment.values().length; i++) {
			for (double eps : epss)
				configs.add("{" + i + ", " + eps + "}");
		}

		String result = String.join(", ", configs);

		System.out.println("{" + result + "}");

	}

}

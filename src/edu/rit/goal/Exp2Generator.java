package edu.rit.goal;

import java.util.LinkedList;
import java.util.List;

public class Exp2Generator {

	public static void main(String[] args) {

		List<String> configs = new LinkedList<>();

		double[] epss = new double[] { .6, 1.0 };
		double[] mus = new double[] { .05, .1 };

		for (int i = 0; i < Assignment.values().length; i++) {
			for (double eps : epss)
				for (double mu : mus)
					configs.add("{" + i + ", " + eps + ", " + mu + "}");
		}

		System.out.println(configs.size() + " configurations.");

		String result = String.join(", ", configs);

		System.out.println("{" + result + "}");

	}

}

package ru.Baalberith.GameDaemon.Utils;

import java.util.ArrayList;
import java.util.List;

public class CountingPattern {
	private List<Integer> numbers = new ArrayList<Integer>();
	private String pattern;

	public CountingPattern(String pattern) {
		this.pattern = pattern;
		if (pattern == null || pattern.trim().isEmpty()) {
			return;
		}

		if (!pattern.contains(",")) {
			parseSQ(pattern);
			return;
		}

		String[] arr = pattern.split(",");
		for (String s : arr) {
			parseSQ(s);
		}
	}

	private void parseSQ(String sq) {
		try {
			if (!sq.contains("-")) {
				Integer a = Integer.parseInt(sq);
				numbers.add(a);
				return;
			}

			String[] arr = sq.split("\\-");
			if (arr.length != 2)
				throw new IllegalArgumentException(
						"Interval must contain 2 numbers\n" + "Pattern: " + pattern + "\n" + "Error at: " + sq);

			Integer a = Integer.parseInt(arr[0]);
			Integer b = Integer.parseInt(arr[1]);

			if (a == b) {
				numbers.add(a);
				return;
			}

			if (a > b) {
				for (int i = a.intValue(); i >= b.intValue(); i--) {
					numbers.add(i);
				}
				return;
			}

			for (int i = a.intValue(); i <= b.intValue(); i++) {
				numbers.add(i);
			}

		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Must be an integer!\n" + "Pattern: " + pattern + "\n" + "Error at: " + sq, e);
		}

	}

	public boolean contains(Integer i) {
		return numbers.contains(i);
	}
}

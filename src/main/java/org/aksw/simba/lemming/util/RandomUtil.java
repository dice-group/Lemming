package org.aksw.simba.lemming.util;

import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

public class RandomUtil {

	public static int getRandomWithExclusion(Random rnd, int bound, Set<Integer> exclude) {
		if (exclude.size() >= bound) {
			throw new IllegalArgumentException(
					"Can't choose a random number since all possible numbers seem to be excluded.");
		}
		OptionalInt result = rnd.ints(0, bound).filter(i -> !exclude.contains(i)).findFirst();
		if (result.isPresent()) {
			return result.getAsInt();
		} else {
			throw new IllegalStateException(
					"The random number generation ended without a result. This shouldn't happen.");
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param rnd Random object
	 * @param set Set 
	 * @return Random object from a set
	 */
	public static <T> T getRandomFromSet(Random rnd, Set<T> set) {
		int item = rnd.nextInt(set.size());
		int i = 0;
		for (Object obj : set) {
			if (i == item)
				return (T) obj;
			i++;
		}
		return null;
	}
}

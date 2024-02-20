package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

public class OfferedItemWrapper<T> implements IOfferedItem<T> {

	private T[] arrBaseItems;

	private Random random;

	private long seed;

	public OfferedItemWrapper(T[] arrBaseItems, long seed) {
		this.arrBaseItems = arrBaseItems;
		this.seed = seed;
		this.random = new Random(seed);
	}
	
	public T[] findIntersection(Set<T> setOfRestrictedItems) {
        return Arrays.stream(arrBaseItems)
                .filter(setOfRestrictedItems::contains)
                .toArray(size -> Arrays.copyOf(arrBaseItems, size));
    }

	@Override
	public T getPotentialItem() {
		return arrBaseItems[random.nextInt(arrBaseItems.length)];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems) {
		T[] intersection = findIntersection(setOfRestrictedItems);
		return intersection[random.nextInt(intersection.length)];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems, boolean reusedProbability) {
		return getPotentialItem(setOfRestrictedItems);
	}

	@Override
	public long getSeed() {
		return seed;
	}

}

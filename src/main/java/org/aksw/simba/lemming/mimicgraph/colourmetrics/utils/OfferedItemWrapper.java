package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.dice_research.ldcbench.generate.SeedGenerator;

public class OfferedItemWrapper<T> implements IOfferedItem<T> {

	private T[] arrBaseItems;

	private Random random;

	public OfferedItemWrapper(T[] arrBaseItems, SeedGenerator seedGen) {
		this.arrBaseItems = arrBaseItems;
		this.random = new Random(seedGen.getNextSeed());
	}
	
	public OfferedItemWrapper(T[] arrBaseItems, Random random) {
		this.arrBaseItems = arrBaseItems;
		this.random = random;
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
		if (intersection.length == 0) {
			return null;
		}
		return intersection[random.nextInt(intersection.length)];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems, boolean reusedProbability) {
		return getPotentialItem(setOfRestrictedItems);
	}

	
	public T getPotentialItemRemove(Set<T> setOfRemoval) {
		T[] minus = Arrays.stream(arrBaseItems)
                .filter(item -> !setOfRemoval.contains(item))
                .toArray(size -> Arrays.copyOf(arrBaseItems, size));
		
		if (minus.length == 0) {
			return null;
		}
		return minus[random.nextInt(minus.length)];
	}

}

package org.aksw.simba.lemming.mimicgraph.colourmetrics.utils;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import org.dice_research.ldcbench.generate.SeedGenerator;
import org.dice_research.ldcbench.generate.SequentialSeedGenerator;

public class OfferedItemWrapper<T> implements IOfferedItem<T> {

	private T[] arrBaseItems;

	private Random random;

	private SeedGenerator seedGen;

	public OfferedItemWrapper(T[] arrBaseItems, SeedGenerator seedGen) {
		this.arrBaseItems = arrBaseItems;
		this.seedGen = seedGen;
		this.random = new Random(seedGen.getNextSeed());
	}
	
	public OfferedItemWrapper(T[] arrBaseItems, Random random) {
		this.arrBaseItems = arrBaseItems;
		this.random = random;
		this.seedGen = new SequentialSeedGenerator(System.currentTimeMillis());
	}

	public T[] findIntersection(Set<T> setOfRestrictedItems) {
        return Arrays.stream(arrBaseItems)
                .filter(setOfRestrictedItems::contains)
                .toArray(size -> Arrays.copyOf(arrBaseItems, size));
		}

	@Override
	public T getPotentialItem() {
		refresh(seedGen.getNextSeed());
		return arrBaseItems[random.nextInt(arrBaseItems.length)];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems) {
		T[] intersection = findIntersection(setOfRestrictedItems);
		if (intersection.length == 0) {
			return null;
		}
		refresh(seedGen.getNextSeed());
		return intersection[random.nextInt(intersection.length)];
	}

	@Override
	public T getPotentialItem(Set<T> setOfRestrictedItems, boolean reusedProbability) {
		return getPotentialItem(setOfRestrictedItems);
	}

	@Override
	public long getSeed() {
		return seedGen.getAsLong();
	}
	
	public void refresh(long seed) {
		this.random = new Random(seed);
	}
	
	public T getPotentialItemRemove(Set<T> setOfRemoval) {
		T[] minus = Arrays.stream(arrBaseItems)
                .filter(item -> !setOfRemoval.contains(item))
                .toArray(size -> Arrays.copyOf(arrBaseItems, size));
		
		if (minus.length == 0) {
			return null;
		}
		refresh(seedGen.getNextSeed());
		return minus[random.nextInt(minus.length)];
	}

}

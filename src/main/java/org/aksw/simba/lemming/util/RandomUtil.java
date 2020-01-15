package org.aksw.simba.lemming.util;

import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

import toools.set.IntSet;

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
    * Adaptation of method toools.set.IntSet.pickRandomElement(Random prng, IntSet excludedElements, boolean remove)
    * to accept empty excludedElements and throws exception when all elements of the set are to be excluded
    * @param set
    * @param prng
    * @param excludedElements
    * @param remove
    * @return
    */
    public static int pickRandomElement(IntSet set, Random prng, IntSet excludedElements, boolean remove){
    	if(excludedElements.size()>=set.size()) {
    		throw new IllegalArgumentException(
                    "Can't choose a random number since all possible numbers seem to be excluded.");
    	}
		while (true){
			int e = set.pickRandomElement(prng);
	
			if (excludedElements!=null && !excludedElements.contains(e)){
				if (remove){
					set.remove(e);
				}
				return e;
			}
		}
	}
}

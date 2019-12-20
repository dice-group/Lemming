package org.aksw.simba.lemming.util;

import java.util.Random;
import java.util.Set;

public class RandomUtil {
	
	public static int getRandomWithExclusion(Random rnd, int bound, Set<Integer> exclude) {
	    int random = rnd.nextInt(bound);
	    for (int ex : exclude) {
	        if (random == ex && random +1 < bound) {
	        	random++;
	        } else {
	        	return random;
	        }
	        
	    }
	    return random;
	}

	

}

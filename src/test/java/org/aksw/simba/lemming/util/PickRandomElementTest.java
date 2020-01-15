package org.aksw.simba.lemming.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

@RunWith(Parameterized.class)
public class PickRandomElementTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        IntSet setVertices = new DefaultIntSet();
        setVertices.addAll(0,1, 2, 3, 4,5,6,7);
        
        IntSet emptyExclusionSet = new DefaultIntSet();
        
        IntSet exclusionSet = new DefaultIntSet();
        exclusionSet.addAll(0,1,3);

        testConfigs.add(new Object[] {setVertices, emptyExclusionSet });
        testConfigs.add(new Object[] {setVertices, exclusionSet });
        
        return testConfigs;
    }

    private IntSet exclusionSet;
    private IntSet setVertices;

    public PickRandomElementTest(IntSet setVertices, IntSet exclusionSet) {
    	this.setVertices = setVertices;
    	this.exclusionSet = exclusionSet;
    }

    @Test
    public void test() {
        Random random = new Random();
        int tries = 20;
        while (tries > 0) {
            int vertId = RandomUtil.pickRandomElement(setVertices, random, exclusionSet, false);
            tries--;

            Assert.assertFalse(exclusionSet.contains(vertId));
            Assert.assertFalse(vertId > setVertices.size());
        }
    }
    
    @Test(expected = IllegalArgumentException.class,timeout=2000)
    public void testFailing() {
        Random random = new Random();
        Set<Integer> excludeAllSet = IntStream.range(0, setVertices.size()).mapToObj(i -> new Integer(i))
                .collect(Collectors.toSet());
        RandomUtil.getRandomWithExclusion(random, setVertices.size(), excludeAllSet);
    }

}

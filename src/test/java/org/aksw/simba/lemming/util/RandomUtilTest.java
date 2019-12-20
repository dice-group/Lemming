package org.aksw.simba.lemming.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Sets;

@RunWith(Parameterized.class)
public class RandomUtilTest {

	@Parameters
	public static Collection<Object[]> data() {
		List<Object[]> testConfigs = new ArrayList<Object[]>();

		testConfigs.add(new Object[] { Sets.newHashSet(1, 2), 5 });
		testConfigs.add(new Object[] { Sets.newHashSet(13423, 212), 250 });
		testConfigs.add(new Object[] { Sets.newHashSet(11, 2, 67, 998), 22 });

		return testConfigs;
	}

	private Set<Integer> exclusionSet;
	private int bound;

	public RandomUtilTest(Set<Integer> exclusionSet, int bound) {
		this.exclusionSet = exclusionSet;
		this.bound = bound;
	}

	@Test
	public void test() {
		Random random = new Random();
		int tries = 20;
		while (tries > 0) {
			int rand = RandomUtil.getRandomWithExclusion(random, bound, exclusionSet);
			tries--;

			Assert.assertFalse(exclusionSet.contains(rand));
			Assert.assertFalse(rand > bound);
		}
	}
}

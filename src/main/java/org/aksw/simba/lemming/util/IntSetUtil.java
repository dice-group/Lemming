package org.aksw.simba.lemming.util;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Sets.SetView;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class IntSetUtil {
	
	public static IntSet intersection(IntSet a, IntSet b) {
		SetView<Integer> set = Sets.intersection(a, b);
		IntSet resultSet = new DefaultIntSet(set.size());
		resultSet.addAll(set);
		return resultSet;
	}
	
	public static IntSet difference(IntSet a, IntSet b) {
		SetView<Integer> set = Sets.difference(a, b);
		IntSet resultSet = new DefaultIntSet(set.size());
		resultSet.addAll(set);
		return resultSet;
	}

	public static IntSet union(IntSet a, IntSet b) {
		SetView<Integer> set = Sets.union(a, b);
		IntSet resultSet = new DefaultIntSet(set.size());
		resultSet.addAll(set);
		return resultSet; 
	}
}

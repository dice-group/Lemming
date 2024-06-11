package org.aksw.simba.lemming.simplexes.distribution;

import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;

import com.carrotsearch.hppc.BitSet;

public interface IPropertyDist {

	BitSet proposePropColor(EdgeColorsSorted edgeColors);

}

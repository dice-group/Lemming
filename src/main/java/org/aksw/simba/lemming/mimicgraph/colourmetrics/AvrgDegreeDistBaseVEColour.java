package org.aksw.simba.lemming.mimicgraph.colourmetrics;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public interface AvrgDegreeDistBaseVEColour {
	
	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getAvrgDegree();
	public ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> getMaxDegree();
	
	public double getMaxDegreeOf(BitSet vertexColour, BitSet edgeColo);
	public double getAverageDegreeOf(BitSet vertexColour, BitSet edgeColo);

}

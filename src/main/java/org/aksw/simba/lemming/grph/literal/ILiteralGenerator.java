package org.aksw.simba.lemming.grph.literal;

import com.carrotsearch.hppc.BitSet;

public interface ILiteralGenerator {
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues);	
}

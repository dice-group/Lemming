package org.aksw.simba.lemming.util;

import java.util.Comparator;

import com.carrotsearch.hppc.BitSet;

public class BitSetComparator implements Comparator<BitSet> {

	@Override
	public int compare(BitSet o1, BitSet o2) {
	    if (o1.equals(o2)) return 0;
	    BitSet xor = (BitSet)o1.clone();
	    xor.xor(o2);
	    int firstDifferent = (int) (xor.length()-1);
	    if(firstDifferent==-1)
	            return 0;

	    return o2.get(firstDifferent) ? 1 : -1;
	}

}

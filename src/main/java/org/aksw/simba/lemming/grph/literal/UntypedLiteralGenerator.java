package org.aksw.simba.lemming.grph.literal;

import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public class UntypedLiteralGenerator extends AbstractLiteralGenerator implements ILiteralGenerator{

	public UntypedLiteralGenerator(
			Map<BitSet, Map<BitSet, Set<String>>> sampleData) {
		super(sampleData);
	}
	
	@Override
	public String getValue(BitSet tColo, BitSet dteColo, int numberOfValues){
		return "foo";
	}	
}

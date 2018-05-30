package org.aksw.simba.lemming.grph.generator;

import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public class RDFLiteralGeneratorTest implements IRDFLiteralGenerator{

	public RDFLiteralGeneratorTest(Map<BitSet, Set<String>> sampleData){
		
	}
	
	@Override
	public String getWords(BitSet dteColo, int numberOfWords) {
		return "hellow world";
	}
}

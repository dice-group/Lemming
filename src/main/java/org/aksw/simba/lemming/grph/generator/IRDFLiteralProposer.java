package org.aksw.simba.lemming.grph.generator;

import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public interface IRDFLiteralProposer {
	public String getWords(BitSet dteColo, Set<String> setOfWords, int numberOfWords);
}

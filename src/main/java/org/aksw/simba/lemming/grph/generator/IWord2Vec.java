package org.aksw.simba.lemming.grph.generator;

import java.util.Map;
import java.util.Set;

public interface IWord2Vec {
	public float[] getVector(String word);
	public String getWord(float[] vector);
	public String[] getWords(float[] vector);
	public Map<String, float[]> getMapWordToVector(Set<String> setOfWords);
	public String getWords(Set<String> setOfWords, int numberOfWords);
}

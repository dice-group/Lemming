package org.aksw.simba.lemming.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * This class allows for the conversion between com.carrotsearch.hppc specific objects 
 * and java.util objects in order to enable serialization
 * @author Ana
 *
 */
public class SerializationParser {

	/*---------------------------------------------------
		Parsing java.util equivalents to com.carrotsearch.hppc objects
	----------------------------------------------------*/
	
	/**
	 * Converts a java.util.BitSet to an equivalent com.carrotsearch.hppc.BitSet
	 * @param bitSet java.util.BitSet
	 * @return com.carrotsearch.hppc.BitSet
	 */
	public static BitSet parseSpBitSet(java.util.BitSet bitSet) {
		long[] bits = bitSet.toLongArray();
		BitSet spBitset = new BitSet(bits, bits.length);
		return spBitset;
	}
	
	/**
	 * Converts a List of java.util.BitSet to an equivalent ObjectArrayList of 
	 * com.carrotsearch.hppc.BitSet
	 * @param utilList List<java.util.BitSet>
	 * @return ObjectArrayList<com.carrotsearch.hppc.BitSet>
	 */
	public static ObjectArrayList<BitSet> parseBitSetArrayList(List<java.util.BitSet> utilList) {
		ObjectArrayList<BitSet> objectArrayList = new ObjectArrayList<BitSet>();
		for(java.util.BitSet utilBitSet: utilList) {
			BitSet spBitSet = parseSpBitSet(utilBitSet);
			objectArrayList.add(spBitSet);
		}
		return objectArrayList;
	}
	
	/**
	 * Converts a Map(String, java.util.BitSet) to a Map(String, com.carrotsearch.hppc.BitSet)
	 * @param map String, java.util.BitSet
	 * @return String, com.carrotsearch.hppc.BitSet
	 */
	public static Map<String, BitSet> parseSpBitSetMap(Map<String, java.util.BitSet> map) {
		Map<String, BitSet> spMap = new HashMap<String, BitSet>();
		Iterator<Entry<String, java.util.BitSet>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			BitSet spBitSet = parseSpBitSet((java.util.BitSet) pair.getValue());
			spMap.put((String) pair.getKey(), spBitSet);
		}
		return spMap;
	}
	
	/*---------------------------------------------------
		Parsing com.carrotsearch.hppc objects to java.util equivalents
	----------------------------------------------------*/
	
	/**
	 * Converts ObjectArrayList to an equivalent List
	 * @param objectArrayList
	 * @return List
	 */
	public static List<java.util.BitSet> parseBitSetArrayList(ObjectArrayList<BitSet> objectArrayList) {
		Iterator<ObjectCursor<BitSet>> vertexColours = objectArrayList.iterator();
		List<java.util.BitSet> serVertexColours = new ArrayList<java.util.BitSet>();
		while (vertexColours.hasNext()) {
			java.util.BitSet parsed = parseBitSet(vertexColours.next().value);
			serVertexColours.add(parsed);
		}
		return serVertexColours;
	}

	/**
	 * Converts a com.carrotsearch.hppc.BitSet to an equivalent java.util.BitSet
	 * @param bitSet com.carrotsearch.hppc.BitSet
	 * @return java.util.BitSet
	 */
	public static java.util.BitSet parseBitSet(BitSet bitSet) {
		long[] bits = bitSet.bits;
		java.util.BitSet utilBitset = java.util.BitSet.valueOf(bits);
		return utilBitset;
	}

	/**
	 * Converts the bitsets in the map to java.util.BitSet
	 * @param map
	 * @return
	 */
	public static Map<String, java.util.BitSet> parseBitSetMap(Map<String, BitSet> map) {
		Map<String, java.util.BitSet> parsedMap = new HashMap<String, java.util.BitSet>();
		Iterator<Entry<String, BitSet>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			java.util.BitSet parsed = parseBitSet((BitSet) pair.getValue());
			parsedMap.put((String) pair.getKey(), parsed);
		}
		return parsedMap;
	}
}

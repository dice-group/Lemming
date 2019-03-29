package org.aksw.simba.lemming.util;

import java.util.Map;

import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.ColourPaletteWrapper;
import org.aksw.simba.lemming.colour.InMemoryPalette;

import com.carrotsearch.hppc.BitSet;

/**
 * This class is a Persistence Helper that allows the parsing of the non-serializable project specific objects to 
 * serializable ones by converting the objects to their corresponding Wrapper classes before persisting the data
 * and vice-versa when reading the data.
 * @author Ana
 *
 */
public class PersHelper {
	
		public static ColourPalette convertCP(ColourPaletteWrapper graphPalette) {
			ColourPaletteWrapper colourPaletteWrapper = new ColourPaletteWrapper(
					graphPalette.getUriColourMap(), 
					graphPalette.getHighestColourId());
			ColourPalette colourPalette = new InMemoryPalette();
			colourPalette.setHighestColourId(colourPaletteWrapper.getHighestColourId());
			colourPalette.setUriColourMap(SerializationParser.parseSpBitSetMap(colourPaletteWrapper.getUriColourMap()));
			return colourPalette;
			
		}
		
		public static ColourPaletteWrapper convertCP(ColourPalette colourPalette) {
			Map<String, BitSet> vpMap = colourPalette.getMapOfURIAndColour();
			Map<String, java.util.BitSet> vpUtilMap = SerializationParser.parseBitSetMap(vpMap);
			ColourPaletteWrapper vertexPaletteWrapper = new ColourPaletteWrapper(vpUtilMap,
					colourPalette.getHighestColourId());
			return vertexPaletteWrapper;
			
		}

}

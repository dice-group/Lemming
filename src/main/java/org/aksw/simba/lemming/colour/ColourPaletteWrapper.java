package org.aksw.simba.lemming.colour;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.BitSet;

/**
 * Wrapper Class to enable the persistence of the ColourPalette class
 * @author Ana
 *
 */
public class ColourPaletteWrapper implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3744241658289486040L;
	protected Map<String, BitSet> uriColourMap = new HashMap<String, BitSet>();
    protected int highestColourId = 0;
    
	public ColourPaletteWrapper() {
		super();
	}
	

	public ColourPaletteWrapper(Map<String, BitSet> uriColourMap, int highestColourId) {
		super();
		this.uriColourMap = uriColourMap;
		this.highestColourId = highestColourId;
	}



	public Map<String, BitSet> getUriColourMap() {
		return uriColourMap;
	}

	public void setUriColourMap(Map<String, BitSet> uriColourMap) {
		this.uriColourMap = uriColourMap;
	}

	public int getHighestColourId() {
		return highestColourId;
	}

	public void setHighestColourId(int highestColourId) {
		this.highestColourId = highestColourId;
	}
    
	
    
    
    

}

package org.aksw.simba.lemming.colour;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.vocabulary.RDF;

import com.carrotsearch.hppc.BitSet;

public class InMemoryPalette implements ColourPalette {

	protected Map<String, BitSet> uriColourMap = new HashMap<String, BitSet>();
	protected int highestColourId = 0;

    @Override
    public void addColour(String uri) {
        if (!uriColourMap.containsKey(uri)) {
            BitSet colour = new BitSet(highestColourId);
            colour.set(highestColourId);
            uriColourMap.put(uri, colour);
            ++highestColourId;
        }
}

    @Override
    public BitSet getColour(String uri) {
        if (uriColourMap.containsKey(uri)) {
            return uriColourMap.get(uri);
        } else {
            return new BitSet();
        }
}

    @Override
    public BitSet getColourMixture(String... uris) {
        BitSet mixture = new BitSet();
        for (int i = 0; i < uris.length; ++i) {
            if (uriColourMap.containsKey(uris[i])) {
                mixture.or(uriColourMap.get(uris[i]));
            }
        }
        return mixture;
}

    @Override
    public BitSet getColourMixture(Set<String> uris) {
        BitSet mixture = new BitSet();
        for (String uri : uris) {
            if (uriColourMap.containsKey(uri)) {
                mixture.or(uriColourMap.get(uri));
            }
        }
        return mixture;
    }

    @Override
    public BitSet addToColour(BitSet colour, String uri) {
		if (uriColourMap.containsKey(uri)) {
			colour.or(uriColourMap.get(uri));
		}
		return colour;
    }

    @Override
    public void mixColour(String source, String target) {
        if (!uriColourMap.containsKey(source)) {
            return;
        }

        BitSet targetColour;
        if (uriColourMap.containsKey(target)) {
            targetColour = uriColourMap.get(target);
            targetColour.or(uriColourMap.get(source));
        }
}

    @Override
    public boolean containsUri(String uri) {
        return uriColourMap.containsKey(uri);
    }

    @Override
    public void setColour(String uri, BitSet colour) {
        uriColourMap.put(uri, colour);
    }

    @Override
    public Set<String> getURIs(BitSet inColour, boolean isProperty){
    	Set<String> setOfURIs = new HashSet<String>();
    	 if(inColour != null){
//	    	Object[] arrOfURIs = uriColourMap.keys;
			Object[] arrOfURIs = uriColourMap.keySet().toArray();
			for (int i = 0; i < arrOfURIs.length; i++) {
//	    		if(uriColourMap.allocated[i]){
				String uri = (String) arrOfURIs[i];
				BitSet colo = (BitSet) uriColourMap.get(uri).clone();
				if (isProperty) {
					// just compare if 2 bitsets are really equal
					if (colo.equals(inColour)) {
						setOfURIs.add(uri);
						break;
					}
				} else {
					// and 2 bitsets
					colo.and(inColour);

					// check if they have matching bits 1
					if (colo.cardinality() <= inColour.cardinality() && !colo.isEmpty()) {
						setOfURIs.add(uri);
					}
				}
//	    		}
			}
		}
		return setOfURIs;
	}

    @Override
    public boolean isColourOfRDFType(BitSet colour){
    	BitSet rdfTypeColour = uriColourMap.get(RDF.type.toString());
    	return colour.equals(rdfTypeColour);
    }
    
	@Override
	public void updateColour(BitSet colour, String uri) {
		if(colour!= null){
			if(uriColourMap.containsKey(uri)){
				BitSet tmpColo = uriColourMap.get(uri);
				if(!tmpColo.equals(colour)){
					colour.or(uriColourMap.get(uri));
					System.err.println("Same URI but different colours");
				}
			}
			uriColourMap.put(uri, colour);
		}
}
	
	@Override
	public void setUriColourMap(Map<String, BitSet> uriColourMap) {
		this.uriColourMap = uriColourMap;
	}

	@Override
	public Map<String, BitSet> getMapOfURIAndColour() {
		return uriColourMap;
	}

	@Override
	public int getHighestColourId() {
		return highestColourId;
	}

	@Override
	public void setHighestColourId(int highestColourId) {
		this.highestColourId = highestColourId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InMemoryPalette other = (InMemoryPalette) obj;
		if (highestColourId != other.highestColourId)
			return false;
		if (uriColourMap == null) {
			if (other.uriColourMap != null)
				return false;
		} else if (!uriColourMap.equals(other.uriColourMap))
			return false;
		return true;
	}

}

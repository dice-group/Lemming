package org.aksw.simba.lemming.colour;

import java.util.Set;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class InMemoryPalette implements ColourPalette {

    protected ObjectObjectOpenHashMap<String, BitSet> uriColourMap = new ObjectObjectOpenHashMap<String, BitSet>();
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

}

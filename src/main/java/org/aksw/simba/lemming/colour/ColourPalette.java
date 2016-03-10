package org.aksw.simba.lemming.colour;

import java.util.Set;

import com.carrotsearch.hppc.BitSet;

public interface ColourPalette {

    public void addColour(String uri);

    public BitSet getColour(String uri);

    public BitSet getColourMixture(String... uris);

    public BitSet getColourMixture(Set<String> uris);

    public BitSet addToColour(BitSet colour, String uri);

    public void mixColour(String source, String target);

    public boolean containsUri(String uri);
}

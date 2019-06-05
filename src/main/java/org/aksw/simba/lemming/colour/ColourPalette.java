package org.aksw.simba.lemming.colour;

import java.util.Map;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;

/**
 * A colour palette is a mapping of class or property URIs to colours.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public interface ColourPalette {

    /**
     * Adds the given URI to the mapping creating a new colour.
     * 
     * @param uri
     *            the URI that should be added to the mapping
     */
    public void addColour(String uri);

    /**
     * Returns the colour of the given URI or null if there is no colour for
     * this URI.
     * 
     * @param uri
     *            the URI for which the colour should be returned
     * @return the colour of the given URI or null if there is no colour for the
     *         given URI
     */
    public BitSet getColour(String uri);

    /**
     * Returns a mixture of the given colours.
     * 
     * @param uris
     *            the URIs of which the colours should be mixed
     * @return a mixture colour containing the colours of the given URIs
     */
    public BitSet getColourMixture(String... uris);

    /**
     * Returns a mixture of the given colours.
     * 
     * @param uris
     *            the URIs of which the colours should be mixed
     * @return a mixture colour containing the colours of the given URIs
     */
    public BitSet getColourMixture(Set<String> uris);

    /**
     * Adds the colour of the given URI to the given colour. If the given URI is
     * not known, the colour is not changed.
     * 
     * @param colour
     *            the colour that should be mixed with the colour of the given
     *            URI.
     * @param uri
     *            the URI of which the colour should be added to the given
     *            colour.
     * @return the given colour.
     */
    public BitSet addToColour(BitSet colour, String uri);

    /**
     * Mixes the colour of the source URI into the colour of the target URI.
     * 
     * @param source
     *            the URI of the source of which the colour should be added to
     *            the target colour
     * @param target
     *            the URI of the target to which the colour of the source will
     *            be added
     */
    public void mixColour(String source, String target);

    /**
     * Returns true if there is a colour for the given URI.
     * 
     * @param uri
     *            the URI for which the existence of a colour should be checked.
     * @return true if the URI is known, else false
     */
    public boolean containsUri(String uri);

    /**
     * Sets the colour of the given URI to the given colour. If the URI is
     * already known, its colour is overwritten.
     * 
     * @param uri
     *            the URI for which the colour should be set
     * @param colour
     *            the new colour of the given URI
     */
    public void setColour(String uri, BitSet colour);

    
    /**
     * Get the URIs corresponding to the given colour.
     * @param colour a given colour (possible mixture colours) whose URIs should be returned
     * @param isProperty 
     * 				if the colour is the colour of properties (or of edges)
     * @return a set of URIS
     */
    public Set<String> getURIs(BitSet colour, boolean isProperty);
    
    /**
     * Update colour for an existing URI. If the URI is not existing ==> create new
     * 
     * @param colour the colour of the updated URI
     * @param uri the URI that would be updated
     */
    public void updateColour(BitSet colour, String uri);
    
    /**
     * Get the map of URIs and colours. This function supports for the evaluation process only.
     * @return the map of URIs and colours
     */
    public Map<String, BitSet> getMapOfURIAndColour();
    
    public boolean isColourOfRDFType(BitSet colour);

    public int getHighestColourId();

    public void setHighestColourId(int highestColourId);

    public void setUriColourMap(Map<String, BitSet> uriColourMap);
    
}

package org.aksw.simba.lemming;

import java.io.Serializable;

import org.aksw.simba.lemming.colour.ColourPaletteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.List;

import grph.Grph;

/**
 * Wrapper Class to enable the persistence of the ColouredGraph class
 * @author Ana
 *
 */
public class ColouredGraphWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7964247861129517487L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ColouredGraphWrapper.class);

	protected Grph graph;
	protected List<BitSet> vertexColours;
	protected List<BitSet>  edgeColours;
	protected ColourPaletteWrapper vertexPalette;
	protected ColourPaletteWrapper edgePalette;
	protected ColourPaletteWrapper dtEdgePalette;

	public ColouredGraphWrapper() {
		super();
	}

	public ColouredGraphWrapper(Grph graph, List <BitSet>  vertexColours, List <BitSet> edgeColours,
			ColourPaletteWrapper vertexPalette, ColourPaletteWrapper edgePalette, ColourPaletteWrapper dtEdgePalette) {
		super();
		this.graph = graph;
		this.vertexColours = vertexColours;
		this.edgeColours = edgeColours;
		this.vertexPalette = vertexPalette;
		this.edgePalette = edgePalette;
		this.dtEdgePalette = dtEdgePalette;
	}

	public Grph getGraph() {
		return graph;
	}

	public void setGraph(Grph graph) {
		this.graph = graph;
	}

	public List <BitSet>  getVertexColours() {
		return vertexColours;
	}

	public void setVertexColours(List <BitSet>  vertexColours) {
		this.vertexColours = vertexColours;
	}

	public List <BitSet>  getEdgeColours() {
		return edgeColours;
	}

	public void setEdgeColours(List <BitSet>  edgeColours) {
		this.edgeColours = edgeColours;
	}

	public ColourPaletteWrapper getVertexPalette() {
		return vertexPalette;
	}

	public void setVertexPalette(ColourPaletteWrapper vertexPalette) {
		this.vertexPalette = vertexPalette;
	}

	public ColourPaletteWrapper getEdgePalette() {
		return edgePalette;
	}

	public void setEdgePalette(ColourPaletteWrapper edgePalette) {
		this.edgePalette = edgePalette;
	}

	public ColourPaletteWrapper getDtEdgePalette() {
		return dtEdgePalette;
	}

	public void setDtEdgePalette(ColourPaletteWrapper dtEdgePalette) {
		this.dtEdgePalette = dtEdgePalette;
	}

}

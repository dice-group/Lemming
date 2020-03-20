package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Map;

import org.aksw.simba.lemming.BaselineGraph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.InMemoryPalette;
import org.aksw.simba.lemming.creation.BaselineCreator;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import toools.set.IntSet;

public class BaselineGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(BaselineGenerator.class);
	private ColouredGraph mimicGraph;
	private BaselineGraph baselineGraph;
	
	public BaselineGenerator(int noNodes, ColouredGraph[] colouredGraphs, long seed) {	
		// colour distribution
		ObjectDistribution<BitSet> vertexDistribution = AvrgVertColoDistMetric.apply(colouredGraphs);
		ObjectDistribution<BitSet> edgeDistribution = AvrgEdgeColoDistMetric.apply(colouredGraphs);

		// estimate an average degree
		double avgDegree = estimateNoEdges(colouredGraphs, noNodes)/noNodes;
		
		// Generate a random, scale-free Barabasi graph
		baselineGraph = new BaselineGraph(noNodes, avgDegree, seed);
		
		// assign colours
		BaselineCreator baselineCreator = new BaselineCreator(baselineGraph);
		baselineCreator.applyEdgeDistribution(edgeDistribution);
		baselineCreator.applyVertexDistribution(vertexDistribution);
		
		mimicGraph = new ColouredGraph(baselineGraph.getGraph().getGraph(), null, null, null);
		copyColourPalette(colouredGraphs, mimicGraph);	
	}
	
	public Map<BitSet, IntSet> getColourVertexIds() {
		return baselineGraph.getColourVertexIds();
	}

	public ColouredGraph getmMimicGraph() {
		return mimicGraph;
	}
	/**
	 * org.aksw.simba.lemming.mimicgraph.generator.AbstractGraphGeneration
	 */
	
	/**
	 * draft estimation of number edges
	 * @param origGrphs
	 */
	protected double estimateNoEdges(ColouredGraph[] origGrphs, int noVertices){
		LOGGER.info("Estimate the number of edges in the new graph.");
		double estimatedEdges = 0;
		if(origGrphs != null && origGrphs.length >0){
			int iNoOfVersions = origGrphs.length;
			double noEdges = 0;
			for(ColouredGraph graph: origGrphs){
				int iNoEdges = graph.getEdges().size();
				int iNoVertices = graph.getVertices().size();
				noEdges += iNoEdges/(iNoVertices *1.0); 
			}
			noEdges *= noVertices;
			noEdges /= iNoOfVersions;
			estimatedEdges = (int)Math.round(noEdges);
			LOGGER.warn("Estimated the number of edges in the new graph is " + estimatedEdges);
		}else{
			LOGGER.warn("The array of original graphs is empty!");
		}
		return estimatedEdges;
	}
	
	protected void copyColourPalette(ColouredGraph[] origGraphs, ColouredGraph mimicGraph){
		if(Constants.IS_EVALUATION_MODE){
			ColourPalette newVertexPalette = new InMemoryPalette();
			ColourPalette newEdgePalette = new InMemoryPalette();
			ColourPalette newDTEdgePalette = new InMemoryPalette();
			
			//copy colour palette of all the original graphs to the new one
			for(ColouredGraph grph: origGraphs){
				// merge vertex colours
				ColourPalette vPalette = grph.getVertexPalette();
				Map<String, BitSet>mapVertexURIsToColours =  vPalette.getMapOfURIAndColour();
				fillColourToPalette(newVertexPalette, mapVertexURIsToColours);
				
				// merge edge colours
				ColourPalette ePalette = grph.getEdgePalette();
				Map<String, BitSet> mapEdgeURIsToColours = ePalette.getMapOfURIAndColour();
				fillColourToPalette(newEdgePalette, mapEdgeURIsToColours);
				
				// merge data typed edge colours
				ColourPalette dtePalette = grph.getDataTypedEdgePalette();
				Map<String, BitSet> mapDTEdgeURIsToColours = dtePalette.getMapOfURIAndColour();
				fillColourToPalette(newDTEdgePalette, mapDTEdgeURIsToColours);
			}
			
			mimicGraph.setVertexPalette(newVertexPalette);
			mimicGraph.setEdgePalette(newEdgePalette);
			mimicGraph.setDataTypeEdgePalette(newDTEdgePalette);
		}
	}
	
	private void fillColourToPalette(ColourPalette palette, Map<String, BitSet> mapOfURIsAndColours){
		Object[]arrObjURIs = mapOfURIsAndColours.keySet().toArray();
		for(int i = 0 ; i < arrObjURIs.length ; i++){
				String uri = (String) arrObjURIs[i];
				BitSet colour = mapOfURIsAndColours.get(uri);
				palette.updateColour(colour, uri);
//			}
		}
	}
	
	

	
	
}
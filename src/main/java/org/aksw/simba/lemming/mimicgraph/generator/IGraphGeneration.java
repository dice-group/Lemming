package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.IDatasetManager;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.vertexselection.IVertexSelector;

import com.carrotsearch.hppc.BitSet;

import it.unimi.dsi.fastutil.ints.IntSet;

//public interface IGraphGeneration {
//	public ColouredGraph generateGraph();
//	public IColourMappingRules getColourMapper();
//	public Map<BitSet,IntSet> getMappingColoursAndVertices();
//	public Map<BitSet,IntSet> getMappingColoursAndEdges();
//
//	public TripleBaseSingleID getProposedTriple(IVertexSelector vertexSelector);
//	
//	public ColouredGraph getMimicGraph();
//	public String getLiteralType(BitSet dteColo);
//	public void setMimicGraph(ColouredGraph refinedGraph);
//	public void setNumberOfThreadsForGenerationProcess(int numberOfThreads);
//	public long getSeed();
//	public void loadOrGenerateGraph(IDatasetManager mDatasetManager, String mimicGraphLoad);
//}

public interface IGraphGeneration {
	public ColouredGraph generateGraph();
	public IColourMappingRules getColourMapper();
	public Map<BitSet,IntSet> getMappingColoursAndVertices();
	public Map<BitSet,IntSet> getMappingColoursAndEdges();
	
	//public BitSet getProposedEdgeColour(BitSet headColour, BitSet tailColour);
	//public BitSet getProposedHeadColour(BitSet edgeColour, BitSet tailColour);
	//public BitSet getProposedTailColour(BitSet headColour, BitSet edgeColour);
	public TripleBaseSingleID getProposedTriple(boolean isRamdom);
	
	public ColouredGraph getMimicGraph();
	public String getLiteralType(BitSet dteColo);
	public void setMimicGraph(ColouredGraph refinedGraph);
	public void setNumberOfThreadsForGenerationProcess(int numberOfThreads);
	public long getSeed();
}

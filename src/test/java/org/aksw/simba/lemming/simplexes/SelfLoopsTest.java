package org.aksw.simba.lemming.simplexes;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric2;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRulesSimplexes;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;

public class SelfLoopsTest {
	
	/**
	 * Test for isolated triangle with vertices having self loops.
	 */
	//@Test
	public void test1() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("isolatedtri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		
		SelfLoopInSimplexAnalysis selfIsoTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfIsoTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsIsolatedTri()); // analyze graph to find self loops
		selfIsoTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions); // compute estimated number of edges for output graph
		
		//Estimated edges
		int estEdgesSelfLoopIsoTri = selfIsoTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoopIsoTri);
		
		IColourMappingRules mColourMapperSelfLoopIsoTri = new ColourMappingRulesSimplexes(selfIsoTriAnalysis.getmGraphIdEdgeIdsForSelfLoop()); // Define mapping of colors for triples
		mColourMapperSelfLoopIsoTri.analyzeRules(origGrphs);
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoopIsoTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoopIsoTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopIsoTri = selfIsoTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom); // create probability distribution for the vertex color
		System.out.println("Propose color of vertex for self loop: ");
		System.out.println(distColoProposerSelfLoopIsoTri.getPotentialItem());
		System.out.println(distColoProposerSelfLoopIsoTri.getPotentialItem());
	}
	
	/**
	 * Test for isolated triangle with vertices having self loops. (Negative test)
	 */
	//@Test
	public void test2() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("triangle_file1.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		
		SelfLoopInSimplexAnalysis selfIsoTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfIsoTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsIsolatedTri()); // analyze graph to find self loops
		selfIsoTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions); // compute estimated number of edges for output graph
		
		//Estimated edges
		int estEdgesSelfLoopIsoTri = selfIsoTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoopIsoTri);
		
		IColourMappingRules mColourMapperSelfLoopIsoTri = new ColourMappingRulesSimplexes(selfIsoTriAnalysis.getmGraphIdEdgeIdsForSelfLoop()); // Define mapping of colors for triples
		mColourMapperSelfLoopIsoTri.analyzeRules(origGrphs);
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoopIsoTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoopIsoTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopIsoTri = selfIsoTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom); // create probability distribution for the vertex color
		if (distColoProposerSelfLoopIsoTri == null)
			System.out.println("Test Passed!");
	}
	
	/**
	 * Test for self loops in connected triangles. (Positive test case)
	 */
	//@Test
	public void test3() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("conntri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		SelfLoopInSimplexAnalysis selfConnTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfConnTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsConnectTriangles());
		selfConnTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		
		int estEdgesSelfLoopConnTri = selfConnTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoopConnTri);
		
		
		IColourMappingRules mColourMapperSelfLoopConnTri = new ColourMappingRulesSimplexes(selfConnTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoopConnTri.analyzeRules(origGrphs);
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopConnTri = selfConnTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoopConnTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoopConnTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		System.out.println("Propose color of vertex for self loop: ");
		System.out.println(distColoProposerSelfLoopConnTri.getPotentialItem());
		System.out.println(distColoProposerSelfLoopConnTri.getPotentialItem());
	}
	
	/**
	 * Test for self loops in connected triangles. (Negative test case)
	 */
	//@Test
	public void test4() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("isolatedtri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		SelfLoopInSimplexAnalysis selfConnTriAnalysis = new SelfLoopInSimplexAnalysis();
		selfConnTriAnalysis.analyze(origGrphs, metric.getmGraphsVertIdsConnectTriangles());
		selfConnTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		
		int estEdgesSelfLoopConnTri = selfConnTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoopConnTri);
		
		
		IColourMappingRules mColourMapperSelfLoopConnTri = new ColourMappingRulesSimplexes(selfConnTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoopConnTri.analyzeRules(origGrphs);
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoopConnTri = selfConnTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoopConnTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoopConnTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		if (distColoProposerSelfLoopConnTri == null)
			System.out.println("Test passed");
		
	}
	
	
	/**
	 * self loops in 1-simplexes connected only to triangles.
	 */
	//@Test
	public void test5() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("1simplexesconntri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		
		SelfLoopInSimplexAnalysis self1SimplexesConnToTriAnalysis = new SelfLoopInSimplexAnalysis();
		self1SimplexesConnToTriAnalysis.analyze(origGrphs, metric.getmGraphsVertId1SimplexesConnToTri());
		self1SimplexesConnToTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		int estEdgesSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoop1SimplexConnToTri);
		
		IColourMappingRules mColourMapperSelfLoop1SimplexConnToTri = new ColourMappingRulesSimplexes(self1SimplexesConnToTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoop1SimplexConnToTri.analyzeRules(origGrphs);
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoop1SimplexConnToTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoop1SimplexConnToTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		System.out.println("Propose color of vertex for self loop: ");
		System.out.println(distColoProposerSelfLoop1SimplexConnToTri.getPotentialItem());
		System.out.println(distColoProposerSelfLoop1SimplexConnToTri.getPotentialItem());
	}

	/**
	 * self loops in 1-simplexes connected only to triangles. (Negative test case)
	 */
	@Test
	public void test6() {
		
		int iNoOfVersions = 1;
		
		ColouredGraph[] origGrphs = new ColouredGraph[iNoOfVersions];
		ColouredGraph graphInput = IOHelper.readGraphFromResource("isolatedtri_selfloop.n3", "N3");
		origGrphs[0] = graphInput;
		
		
		int mIDesiredNoOfVertices = 1000;
		
		//random object
		long seed = Long.parseLong("1694360290695");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		// Call metric
		NodeIteratorMetric2 metric= new NodeIteratorMetric2();
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// compute triangles information for input graph
				metric.computeTriangles(graph);
			}
		}
		
		
		SelfLoopInSimplexAnalysis self1SimplexesConnToTriAnalysis = new SelfLoopInSimplexAnalysis();
		self1SimplexesConnToTriAnalysis.analyze(origGrphs, metric.getmGraphsVertId1SimplexesConnToTri());
		self1SimplexesConnToTriAnalysis.computeEstimatedEdges(origGrphs, mIDesiredNoOfVertices, iNoOfVersions);
		int estEdgesSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.getEstimatedNoEdges();
		System.out.println("Estimated edges for self loop: " + estEdgesSelfLoop1SimplexConnToTri);
		
		IColourMappingRules mColourMapperSelfLoop1SimplexConnToTri = new ColourMappingRulesSimplexes(self1SimplexesConnToTriAnalysis.getmGraphIdEdgeIdsForSelfLoop());
		mColourMapperSelfLoop1SimplexConnToTri.analyzeRules(origGrphs);
		
		OfferedItemByRandomProb<BitSet> distColoProposerSelfLoop1SimplexConnToTri = self1SimplexesConnToTriAnalysis.createVertColoProposer(iNoOfVersions, mRandom);
		
		for (ColouredGraph graph : origGrphs) {
			if (graph!= null) {
				// iterate over graph edges and propose colors
				for (int edgeId: graph.getEdges()) {
						int headOfTheEdge = graph.getHeadOfTheEdge(edgeId);
						BitSet possHeadColour = graph.getVertexColour(headOfTheEdge);
						Set<BitSet> tailColours = mColourMapperSelfLoop1SimplexConnToTri.getTailColours(possHeadColour);
						if (tailColours.size() > 0) {
						System.out.println("Head color: " + possHeadColour);
						System.out.println("Returned Tail Color(s) by color mapper: " + tailColours);
						
						int tailOfTheEdge = graph.getTailOfTheEdge(edgeId);
						BitSet possTailColour = graph.getVertexColour(tailOfTheEdge);
						Set<BitSet> headColours = mColourMapperSelfLoop1SimplexConnToTri.getHeadColours(possTailColour);
						System.out.println("Tail color: " + possTailColour);
						System.out.println("Returned Head Color(s) by color mapper: " + headColours);
						break;
					}
				}
				
			}
		}
		
		if (distColoProposerSelfLoop1SimplexConnToTri == null) {
			System.out.println("Test passed");
		}
	}
	
}

package org.aksw.simba.lemming.simplexes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric2;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import it.unimi.dsi.fastutil.ints.IntSet;


public class PermutationTrianglesMapTest {
	
	//@Test
	public void singleTriangle() {
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file1.n3", "N3");
		
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);

		System.out.println(graph.getRDFTypePropertyColour());
		
		NodeIteratorMetric metric;
		metric = new NodeIteratorMetric();
		metric.computeTriangles(graph);
		
		TriangleDistribution triangleDistributionObj = new TriangleDistribution(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes(), metric.getmTriangleColorsProbability(), 1, mRandom);
		System.out.println(triangleDistributionObj.getmTriangleColorsv1v2v3());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv1v3v2());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv2v3v1());
		
		//get vertex colors of existing graph and check if distributions can propose vertex color for them
		IntSet vertices = graph.getVertices();
		
		// list for storing all vertex colors
		List<BitSet> vertexColors = new ArrayList<BitSet>();
		
		//iterate over vertices
		for (int vertexId: vertices) {
			BitSet vertexColour = graph.getVertexColour(vertexId);
			if (!vertexColour.isEmpty())
				vertexColors.add(vertexColour);
		}
		
		//TODO: Add assertions for below
		System.out.println("Input Vertex colors: " + vertexColors.get(0) + ", " + vertexColors.get(1));
		BitSet proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(vertexColors.get(0), vertexColors.get(1));
		System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
		
		System.out.println("Input Vertex colors: " + vertexColors.get(1) + ", " + vertexColors.get(2));
		proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(vertexColors.get(1), vertexColors.get(2));
		System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
		
		System.out.println("Input Vertex colors: " + vertexColors.get(0) + ", " + vertexColors.get(2));
		proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(vertexColors.get(0), vertexColors.get(2));
		System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
	}
	
	//@Test
	public void twoTrianglesWithCommonEdge() {
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file2.n3", "N3");
//		
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
//		
//		
		System.out.println(graph.getRDFTypePropertyColour());
//		
		NodeIteratorMetric metric;
		metric = new NodeIteratorMetric();
		metric.computeTriangles(graph);
//		
		TriangleDistribution triangleDistributionObj = new TriangleDistribution(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes(), metric.getmTriangleColorsProbability(), 1, mRandom);
		System.out.println(triangleDistributionObj.getmTriangleColorsv1v2v3());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv1v3v2());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv2v3v1());
		
		// get map storing triangles information
		ObjectObjectOpenHashMap<TriangleColours, double[]> getmTrianglesColoursTrianglesCountsResourceNodes = metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes();
		
		// temporary variable to store the triangles
		Set<TriangleColours> setTriangles = new HashSet<TriangleColours>();
		
		Object[] keys = getmTrianglesColoursTrianglesCountsResourceNodes.keys;
		for (int i = 0; i < keys.length;i++) {
			if (getmTrianglesColoursTrianglesCountsResourceNodes.allocated[i]) {
				TriangleColours triangle = (TriangleColours) keys[i];
				setTriangles.add(triangle);
			}
		}
		
		// probability distribution for the found triangles
		for (TriangleColours triangleObj: setTriangles) {
			
			System.out.println("Input Vertex colors: " + triangleObj.getA() + ", " + triangleObj.getB());
			BitSet proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getA(), triangleObj.getB());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
			
			System.out.println("Input Vertex colors: " + triangleObj.getB() + ", " + triangleObj.getC());
			proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getB(), triangleObj.getC());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
			
			System.out.println("Input Vertex colors: " + triangleObj.getA() + ", " + triangleObj.getC());
			proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getA(), triangleObj.getC());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
		}
//		
		
	}
	
	//@Test
	public void twoSeparateTriangles() {
		ColouredGraph graph = IOHelper.readGraphFromResource("triangle_file3.n3", "N3");
//		
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
//		
//		
		System.out.println(graph.getRDFTypePropertyColour());
//		
		NodeIteratorMetric metric;
		metric = new NodeIteratorMetric();
		metric.computeTriangles(graph);
//		
		TriangleDistribution triangleDistributionObj = new TriangleDistribution(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes(), metric.getmTriangleColorsProbability(), 1, mRandom);
		System.out.println(triangleDistributionObj.getmTriangleColorsv1v2v3());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv1v3v2());
		//System.out.println(triangleDistributionObj.getmTriangleColorsv2v3v1());
		
		// get map storing triangles information
		ObjectObjectOpenHashMap<TriangleColours, double[]> getmTrianglesColoursTrianglesCountsResourceNodes = metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes();
				
				// temporary variable to store the triangles
				Set<TriangleColours> setTriangles = new HashSet<TriangleColours>();
				
				Object[] keys = getmTrianglesColoursTrianglesCountsResourceNodes.keys;
				for (int i = 0; i < keys.length;i++) {
					if (getmTrianglesColoursTrianglesCountsResourceNodes.allocated[i]) {
						TriangleColours triangle = (TriangleColours) keys[i];
						setTriangles.add(triangle);
					}
				}
				
				// probability distribution for the found triangles
		for (TriangleColours triangleObj: setTriangles) {
					
			System.out.println("Input Vertex colors: " + triangleObj.getA() + ", " + triangleObj.getB());
			BitSet proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getA(), triangleObj.getB());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
					
			System.out.println("Input Vertex colors: " + triangleObj.getB() + ", " + triangleObj.getC());
			proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getB(), triangleObj.getC());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
					
			System.out.println("Input Vertex colors: " + triangleObj.getA() + ", " + triangleObj.getC());
			proposeVertexColorForVertex3 = triangleDistributionObj.proposeVertexColorForVertex3(triangleObj.getA(), triangleObj.getC());
			System.out.println("Proposed vertex color: " + proposeVertexColorForVertex3);
		}
//		
	}
	
	@Test
	public void newApproachMetricDistributionCheck() {
		String SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH = "SemanticWebDogFood/";
		SemanticWebDogFoodDatasetTest mDatasetManager = new SemanticWebDogFoodDatasetTest(2);
		ColouredGraph graphs[] = new ColouredGraph[20];
		graphs= mDatasetManager.readGraphsFromFiles(SEMANTIC_DOG_FOOD_DATA_FOLDER_PATH);
		
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);

		System.out.println(graphs[0].getNumberOfVertices());
		
		System.out.println(graphs[0].getNumberOfEdges());

		NodeIteratorMetric2 metric;
		metric = new NodeIteratorMetric2();
		
		metric.computeTriangles(graphs[0]);
		metric.computeTriangles(graphs[1]);;
		
		
		System.out.println(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes());

		System.out.println(metric.getMstatsVertEdges());
		
		TriangleDistribution2 triDist = new TriangleDistribution2(metric.getmTriangleColoursTriangleCountsEdgeCountsResourceNodes(), metric.getmIsolatedTriColoEdgesTriCountDistAvg(), 2, 2773, mRandom); // 1000 - desired number of vertices
		System.out.println(triDist.getmTriangleColorsv1v2v3());

	}
	
	//@Test
	public void arrayLengthTest() {
		int[] arrayObj = new int[20]; 
		
		long seed = Long.parseLong("1694360290692");
		System.out.println("Current Seed is "+seed);
		Random mRandom = new Random(seed);
		
		arrayObj[0] = 2;
		arrayObj[1] = 3;
		
		System.out.println(arrayObj.length);
		// Array declared of size 20. Thus, length will always be 20.
		// Here, only first two indexes are assigned specific values and rest are assigned value 0.
		
		for (int i=0; i < arrayObj.length; i++) {
			System.out.println(arrayObj[i]);
		}
		
	}

}

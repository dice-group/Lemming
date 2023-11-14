package org.aksw.simba.lemming.simplexes;

import java.util.HashSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.edgetriangles.NodeIteratorMetric;
import org.aksw.simba.lemming.util.IOHelper;
import org.junit.Test;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

/**
 * Writing this test class to check conversion of BitSet to Long. This is required for sorting of BitSet. 
 * The bitset colors for triangle vertices can be sorted using this approach.
 * The class includes testcase for BitSet to Long conversion and checking if the triangles computed using the metric are correctly sorted.
 */
public class BitSetToLongConversionTest {
	/**
	 * Test case to check conversion of BitSet to Long.
	 */
	//@Test
	public void convertBitSetToLong() {
		ColouredGraph graph = IOHelper.readGraphFromResource("graph1.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop_2.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("email-Eu-core.n3", "N3");
	
		ObjectArrayList<BitSet> vertexColours = graph.getVertexColours();
		System.out.println("Vertex Colors: " + vertexColours);
		
		// Logic to convert Bitset to Long
		System.out.println("Converting BitSet to Long");
		for(int i=0; i< vertexColours.size(); i++) {
			long[] bits = vertexColours.get(i).bits;
			System.out.println(bits[0]);
		}
		
	}
	
	
	/**
	 * Test case to compute Triangles and check the sorting of BitSet values.
	 */
	@Test
	public void computeTriangles() {
		//TODO: Update the this test case
		
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph1.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop.n3", "N3");
		//ColouredGraph graph = IOHelper.readGraphFromResource("graph_loop_2.n3", "N3");
//		ColouredGraph graph = IOHelper.readGraphFromResource("email-Eu-core.n3", "N3");
//		
//		System.out.println(graph.getRDFTypePropertyColour());
//		
//		NodeIteratorMetric metric;
//		metric = new NodeIteratorMetric();
//		metric.computeTriangles(graph);
//		
//		HashSet<TriangleColours> setOfTriangleshavingClassNodes = metric.getSetOfTriangleshavingClassNodes();
//		HashSet<TriangleColours> setOfTriangleshavingResourceNodes = metric.getSetOfTriangleshavingResourceNodes();
//		
//		// Iterate over Triangles having only resource nodes
//		System.out.println("Checking triangles only with resource nodes....");
//		for(TriangleColours triangleColour: setOfTriangleshavingResourceNodes) {
//			//System.out.println(triangleColour);
//			System.out.println("Triangle:");
//			System.out.println(triangleColour.getA() + ", " + triangleColour.getB() + ", " + triangleColour.getC());
//			System.out.println(triangleColour.getA().bits[0] + ", " + triangleColour.getB().bits[0] + ", " + triangleColour.getC().bits[0]);
//			System.out.println();
//		}
//		
//		System.out.println("Checking triangles that could include class nodes....");
//		for(TriangleColours triangleColour: setOfTriangleshavingClassNodes) {
//			//System.out.println(triangleColour);
//			System.out.println("Triangle:");
//			System.out.println(triangleColour.getA() + ", " + triangleColour.getB() + ", " + triangleColour.getC());
//			System.out.println(triangleColour.getA().bits[0] + ", " + triangleColour.getB().bits[0] + ", " + triangleColour.getC().bits[0]);
//			System.out.println();
//		}
	
		
	}
}

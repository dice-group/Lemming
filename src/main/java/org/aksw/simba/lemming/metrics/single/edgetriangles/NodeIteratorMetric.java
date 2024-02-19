package org.aksw.simba.lemming.metrics.single.edgetriangles;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;
import toools.collections.primitive.LucIntSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.IColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.TriangleMetric;
import org.aksw.simba.lemming.simplexes.TriColos;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

/**
 * @author DANISH AHMED on 6/13/2018
 */
public class NodeIteratorMetric extends AbstractMetric implements TriangleMetric {
	
	// HashSet to store information of triangles
	//HashSet<TriangleColours> setOfTriangleshavingResourceNodes = new HashSet<>();
	
	/**
	 * Map for storing vertex colors for the triangle along with the probability for them in terms of triangle count and edge count in an array.
	 * Note: Probability to form triangle is stored at the 0th index of the array, the probability that triangle has more edges is stored at the 1st index, and the raw count of triangles is stored at the 3rd index. 
	 */
	ObjectObjectOpenHashMap<TriColos, double[]> mTriangleColoursTriangleEdgeCountsResourceNodes = new ObjectObjectOpenHashMap<TriColos, double[]>();
	
	/**
	 * Map for storing vertex colors for the triangle along with the probability. The probability is stored for the third vertex color given two vertex colors. 
	 */
	private ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> mTriangleColorsProbability  = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>>();
	
	/**
	 * Map for storing vertex colors for the triangle along with their edge count
	 */
	ObjectIntOpenHashMap<TriColos> mTrianglesColoursEdgeCountsClassNodes = new ObjectIntOpenHashMap<TriColos>();
	
	/**
	 * Map for storing vertex colors for the triangle along with their edge count
	 */
	ObjectIntOpenHashMap<TriColos> mTrianglesColoursTrianglesCountsClassNodes = new ObjectIntOpenHashMap<TriColos>();
	
	HashSet<TriColos> setOfTriangleshavingClassNodes = new HashSet<TriColos>();
	
	ObjectDoubleOpenHashMap<BitSet> mColoCountVertConnectedToTriangle = new ObjectDoubleOpenHashMap<BitSet>();
	

	// First Lengthy approach
	// HashMap to store number of vertices and edges for different possible cases
	// Incremental key
	// Value:
	//0th index: Number of vertices included in resource triangles
	//1st index: Number of edges for resource triangles
	//2nd index: Number of vertices included in class triangles
	//3rd index: Number of edges for class triangles
	//4th index: Number of vertices that are part of both type of triangles
	//5th index: Number of edges part of both type of triangles
	//6th index: Number of vertices not part of any triangle
	//7th index: Number of edges not part of any triangle
	//8th index: Number of vertices included in a triangle but also forms a separate 1-simplex within the triangle or outside
	//9th index: Number of edges for the above case
	
	//2nd approach
	// Incremental key
	// Value:
	//0th index: Number of vertices included in triangles
	//1st index: Number of edges for triangles
	//2nd index: Number of vertices included in triangles and also form other 1-simplex
	//3rd index: Number of edges for the above case
	//4th index: Number of vertices not part of triangles
	//5th index: Number of edges for the above case
	Map<Integer, List<Integer>> mstatsVertEdges = new HashMap<Integer, List<Integer>>();
	int graphId = 1;
	
	/**
	 * Map object storing edge Ids found in triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing common edge Ids connected to vertices in triangles and other vertices that are not in triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsCommonEdgesIds = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing edge Ids connecting 2-simplexes.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsConnectTriangles = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
	/**
	 * Map object storing edges Ids not found in triangles for every input graph.
	 */
	ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsNotInTriangle = new ObjectObjectOpenHashMap<Integer, IntSet>();
	
    public NodeIteratorMetric() {
        super("#edgetriangles");
    }

    @Override
    public double apply(IColouredGraph graph) {
    	
        IntSet[] edges = new IntSet[graph.getGraph().getNumberOfVertices()];

        Grph grph = getUndirectedGraph(graph.getGraph());

        for (int i = 0; i < edges.length; ++i) {
            edges[i] = grph.getOutEdges(i);
            edges[i].addAll(grph.getInEdges(i));
        }

        int numberOfTriangles = 0;
        IntSet vertGrph = graph.getVertices();
        IntSet visitedVertices = new DefaultIntSet(vertGrph.size());
        for (int vertex : vertGrph) {
            IntSet neighbors = IntSetUtil.difference(
                    IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)), visitedVertices);
            for (int neighbor1 : neighbors) {
                IntSet neighbors1 = IntSetUtil.difference(
                        IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)),
                        visitedVertices);
                for (int neighbor2 : neighbors) {
                    if (vertex != neighbor1 && vertex != neighbor2 && neighbor1 < neighbor2
                            && neighbors1.contains(neighbor2)) {
                        numberOfTriangles = numberOfTriangles
                                + (IntSetUtil.intersection(edges[vertex], edges[neighbor1]).size()
                                        * IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]).size()
                                        * IntSetUtil.intersection(edges[neighbor2], edges[vertex]).size());
                    }
                }
            }
            visitedVertices.add(vertex);
        }
        return numberOfTriangles;
    }
    
    public void computeTriangles(ColouredGraph graph){
    	
    	//temporary variables to track count of number of triangles for class and resource nodes
    	//int countClassTriangles = 0;
    	//temporary variables to track vertices forming only triangles
    	//IntSet verticesOnlyFormingTriangleClass = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	// temporary variable for tracking edge Ids within triangle
    	//IntSet edgesWithinTrianglesClass = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// Variable to track count of triangles
    	int countResourceTriangles = 0;
    	
    	//Variable to track count of edges
    	int numOfEdgesInTriangle = 0;
    	
    	//temporary map to store count of triangles and edges, which is later used to compute probabilities
    	ObjectObjectOpenHashMap<TriColos, int[]> mTriangleColoursTriangleEdgeCountsTemp = new ObjectObjectOpenHashMap<TriColos, int[]>();
    	
    	//temporary variables to track vertices forming only triangles
    	IntSet verticesOnlyFormingTriangleResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// temporary variable for tracking edge Ids within triangle
    	IntSet edgesWithinTrianglesResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// temporary variable to store common edge Ids connected to vertices in triangles and other vertices that are not in triangles
    	IntSet commonEdgesTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	//temporary variable to track vertices that have additional edges within triangle
    	IntSet commonVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
    	
    	// variable to track total number of additional edges within triangles in a graph
    	int totalAdditionalEdgesInTriangle = 0;
    	
    	//commenting this statement. getUndirectedGraph creates a new vertex only for edges found in the graph. As RDF type edges are removed some vertices will not be considered
    	IntSet[] edges = new IntSet[graph.getGraph().getNumberOfVertices()];
    	//for (int i = 0; i < edges.length; ++i) {
            //edges[i] = grph.getOutEdges(i);
            //edges[i].addAll(grph.getInEdges(i));
        //}

        Grph grph = getUndirectedGraph(graph.getGraph()); // creates graph based on edges
        IntSet vertices = grph.getVertices();

        for (int vertexId:vertices) {
        	edges[vertexId] = grph.getOutEdges(vertexId);
        	edges[vertexId].addAll(grph.getInEdges(vertexId));
        }

        int numberOfTriangles = 0;
        //IntSet vertGrph = graph.getVertices();//commenting based on same reason as above
        IntSet vertGrph = grph.getVertices();
        IntSet visitedVertices = new DefaultIntSet(vertGrph.size());
        for (int vertex : vertGrph) {
            IntSet neighbors = IntSetUtil.difference(
                    IntSetUtil.union(grph.getInNeighbors(vertex), grph.getOutNeighbors(vertex)), visitedVertices);
            for (int neighbor1 : neighbors) {
                IntSet neighbors1 = IntSetUtil.difference(
                        IntSetUtil.union(grph.getInNeighbors(neighbor1), grph.getOutNeighbors(neighbor1)),
                        visitedVertices);
                for (int neighbor2 : neighbors) {
                    if (vertex != neighbor1 && vertex != neighbor2 && neighbor1 < neighbor2
                            && neighbors1.contains(neighbor2)) {
                    	
                    	IntSet intersection1 = IntSetUtil.intersection(edges[vertex], edges[neighbor1]);
                    	IntSet intersection2 = IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]);
                    	IntSet intersection3 = IntSetUtil.intersection(edges[neighbor2], edges[vertex]);
                    	
                    	
                    	int intersectionResult1 = intersection1.size();
                    	int intersectionResult2 = intersection2.size();
                    	int intersectionResult3 = intersection3.size();
                    	
                    	int intersectionResult = (intersectionResult1 * intersectionResult2 * intersectionResult3);
                    	
                    	
                        numberOfTriangles = numberOfTriangles + intersectionResult;
                        
                        if (intersectionResult > 0) {
                        	
                        	TriColos tempObj = new TriColos(graph.getVertexColour(vertex), graph.getVertexColour(neighbor1), graph.getVertexColour(neighbor2));
                        	//TriangleColours tempObj = new TriangleColours(graph.getVertexColour(vertex), graph.getVertexColour(neighbor1), graph.getVertexColour(neighbor2), intersectionResult1 + intersectionResult2 + intersectionResult3); // Need to update the intersectionResult to intersectionResult1 + intersectionResult2 + intersectionResult3?
                        	
                        	// Set to store colors of every edge within the nodes forming a triangle
                        	HashSet<BitSet> setEdgeColors = new HashSet<BitSet>();
                        	
                        	// get colours of all edges and add to the Set
                        	for(int edgeIdVal: IntSetUtil.union( IntSetUtil.intersection(edges[vertex], edges[neighbor1]),IntSetUtil.union(IntSetUtil.intersection(edges[neighbor1], edges[neighbor2]), IntSetUtil.intersection(edges[neighbor2], edges[vertex]))) ) {
                        		setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
                        	}
                        	
                        	// if RDF type edge is not present among all the edges then add the node colours to setOfTriangleshavingResourceNodes set
                        	if ( !setEdgeColors.contains(graph.getRDFTypePropertyColour()) ) {
                        		
                        		// Add edges within triangle for resource nodes only
                            	edgesWithinTrianglesResource.addAll(intersection1);
                            	edgesWithinTrianglesResource.addAll(intersection2);
                            	edgesWithinTrianglesResource.addAll(intersection3);
                            	
                            	// Store the vertex forming triangle into temporary set for resource nodes only
                            	verticesOnlyFormingTriangleResource.add(vertex);
                            	verticesOnlyFormingTriangleResource.add(neighbor1);
                            	verticesOnlyFormingTriangleResource.add(neighbor2);
                            	
                        		//countResourceTriangles=countResourceTriangles+ ((int) ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3));
                            	countResourceTriangles=countResourceTriangles+ intersectionResult;
                        		numOfEdgesInTriangle = numOfEdgesInTriangle + intersectionResult1 + intersectionResult2 + intersectionResult3;
                        		
                        		if (mTriangleColoursTriangleEdgeCountsTemp.containsKey(tempObj)) {
                        			
                        			// triangle was already found previously, update the count of triangle and edges
                        			int[] triangleCountEdgeCountArr = mTriangleColoursTriangleEdgeCountsTemp.get(tempObj);
                        			
                        			//previous count of edges
                        			int previousEdgeCount = triangleCountEdgeCountArr[1];
                        			int previousTriangleCount = triangleCountEdgeCountArr[0];
                        			
                        			//create new updated array
                        			int[] newTriangleEdgeCountsArr = new int[2];
                        			//newTriangleEdgeCountsArr[0] = previousTriangleCount + (int) ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3);
                        			newTriangleEdgeCountsArr[0] = previousTriangleCount + intersectionResult;
                        			newTriangleEdgeCountsArr[1] = previousEdgeCount + intersectionResult1 + intersectionResult2 + intersectionResult3;

                        			// update the hashmap
                        			mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
                        			
                            	}
                            	else {
                            		
                            		//create an array for triangle and edge counts of triangle
                        			int[] newTriangleEdgeCountsArr = new int[2];
                        			//newTriangleEdgeCountsArr[0] = (int) ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3);
                        			newTriangleEdgeCountsArr[0] = intersectionResult;
                        			newTriangleEdgeCountsArr[1] = intersectionResult1 + intersectionResult2 + intersectionResult3;
                            		
                            		// triangle is found for the first time
                        			mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
                            	}
                        	}
                        	else {
                        		
                        		// Right now not storing information about triangles formed using classes. Thus, commenting it out
                        		
								/*
								 * // Add edges within triangle edgesWithinTrianglesClass.addAll(intersection1);
								 * edgesWithinTrianglesClass.addAll(intersection2);
								 * edgesWithinTrianglesClass.addAll(intersection3);
								 * 
								 * // Store the vertex forming triangle into temporary set
								 * verticesOnlyFormingTriangleClass.add(vertex);
								 * verticesOnlyFormingTriangleClass.add(neighbor1);
								 * verticesOnlyFormingTriangleClass.add(neighbor2);
								 * 
								 * countClassTriangles=countClassTriangles+intersectionResult1 +
								 * intersectionResult2 + intersectionResult3;
								 * 
								 * if (mTrianglesColoursEdgeCountsClassNodes.containsKey(tempObj)) { int
								 * previousEdgeCountClassNode =
								 * mTrianglesColoursEdgeCountsClassNodes.get(tempObj);
								 * 
								 * mTrianglesColoursEdgeCountsClassNodes.put(tempObj, previousEdgeCountClassNode
								 * + intersectionResult1 + intersectionResult2 + intersectionResult3);
								 * 
								 * // update the count of triangles int previousTriangleCount =
								 * mTrianglesColoursTrianglesCountsClassNodes.get(tempObj);
								 * mTrianglesColoursTrianglesCountsClassNodes.put(tempObj, previousTriangleCount
								 * + (int) ((intersectionResult1 + intersectionResult2 +
								 * intersectionResult3)/3)); } else { // triangle is found for the first time
								 * mTrianglesColoursEdgeCountsClassNodes.put(tempObj, intersectionResult1 +
								 * intersectionResult2 + intersectionResult3);
								 * 
								 * 
								 * mTrianglesColoursTrianglesCountsClassNodes.put(tempObj, (int)
								 * ((intersectionResult1 + intersectionResult2 + intersectionResult3)/3)); }
								 */
								 
                        	}
                        }
                    }
                }
            }
            visitedVertices.add(vertex);
        }
        
        //Temp map for storing probability for the third vertex color given two vertex colors
        ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>> mTriangleColorsProbabilityTemp  = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>>();
        
        //************************** Logic to compute probabilities for triangles, edge counts and also store raw counts for the triangles *********************************************//
        // iterate over found triangle colors
        Object[] triangleColorsKeys = mTriangleColoursTriangleEdgeCountsTemp.keys;
        for (int triColKey = 0; triColKey < triangleColorsKeys.length; triColKey++) {
        	if (mTriangleColoursTriangleEdgeCountsTemp.allocated[triColKey]) {
        		TriColos triangleColours = (TriColos) triangleColorsKeys[triColKey];
        		int[] triangleEdgeCount = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);
        		double triDistribution = triangleEdgeCount[0] * 1.0 / countResourceTriangles;
        		double edgeDistribution = triangleEdgeCount[1] * 1.0 / numOfEdgesInTriangle;
        		
        		//Store vertex colors for the triangle in temporary map
        		storeTriangleColorsInMaps(mTriangleColorsProbabilityTemp, triangleColours.getA(), triangleColours.getB(), triangleColours.getC(), triangleEdgeCount[0]);
        		storeTriangleColorsInMaps(mTriangleColorsProbabilityTemp, triangleColours.getA(), triangleColours.getC(), triangleColours.getB(), triangleEdgeCount[0]);
        		storeTriangleColorsInMaps(mTriangleColorsProbabilityTemp, triangleColours.getB(), triangleColours.getC(), triangleColours.getA(), triangleEdgeCount[0]);
        		
        		// create new array to create updated distributions and count of triangles
    			double[] newTriEdgeDist = new double[3];
    			newTriEdgeDist[0] = triDistribution;
    			newTriEdgeDist[1] = edgeDistribution;
    			newTriEdgeDist[2] = triangleEdgeCount[0];
        		
        		// update the distribution in global maps
        		if (mTriangleColoursTriangleEdgeCountsResourceNodes.containsKey(triangleColours)) {
        			
        			// get old distribution values
        			double[] previousTriEdgedist = mTriangleColoursTriangleEdgeCountsResourceNodes.get(triangleColours);
        			
        			// update the distribution values
        			newTriEdgeDist[0] = newTriEdgeDist[0] + previousTriEdgedist[0];
        			newTriEdgeDist[1] = newTriEdgeDist[1] + previousTriEdgedist[1];
        			newTriEdgeDist[2] = newTriEdgeDist[2] + previousTriEdgedist[2];
        			
        		}
        		mTriangleColoursTriangleEdgeCountsResourceNodes.put(triangleColours, newTriEdgeDist);
        	}
        }
        
        //********************** Logic to store values in nested hashmap probability for the third vertex color given two vertex colors **************************************//
        Object[] keysVertexColo1Tri = mTriangleColorsProbabilityTemp.keys; // get all 1st vertex colors
        for (int i=0; i< keysVertexColo1Tri.length;i++) {
        	if (mTriangleColorsProbabilityTemp.allocated[i]) {
        		BitSet vertexColo1Tri = (BitSet) keysVertexColo1Tri[i];// store 1st vertex color key
        		
        		ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mVColo2VColo3CountTriTemp = mTriangleColorsProbabilityTemp.get(vertexColo1Tri);// get nested hashmap
        		Object[] keysVertexColo2Tri = mVColo2VColo3CountTriTemp.keys; // get all 2nd vertex colors (from the first nested hashmap)
        		for (int j=0; j < keysVertexColo2Tri.length; j++) {
        			if (mVColo2VColo3CountTriTemp.allocated[j]) {
        				BitSet vertexColo2Tri = (BitSet) keysVertexColo2Tri[j];// store 2nd vertex color key
        				
        				ObjectIntOpenHashMap<BitSet> mVColo3CountTriTemp = mVColo2VColo3CountTriTemp.get(vertexColo2Tri);// get 2nd nested hashmap
        				Object[] keysVertexColo3Tri = mVColo3CountTriTemp.keys;// get all 3rd vertex colors (from the second nested hashmap)
        				
        				int tempTotalCountOfTriangles = 0;
        				//first iteration for the last hashmap to get the total count
        				for (int k=0; k < keysVertexColo3Tri.length; k++) {
        					if (mVColo3CountTriTemp.allocated[k]) {
        						BitSet vertexColo3Tri = (BitSet) keysVertexColo3Tri[k];
        						tempTotalCountOfTriangles = tempTotalCountOfTriangles + mVColo3CountTriTemp.get(vertexColo3Tri);
        					}
        				}
        				
        				// Calculate distribution for the vertex colors and update the global hashmap
        				for (int k=0; k < keysVertexColo3Tri.length; k++) {
        					if (mVColo3CountTriTemp.allocated[k]) {
        						BitSet vertexColo3Tri = (BitSet) keysVertexColo3Tri[k];
        						double distributionValue = mVColo3CountTriTemp.get(vertexColo3Tri) * (1.0) / tempTotalCountOfTriangles;
        						
        						ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>> mVColo2VColo3CountTri = mTriangleColorsProbability.get(vertexColo1Tri);
        						if (mVColo2VColo3CountTri == null) {
        							mVColo2VColo3CountTri = new ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>();
        						}
        						
        						ObjectDoubleOpenHashMap<BitSet> mVColo3CountTri = mVColo2VColo3CountTri.get(vertexColo2Tri);
        						if (mVColo3CountTri == null) {
        							mVColo3CountTri = new ObjectDoubleOpenHashMap<BitSet>();
        							mVColo2VColo3CountTri.put(vertexColo2Tri, mVColo3CountTri);
        						}
        						
        						mVColo3CountTri.putOrAdd(vertexColo3Tri, distributionValue, distributionValue);
        						mTriangleColorsProbability.put(vertexColo1Tri, mVColo2VColo3CountTri);
        					}
        				}
        				
        			}
        			
        		}
        	}
        }
        
        //*************************** Logic to compute 1-simplex connecting two simplex ********************************//
        // Evaluate edges not in triangles and check if two end points are vertices of a triangle
        IntSet edgesNotInTriangles = IntSetUtil.difference(graph.getEdges(), edgesWithinTrianglesResource);
        IntSet edgeIdConnectingTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
        for (int edgeIdNotTri : edgesNotInTriangles) {
        	IntSet verticesEdgeNotTri = graph.getVerticesIncidentToEdge(edgeIdNotTri);
        	if (verticesOnlyFormingTriangleResource.containsAll(verticesEdgeNotTri)) {
        		edgeIdConnectingTriangles.add(edgeIdNotTri);
        	}
        }
        
        
        
        
        // Logic for computing the number of vertices and edges for different cases
        IntSet diffTotalVertices = IntSetUtil.difference(vertGrph, verticesOnlyFormingTriangleResource);
        
        // variable to track number of simplexes connected to Vertices of triangle
        int numSimplexesVertConnectTriangle = 0;
        // Map for storing Color of vertices connecting to triangle. Later, this map is used to compute distributions
        ObjectIntOpenHashMap<BitSet> mVertColoConnectTriangleTemp = new ObjectIntOpenHashMap<BitSet>();
        
        for(int vertexId: diffTotalVertices) {
        	IntSet neighbors = IntSetUtil.union(grph.getInNeighbors(vertexId), grph.getOutNeighbors(vertexId));
        	IntSet intersectionTriangleVerts = IntSetUtil.intersection(neighbors, verticesOnlyFormingTriangleResource);
        	for(int intersectVertId: intersectionTriangleVerts) {
        		
        		IntSet tempIntersection = IntSetUtil.intersection(edges[vertexId], edges[intersectVertId]);
        		
        		// *************Logic to not consider RDF Type edges*************** //
            	HashSet<BitSet> setEdgeColors = new HashSet<BitSet>(); // Set to store colors of every edge within the nodes forming a triangle
            	
            	// get colours of all edges and add to the Set
            	for(int edgeIdVal: tempIntersection ) {
            		setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
            	}
            	
            	
            	if (!setEdgeColors.contains(graph.getRDFTypePropertyColour())) {
	        		commonVertices.add(intersectVertId);
	        		totalAdditionalEdgesInTriangle = totalAdditionalEdgesInTriangle + tempIntersection.size();
	        		commonEdgesTriangles.addAll(tempIntersection);
	        		
	        		numSimplexesVertConnectTriangle++;
	        		//mColoCountVertConnectedToTriangle.putOrAdd(graph.getVertexColour(vertexId), 1, 1); //commenting using temporary map to compute the distribution
	        		mVertColoConnectTriangleTemp.putOrAdd(graph.getVertexColour(vertexId), 1, 1);
            	}
        	}
        }
        
        //Compute distribution of vertices connecting to triangles
        Object[] vertColoConntTriArr = mVertColoConnectTriangleTemp.keys;
        for (int vertConnIndex = 0; vertConnIndex < vertColoConntTriArr.length; vertConnIndex++) {
        	if (mVertColoConnectTriangleTemp.allocated[vertConnIndex]) {
        		BitSet vertColoConntTri = (BitSet) vertColoConntTriArr[vertConnIndex];
        		double distVertColoConnt = mVertColoConnectTriangleTemp.get(vertColoConntTri) * 1.0 / numSimplexesVertConnectTriangle;
        		mColoCountVertConnectedToTriangle.putOrAdd(vertColoConntTri, distVertColoConnt, distVertColoConnt);
        	}
        }
        
        
        
        // remove vertices that are in the triangle but are also forming 1-simplex
        //verticesOnlyFormingTriangle.removeAll(commonVertices); // commenting the removal of common vertices since they are also part of triangle
        
        // TODO:*************** PENDING: Check for self-loop? before performing following computations **************************
        
        // Number of vertices for different cases in a variable
        int numCommonVert = commonVertices.size();
        int numVertFormingOnlyTriangle = verticesOnlyFormingTriangleResource.size();
        
        // Temporary List to store output for different graphs
        List<Integer> tempList = new ArrayList<Integer>();
        tempList.add(numVertFormingOnlyTriangle); // 0th index - Number of vertices Forming only triangles
        tempList.add(edgesWithinTrianglesResource.size()); // 1st index - Number of edges for vertices only forming triangles
        tempList.add(numCommonVert); // 2nd index - Common vertices part of triangles and also other 1-simplex
        tempList.add(totalAdditionalEdgesInTriangle); // 3rd index - Common edges that could be part of triangles and/or edges connecting vertices forming triangle and 1-simplex.
        tempList.add(vertGrph.size() - numVertFormingOnlyTriangle); // 4th index - Number of vertices that are not part of any triangle // Updated:  removed this "- numCommonVert" from the expression since common vertices are considered part of triangle.
        tempList.add(grph.getEdges().size() - edgesWithinTrianglesResource.size() - totalAdditionalEdgesInTriangle); // 5th index - Number of edges that are not part of any triangle
        tempList.add(edgeIdConnectingTriangles.size()); // 6th index - number of edges that are connecting triangles with 1-simplex
        
        
        // ****************** Logic for calculation for edges of rdf type for vertices not connected to triangles
        /*
        diffTotalVertices = IntSetUtil.difference(vertGrph, verticesOnlyFormingTriangle); // Get common vertices (inside triangle and 1-simplex) and vertices not linked to triangle
        int numOfrdfTypeEdgesRemainingVertices = computeCountOfRDFTypeEdges(diffTotalVertices, graph, edges, edgesWithinTriangles);
        
        // add the computed counts in the ArrayList
        tempList.add(numOfrdfTypeEdgesRemainingVertices); // 6th index - Number of rdf type edges for common vertices and vertices not linked to triangles
        */
        System.out.println("Graph Id: " + graphId);
        //System.out.println("Count of edges for Resource triangles: " + countResourceTriangles);
        //System.out.println("Count of edges for class triangles: " + countClassTriangles);
        System.out.println("Count of edges for Resource triangles: " + edgesWithinTrianglesResource.size());
        //System.out.println("Count of edges for class triangles: " + edgesWithinTrianglesClass.size());
        
        mstatsVertEdges.put(graphId, tempList);
        
        // store edges ids for different cases in the map
        mGraphsEdgesIdsTriangle.put(graphId, edgesWithinTrianglesResource);
        mGraphsEdgesIdsNotInTriangle.put(graphId,  IntSetUtil.difference(IntSetUtil.difference(grph.getEdges(), edgesWithinTrianglesResource),  commonEdgesTriangles));
        mGraphsCommonEdgesIds.put(graphId, commonEdgesTriangles);
        
        // store edge Ids for 1-simplexes connecting triangles
        mGraphsEdgesIdsConnectTriangles.put(graphId, edgeIdConnectingTriangles);
        
        graphId++;
        
    }
    
    public ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectDoubleOpenHashMap<BitSet>>> getmTriangleColorsProbability() {
		return mTriangleColorsProbability;
	}

	/**
	 * This generic function is used to store the different permutation of TriangleColors. It evaluates the inputmap, if the colors are not already present then it creates new nested maps and stores them. Otherwise, the existing maps are updated.
	 * @param triColorMapToUpdate - Input map to update.
	 * @param firstVertexColor - First vertex color (first key .i.e. key for the outer map)
	 * @param secondVertexColor - Second vertex color (second key .i.e. key for the first nested map)
	 * @param thirdVertexColor - Third vertex color (third key .i.e. key for the second nested map)
	 * @param countToUpdate - Count for the vertex color of the triangle.
	 */
	private void storeTriangleColorsInMaps(ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>> triColorMapToUpdate, BitSet firstVertexColor, BitSet secondVertexColor, 
			BitSet thirdVertexColor, int countToUpdate) {
		ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mTriColorsv2v3Temp = triColorMapToUpdate.get(firstVertexColor);
		
		if (mTriColorsv2v3Temp == null) { // Logic when none of the colors are previously observed.
			
			// Temporary map for 3rd vertex (key) and count (value)
			ObjectIntOpenHashMap<BitSet> mTriColorv3CountTemp = new ObjectIntOpenHashMap<BitSet>();
			mTriColorv3CountTemp.put(thirdVertexColor, countToUpdate);
			
			
			//Temporary map for 2nd vertex (key) and map of 3rd vertex with count (value)
			ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>> mTriColorv2v3Temp = new ObjectObjectOpenHashMap<BitSet, ObjectIntOpenHashMap<BitSet>>();
			mTriColorv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
			
			triColorMapToUpdate.put(firstVertexColor, mTriColorv2v3Temp);
		}else {
			//Logic when first vertex color was already previously observed
			
			// Checking if second vertex color also exists
			ObjectIntOpenHashMap<BitSet> mTriColorv3Temp = mTriColorsv2v3Temp.get(secondVertexColor);
			
			if(mTriColorv3Temp == null) {
				// when second vertex color was not previously observed
				
				// Temporary map for 3rd vertex (key) and count (value)
				ObjectIntOpenHashMap<BitSet> mTriColorv3CountTemp = new ObjectIntOpenHashMap<BitSet>();
				mTriColorv3CountTemp.put(thirdVertexColor, countToUpdate);
				
				// add second vertex color and map of 3rd vertex with count (value)
				mTriColorsv2v3Temp.put(secondVertexColor, mTriColorv3CountTemp);
				
			}else {
				// Logic when first vertex and second vertex color already exists in the map.
				// Note: All three vertex colors could not present in the map. This is already evaluated in the Triangle metric computation
				
				// Add third vertex color 
				mTriColorv3Temp.put(thirdVertexColor, countToUpdate);
			}
		}
	}
    
    
    public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsConnectTriangles() {
		return mGraphsEdgesIdsConnectTriangles;
	}

	public ObjectObjectOpenHashMap<TriColos, double[]> getmTriangleColoursTriangleCountsEdgeCountsResourceNodes() {
		return mTriangleColoursTriangleEdgeCountsResourceNodes;
	}

	public ObjectDoubleOpenHashMap<BitSet> getmColoCountVertConnectedToTriangle() {
		return mColoCountVertConnectedToTriangle;
	}

	public ObjectIntOpenHashMap<TriColos> getmTrianglesColoursTrianglesCountsClassNodes() {
		return mTrianglesColoursTrianglesCountsClassNodes;
	}

	public ObjectIntOpenHashMap<TriColos> getmTrianglesColoursEdgeCountsClassNodes() {
		return mTrianglesColoursEdgeCountsClassNodes;
	}

	/**
     * This function computes the count of total number of edges of rdf type for the input set of vertices. The computation is performed for the graph, the given set of edges and the edges that should not be considered. 
     * @return
     */
    private int computeCountOfRDFTypeEdges(IntSet verticesIdSet, ColouredGraph graph, IntSet[] edges, IntSet edgesNotToConsider) {
    	// ************ Logic for calculation for edges of rdf type for common vertices ************
    	IntSet edgesForInputVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
        
        // Iterate over all vertices and store their edgeIds
        for(int vertexIdCommon : verticesIdSet) {
        	edgesForInputVertices.addAll(edges[vertexIdCommon]);
        }
        
        edgesForInputVertices = IntSetUtil.difference(edgesForInputVertices, edgesNotToConsider);
        
        // Variables to store color of rdf type edge and count of such an edge 
        Object rdfTypePropertyColour = graph.getRDFTypePropertyColour();
        int numOfrdfTypeEdgesCommon = 0;
        
        //For the found edges, evaluate their color and compute the count of total number of edges of rdf type
        for (int edgeIdTemp: edgesForInputVertices) {
        	if (graph.getEdgeColour(edgeIdTemp).equals(rdfTypePropertyColour))
        		numOfrdfTypeEdgesCommon = numOfrdfTypeEdgesCommon + 1;
        }
        
        return numOfrdfTypeEdgesCommon;
    }

    public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsTriangle() {
		return mGraphsEdgesIdsTriangle;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsCommonEdgesIds() {
		return mGraphsCommonEdgesIds;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsNotInTriangle() {
		return mGraphsEdgesIdsNotInTriangle;
	}

	public Map<Integer, List<Integer>> getMstatsVertEdges() {
		return mstatsVertEdges;
	}

	

	public HashSet<TriColos> getSetOfTriangleshavingClassNodes() {
		return setOfTriangleshavingClassNodes;
	}

	private Grph getUndirectedGraph(Grph graph) {
        Grph undirectedGraph = new InMemoryGrph();
        for (int e : graph.getEdges()) {
            int sourceNode = graph.getOneVertex(e);
            int targetNode = graph.getTheOtherVertex(e, sourceNode);
            undirectedGraph.addUndirectedSimpleEdge(sourceNode, targetNode);
        }
        return undirectedGraph;
    }

    @Override
    public double calculateComplexity(int edges, int vertices) {
        return vertices * Math.pow(edges, 2) * (edges / (double) vertices);
    }
}
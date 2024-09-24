package org.aksw.simba.lemming.simplexes.analysis;

import java.util.HashSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.IntSetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class is based on NodeIteratorMetric that counts number of triangles.
 */
public class FindTri {
	private static final Logger LOGGER = LoggerFactory.getLogger(FindTri.class);

	/**
	 * Map for storing vertex colors for the triangle along with the probability for
	 * them in terms of triangle count and edge count in an array. Note: Count of
	 * triangles is stored at the 0th index of the array, count of edges in the
	 * triangle is stored at the 1st index, the probability of triangle is stored at
	 * the 2nd index and the average count of triangles per no. of vertices in the
	 * input graph is stored at the 3rd index.
	 */
	private ObjectObjectOpenHashMap<TriColours, double[]> mTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriColours, double[]>();

	/**
	 * Map for storing vertex colors specifically for isolated triangles along with
	 * the probability for them in terms of triangle count and edge count in an
	 * array. Note: Count of triangles is stored at the 0th index of the array,
	 * count of edges in the triangle is stored at the 1st index, and the
	 * probability of triangle is stored at the 2nd index.
	 */
	private ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColoEdgesTriCountDistAvg = new ObjectObjectOpenHashMap<TriColours, double[]>();

	int graphId = 1;

	// ****************************************** Maps for Storing edges
	// ************************************************//
	/**
	 * Map object storing edge Ids found in triangles for every input graph.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsTriangle = new ObjectObjectOpenHashMap<Integer, IntSet>();

	/**
	 * Map object storing edge Ids found in isolated triangles for every input
	 * graph.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsEdgesIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();

	// ****************************************** Maps for Storing vertices
	// ************************************************//
	/**
	 * Map object storing vertex Ids found in isolated triangles for every input
	 * graph. Note: This map is used later to evaluate vertices and find self loops
	 * for isolated triangles.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsIsolatedTri = new ObjectObjectOpenHashMap<Integer, IntSet>();

	/**
	 * Map object storing vertex Ids connected 2-simplexes for every input graph.
	 * Note: Similar to above, this map is also used to evaluate vertices and find
	 * self loops.
	 */
	private ObjectObjectOpenHashMap<Integer, IntSet> mGraphsVertIdsConnectTriangles = new ObjectObjectOpenHashMap<Integer, IntSet>();

	public FindTri() {

	}

	public void computeTriangles(ColouredGraph graph) {
		// tracks count of resource triangles
		int countResourceTriangles = 0;
		// track vertices forming connected triangles
		IntSet verticesOnlyFormingTriangleResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
		// track edges in connected triangles
		IntSet edgesWithinTrianglesResource = new DefaultIntSet(Constants.DEFAULT_SIZE);
		// Stores counts of triangles and edges for each set of connected triangle
		// colours
		ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleEdgeCountsTemp = new ObjectObjectOpenHashMap<TriColours, double[]>();

		// track vertices forming isolated triangles
		IntSet verticesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
		// track edges in isolated triangles
		IntSet edgesForIsolatedTriangles = new DefaultIntSet(Constants.DEFAULT_SIZE);
		// tracks count of isolated triangles
		int countIsolatedTriangles = 0;

		// Stores counts of triangles and edges for each set of isolated triangle
		// colours
		ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosTriEdgeCountsTemp = new ObjectObjectOpenHashMap<TriColours, double[]>();

		// create undirected graph
		Grph grph = getUndirectedGraph(graph.getGraph());
		IntSet vertices = grph.getVertices();

		// get edges of all vertices
		IntSet[] edges = new IntSet[graph.getGraph().getNumberOfVertices()];
		for (int vertexId : vertices) {
			edges[vertexId] = grph.getEdgesIncidentTo(vertexId);
		}

		// Logic to find set of class vertices
		IntSet classVerticesSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
		LOGGER.info("Finding triangles in graph: " + graphId);
		for (int edgeId : graph.getEdges()) {
			if (graph.getEdgeColour(edgeId).equals(graph.getRDFTypePropertyColour())) {
				int classVertexID = graph.getHeadOfTheEdge(edgeId);
				classVerticesSet.add(classVertexID);
			}
		}

		IntSet vertGrph = grph.getVertices();
		IntSet visitedVertices = new DefaultIntSet(vertGrph.size());
		for (int curVertex : vertGrph) {
			IntSet neighbors = IntSetUtil.difference(grph.getNeighbours(curVertex), visitedVertices);
			for (int vN1 : neighbors) {
				IntSet neighbors1 = IntSetUtil.difference(grph.getNeighbours(vN1), visitedVertices);
				for (int vN2 : neighbors) {
					if (curVertex != vN1 && curVertex != vN2 && vN1 < vN2 && neighbors1.contains(vN2)) {

						IntSet intersection1 = IntSetUtil.intersection(edges[curVertex], edges[vN1]);
						IntSet intersection2 = IntSetUtil.intersection(edges[vN1], edges[vN2]);
						IntSet intersection3 = IntSetUtil.intersection(edges[vN2], edges[curVertex]);

						int curN1Size = intersection1.size();
						int vN1vN2Size = intersection2.size();
						int vN2curSize = intersection3.size();
						
						int intersectionResult = curN1Size * vN1vN2Size * vN2curSize;
                        
                        if (intersectionResult == 0) {
                        	continue;
                        }

						int noEdges = curN1Size + vN1vN2Size + vN2curSize;

						TriColours tempObj = new TriColours(graph.getVertexColour(curVertex),
								graph.getVertexColour(vN1), graph.getVertexColour(vN2));

						// Set to store colors of every edge within the nodes forming a triangle
						HashSet<BitSet> setEdgeColors = new HashSet<BitSet>();
						IntSet intUnion = IntSetUtil.union(intersection1,
								IntSetUtil.union(intersection2, intersection3));
						for (int edgeIdVal : intUnion) {
							setEdgeColors.add(graph.getEdgeColour(edgeIdVal));
						}

						// if RDF type edge is not present among all the edges then add the node colours
						if (!setEdgeColors.contains(graph.getRDFTypePropertyColour())) {
							if (!verticesFormingIsolatedTriangles(curVertex, vN1, vN2, graph, classVerticesSet)) {
								// Add edges within triangle for resource nodes only
								edgesWithinTrianglesResource.addAll(intUnion);

								// Store the vertex forming triangle into temporary set for resource nodes only
								verticesOnlyFormingTriangleResource.add(curVertex);
								verticesOnlyFormingTriangleResource.add(vN1);
								verticesOnlyFormingTriangleResource.add(vN2);

								countResourceTriangles++;

								// triangle was already found previously, update the count of triangle and edges
								if (mTriangleColoursTriangleEdgeCountsTemp.containsKey(tempObj)) {
									double[] triangleCountEdgeCountArr = mTriangleColoursTriangleEdgeCountsTemp
											.get(tempObj);
									double previousTriangleCount = triangleCountEdgeCountArr[0];
									double previousEdgeCount = triangleCountEdgeCountArr[1];

									// create new count
									double[] newTriangleEdgeCountsArr = new double[2];
									newTriangleEdgeCountsArr[0] = previousTriangleCount + 1;
									newTriangleEdgeCountsArr[1] = previousEdgeCount + noEdges;

									// update the map
									mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
								} else {
									// otherwise, create a new entry
									double[] newTriangleEdgeCountsArr = { 1.0, noEdges };
									mTriangleColoursTriangleEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
								}
							} else {
								// update results for isolated triangles since vertices form isolated triangles

								// Add edges within triangle for resource nodes only
								edgesForIsolatedTriangles.addAll(intUnion);

								// Store the vertex forming triangle into temporary set for resource nodes only
								verticesForIsolatedTriangles.add(curVertex);
								verticesForIsolatedTriangles.add(vN1);
								verticesForIsolatedTriangles.add(vN2);

								countIsolatedTriangles++;

								if (mIsolatedTriColosTriEdgeCountsTemp.containsKey(tempObj)) {

									// triangle was already found previously, update the count of triangle and edges
									double[] triangleCountEdgeCountArr = mIsolatedTriColosTriEdgeCountsTemp
											.get(tempObj);
									double previousTriangleCount = triangleCountEdgeCountArr[0];
									double previousEdgeCount = triangleCountEdgeCountArr[1];

									// create new updated array
									double[] newTriangleEdgeCountsArr = new double[2];
									newTriangleEdgeCountsArr[0] = previousTriangleCount + 1;
									newTriangleEdgeCountsArr[1] = previousEdgeCount + noEdges;

									// update the map
									mIsolatedTriColosTriEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);

								} else {
									// isolated triangle found for the firsttime
									double[] newTriangleEdgeCountsArr = { 1.0, noEdges };
									mIsolatedTriColosTriEdgeCountsTemp.put(tempObj, newTriangleEdgeCountsArr);
								}
							}

						}
					}
				}
			}
			visitedVertices.add(curVertex);
		}

		// update counts of connected triangles
		updateCountConn2Simplexes(graph, mTriangleColoursTriangleEdgeCountsTemp, countResourceTriangles);
		// update counts of isolated triangles
		updateCountIso2Simplexes(graph, mIsolatedTriColosTriEdgeCountsTemp, countIsolatedTriangles);
		// Isolated triangles vertices
		mGraphsVertIdsIsolatedTri.put(graphId, verticesForIsolatedTriangles);
		// Connected triangles vertices
		mGraphsVertIdsConnectTriangles.put(graphId, verticesOnlyFormingTriangleResource);
		// Edge Ids connected 2-simplexes
		mGraphsEdgesIdsTriangle.put(graphId, edgesWithinTrianglesResource);
		// store edge ids for isolated triangles
		mGraphsEdgesIdsIsolatedTri.put(graphId, edgesForIsolatedTriangles);
		graphId++;
	}

	private void updateCountConn2Simplexes(ColouredGraph graph,
			ObjectObjectOpenHashMap<TriColours, double[]> mTriangleColoursTriangleEdgeCountsTemp,
			int countResourceTriangles) {
		// Logic to get total number of vertices in the input graph excluding class
		// vertices
		double totalVertices = graph.getNumberOfVertices();
		// remove number of class vertices from the total vertices count
//        totalVertices = totalVertices - classVerticesSet.size();// class vertices should not be subtracted from the total vertices. since they are also considered in input number of vertices required in output graph.

		// ******************************* Logic to update map for connected triangles
		// *********************************//
		// temporary variable to store total triangle distribution
		double totalDistTri = 0.0;

		// Iterate over temporary map and add an element at the 3rd index of the array
		// (No. of edges in triangle)/(Total number of triangles)
		Object[] keysTriColo = mTriangleColoursTriangleEdgeCountsTemp.keys;
		for (int i = 0; i < keysTriColo.length; i++) {
			if (mTriangleColoursTriangleEdgeCountsTemp.allocated[i]) {
				TriColours triangleColours = (TriColours) keysTriColo[i];
				double[] arrCountsDist = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);

				// create a new array for storing avg count
				double[] arrCountDistTemp = new double[3];
				arrCountDistTemp[0] = arrCountsDist[0];
				arrCountDistTemp[1] = arrCountsDist[1];
				arrCountDistTemp[2] = (arrCountsDist[1] * 1.0) / countResourceTriangles;
				totalDistTri = totalDistTri + arrCountDistTemp[2]; // incrementing total distribution

				mTriangleColoursTriangleEdgeCountsTemp.put(triangleColours, arrCountDistTemp); // update the map with
																								// the new array
			}
		}

		// iterate over results and update the global map
		keysTriColo = mTriangleColoursTriangleEdgeCountsTemp.keys;
		for (int i = 0; i < keysTriColo.length; i++) {
			if (mTriangleColoursTriangleEdgeCountsTemp.allocated[i]) {
				TriColours triangleColours = (TriColours) keysTriColo[i];
				double[] arrCountDist = mTriangleColoursTriangleEdgeCountsTemp.get(triangleColours);

				// create a new array for storing avg count
				double[] arrCountDistTemp = new double[4]; // additional element in the array for storing average count
															// of triangles per vertices
				arrCountDistTemp[0] = arrCountDist[0];
				arrCountDistTemp[1] = arrCountDist[1];
				arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution
				// Add at the 3rd index, avg count of triangles per total number of vertices in
				// the input graph. Note: 0th and 1st indices store the number of edges and
				// number of triangles respectively.
				arrCountDistTemp[3] = arrCountDist[0] * 1.0 / totalVertices;

				LOGGER.debug("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", "
						+ triangleColours.getC() + ".");
				LOGGER.debug("Total number of vertices: " + totalVertices);
				LOGGER.debug("Number of triangles: " + arrCountDist[0]);
				LOGGER.debug("Number of edges: " + arrCountDist[1]);
				LOGGER.debug("Dist: " + arrCountDist[2]);
				LOGGER.debug("Avg per no. of vertices: " + arrCountDistTemp[3]);

				// update the distribution in global maps
				if (mTriColoEdgesTriCountDistAvg.containsKey(triangleColours)) {

					// get old distribution values
					double[] previousCountdist = mTriColoEdgesTriCountDistAvg.get(triangleColours);

					// update the distribution values
					arrCountDistTemp[0] += previousCountdist[0];
					arrCountDistTemp[1] += previousCountdist[1];
					arrCountDistTemp[2] += previousCountdist[2];
					arrCountDistTemp[3] += previousCountdist[3];

				}
				mTriColoEdgesTriCountDistAvg.put(triangleColours, arrCountDistTemp);
			}
		}
	}

	private void updateCountIso2Simplexes(ColouredGraph graph,
			ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosTriEdgeCountsTemp,
			int countIsolatedTriangles) {
		// Logic to get total number of vertices in the input graph excluding class
		// vertices
		double totalVertices = graph.getNumberOfVertices();

		// *************************** Logic to update map for isolated triangles
		// ***************************************//

		double totalDistTri = 0.0;

		// Iterate over temporary map and add an element at the 3rd index of the array
		// (No. of edges in triangle)/(Total number of triangles)
		Object[] keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
		for (int i = 0; i < keysTriColo.length; i++) {
			if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
				TriColours triangleColours = (TriColours) keysTriColo[i];
				double[] arrCountsDist = mIsolatedTriColosTriEdgeCountsTemp.get(triangleColours);

				// create a new array for storing avg count
				double[] arrCountDistTemp = new double[3];
				arrCountDistTemp[0] = arrCountsDist[0];
				arrCountDistTemp[1] = arrCountsDist[1];
				arrCountDistTemp[2] = (arrCountsDist[1] * 1.0) / countIsolatedTriangles;
				totalDistTri = totalDistTri + arrCountDistTemp[2]; // incrementing total distribution

				mIsolatedTriColosTriEdgeCountsTemp.put(triangleColours, arrCountDistTemp); // update the map with the
																							// new array
			}
		}

		LOGGER.debug("Isolated triangles: ");
		// iterate over results and update the global map
		keysTriColo = mIsolatedTriColosTriEdgeCountsTemp.keys;
		for (int i = 0; i < keysTriColo.length; i++) {
			if (mIsolatedTriColosTriEdgeCountsTemp.allocated[i]) {
				TriColours triangleColours = (TriColours) keysTriColo[i];
				double[] arrCountDist = mIsolatedTriColosTriEdgeCountsTemp.get(triangleColours);

				// create a new array for storing avg count
				double[] arrCountDistTemp = new double[3]; // additional element in the array for storing average count
															// of triangles per vertices
				arrCountDistTemp[0] = arrCountDist[0];
				arrCountDistTemp[1] = arrCountDist[1];
				arrCountDistTemp[2] = (arrCountDist[2] * 1.0) / totalDistTri; // update the distribution

				LOGGER.debug("Triangle colors: " + triangleColours.getA() + ", " + triangleColours.getB() + ", "
						+ triangleColours.getC() + ".");
				LOGGER.debug("Total number of vertices: " + totalVertices);
				LOGGER.debug("Number of triangles: " + arrCountDist[0]);
				LOGGER.debug("Number of edges: " + arrCountDist[1]);
				LOGGER.debug("Dist: " + arrCountDist[2]);

				// update the distribution in global maps
				if (mIsolatedTriColoEdgesTriCountDistAvg.containsKey(triangleColours)) {

					// get old distribution values
					double[] previousCountdist = mIsolatedTriColoEdgesTriCountDistAvg.get(triangleColours);

					// update the distribution values
					arrCountDistTemp[0] = arrCountDistTemp[0] + previousCountdist[0];
					arrCountDistTemp[1] = arrCountDistTemp[1] + previousCountdist[1];
					arrCountDistTemp[2] = arrCountDistTemp[2] + previousCountdist[2];

				}
				mIsolatedTriColoEdgesTriCountDistAvg.put(triangleColours, arrCountDistTemp); // update the map with the
																								// new array
			}
		}
	}

	private boolean verticesFormingIsolatedTriangles(int vertexID1, int vertexID2, int vertexID3, ColouredGraph graph,
			IntSet classVertices) {
		// get vertices incident on input ids
		IntSet vertices1Incident = IntSetUtil.union(graph.getInNeighbors(vertexID1), graph.getOutNeighbors(vertexID1));
		IntSet vertices2Incident = IntSetUtil.union(graph.getInNeighbors(vertexID2), graph.getOutNeighbors(vertexID2));
		IntSet vertices3Incident = IntSetUtil.union(graph.getInNeighbors(vertexID3), graph.getOutNeighbors(vertexID3));

		// find union of vertices incident to all ids
		IntSet unionResult = IntSetUtil.union(IntSetUtil.union(vertices1Incident, vertices2Incident),
				vertices3Incident);

		// do not consider class vertices
		IntSet differenceResult = IntSetUtil.difference(unionResult, classVertices);

		IntSet inputVertexIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
		inputVertexIDs.add(vertexID1);
		inputVertexIDs.add(vertexID2);
		inputVertexIDs.add(vertexID3);

		differenceResult = IntSetUtil.difference(differenceResult, inputVertexIDs);

		if (differenceResult.size() == 0)
			return true;

		return false;

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

	// ****************************public getters for triangle counts, edges and
	// vertices*****************************************//

	public ObjectObjectOpenHashMap<TriColours, double[]> getmTriColoEdgesTriCountDistAvg() {
		return mTriColoEdgesTriCountDistAvg;
	}

	public ObjectObjectOpenHashMap<TriColours, double[]> getmIsolatedTriColoEdgesTriCountDistAvg() {
		return mIsolatedTriColoEdgesTriCountDistAvg;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsConnectedTri() {
		return mGraphsEdgesIdsTriangle;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsEdgesIdsIsolatedTri() {
		return mGraphsEdgesIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsIsolatedTri() {
		return mGraphsVertIdsIsolatedTri;
	}

	public ObjectObjectOpenHashMap<Integer, IntSet> getmGraphsVertIdsConnectedTri() {
		return mGraphsVertIdsConnectTriangles;
	}

}
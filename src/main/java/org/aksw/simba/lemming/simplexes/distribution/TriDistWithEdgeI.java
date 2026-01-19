package org.aksw.simba.lemming.simplexes.distribution;

import java.util.Random;

import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.simplexes.EdgeColorsSorted;
import org.aksw.simba.lemming.simplexes.TriColours;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;


public class TriDistWithEdgeI extends TriDistI {

	/**
	 * Map for storing vertex colors for the edge along with the probability for
	 * them and distributions in an array. Note: Count of edges is stored at the 0th
	 * index of the array, the probability of an edge is stored at the 1st index and
	 * the average count of edges per no. of vertices in the input graph is stored
	 * at the 2nd index.
	 */
	private ObjectObjectOpenHashMap<EdgeColorsSorted, double[]> mColoEdgesCountDistAvg = new ObjectObjectOpenHashMap<EdgeColorsSorted, double[]>();

	double totalEdgeDist = 0;

	/**
	 * Object for storing distributions by analyzing triangles found in all input
	 * graphs.
	 */
	private OfferedItemByRandomProb<EdgeColorsSorted> potentialEdgeProposer;

	public TriDistWithEdgeI(ObjectObjectOpenHashMap<TriColours, double[]> mTriColoEdgesTriCountDistAvg,
			ObjectObjectOpenHashMap<TriColours, double[]> mIsolatedTriColosCountsDist, int iNoOfVersions,
			int mIDesiredNoOfVertices, Random mRandom) {
		super();
		this.iNoOfVersions = iNoOfVersions;
		this.mRandom = mRandom;

		// Initializing the map variables
		mTriangleColorsv1v2v3 = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, double[]>>>();
		// this.mTriangleColorsv1v2v3 = mTriangleColorsProbability;

		// Update average vertices count for triangles and create different permutations
		// for triangle colors
		Object[] keysTriangleColours = mTriColoEdgesTriCountDistAvg.keys;
		for (int i = 0; i < keysTriangleColours.length; i++) {
			if (mTriColoEdgesTriCountDistAvg.allocated[i]) {
				TriColours triangleColorObj = (TriColours) keysTriangleColours[i];
				double[] triangleCountsDistArr = mTriColoEdgesTriCountDistAvg.get(triangleColorObj);
				triangleCountsDistArr[3] = (triangleCountsDistArr[3] * mIDesiredNoOfVertices) / iNoOfVersions;

				// Update or insert into first map to store the combination of 1st vertex, 2nd
				// vertex, and 3rd vertex, and their count
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getB(),
						triangleColorObj.getC(), triangleCountsDistArr);
				updateEdgeColorsProb(triangleColorObj.getA(), triangleColorObj.getB(), triangleCountsDistArr);

				// Update or insert into second map to store the combination of 2nd vertex, 3rd
				// vertex, and 1st vertex, and their count
				// Note: BitSet objects are already sorted in the TriangleColors object, and
				// here the first two BitSet parameters of the function should be sorted in
				// ascending order
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getB(), triangleColorObj.getC(),
						triangleColorObj.getA(), triangleCountsDistArr);
				updateEdgeColorsProb(triangleColorObj.getB(), triangleColorObj.getC(), triangleCountsDistArr);

				// Update or insert into second map to store the combination of 2nd vertex, 3rd
				// vertex, and 1st vertex, and their count
				storeTriangleColorsInMaps(mTriangleColorsv1v2v3, triangleColorObj.getA(), triangleColorObj.getC(),
						triangleColorObj.getB(), triangleCountsDistArr);
				updateEdgeColorsProb(triangleColorObj.getA(), triangleColorObj.getC(), triangleCountsDistArr);

			}
		}

		// *************************** Below computation is performed for Triangle
		// colours and edge counts **********************//
		// Below distribution is used to propose a triangle and more edges are added to
		// this triangle.

		// Initialize distribution of triangle colors based on edge count
		potentialConnectedTriangleProposer = initializeDistributionAllTriangles(mTriColoEdgesTriCountDistAvg);

		potentialIsolatedTriangleProposer = initializeDistributionAllTriangles(mIsolatedTriColosCountsDist);

		// Add the probability of the third vertex given two vertices
		updateVertexProbabilities();

		// Compute Final distribution for edge colors
		computeFinalDistributionEdgeColorsProb();

		// Initialize Probability distribution
		potentialEdgeProposer = initializeDistributionAllEdgeColos(mColoEdgesCountDistAvg);
	}

	/**
	 * This method initializes the probability distribution of edgecolors.
	 * 
	 */
	private OfferedItemByRandomProb<EdgeColorsSorted> initializeDistributionAllEdgeColos(
			ObjectObjectOpenHashMap<EdgeColorsSorted, double[]> mEdgeColoursCountsDist) {

		OfferedItemByRandomProb<EdgeColorsSorted> edgeProposerOutput;

		int numberOfDistinctEdgeColours = mEdgeColoursCountsDist.assigned;

		// create sample space & values for triangle colors
		EdgeColorsSorted[] edgeColorsSampleSpace = new EdgeColorsSorted[numberOfDistinctEdgeColours];
		double[] computedArrFromInput = new double[numberOfDistinctEdgeColours];

		Object[] keysTriangleColours = mEdgeColoursCountsDist.keys;

		int i = 0; // temp variable to track index of array
		for (int mapKeyIndex = 0; mapKeyIndex < keysTriangleColours.length; mapKeyIndex++) {
			if (mEdgeColoursCountsDist.allocated[mapKeyIndex]) {
				EdgeColorsSorted edgeColorObj = (EdgeColorsSorted) keysTriangleColours[mapKeyIndex];
				double[] edgeColoCountArr = mEdgeColoursCountsDist.get(edgeColorObj);
				double countOfEdges = edgeColoCountArr[0];// probability of edge is stored at 1st index

				edgeColorsSampleSpace[i] = edgeColorObj;

				double tempMeanValue = countOfEdges;// * 1.0 / iNoOfVersions;
				computedArrFromInput[i] = tempMeanValue;

				i++;
			}
		}

		ObjectDistribution<EdgeColorsSorted> potentialEdge = new ObjectDistribution<EdgeColorsSorted>(
				edgeColorsSampleSpace, computedArrFromInput);
		if (!potentialEdge.isEmpty()) // When samplespace is empty, initialzing the proposer with null
			edgeProposerOutput = new OfferedItemByRandomProb<EdgeColorsSorted>(potentialEdge, mRandom);
		else
			edgeProposerOutput = null;

		return edgeProposerOutput;

	}

	private void updateEdgeColorsProb(BitSet sortedColo1, BitSet sortedColo2, double[] distArr) {
		EdgeColorsSorted edgeColoObj = new EdgeColorsSorted(sortedColo1, sortedColo2);
		double[] clonedArr = distArr.clone();
		double[] arrTemp = new double[1];
		arrTemp[0] = clonedArr[2];
		totalEdgeDist = totalEdgeDist + arrTemp[0];

		if (mColoEdgesCountDistAvg.containsKey(edgeColoObj)) {
			// get old distribution values
			double[] previousCountdist = mColoEdgesCountDistAvg.get(edgeColoObj);

			// update the distribution values
			arrTemp[0] = arrTemp[0] + previousCountdist[0];

		}
		mColoEdgesCountDistAvg.put(edgeColoObj, arrTemp); // update the map with the new array

	}

	private void computeFinalDistributionEdgeColorsProb() {
		Object[] keysEdgeColos = mColoEdgesCountDistAvg.keys;
		for (int i = 0; i < keysEdgeColos.length; i++) {
			if (mColoEdgesCountDistAvg.allocated[i]) {
				EdgeColorsSorted edgeColorsSorted = (EdgeColorsSorted) keysEdgeColos[i];
				double[] arrDist = mColoEdgesCountDistAvg.get(edgeColorsSorted);
				arrDist[0] = arrDist[0] * 1.0 / totalEdgeDist;
				mColoEdgesCountDistAvg.put(edgeColorsSorted, arrDist);
			}
		}
	}

	public OfferedItemByRandomProb<EdgeColorsSorted> getPotentialEdgeProposer() {
		return potentialEdgeProposer;
	}
}

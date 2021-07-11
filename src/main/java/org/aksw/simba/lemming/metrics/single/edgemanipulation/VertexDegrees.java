package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.ColouredGraph;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Class for storing in and out degree of vertices.
 * 
 * @author Atul
 *
 */
public class VertexDegrees {

	// Maps for storing in-degree and out-degree for different vertices
	private int mMapVerticesInDegree[];
	private int mMapVerticesOutDegree[];

	/**
	 * Initializing the Maps and calling computeVerticesDegree method.
	 * 
	 * @param clonedGraph
	 */
	public VertexDegrees(ColouredGraph clonedGraph) {
		// Initialize Hash Map for storing degree of vertices
		computeVerticesDegree(clonedGraph);
	}

	/**
	 * Computing the in degree and out degree for all vertices in graph.
	 * 
	 * @param graph
	 */
	private void computeVerticesDegree(ColouredGraph graph) {
		IntSet vertices = graph.getVertices();
		mMapVerticesInDegree = new int[vertices.size()];
		mMapVerticesOutDegree = new int[vertices.size()];
		int index = 0;
		IntIterator iterator = vertices.iterator();
		while (iterator.hasNext()) {
			int nextInt = iterator.nextInt();
			int inVertexDegree = graph.getGraph().getInVertexDegree(nextInt);
			int outVertexDegree = graph.getGraph().getOutVertexDegree(nextInt);

			mMapVerticesInDegree[index] = inVertexDegree;
			mMapVerticesOutDegree[index] = outVertexDegree;
			index++;
		}
	}

	/**
	 * Returns in degree for input vertex id.
	 * 
	 * @param vertexId
	 * @return
	 */
	public int getVertexInDegree(int vertexId) {
		return mMapVerticesInDegree[vertexId];
	}

	/**
	 * Returns out degree for input vertex id.
	 * 
	 * @param vertexId
	 * @return
	 */
	public int getVertexOutDegree(int vertexId) {
		return mMapVerticesOutDegree[vertexId];
	}

	/**
	 * Returns out degree for input vertex id.
	 * 
	 * @param vertexId
	 * @return
	 */
	public int getVertexDegree(int vertexId, DIRECTION direction) {

		int degree;
		if (direction == DIRECTION.in) {
			degree = mMapVerticesInDegree[vertexId];
		} else {
			degree = mMapVerticesOutDegree[vertexId];
		}

		return degree;
	}

	/**
	 * Returns all vertices with input in degree.
	 * 
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForInDegree(int degree) {
		IntSet setOfVertices = new IntOpenHashSet();

		for (int j = 0; j < mMapVerticesInDegree.length; ++j) {
			if (mMapVerticesInDegree[j] == degree) {
				setOfVertices.add(j);
			}
		}

		return setOfVertices;
	}

	/**
	 * Returns all vertices with input out degree.
	 * 
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForOutDegree(int degree) {
		IntSet setOfVertices = new IntOpenHashSet();

		for (int j = 0; j < mMapVerticesOutDegree.length; ++j) {
			if (mMapVerticesOutDegree[j] == degree) {
				setOfVertices.add(j);
			}
		}

		return setOfVertices;
	}

	/**
	 * Returns all vertices with input degree and direction.
	 * 
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForDegree(int degree, DIRECTION direction) {
		IntSet setOfVertices = new IntOpenHashSet();

		int mMapVerticesDegreeTemp[];

		if (direction == DIRECTION.in) {
			mMapVerticesDegreeTemp = mMapVerticesInDegree;
		} else {
			mMapVerticesDegreeTemp = mMapVerticesOutDegree;
		}

		for (int j = 0; j < mMapVerticesDegreeTemp.length; ++j) {
			if (mMapVerticesDegreeTemp[j] == degree) {
				setOfVertices.add(j);
			}
		}

		return setOfVertices;
	}

	/**
	 * Updates the in-degree for input vertex id with specified additionValue.
	 * 
	 * @param vertexId
	 * @param additionValue
	 */
	public void updateVertexInDegree(int vertexId, int additionValue) {
		mMapVerticesInDegree[vertexId] = mMapVerticesInDegree[vertexId] + additionValue;
	}

	/**
	 * Updates the out-degree for input vertex id with specified additionValue.
	 * 
	 * @param vertexId
	 * @param additionValue
	 */
	public void updateVertexOutDegree(int vertexId, int additionValue) {
		mMapVerticesOutDegree[vertexId] = mMapVerticesOutDegree[vertexId] + additionValue;
	}

	public int[] getMapVerticesInDegree() {
		return mMapVerticesInDegree;
	}

	public int[] getMapVerticesOutDegree() {
		return mMapVerticesOutDegree;
	}
}

package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import org.aksw.simba.lemming.ColouredGraph;

import com.carrotsearch.hppc.IntIntOpenHashMap;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/** Class for storing in and out degree of vertices.
 * @author Atul
 *
 */
public class VertexDegrees {

	// Maps for storing in-degree and out-degree for different vertices
	private IntIntOpenHashMap mMapVerticesinDegree;
	private IntIntOpenHashMap mMapVerticesoutDegree;

	/**
	 * Initializing the Maps and calling computeVerticesDegree method.
	 * @param clonedGraph
	 */
	public VertexDegrees(ColouredGraph clonedGraph) {
		// Initialize Hash Map for storing degree of vertices
		mMapVerticesinDegree = new IntIntOpenHashMap();
		mMapVerticesoutDegree = new IntIntOpenHashMap();
		computeVerticesDegree(clonedGraph);
	}

	/**
	 * Computing the in degree and out degree for all vertices in graph.
	 * @param graph
	 */
	private void computeVerticesDegree(ColouredGraph graph) {
		IntSet vertices = graph.getVertices();
		IntIterator iterator = vertices.iterator();
		while (iterator.hasNext()) {
			int nextInt = iterator.nextInt();
			int inVertexDegree = graph.getGraph().getInVertexDegree(nextInt);
			int outVertexDegree = graph.getGraph().getOutVertexDegree(nextInt);

			mMapVerticesinDegree.put(nextInt, inVertexDegree);
			mMapVerticesoutDegree.put(nextInt, outVertexDegree);
		}
	}

	/**
	 * Returns in degree for input vertex id.
	 * @param vertexId
	 * @return
	 */
	public int getVertexIndegree(int vertexId) {
		int inDegree = mMapVerticesinDegree.get(vertexId);
		return inDegree;
	}

	/**
	 * Returns out degree for input vertex id.
	 * @param vertexId
	 * @return
	 */
	public int getVertexOutdegree(int vertexId) {
		int outDegree = mMapVerticesoutDegree.get(vertexId);
		return outDegree;
	}
	
	/**
	 * Returns out degree for input vertex id.
	 * @param vertexId
	 * @return
	 */
	public int getVertexdegree(int vertexId, DIRECTION direction) {

		int degree;
		if (direction == DIRECTION.in) {
			degree = mMapVerticesinDegree.get(vertexId);
		} else {
			degree = mMapVerticesoutDegree.get(vertexId);
		}

		return degree;
	}
	

	/**
	 * Returns all vertices with input in degree.
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForIndegree(int degree) {
		IntSet setOfVertices = new IntOpenHashSet();
		int keys[] = mMapVerticesinDegree.keys().toArray();

		for (int j = 0; j < keys.length; ++j) {
			if (mMapVerticesinDegree.get(keys[j]) == degree) {
				setOfVertices.add(keys[j]);
			}
		}

		return setOfVertices;
	}
	
	/**
	 * Returns all vertices with input out degree.
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForOutdegree(int degree) {
		IntSet setOfVertices = new IntOpenHashSet();
		int keys[] = mMapVerticesoutDegree.keys().toArray();

		for (int j = 0; j < keys.length; ++j) {
			if (mMapVerticesoutDegree.get(keys[j]) == degree) {
				setOfVertices.add(keys[j]);
			}
		}

		return setOfVertices;
	}
	
	/**
	 * Returns all vertices with input degree and direction.
	 * @param degree
	 * @return
	 */
	public IntSet getVerticesForDegree(int degree, DIRECTION direction) {
		IntSet setOfVertices = new IntOpenHashSet();
		
		IntIntOpenHashMap mMapVerticesDegreeTemp = new IntIntOpenHashMap();
		
		if(direction == DIRECTION.in) {
			mMapVerticesDegreeTemp = mMapVerticesinDegree;
		}
		else {
			mMapVerticesDegreeTemp = mMapVerticesoutDegree;
		}
		
		int keys[] = mMapVerticesDegreeTemp.keys().toArray();

		for (int j = 0; j < keys.length; ++j) {
			if (mMapVerticesDegreeTemp.get(keys[j]) == degree) {
				setOfVertices.add(keys[j]);
			}
		}

		return setOfVertices;
	}

	/**
	 * Updates the in-degree for input vertex id with specified additionValue.
	 * @param vertexId
	 * @param additionValue
	 */
	public void updateVertexIndegree(int vertexId, int additionValue) {
		mMapVerticesinDegree.putOrAdd(vertexId, vertexId, additionValue);
	}
	
	/**
	 * Updates the out-degree for input vertex id with specified additionValue.
	 * @param vertexId
	 * @param additionValue
	 */
	public void updateVertexOutdegree(int vertexId, int additionValue) {
		mMapVerticesoutDegree.putOrAdd(vertexId, vertexId, additionValue);
	}

	public IntIntOpenHashMap getmMapVerticesinDegree() {
		return mMapVerticesinDegree;
	}

	public IntIntOpenHashMap getmMapVerticesoutDegree() {
		return mMapVerticesoutDegree;
	}
}

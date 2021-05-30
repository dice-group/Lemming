package org.aksw.simba.lemming.metrics.single.edgemanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This class computes and stores the candidates for metrics and their metric values. These values can be used again instead of re-computing them. 
 * @author Atul
 *
 */
public class MetricComputations {
	

	private HashMap<String, IntSet> mMapCandidatesMetricTemp; // Temp Map for storing candidate vertices for metric computation
	private HashMap<String, Double> mMapCandidatesMetricValuesTemp; //Temp Map for storing metric values
	VertexDegrees mVertexDegrees;
	
	private HashMap<String, IntSet> mMapCandidatesMetric= new HashMap<>(); // Map for storing candidate vertices for Max Degree metric computation
	private HashMap<String, Double> mMapCandidatesMetricValues= new HashMap<>(); // Map for storing max degree of vertices

	public MetricComputations(ColouredGraph clonedGraph) {
		

		List<String> metrics = new ArrayList<>();
		metrics.add("RemoveAnEdgeIndegree");
		metrics.add("RemoveAnEdgeOutdegree");
		metrics.add("AddAnEdgeIndegree");
		metrics.add("AddAnEdgeOutdegree");

		// Initialize Hash map for candidate vertices (MaxVertexDegreeMetric).
		mMapCandidatesMetricTemp = new HashMap<>();
		mMapCandidatesMetric = new HashMap<>();
		for (String metric : metrics) {
			mMapCandidatesMetric.put(metric, new IntOpenHashSet());
			mMapCandidatesMetricTemp.put(metric, new IntOpenHashSet());
		}

		// Initialize Hash map for storing maximum vertex degrees for different cases.
		mMapCandidatesMetricValuesTemp = new HashMap<>();
		mMapCandidatesMetricValues = new HashMap<>();
		for (String metric : metrics) {
			mMapCandidatesMetricValues.put(metric, 0.0);
			mMapCandidatesMetricValuesTemp.put(metric, 0.0);
		}

		mVertexDegrees = new VertexDegrees(clonedGraph);
	}

	public HashMap<String, IntSet> getmMapCandidatesMetricTemp() {
		return mMapCandidatesMetricTemp;
	}

	public void setmMapCandidatesMetricTemp(HashMap<String, IntSet> mMapCandidatesMetricTemp1,String key) {
			mMapCandidatesMetricTemp.replace(key, mMapCandidatesMetricTemp1.get(key));
	}

	public HashMap<String, Double> getmMapCandidatesMetricValuesTemp() {
		return mMapCandidatesMetricValuesTemp;
	}

	public void setmMapCandidatesMetricValuesTemp(HashMap<String, Double> mMapCandidatesMetricValuesTemp1, String key) {
		//for(String key : mMapCandidatesMetricValuesTemp1.keySet()) {
			mMapCandidatesMetricValuesTemp.replace(key, mMapCandidatesMetricValuesTemp1.get(key));
		//}
	}

	public HashMap<String, IntSet> getmMapCandidatesMetric() {
		return mMapCandidatesMetric;
	}

	/** Storing values from temporary maps.
	 * @param mMapCandidatesMetric1 - Map storing the candidates.
	 * @param mMapCandidatesMetricValues1 - Map storing the candidate values.
	 */
	public void setCandidatesMetricRemoveAnEdge(HashMap<String, IntSet> mMapCandidatesMetric1, HashMap<String, Double> mMapCandidatesMetricValues1) {

		mMapCandidatesMetric.replace("RemoveAnEdgeIndegree", mMapCandidatesMetric1.get("RemoveAnEdgeIndegree"));
		mMapCandidatesMetric.replace("RemoveAnEdgeOutdegree", mMapCandidatesMetric1.get("RemoveAnEdgeOutdegree"));
		mMapCandidatesMetricValues.replace("RemoveAnEdgeIndegree", mMapCandidatesMetricValues1.get("RemoveAnEdgeIndegree"));
		mMapCandidatesMetricValues.replace("RemoveAnEdgeOutdegree", mMapCandidatesMetricValues1.get("RemoveAnEdgeOutdegree"));

	}
	
	public void setCandidatesMetricAddAnEdge(HashMap<String, IntSet> mMapCandidatesMetric1, HashMap<String, Double> mMapCandidatesMetricValues1) {

		mMapCandidatesMetric.replace("AddAnEdgeIndegree", mMapCandidatesMetric1.get("AddAnEdgeIndegree"));
		mMapCandidatesMetric.replace("AddAnEdgeOutdegree", mMapCandidatesMetric1.get("AddAnEdgeOutdegree"));
		mMapCandidatesMetricValues.replace("AddAnEdgeIndegree", mMapCandidatesMetricValues1.get("AddAnEdgeIndegree"));
		mMapCandidatesMetricValues.replace("AddAnEdgeOutdegree", mMapCandidatesMetricValues1.get("AddAnEdgeOutdegree"));

	}

	public HashMap<String, Double> getmMapCandidatesMetricValues() {
		return mMapCandidatesMetricValues;
	}


	/**
	 * The method checks if we need to compute in degree or out-degree and then
	 * calls the metricComputationMaxDegree with correct parameters or for other metrics calls the apply method.
	 * Returns the metric value.
	 * 
	 * @param triple - The edge that is modified.
	 * @param metric - metric which should be calculated.
	 * @param graph - graph for which metric is calculated.
	 * @param graphOperation - can be "RemoveAnEdge" or "AddAnEdge" indicating how the edge is modified.
	 * @return
	 */
	public double getMetricValue(TripleBaseSingleID triple, SingleValueMetric metric, ColouredGraph graph,
			String graphOperation) {
		double metVal = 0;

		switch (metric.getName()) {
			case "maxInDegree":
				graphOperation = graphOperation + "Indegree";				
				metVal = metricComputationMaxDegree(metric, graph, graphOperation, DIRECTION.in, triple.headId, triple, checkIfRemoveOrAddEdge(graphOperation));
				break;
				// Logic to check if maxDegreeMetric call is required for maxInDegree, defined in method : metricComputationMaxDegree

			case "maxOutDegree":
				graphOperation = graphOperation + "Outdegree";
				metVal = metricComputationMaxDegree(metric, graph, graphOperation, DIRECTION.out, triple.tailId, triple, checkIfRemoveOrAddEdge(graphOperation));
				break;

			default:// If metric is other than maxInDegree and maxOutDegree then apply the metric
				metVal = metric.apply(graph);
		}

		return metVal;
	}
	
	/**
	 * Returns +1 or -1, specifies how the degree is modified for input operation.
	 * @param graphOperation can be "RemoveAnEdge" or "AddAnEdge" indicating how the edge is modified.
	 * @return
	 */
	private int checkIfRemoveOrAddEdge(String graphOperation) {
		int updateVertexDegree;
		if (graphOperation.contains("RemoveAnEdge")) {
			// Since Edge is removed update the degrees of vertices by subtracting 1
			updateVertexDegree = -1;
		}
		else {
			// Since Edge is added update the degrees of vertices by adding 1.
			updateVertexDegree = 1;
		}
		return updateVertexDegree;
	}
	
	/** Checks If previous candidates are not changed.
	 * @param metricName
	 * @param triple
	 */
	public void verifyCandidates(String metricName,TripleBaseSingleID triple ) {
		for(String key:mMapCandidatesMetric.keySet()) {
			if(!key.contains(metricName)) {
				IntSet intSet = mMapCandidatesMetric.get(key);
				if(intSet.contains(triple.headId) || intSet.contains(triple.tailId)) {
					mMapCandidatesMetric.replace(key, new IntOpenHashSet());
					mMapCandidatesMetricValues.replace(key, 0.0);
				}
			}
		}
	}

	/**
	 * The method contains logic that reduces the number of calls to apply method
	 * for the max vertex degree metric.
	 * 
	 * @param metric - metric which should be calculated.
	 * @param graph - input graph.
	 * @param metricName - can be "RemoveAnEdge" or "AddAnEdge" indicating how the edge is modified.
	 * @param direction - this is in or out based on the operation.
	 * @param tripleID - The vertex that is modified.
	 * @return
	 */
	private double metricComputationMaxDegree(SingleValueMetric metric, ColouredGraph graph, String metricName,
			DIRECTION direction, int tripleID, TripleBaseSingleID triple, int updateVertexDegree) {
		double metVal;
		
		// Logic to update the degree for the vertices
		//int edgeUpdate = updateVerticesdegree(metricName, triple); // For removing an edge it will be -1 and for adding an edge it will be +1.
		//mVertexDegrees.updateVertexIndegree(triple.headId, updateVertexDegree);
		//mVertexDegrees.updateVertexOutdegree(triple.tailId, updateVertexDegree);	

		// Set Temporary maps
		setmMapCandidatesMetricTemp(mMapCandidatesMetric, metricName);
		setmMapCandidatesMetricValuesTemp(mMapCandidatesMetricValues, metricName);
		
		IntSet intSetTemp = mMapCandidatesMetricTemp.get(metricName);// Get the current candidate set

		if (intSetTemp.size() == 0) { // Initially the Candidate set will be empty, hence need to call the apply method and store the candidates

			metVal = metric.apply(graph); // apply the metric and get the value
			
			mMapCandidatesMetricValuesTemp.replace(metricName, metVal); // Store the metric value for later use
			mMapCandidatesMetricValues.put(metricName, metVal);
			
			IntSet maxDegreeVertices;
			maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction); // Get the vertex with the metric value
			//mMapCandidatesMetricTemp.replace(metricName, maxDegreeVertices);
			intSetTemp.addAll(maxDegreeVertices); // store the vertex with metric value in candidate set

		} else {

			if (intSetTemp.contains(tripleID)) { // The Edge for vertex in candidate set is modified
				metVal = mMapCandidatesMetricValuesTemp.get(metricName);
				
				if (intSetTemp.size() == 1) { // If there is only single vertex in the candidate list then update the max degree value
					
					if(updateVertexDegree > 0) {
						metVal = metVal + updateVertexDegree;
					}else {
						metVal = metric.apply(graph); // apply the metric and get the value
						IntSet maxDegreeVertices;
						maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction); // Get the vertex with the metric value
						
						//intSetTemp.clear(); // Remove previous candidates
						mMapCandidatesMetricTemp.replace(metricName, new IntOpenHashSet());
						
						//intSetTemp.addAll(maxDegreeVertices); // store the vertex with metric value in candidate set
						mMapCandidatesMetricTemp.replace(metricName, maxDegreeVertices);
						
					}
					mMapCandidatesMetricValuesTemp.replace(metricName, metVal); // Store the metric value for later use
					
					
					//metVal = metVal + updateVertexDegree;
					//mMapCandidatesMetricValues.replace(metricName, metVal + updateVertexDegree);
				}else { 

					if(updateVertexDegree > 0) {
						// The other vertices that exist in the candidate set can be removed since the max degree will be increased
						
						//intSetTemp.clear();
						mMapCandidatesMetricTemp.replace(metricName, new IntOpenHashSet());
						
						
						IntSet candidate = new IntOpenHashSet();
						candidate.add(tripleID);
						//intSetTemp.add(tripleID);
						mMapCandidatesMetricTemp.replace(metricName, candidate);
						
						
						metVal = metVal + updateVertexDegree;
						mMapCandidatesMetricValuesTemp.replace(metricName, metVal + updateVertexDegree);
					}else {
						// The current vertex can be removed from the candidate set can be removed since its degree is reduced 
						// and the previous max degree value can be used.
						IntSet candidates= new IntOpenHashSet();
						IntIterator iterator = intSetTemp.iterator();
						while(iterator.hasNext()) {
							int tempTripleId = iterator.nextInt();
							if(tripleID!=tempTripleId)
								candidates.add(iterator.nextInt());
						}
						mMapCandidatesMetricTemp.replace(metricName, candidates);
						//intSetTemp.remove(tripleID);
					}
				}
				
				//checkCandidatesVertices();
			} else { // If Edge for vertex in candidate set is not modified then we can use the previously stored values
				metVal = mMapCandidatesMetricValuesTemp.get(metricName);
				int inVertexDegreeTemp;
				inVertexDegreeTemp = mVertexDegrees.getVertexdegree(tripleID,direction);
				

				if (inVertexDegreeTemp == metVal) {
					// If vertex has a degree similar to metric value previously stored, add the vertex in candidate set
					
					IntSet candidates= new IntOpenHashSet();
					candidates.add(inVertexDegreeTemp);
					IntIterator iterator = intSetTemp.iterator();
					while(iterator.hasNext()) {
						candidates.add(iterator.nextInt());
					}
					mMapCandidatesMetricTemp.replace(metricName, candidates);
					
					//intSetTemp.add(inVertexDegreeTemp); 
				}

			}
		}

		return metVal;
	}
	
	/** For removing an edge from a graph, update the vertex in and out degrees.
	 * @param triple
	 */
	public void removeEdgeFromGraph(TripleBaseSingleID triple) {

		//graph.removeEdge(triple.edgeId);
		mVertexDegrees.updateVertexIndegree(triple.headId, -1);
		mVertexDegrees.updateVertexOutdegree(triple.tailId, -1);
	}
	
	/** For adding an edge in a graph, update the vertex in and out degrees.
	 * @param triple
	 */
	/**
	 * @param triple
	 */
	public void addEdgeToGraph(TripleBaseSingleID triple) {
         //graph.addEdge(tail, head, color);
         mVertexDegrees.updateVertexIndegree(triple.headId, 1);
 		mVertexDegrees.updateVertexOutdegree(triple.tailId, 1);
    }

}

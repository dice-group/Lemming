package org.aksw.simba.lemming.metrics.single;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.AbstractMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexDegreeMetricResult.GRAPHOPERATION;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.VertexDegrees;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;

import grph.Grph.DIRECTION;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * This metric is the highest degree of in or outgoing edges in the graph.
 * 
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class MaxVertexDegreeMetric extends AbstractMetric implements SingleValueMetric {

    protected DIRECTION direction;

    public MaxVertexDegreeMetric(DIRECTION direction) {
        super(direction == DIRECTION.in ? "maxInDegree" : "maxOutDegree");
        this.direction = direction;
    }

    @Override
    public double apply(ColouredGraph graph) {
        if (direction == DIRECTION.in) {
            return graph.getGraph().getMaxInEdgeDegrees();
        } else {
            return graph.getGraph().getMaxOutEdgeDegrees();
        }
    }
    
    /** Stores the previously computed values in UpdateMetricResult object and checks if degrees is not updated for vertices in candidate set.
     * @param graph - input graph.
     * @param graphOperation - boolean value indicating graph operation. ("true" for adding an edge and "false" for removing an edge)
     * @param triple - Edge on which graph operation is performed.
     * @param newMetricResult - UpdatableMetricResult object containing the results that should be updated.
     * @return UpdatableMetricResult object with updated candidate set and metric values.
     */
    @Override
    public UpdatableMetricResult applyUpdatable(ColouredGraph graph, boolean graphOperation, TripleBaseSingleID triple, UpdatableMetricResult newMetricResult) {
    	MaxVertexDegreeMetricResult metricResultTempObj = (MaxVertexDegreeMetricResult) newMetricResult;
    	if(graphOperation) {
    		metricResultTempObj.setCandidatesMetricAddAnEdge(metricResultTempObj.getmMapCandidatesMetricTemp(),metricResultTempObj.getmMapCandidatesMetricValuesTemp());
    		metricResultTempObj.verifyCandidates(GRAPHOPERATION.AddAnEdgeIndegree, triple);
    		metricResultTempObj.verifyCandidates(GRAPHOPERATION.AddAnEdgeOutdegree, triple);
    	}else {
    		metricResultTempObj.setCandidatesMetricRemoveAnEdge(metricResultTempObj.getmMapCandidatesMetricTemp(),metricResultTempObj.getmMapCandidatesMetricValuesTemp());
    		metricResultTempObj.verifyCandidates(GRAPHOPERATION.RemoveAnEdgeIndegree, triple);
    		metricResultTempObj.verifyCandidates(GRAPHOPERATION.RemoveAnEdgeOutdegree, triple);
    	}
    	
    	return metricResultTempObj;
    }
    
    /**
     * The method checks if we need to compute in degree or out-degree and then calls the metricComputationMaxDegree with correct parameters.
     * @param triple - edge on which graph operation is performed.
     * @param metric - input metric which needs to be computed.
     * @param graph - input graph.
     * @param graphOperation - boolean value indicating graph operation. ("true" for adding an edge and "false" for removing an edge)
     * @param previousResult - UpdatableMetricResult object containing the previous computed results.
     * @return
     */
    @Override
    public UpdatableMetricResult update(TripleBaseSingleID triple, ColouredGraph graph,
            boolean graphOperation, UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
    	UpdatableMetricResult newMetricResult;
    	
		switch (getName()) {
			case "maxInDegree":	
				
				if(graphOperation) { // graphOperation is true then add an edge otherwise its remove an edge 
					newMetricResult = metricComputationMaxDegree(graph, GRAPHOPERATION.AddAnEdgeIndegree, DIRECTION.in, triple.headId, triple, 1, previousResult, mVertexDegrees);
				}else {
					newMetricResult = metricComputationMaxDegree(graph, GRAPHOPERATION.RemoveAnEdgeIndegree, DIRECTION.in, triple.headId, triple, -1, previousResult, mVertexDegrees);
				}
				
				break;
				// Logic to check if maxDegreeMetric call is required for maxInDegree, defined in method : metricComputationMaxDegree

			case "maxOutDegree":
				if(graphOperation) {
					newMetricResult = metricComputationMaxDegree(graph, GRAPHOPERATION.AddAnEdgeOutdegree, DIRECTION.out, triple.headId, triple, 1, previousResult, mVertexDegrees);
				}else {
					newMetricResult = metricComputationMaxDegree(graph, GRAPHOPERATION.RemoveAnEdgeOutdegree, DIRECTION.out, triple.headId, triple, -1, previousResult, mVertexDegrees);
				}
				break;
			
			default:// If metric is other than maxInDegree and maxOutDegree then apply the metric
				newMetricResult = applyUpdatable(graph, graphOperation, triple, previousResult);
		}
		
        return newMetricResult;
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
	private UpdatableMetricResult metricComputationMaxDegree( ColouredGraph graph, GRAPHOPERATION metricName,
			DIRECTION direction, int tripleID, TripleBaseSingleID triple, int updateVertexDegree, UpdatableMetricResult previousResult, VertexDegrees mVertexDegrees) {
		double metVal;

		MaxVertexDegreeMetricResult metricResultTempObj;
		if (previousResult instanceof MaxVertexDegreeMetricResult) {
			metricResultTempObj = (MaxVertexDegreeMetricResult) previousResult;
		}else {
			metricResultTempObj = new MaxVertexDegreeMetricResult(getName(), 0.0);
		}
		
		// Set Temporary maps
		metricResultTempObj.setmMapCandidatesMetricTemp( metricResultTempObj.getmMapCandidatesMetric(), metricName);
		metricResultTempObj.setmMapCandidatesMetricValuesTemp(metricResultTempObj.getmMapCandidatesMetricValues(), metricName);
		
		IntSet intSetTemp = metricResultTempObj.getmMapCandidatesMetricTemp().get(metricName);// Get the current candidate set

		if (intSetTemp.size() == 0) { // Initially the Candidate set will be empty, hence need to call the apply method and store the candidates

			metVal = apply(graph); // apply the metric and get the value
			
			metricResultTempObj.getmMapCandidatesMetricValuesTemp().replace(metricName, metVal); // Store the metric value for later use
			metricResultTempObj.getmMapCandidatesMetricValues().put(metricName, metVal);
			
			IntSet maxDegreeVertices;
			maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction); // Get the vertex with the metric value
			intSetTemp.addAll(maxDegreeVertices); // store the vertex with metric value in candidate set

		} else {

			if (intSetTemp.contains(tripleID)) { // The Edge for vertex in candidate set is modified
				metVal = metricResultTempObj.getmMapCandidatesMetricValuesTemp().get(metricName);
				
				if (intSetTemp.size() == 1) { // If there is only single vertex in the candidate list then update the max degree value
					
					if(updateVertexDegree > 0) {
						metVal = metVal + updateVertexDegree;
					}else {
						metVal = apply(graph); // apply the metric and get the value
						IntSet maxDegreeVertices;
						maxDegreeVertices = mVertexDegrees.getVerticesForDegree((int) metVal, direction); // Get the vertex with the metric value
						
						// Remove previous candidates
						metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, new IntOpenHashSet());
						
						// store the vertex with metric value in candidate set
						metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, maxDegreeVertices);
						
					}
					metricResultTempObj.getmMapCandidatesMetricValuesTemp().replace(metricName, metVal); // Store the metric value for later use
					
					
				}else { 

					if(updateVertexDegree > 0) {
						// The other vertices that exist in the candidate set can be removed since the max degree will be increased
						
						metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, new IntOpenHashSet());
						
						
						IntSet candidate = new IntOpenHashSet();
						candidate.add(tripleID);
						metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, candidate);
						
						
						metVal = metVal + updateVertexDegree;
						metricResultTempObj.getmMapCandidatesMetricValuesTemp().replace(metricName, metVal + updateVertexDegree);
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
						metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, candidates);
					}
				}
				
			} else { // If Edge for vertex in candidate set is not modified then we can use the previously stored values
				metVal = metricResultTempObj.getmMapCandidatesMetricValuesTemp().get(metricName);
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
					metricResultTempObj.getmMapCandidatesMetricTemp().replace(metricName, candidates);
					
				}

			}
		}
		metricResultTempObj.setResult(metVal);// Set the new computed metric value as result
		return metricResultTempObj;
	}

}

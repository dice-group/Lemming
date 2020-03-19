package org.aksw.simba.lemming.tools;

import java.util.HashMap;
import java.util.Map;

import org.aksw.simba.lemming.BaselineGraph;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.BaselineCreator;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.metrics.dist.EdgeColourDistributionMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.VertexColourDistributionMetric;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.carrotsearch.hppc.BitSet;

public class BaselineGenerator {
	
	public static void main(String[] args) {
		Map<String, String> mapArgs = parseArguments(args);
		
		long seed = System.currentTimeMillis();
		if(mapArgs.get("-s")!=null) {
			seed = Long.parseLong(mapArgs.get("-s"));
		}
		
		Model model = ModelFactory.createDefaultModel();
		model.read(mapArgs.get("-f"));
				
		GraphCreator creator = new GraphCreator();
		ColouredGraph colouredGraph = creator.processModel(model);
		
		// vertex colour distribution
		VertexColourDistributionMetric vertexColourDistributionMetric = new VertexColourDistributionMetric();
		ObjectDistribution<BitSet> vertexDistribution = vertexColourDistributionMetric.apply(colouredGraph);
		
		//edge colour distribution
		EdgeColourDistributionMetric edgeColourDistributionMetric = new EdgeColourDistributionMetric();
		ObjectDistribution<BitSet> edgeDistribution = edgeColourDistributionMetric.apply(colouredGraph);
		
		// Generate a random, scale-free Barabasi graph
		int noNodes = colouredGraph.getVertices().size();
		double avgDegree = colouredGraph.getAverageDegree();
		BaselineGraph baselineGraph = new BaselineGraph(noNodes, avgDegree, seed);
				
		//assign colours
		BaselineCreator baselineCreator = new BaselineCreator(baselineGraph);
		baselineCreator.applyEdgeDistribution(edgeDistribution);
		baselineCreator.applyVertexDistribution(vertexDistribution);
		
	}
	/**
	 * -f file path
	 * -s seed
	 * @param args
	 */
	private static Map<String, String> parseArguments(String[] args){
		Map<String, String> mapArgs = new HashMap<String, String>();
		
		if(args.length != 0){
			for(int i = 0 ; i < args.length ; i++){
				String param = args[i];
				if((i+1) < args.length){
					String value = args[i+1];
					// file path
					if(param.equalsIgnoreCase("-f") ){
						mapArgs.put("-f", value);
					}
					//seed
					else if(param.equalsIgnoreCase("-s")){
						mapArgs.put("-s", value);
					}
				}
			}
		}
		return mapArgs;
	}



}

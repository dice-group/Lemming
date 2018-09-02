package org.aksw.simba.lemming.mimicgraph.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.expression.Expression;
import org.aksw.simba.lemming.metrics.single.SingleValueMetric;
import org.aksw.simba.lemming.metrics.single.edgemanipulation.EdgeModifier;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.ConstantValuesComputation;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.mimicgraph.metricstorage.MetricAndConstantValuesCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphOptimization {
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphOptimization.class);
	
	private int mMaxIteration = 1000 ;
	private int mMaxRepeatedSelection = 1000;
	private boolean mProcessRandomly = true;
	
	private IGraphGeneration mGraphGenerator;
	private EdgeModifier mEdgeModifier;
	private ConstantValuesComputation mErrScoreCalculator;
	private List<Double> mLstErrorScore; 
	
	/*-----------------------------------------------
	 * Variable for storing calculation information *
	 -----------------------------------------------*/
	
	
	public GraphOptimization(ColouredGraph[] origGrphs,
			IGraphGeneration graphGenerator, List<SingleValueMetric> metrics,  MetricAndConstantValuesCarrier valueCarriers) {
		
		
		mLstErrorScore = new ArrayList<Double>();
		/*
		 *  mErrScoreCalculator is used to compute the error score compared to original
		 *  constant values of the original graphs
		 */
		mErrScoreCalculator = new ConstantValuesComputation(origGrphs, valueCarriers);
		
		// the graph generator
		mGraphGenerator = graphGenerator;
		
		ColouredGraph clonedGrph = mGraphGenerator.getMimicGraph().clone();
		mEdgeModifier = new EdgeModifier(clonedGrph, metrics);
	}
	
	
	public void setRefineGraphRandomly(boolean isRandom){
		mProcessRandomly = isRandom;
	}
	
	
	public ColouredGraph refineGraph(){
		
		int noOfRepeatedParent = 0;

		double lErrScore = Double.NaN; 
		double rErrScore = Double.NaN;
		
		ObjectDoubleOpenHashMap<String> baseMetricValues = mEdgeModifier.getOriginalMetricValues();

		double pErrScore = mErrScoreCalculator.computeErrorScore(baseMetricValues); 
		for(int i = 0 ; i < mMaxIteration ; ++i){
			
			// add errorScore to tracking list result
			mLstErrorScore.add(pErrScore);
			
			// go left by removing an edge
			TripleBaseSingleID lTriple = getOfferedEdgeforRemoving(mEdgeModifier.getGraph());
			ObjectDoubleOpenHashMap<String> metricValuesOfLeft = mEdgeModifier.tryToRemoveAnEdge(lTriple);
			//System.out.println("[L]Aft -Number of edges: "+ parentGrph.getEdges().size());
			lErrScore = mErrScoreCalculator.computeErrorScore(metricValuesOfLeft);

			
			 // go right by adding a new edge
			TripleBaseSingleID rTriple = getOfferedEdgeForAdding(mEdgeModifier.getGraph());
			//System.out.println("[R]Aft -Number of edges: "+ parentGrph.getEdges().size());
			ObjectDoubleOpenHashMap<String> metricValuesOfRight =  mEdgeModifier.tryToAddAnEdge(rTriple);
			rErrScore = mErrScoreCalculator.computeErrorScore(metricValuesOfRight);
			
			// find min error score
			double minErrScore = minValues(pErrScore, lErrScore, rErrScore);
			
			System.out.println("("+i+"/ "+mMaxIteration+") Mid: "+ pErrScore 
					+ " - Left: "+ lErrScore +" - Right: " + rErrScore);
			
			if(minErrScore == lErrScore){
				
				pErrScore = lErrScore;
				
				noOfRepeatedParent = 0;
				mEdgeModifier.updateMapMetricValues(metricValuesOfLeft);
				mEdgeModifier.executeRemovingAnEdge();
				continue;
			}
			if(minErrScore == rErrScore){
				
				pErrScore = rErrScore;
				
				noOfRepeatedParent = 0;
				mEdgeModifier.updateMapMetricValues(metricValuesOfRight);
				mEdgeModifier.executeAddingAnEdge();
				continue;
			}
			
			noOfRepeatedParent ++;
			
			if(noOfRepeatedParent == mMaxRepeatedSelection){
				LOGGER.info("Cannot find better refined graph! Break the loop!");
				break;
			}
		}
		
		return mEdgeModifier.getGraph();
	}
	
	/**
	 * Compute the smallest error score among the three inputs
	 * 
	 * @param pErrScore the error score at the parent node
	 * @param lErrScore the error score if go left
	 * @param rErrScore the error score if go right
	 * @return the smalles error score among them
	 */
	private double minValues(double pErrScore, double lErrScore, double rErrScore){
		double minErrScore = Double.MAX_VALUE;
		if(pErrScore != Double.NaN && pErrScore < minErrScore){
			minErrScore = pErrScore;
		}
		
		if(lErrScore!= Double.NaN && lErrScore < minErrScore){
			minErrScore = lErrScore;
		}
		
		if(rErrScore != Double.NaN && rErrScore < minErrScore){
			minErrScore = rErrScore;
		}
		
		return minErrScore;
	}
	
	
	/**
	 * remove an edge 
	 * @param clonedGrph the target graph
	 * @a
	 */
	private TripleBaseSingleID getOfferedEdgeforRemoving(ColouredGraph clonedGrph){
		IntSet setOfEdges =	clonedGrph.getEdges();
		Random rand = new Random();
		setOfEdges = clonedGrph.getEdges();
		int[] arrEdges = setOfEdges.toIntArray();
		// randomly choose edge id to remove
		int edgeId = arrEdges[rand.nextInt(arrEdges.length)];
		BitSet edgeColour = clonedGrph.getEdgeColour(edgeId);
		
		//track the head and tail of the removed edge
		TripleBaseSingleID triple = new TripleBaseSingleID();
		triple.tailId = clonedGrph.getTailOfTheEdge(edgeId);
		triple.headId = clonedGrph.getHeadOfTheEdge(edgeId);
		triple.edgeId = edgeId;
		triple.edgeColour = edgeColour;
		
		return triple;
	}
	
    
	/**
	 * add an edge
	 * @param mimicGrph the target graph
	 */
	private TripleBaseSingleID getOfferedEdgeForAdding(ColouredGraph mimicGrph){
		return mGraphGenerator.getProposedTriple(mProcessRandomly);
	}

	public void printResult(double startingTime){
		BufferedWriter fWriter ;
		try{
			LOGGER.warn("Output results to file!");
			double currentTime =  System.currentTimeMillis();
			fWriter = new BufferedWriter( new FileWriter("LemmingEx.result", true));
			// number of input graphs
			fWriter.write("#----------------------------------------------------------------------#\n");
			fWriter.write("# Graph Generation: " + LocalDateTime.now().toString() +".\n");
			fWriter.write("# Total number of input graphs: " + mErrScoreCalculator.getNumberOfGraphs() +".\n");
			fWriter.write("# Generate a mimic graph of "+ mEdgeModifier.getGraph().getVertices().size()+" vertices and "+ mEdgeModifier.getGraph().getEdges().size()+" edges.\n");
			fWriter.write("# Duration: "+ (currentTime - startingTime)+" (ms).\n");
			fWriter.write("#----------------------------------------------------------------------#\n");
			
			// metric values of all graphs
			fWriter.write("\n");
			fWriter.write("- Metric Values\n");
			Map<String, Map<String, Double>> mapInputGraphMetricValues = mErrScoreCalculator.getMapMetricValuesOfInputGraphs();
			ObjectDoubleOpenHashMap<String> mOrigMetricValuesOfMimicGrpah = mEdgeModifier.getOriginalMetricValues();
			ObjectDoubleOpenHashMap<String> mOptimizedMetricValues = mEdgeModifier.getOptimizedMetricValues();
			Object[] arrMetricNames = mOrigMetricValuesOfMimicGrpah.keys;
			for(int i = 0 ; i < arrMetricNames.length ; i++){
				if(mOrigMetricValuesOfMimicGrpah.allocated[i]){
					String metricName = (String) arrMetricNames[i];
					fWriter.write("-- Metric: "+ metricName + ":\n");
					
					int idxGraph = 1;
					Set<String> setKeyGraphs = mapInputGraphMetricValues.keySet();
					for(String key: setKeyGraphs){
						Map<String, Double> mapInputGraphVal = mapInputGraphMetricValues.get(key);
						double inputGraphValue = mapInputGraphVal.containsKey(metricName)? mapInputGraphVal.get(metricName): Double.NaN;
						fWriter.write("\t Graph "+idxGraph+": "+ inputGraphValue + "\n");
						idxGraph ++;
					}
					
					double originalVal = mOrigMetricValuesOfMimicGrpah.get(metricName);
					fWriter.write("\t The first mimic graph: "+ originalVal + "\n");
					double optimizedVal = mOptimizedMetricValues.get(metricName);
					fWriter.write("\t The opimized mimic graph: "+ optimizedVal + "\n");
				}
			}
			fWriter.write("\n");
			fWriter.write("- Constant expressions\n");
			// constant expressions and their values for each graphs 
			Map<Expression, Map<String, Double>>mapConstantValues = mErrScoreCalculator.getMapConstantExpressions();
			Set<Expression> setExprs = mapConstantValues.keySet();
			for(Expression expr: setExprs){
				fWriter.write("-- Expression: "+ expr.toString() + ":\n");
				
				Map<String, Double> mapGraphAndConstantValues = mapConstantValues.get(expr);
				Set<String> setKeyGraphs = mapGraphAndConstantValues.keySet();
				int idxGraph = 1;
				for(String key: setKeyGraphs){
					double constVal = mapGraphAndConstantValues.get(key);
					fWriter.write("\t Graph "+idxGraph+": "+ constVal + "\n");
					idxGraph ++;	
				}
				
				double origConstantVal = expr.getValue(mOrigMetricValuesOfMimicGrpah);
				fWriter.write("\t The first mimic graph: "+ origConstantVal + "\n");
				double optimizedConstantVal = expr.getValue(mOptimizedMetricValues);
				fWriter.write("\t The opimized mimic graph: "+ optimizedConstantVal + "\n");
			}
			
			fWriter.write("\n");
			fWriter.write("- Error score of "+ mMaxIteration + " iteration\n");
			// list of of error score
			fWriter.write("\t [");
			for(int i = 0 ; i < mLstErrorScore.size() ; i++){
				if(i < mLstErrorScore.size() -1){
					fWriter.write(mLstErrorScore.get(i) + "; ");
				}else{
					fWriter.write(mLstErrorScore.get(i)+"");
				}
			}
			fWriter.write("]\n");
			
			
			fWriter.write("\n\n\n");
			fWriter.close();
			
		}catch(Exception ex){
			LOGGER.warn("Cannot output results to file! Please check: " + ex.getMessage());
		}
	}
}



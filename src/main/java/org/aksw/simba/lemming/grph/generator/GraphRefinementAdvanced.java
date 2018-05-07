package org.aksw.simba.lemming.grph.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.dist.utils.NormalDistribution;
import org.aksw.simba.lemming.rules.IColourMappingRules;
import org.aksw.simba.lemming.rules.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

public class GraphRefinementAdvanced {
	private static final Logger LOGGER = LoggerFactory.getLogger(GraphRefinementAdvanced.class);
	
	private int mMaxIteration = 1000 ;
	private boolean mProcessRandomly = true;
	private int mMaxRepeatedSelection = 5000;
	
	private IGraphGeneration mGraphGenerator;
	
	// list of removed edges associated with vertices
	private List<TripleBaseSingleID> mRemovedTriples;
	
	// list of added edges associated with vertices
	private List<TripleBaseSingleID> mAddedTriples;
	
	private NormalDistribution mErrScoreCalculator;
	
	public GraphRefinementAdvanced(ColouredGraph[] origGrphs, int iNoOfIteration, 
			IGraphGeneration graphGenerator, SortedSet<RefinementNode> constExprs) {
		
		/*
		 *  mErrScoreCalculator is used to compute the error score compared to original
		 *  constant values of the original graphs
		 */
		mErrScoreCalculator = new NormalDistribution(origGrphs, constExprs);
		
		// the graph generator
		mGraphGenerator = graphGenerator;
		
		// the maximum number of iteration for graph refinement
		mMaxIteration = iNoOfIteration;
		
		//set of removed triples
		mRemovedTriples = new ArrayList<TripleBaseSingleID>();
		
		// list of added triples
		mAddedTriples = new ArrayList<TripleBaseSingleID>();
	}
	
	
	public void setRefineGraphRandomly(boolean isRandom){
		mProcessRandomly = isRandom;
	}
	public ColouredGraph refineGraph(){
		ColouredGraph clonedGrph = mGraphGenerator.getMimicGraph().clone();
		int noOfRepeatedParent = 0;
		
		double lErrScore = Double.NaN; 
		double rErrScore = Double.NaN;
		double pErrScore = mErrScoreCalculator.computeErrorScore(clonedGrph); 
		
		for(int i = 0 ; i < mMaxIteration ; ++i){

			// go left
			removeEdges(clonedGrph);
			//System.out.println("[L]Aft -Number of edges: "+ parentGrph.getEdges().size());
			lErrScore = mErrScoreCalculator.computeErrorScore(clonedGrph);
			undoChangingGrph(clonedGrph, Constants.REMOVE_ACTION);
			
			 // go right
			addEdges(clonedGrph);
			//System.out.println("[R]Aft -Number of edges: "+ parentGrph.getEdges().size());
			rErrScore = mErrScoreCalculator.computeErrorScore(clonedGrph);
			undoChangingGrph(clonedGrph, Constants.ADD_ACTION);
			
			// find min error score
			double minErrScore = minValues(pErrScore, lErrScore, rErrScore);
			
			if(minErrScore == lErrScore){
				
				pErrScore = lErrScore;
				
				noOfRepeatedParent = 0;
				//System.out.println("[SL]Be4 -Number of edges: "+ parentGrph.getEdges().size());
				redoChangingGrph(clonedGrph, Constants.REMOVE_ACTION);
				//System.out.println("[SL]Redo -Number of edges: "+ parentGrph.getEdges().size());
				System.out.println("[GoLeft] Number of edges: "+ clonedGrph.getEdges().size());
				continue;
			}
			if(minErrScore == rErrScore){
				
				pErrScore = rErrScore;
				
				noOfRepeatedParent = 0;
				//System.out.println("[SR]Be4 -Number of edges: "+ parentGrph.getEdges().size());
				redoChangingGrph(clonedGrph, Constants.ADD_ACTION);
				//System.out.println("[SR]Redo -Number of edges: "+ parentGrph.getEdges().size());
				System.out.println("[GoRight] Number of edges: "+ clonedGrph.getEdges().size());
				continue;
			}
			
			System.out.println("[AtMid "+i+"] Mid: "+ pErrScore 
					+ " - Left: "+ lErrScore +" - Right: " + rErrScore);
			
			noOfRepeatedParent ++;
			
			if(noOfRepeatedParent == mMaxRepeatedSelection){
				LOGGER.info("Cannot find better refined graph! Break the loop!");
				break;
			}
		}
		
		return clonedGrph;
	}
	
	
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
	
	private void undoChangingGrph(ColouredGraph mimicGrph, int afterAction){
		// already remove an existing edge ==> add the edge again
		if(afterAction == Constants.REMOVE_ACTION){
			TripleBaseSingleID latestTriple = mRemovedTriples.get(mRemovedTriples.size()-1);
			BitSet edgeColour = latestTriple.edgeColour;
			int edgeId = mimicGrph.addEdge(latestTriple.tailId, latestTriple.headId, edgeColour);
			latestTriple.edgeId = edgeId;
		}else{
			// already add a new edge ==> remove the edge
			if(afterAction == Constants.ADD_ACTION){
				TripleBaseSingleID latestTripple = mAddedTriples.get(mAddedTriples.size() - 1);
				int edgeId = latestTripple.edgeId;
				// remove edge
				mimicGrph.removeEdge(edgeId);
			}
		}
	}
	
	private void redoChangingGrph(ColouredGraph mimicGrph, int beforeAction){
		// remove the same edge as previous removing action
		if(beforeAction == Constants.REMOVE_ACTION){
			TripleBaseSingleID latestTriple = mRemovedTriples.get(mRemovedTriples.size()-1);
			int edgeId = latestTriple.edgeId;
			mimicGrph.removeEdge(edgeId);			
		}else{
			// add the same edge again as previous adding action
			if(beforeAction == Constants.ADD_ACTION){
				TripleBaseSingleID latestTriple = mAddedTriples.get(mAddedTriples.size()-1);
				BitSet edgeColour = latestTriple.edgeColour;
				int edgeId = mimicGrph.addEdge(latestTriple.tailId, latestTriple.headId, edgeColour);
				latestTriple.edgeId = edgeId;	
			}
		}
	}
	
	private void removeEdges(ColouredGraph clonedGrph){
		IntSet setOfEdges =	clonedGrph.getEdges();
		Random rand = new Random();
		setOfEdges = clonedGrph.getEdges();
		int[] arrEdges = setOfEdges.toIntArray();
		// randomly choose edge id to remove
		int edgeId = arrEdges[rand.nextInt(arrEdges.length)];
		BitSet edgeColour = clonedGrph.getEdgeColour(edgeId);
		
		//track the head and tail of the removed edge
		TripleBaseSingleID tripple = new TripleBaseSingleID();
		tripple.tailId = clonedGrph.getTailOfTheEdge(edgeId);
		tripple.headId = clonedGrph.getHeadOfTheEdge(edgeId);
		tripple.edgeId = edgeId;
		tripple.edgeColour = edgeColour;
		//add the head and tail associated to the removed edge to the list
		mRemovedTriples.add(tripple);
		// remove edge
		clonedGrph.removeEdge(edgeId);
		//System.out.println("\t - reID: " + edgeId +"("+headID+","+tailID+")"+ " c: " + lGrph.getEdgeColour(edgeId));
	}
	
	private void addEdges(ColouredGraph mimicGrph){
		TripleBaseSingleID pair = mGraphGenerator.getProposedTriple(mProcessRandomly);
		int edgeId = mimicGrph.addEdge(pair.tailId, pair.headId, pair.edgeColour);
		// track head and tail of the edge
		pair.edgeId = edgeId;
		mAddedTriples.add(pair);
	}
}



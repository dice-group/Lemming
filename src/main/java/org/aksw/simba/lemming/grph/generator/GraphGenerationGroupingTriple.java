/**
 * Topic: Mimiced RDF Graph Generation
 * An extension of Lemming Project
 *
 * https://github.com/dice-group/Lemming
 */
package org.aksw.simba.lemming.grph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.multi.TripleColourDistributionMetric;
import org.aksw.simba.lemming.rules.TripleBaseSetOfIDs;
import org.apache.jena.ext.com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;

/**
 * Graph generation based on the group of triple's colours.
 * A triple's colour includes tail's colour, head's colour and the colour of edge connecting them
 * @author nptsy
 */
public class GraphGenerationGroupingTriple extends AbstractGraphGeneration
		implements IGraphGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationGroupingTriple.class);
	
	private Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> mTrippleMapOfTailHeadEdgeRates;
	private List<TripleColourDistributionMetric> mLstEVColorMapping;
	private int maxIterationFor1EdgeColo ;
	
	/*
	 * Constructor
	 */
	public GraphGenerationGroupingTriple(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		mTrippleMapOfTailHeadEdgeRates = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		mLstEVColorMapping = new ArrayList<TripleColourDistributionMetric>();
		maxIterationFor1EdgeColo = 1000;
		
		/*
		 *  compute edges and vertices distribution over each triple's colour
		 */
		computeEVColoDist(origGrphs);
		
		//compute average distribution of edges/ vertices of each triple's colour
		computeAverageEVColoDistribution();
		
		// assign specific number of edges to each grouped triple
		assignEdgesToGroupedTriple();
		
		//assign specific number of vertices to each grouped triple
		assignVerticesToGroupedTriple();
	}
	
	private void computeEVColoDist(ColouredGraph[] origGrphs){
		for(ColouredGraph grph: origGrphs){
			TripleColourDistributionMetric colorMapping = new TripleColourDistributionMetric();
			colorMapping.applyWithSingleThread(grph);
			mLstEVColorMapping.add(colorMapping);
			System.out.println("-------------");
			//testing
			colorMapping.printInfo();
		}
	}
	
	private void computeAverageEVColoDistribution(){
		Set<BitSet> vertColors = mMapColourToVertexIDs.keySet();
		Set<BitSet> edgeColors = mMapColourToEdgeIDs.keySet();
		
		for (BitSet tailColo : vertColors) {
			for (BitSet headColo : vertColors) {
				for (BitSet edgeColo : edgeColors) {

					int avrgDenominator = 0;
					double totalTailPercentage = 0;
					double totalEdgePercentage = 0;
					double totalHeadPercentage = 0;

					int iNoOfSamples = mLstEVColorMapping.size();
					for (int i = 0; i < iNoOfSamples; i++) {
						TripleColourDistributionMetric coloMapper = mLstEVColorMapping
								.get(i);

						double noOfHeads = coloMapper.getNoOfIncidentHeads(tailColo, edgeColo, headColo);
						double noOfTails = coloMapper.getNoOfIncidentTails(tailColo, edgeColo, headColo);
						double noOfEdges = coloMapper.getNoOfIncidentEdges(tailColo, edgeColo, headColo);

						if (noOfHeads != 0 && noOfTails != 0 && noOfEdges != 0) {
							avrgDenominator++;
							totalHeadPercentage += (noOfHeads / coloMapper.getTotalNoOfVerticesIn(headColo));
							totalTailPercentage += (noOfTails / coloMapper.getTotalNoOfVerticesIn(tailColo));
							totalEdgePercentage += (noOfEdges / coloMapper.getTotalNoOfEdgesIn(edgeColo));
						} else {
							// for testing only
							if ((noOfHeads + noOfTails + noOfHeads) != 0) {
								System.err
										.println("What wrong the result is!!!");
							}
						}
					}

//					if(avrgDenominator == 3){
//						System.err
//						.println("What wrong the result is 3 !!!");
//					}
//					
//					if(avrgDenominator == 2){
//						System.err
//						.println("What wrong the result is 2 !!!");
//					}
					
					if (avrgDenominator != 0) {
						avrgDenominator = mLstEVColorMapping.size();
						
						totalTailPercentage = totalTailPercentage
								/ avrgDenominator;
						totalEdgePercentage = totalEdgePercentage
								/ avrgDenominator;
						totalHeadPercentage = totalHeadPercentage
								/ avrgDenominator;

						TripleBaseSetOfIDs trippleForEdge = new TripleBaseSetOfIDs(
								tailColo, totalTailPercentage, edgeColo,
								totalEdgePercentage, headColo,
								totalHeadPercentage);
						putToMap(tailColo, headColo, edgeColo, trippleForEdge,
								mTrippleMapOfTailHeadEdgeRates);
					}
				}
			}
		}
	}
	
	private void putToMap(BitSet firstKey, BitSet secondKey, BitSet thirdKey, TripleBaseSetOfIDs val, 
			Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> changedMap){
		if(changedMap == null){
			changedMap = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		}
		
		Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapSecondThird = changedMap.get(firstKey);
		
		if(mapSecondThird == null){
			mapSecondThird = new HashMap<BitSet, Map<BitSet, TripleBaseSetOfIDs>>();
			changedMap.put(firstKey, mapSecondThird);
		}
		
		Map<BitSet, TripleBaseSetOfIDs> mapThird = mapSecondThird.get(secondKey);
		
		if(mapThird == null){
			mapThird = new HashMap<BitSet, TripleBaseSetOfIDs>();
			mapSecondThird.put(secondKey, mapThird);
		}

		if(!mapThird.containsKey(thirdKey)){
			mapThird.put(thirdKey, val);
		}else{
			LOGGER.error("[putToMap] Something is wrong!");
			System.err.println("[putToMap] Something is wrong!");
		}
	}
	
	private void assignEdgesToGroupedTriple() {
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		
		for (BitSet edgeColo : setEdgeColo) {

			List<TripleBaseSetOfIDs> lstGrpTriples = new ArrayList<TripleBaseSetOfIDs>();
			List<Double> lstEdgeRatePerGrpTriple = new ArrayList<Double>();
			for (BitSet tailColo : setVertColo) {
				if (mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)) {
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates
							.get(tailColo);
					for (BitSet headColo : setVertColo) {
						if (mapHeadEdgeToGrpTriples.containsKey(headColo)) {
							Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples
									.get(headColo);
							
							if(mapEdgeToGrpTriples.containsKey(edgeColo)){
								TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
								
								if(triple != null){
									lstGrpTriples.add(triple);
									lstEdgeRatePerGrpTriple.add(triple.noOfEdges);
								}
							}
						}
					}
				}
			}
			
			// assign specific number of edges in "edgeColo" to each of grouped triples
			if(lstGrpTriples.size() > 0){
				TripleBaseSetOfIDs[] arrGrpTriples = lstGrpTriples.toArray(new TripleBaseSetOfIDs[0]);
				double[] arrEdgeRatePerTriple = Doubles.toArray(lstEdgeRatePerGrpTriple);
				
				ObjectDistribution<TripleBaseSetOfIDs> objDist = new ObjectDistribution<TripleBaseSetOfIDs>(arrGrpTriples, arrEdgeRatePerTriple);
				
				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(objDist);
				
				IntSet setEdges = mMapColourToEdgeIDs.get(edgeColo);
				
				if(setEdges!=null && setEdges.size() >0){
					
					int[] arrEdges = setEdges.toIntArray();
					for (int i = 0 ; i < arrEdges.length ; i++){
						TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
						offeredGrpTriple.edgeIDs.add(arrEdges[i]);
					}
				}
			}
		}
		
		System.out.println("Done assing edges to grouped triples");
	}
	
	private void assignVerticesToGroupedTriple(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		
		for(BitSet tailColo : setVertColo){
			IntSet setTails = mMapColourToVertexIDs.get(tailColo);
			int[] arrTails = setTails.toIntArray();
			
			for(BitSet headColo: setVertColo){
				IntSet setHeads = mMapColourToVertexIDs.get(tailColo);
				int[]arrHeads = setHeads.toIntArray();
				
				for(BitSet edgeColo: setEdgeColo){
					// tail distribution
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
					if(mapHeadEdgeToGrpTriples != null){
						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
						if(mapEdgeToGrpTriples!= null){
							TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
							if(triple != null){
								if(triple.edgeIDs.size() > 0){
									
									Random rand = new Random();
									
									/// tails
									double noOfTails = Math.round(triple.noOfTails * setTails.size() + 0.5);
									if(noOfTails > triple.edgeIDs.size()){
										noOfTails  = triple.edgeIDs.size();
									}
									
									if(noOfTails == 0)
										noOfTails = 1;
									
									triple.noOfTails = noOfTails;
									
//									if(noOfTails >= arrTails.length){
//										for(int i = 0 ; i < arrTails.length; i++){
//											int randTailId = arrTails[i];
//											triple.tailIDs.add(randTailId);
//										}
//									}else{
//										for(int i = 0 ; i < noOfTails; i++){
//											int randTailId = arrTails[rand.nextInt(arrTails.length)];
//											if(!triple.tailIDs.contains(randTailId)){
//												triple.tailIDs.add(randTailId);
//											}else{
//												i--;
//											}
//										}
//									}
									
									/// heads
									double noOfHeads = Math.round(triple.noOfHeads * setHeads.size() + 0.5);
									
									if(noOfHeads > triple.edgeIDs.size()){
										noOfHeads  = triple.edgeIDs.size();
									}
									
									if(noOfHeads == 0)
										noOfHeads = 1;
									
									triple.noOfHeads = noOfHeads;
									
//									if(noOfHeads >= arrHeads.length){
//										for(int i = 0 ; i < arrHeads.length; i++){
//											int randHeadId = arrTails[i];
//											triple.headIDs.add(randHeadId);
//										}
//									}else{
//										for(int i = 0 ; i < noOfHeads; i++){
//											int randHeadId = arrHeads[rand.nextInt(arrHeads.length)];
//											if(!triple.headIDs.contains(randHeadId))
//												triple.headIDs.add(randHeadId);
//											else
//												i--;
//										}
//									}
								}
							}
						}
					}
				}
			}
		}
	}

	
	public ColouredGraph generateGraph(){
		
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		
		for(BitSet tailColo: setVertColo){
			for(BitSet headColo : setVertColo){
				for(BitSet edgeColo: setEdgeColo){
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
					if(mapHeadEdgeToGrpTriples != null){
						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
						if(mapEdgeToGrpTriples!= null){
							TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
							if(triple != null){
								if(triple.noOfEdges != 0){
									double noOfEdges = triple.edgeIDs.size();
//									int[] arrHeadIDs = triple.headIDs.toIntArray();
//									int[] arrTailIDs = triple.tailIDs.toIntArray();
									
									int[] arrHeadIDs = getRandomVertices(triple.headColour, triple.noOfHeads);
									int[] arrTailIDs = getRandomVertices(triple.tailColour, triple.noOfTails);
									triple.headIDs.addAll(arrHeadIDs);
									triple.tailIDs.addAll(arrTailIDs);
									
									
									IntSet connectedHeadIds = new DefaultIntSet();
									IntSet connectedTailIds = new DefaultIntSet();
									
									Random rand = new Random();
									for(int i = 0 ; i < noOfEdges ; i++){
										int headId = arrHeadIDs[rand.nextInt(arrHeadIDs.length)];
										int tailId = arrTailIDs[rand.nextInt(arrTailIDs.length)];
										
										if(connectedHeadIds.size() < arrHeadIDs.length){
											while(connectedHeadIds.contains(headId)){
												headId = arrHeadIDs[rand.nextInt(arrHeadIDs.length)];
											}
											
											connectedHeadIds.add(headId);
										}else{
											headId = arrHeadIDs[rand.nextInt(arrHeadIDs.length)];
										}
										
										if(connectedTailIds.size() < arrTailIDs.length){
											while(connectedTailIds.contains(tailId)){
												tailId = arrTailIDs[rand.nextInt(arrTailIDs.length)];
											}
											
											connectedTailIds.add(tailId);
										}else{
											tailId = arrTailIDs[rand.nextInt(arrTailIDs.length)];
										}
										
										mMimicGraph.addEdge(tailId, headId, edgeColo);
									}	
								}
							}
						}
					}
				}
			}
		}
		//printInfo();
		
		return mMimicGraph;
	}
	
	private int[] getRandomVertices(BitSet vertColo, double iNoOfVertices){
		IntSet setVertices = mMapColourToVertexIDs.get(vertColo);
		if(setVertices != null){
			Random rand = new Random();
			int[] arrVertices = setVertices.toIntArray();
			
			IntSet res = new DefaultIntSet();
			
			if(iNoOfVertices >= arrVertices.length){
				iNoOfVertices = arrVertices.length;
				return arrVertices;
			}
			
			while(iNoOfVertices > 0){
				int vertId = arrVertices[rand.nextInt(arrVertices.length)];
				if(!res.contains(vertId)){
					res.add(vertId);
					iNoOfVertices --;
				}
			}
			
			return res.toIntArray();
		}
		return null;
	}
	
	public void printInfo(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		
		
		double totalEdges = 0;
		
		for(BitSet edgeColo: setEdgeColo){
			System.err.println("Edge colour: " + edgeColo );
			double dRate = 0 ;
			double iNoOfEdges = 0 ;
			for(BitSet tailColo: setVertColo){
				if(mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)){
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
					for(BitSet headColo : setVertColo){
						if(mapHeadEdgeToGrpTriples.containsKey(headColo)){
							Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
							
							if(mapEdgeToGrpTriples.containsKey(edgeColo)){
								TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
								
								dRate += triple.noOfEdges;
								iNoOfEdges += triple.edgeIDs.size();
								
								System.out.println("\t");
								System.out.println("\t Tails: " + triple.tailColour + " - metric: " + triple.noOfTails + " - no of tails : " + triple.tailIDs.size());
								System.out.println("\t Heads: " + triple.headColour + " - metric: " + triple.noOfHeads + " - no of heads : " + triple.headIDs.size());
								System.out.println("\t Edges: " + triple.edgeColour + " - metric: " + triple.noOfEdges + " - no of edges : " + triple.edgeIDs.size());
							}
						}
					}
				}
			}
			totalEdges += iNoOfEdges;
			System.out.println("--------");
			System.out.println("Number of assigned edges in " + edgeColo + " is " + iNoOfEdges + "( real no of edges: "+mMapColourToEdgeIDs.get(edgeColo).size()+" )");
		}
		
		System.out.println("--------------------------------------" );
		System.out.println("Total edges is " + totalEdges);
		
	}
	
//	private void assignVerticesToGroupedTriple(){
//		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
//		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
//		
//		
//		// process for tail
//		for (BitSet tailColo : setVertColo) {
//			List<TripleBaseSetOfIDs> lstGrpTriples = new ArrayList<TripleBaseSetOfIDs>();
//			List<Double> lstTailRatePerGrpTriple = new ArrayList<Double>();
//			
//			if (mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)) {
//				Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates
//						.get(tailColo);
//				for (BitSet headColo : setVertColo) {
//					if (mapHeadEdgeToGrpTriples.containsKey(headColo)) {
//						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples
//								.get(headColo);
//						for (BitSet edgeColo : setEdgeColo) {
//							if(mapEdgeToGrpTriples.containsKey(edgeColo)){
//								TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
//								
//								if(triple != null && triple.edgeIDs.size() > 0){
//									lstGrpTriples.add(triple);
//									lstTailRatePerGrpTriple.add(triple.noOfEdges);
//								}
//							}
//						}
//					}
//				}
//			}
//			
//			
//			// assign specific number of vertices in "tailColo" to each of grouped triples
//			if(lstGrpTriples.size() > 0){
//				TripleBaseSetOfIDs[] arrGrpTriples = lstGrpTriples.toArray(new TripleBaseSetOfIDs[0]);
//				double[] arrEdgeRatePerTriple = Doubles.toArray(lstTailRatePerGrpTriple);
//				
//				ObjectDistribution<TripleBaseSetOfIDs> objDist = new ObjectDistribution<TripleBaseSetOfIDs>(arrGrpTriples, arrEdgeRatePerTriple);
//				
//				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(objDist);
//				
//				IntSet setVertices = mMapColourToVertexIDs.get(tailColo);
//				
//				if(setVertices!=null && setVertices.size() >0){
//					
//					int[] arrVertices = setVertices.toIntArray();
//					for (int i = 0 ; i < arrVertices.length ; i++){
//						TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
//						offeredGrpTriple.tailIDs.add(arrVertices[i]);
//					}
//				}
//			}
//			
//		}
//		
//		
//		// process for head
//		
//	}
	
}

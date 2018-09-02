package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgInDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgOutDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.TripleColourDistributionMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSetOfIDs;
import org.aksw.simba.lemming.util.Constants;
import org.apache.jena.ext.com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class GraphGenerationClusteringBased2 extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationClusteringBased2.class);
	
	private Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> mTrippleMapOfTailHeadEdgeRates;
	private List<TripleColourDistributionMetric> mLstEVColorMapping;
	private int maxIterationFor1EdgeColo ;
	
	/*
	 * the key1: the out-edge's colors, the key2: the vertex's colors and the value is the map of potential degree 
	 * to each vertex's id
	 */
	protected ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleODegreePerOEColo;

	/*
	 * the key1: the in-edge's colors, the key2: the vertex's colors and the value is the map of potential degree 
	 * to each vertex's id
	 */
	protected ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>> mapPossibleIDegreePerIEColo;
	
	public GraphGenerationClusteringBased2(int iNumberOfVertices, ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		mTrippleMapOfTailHeadEdgeRates = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		mLstEVColorMapping = new ArrayList<TripleColourDistributionMetric>();
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		
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
		
		//compute potential degree for each of vertices
		computePotentialIODegreePerVert(origGrphs);
	}

public ColouredGraph generateGraph(){
		
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		
		for(BitSet tailColo: setVertColo){
			for(BitSet headColo : setVertColo){
				Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
				if(mapHeadEdgeToGrpTriples != null){
					Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
					
					if(mapEdgeToGrpTriples!= null){
						for(BitSet edgeColo: setEdgeColo){
							TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
							if(triple != null){
								double noOfEdges = triple.edgeIDs.size();
								if(noOfEdges != 0){
	//									int[] arrHeadIDs = triple.headIDs.toIntArray();
	//									int[] arrTailIDs = triple.tailIDs.toIntArray();
									ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadIdProposers = mapPossibleIDegreePerIEColo.get(edgeColo);
									ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailIdProposers = mapPossibleODegreePerOEColo.get(edgeColo);
									
									IntSet setOfRandomHeadIds = getRandomVertices(triple.headColour, triple.noOfHeads, mapHeadIdProposers);
									IntSet setOfRandomTailIds = getRandomVertices(triple.tailColour, triple.noOfTails, mapTailIdProposers);
									
									IOfferedItem<Integer> headIdsProposer = mapHeadIdProposers.get(triple.headColour);
									IOfferedItem<Integer> tailIdsProposer = mapTailIdProposers.get(triple.tailColour);
									
									if(setOfRandomHeadIds == null || setOfRandomTailIds == null){
										System.err.println("There exists no vertices in " + triple.headColour +" or " + triple.tailColour +" colour");
										continue;
									}
									
									triple.headIDs.addAll(setOfRandomHeadIds.toIntArray());
									triple.tailIDs.addAll(setOfRandomTailIds.toIntArray());
									
									Set<Integer> setTailIds = new HashSet<Integer>();
									setTailIds.addAll(setOfRandomTailIds.toIntegerArrayList());
									Set<Integer> setHeadIds = new HashSet<Integer>();
									setHeadIds.addAll(setOfRandomHeadIds.toIntegerArrayList());
									
									/*
									 *  standardize the amount of edges and vertices
									 *  this makes sure there is no pair of vertices are connected by 
									 *  2 edges in same colour 
									 */
									if(setHeadIds.size() * setTailIds.size() < noOfEdges){
										LOGGER.warn("Not generate " + (noOfEdges - (setHeadIds.size() * setTailIds.size())) + " edges in "+ edgeColo );
										noOfEdges = setHeadIds.size() * setTailIds.size();
									}
									
									Map<Integer, IntSet> mapConnected = new HashMap<Integer, IntSet>();
									
									int headId = -1;
									int i = 0 ; 
									while(i < noOfEdges){
										// select a random tail
										int tailId = tailIdsProposer.getPotentialItem(setTailIds);
										
										setOfRandomHeadIds = triple.headIDs.clone();
										
										// get list of already connected head
										IntSet setConnectedHeads = mapConnected.get(tailId);
										if(setConnectedHeads == null){
											setConnectedHeads = new DefaultIntSet();
											mapConnected.put(tailId, setConnectedHeads);
										}else{
											setOfRandomHeadIds.removeAll(setConnectedHeads);
										}
										
										setHeadIds.clear();
										setHeadIds.addAll(setOfRandomHeadIds.toIntegerArrayList());
										
										if(setHeadIds.size() == 0){
											setTailIds.remove(tailId);
											LOGGER.warn("No head for connection any more! Process another tail ids");
											continue;
										}
										
										headId = headIdsProposer.getPotentialItem(setHeadIds);
										
										
										if(connectableVertices(tailId, headId, triple.edgeColour)){
											mMimicGraph.addEdge(tailId, headId, edgeColo);
											setConnectedHeads.add(headId);
											i++;
										}
									}// end while	
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
	
	private IntSet getRandomVertices(BitSet vertColo, double iNoOfVertices, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapVertexIdsProposers){
		IntSet setVertices = mMapColourToVertexIDs.get(vertColo);
		IOfferedItem<Integer> vertexIdsProposer= mapVertexIdsProposers.get(vertColo);
		if(setVertices != null && vertexIdsProposer!= null){
			
			int[] arrVertices= setVertices.toIntArray();
			
			IntSet res = new DefaultIntSet();
			
			if(iNoOfVertices >= arrVertices.length){
				iNoOfVertices = arrVertices.length;
				return setVertices;
			}
			
			while(iNoOfVertices > 0){
				int vertId = vertexIdsProposer.getPotentialItem();
				if(!res.contains(vertId)){
					res.add(vertId);
					iNoOfVertices --;
				}
			}
			
			return res;
		}
		return null;
	}
	
	private void computeEVColoDist(ColouredGraph[] origGrphs){
		for(ColouredGraph grph: origGrphs){
			TripleColourDistributionMetric colorMapping = new TripleColourDistributionMetric();
			colorMapping.applyWithSingleThread(grph);
			mLstEVColorMapping.add(colorMapping);
			//System.out.println("-------------");
			//testing
			//colorMapping.printInfo();
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
						TripleColourDistributionMetric tripleColourMapper = mLstEVColorMapping
								.get(i);

						double noOfHeads = tripleColourMapper.getNoOfIncidentHeads(tailColo, edgeColo, headColo);
						double noOfTails = tripleColourMapper.getNoOfIncidentTails(tailColo, edgeColo, headColo);
						double noOfEdges = tripleColourMapper.getNoOfIncidentEdges(tailColo, edgeColo, headColo);

						if (noOfHeads != 0 && noOfTails != 0 && noOfEdges != 0) {
							avrgDenominator++;
							totalHeadPercentage += (noOfHeads / tripleColourMapper.getTotalNoOfVerticesIn(headColo));
							totalTailPercentage += (noOfTails / tripleColourMapper.getTotalNoOfVerticesIn(tailColo));
							totalEdgePercentage += (noOfEdges / tripleColourMapper.getTotalNoOfEdgesIn(edgeColo));
						} else {
							// for testing only
							if ((noOfHeads + noOfTails + noOfHeads) != 0) {
								System.err
										.println("What wrong the result is!!!");
							}
						}
					}

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
			
			for(BitSet headColo: setVertColo){
				IntSet setHeads = mMapColourToVertexIDs.get(tailColo);
				
				for(BitSet edgeColo: setEdgeColo){
					// tail distribution
					Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
					if(mapHeadEdgeToGrpTriples != null){
						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
						if(mapEdgeToGrpTriples!= null){
							TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
							if(triple != null){
								if(triple.edgeIDs.size() > 0){
									
									/// tails
									double noOfTails = Math.round(triple.noOfTails * setTails.size() + 0.5);
									if(noOfTails > triple.edgeIDs.size()){
										noOfTails  = triple.edgeIDs.size();
									}
									
									if(noOfTails == 0)
										noOfTails = 1;
									
									triple.noOfTails = noOfTails;
									
									/// heads
									double noOfHeads = Math.round(triple.noOfHeads * setHeads.size() + 0.5);
									
									if(noOfHeads > triple.edgeIDs.size()){
										noOfHeads  = triple.edgeIDs.size();
									}
									
									if(noOfHeads == 0)
										noOfHeads = 1;
									
									triple.noOfHeads = noOfHeads;
									
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void computePotentialIODegreePerVert(ColouredGraph[] origGrphs){
		// compute for each vertex's colour, the average out-degree associated with a specific edge's colour
		AvrgInDegreeDistBaseVEColo avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		// compute for each vertex's colour, the average in-degree associated with a specific edge's colour
		AvrgOutDegreeDistBaseVEColo avrgOutDegreeAnalyzer = new AvrgOutDegreeDistBaseVEColo(origGrphs);
		
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		
		for(BitSet edgeColo : setEdgeColours){
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			
			for(BitSet tailColo : setTailColours){
				if(setVertexColours.contains(tailColo)){
					double avrgOutDegree = avrgOutDegreeAnalyzer.getAvarageOutDegreeOf(tailColo, edgeColo);
					
					// get list tailIDs 
					int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
					double[] possOutDegreePerTailIDs = new double[arrTailIDs.length];
					Integer[] objTailIDs = new Integer[arrTailIDs.length];
					// for each tail id, we compute the potential out degree for it
					Random random = new Random();
					
					for(int i = 0 ; i < arrTailIDs.length ; i++){
						objTailIDs[i] = arrTailIDs[i];
						// generate a random out degree for each vertex in its set based on the computed average out-degree
						int possDeg = PoissonDistribution.randomXJunhao(avrgOutDegree, random);
						if(possDeg == 0)
							possDeg = 1;
						
						possOutDegreePerTailIDs[i] = (double)possDeg;
					}
					
					ObjectDistribution<Integer> potentialOutDegree = new ObjectDistribution<Integer>(objTailIDs, possOutDegreePerTailIDs);
					OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialOutDegree, random);
					
					// put to map potential degree proposer
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapPossODegree = mapPossibleODegreePerOEColo.get(edgeColo);
					if(mapPossODegree == null){
						mapPossODegree = new ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> ();
						mapPossibleODegreePerOEColo.put(edgeColo, mapPossODegree);
					}
					
					IOfferedItem<Integer> outDegreeProposer = mapPossODegree.get(tailColo);
					if(outDegreeProposer == null){
						mapPossODegree.put(tailColo, potentialDegreeProposer);
					}else{
						LOGGER.error("Something is seriously happening");
					}
				}
			}
			
			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);
			for(BitSet headColo : setHeadColours){
				if(setVertexColours.contains(headColo)){
					double avrgInDegree = avrgInDegreeAnalyzer.getAvarageInDegreeOf(edgeColo, headColo);
					
					int [] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
					
					double[] possOutDegreePerHeadDs = new double[arrHeadIDs.length];
					Integer[] objHeadIDs = new Integer[arrHeadIDs.length];
					Random random = new Random();
					
					// for each head id, we compute the potential in degree for it
					for(int i = 0; i < arrHeadIDs.length ; i++){
						objHeadIDs[i] = arrHeadIDs[i];
						// generate a random in degree for each vertex in its set based on the computed average in-degree
						int possDeg = PoissonDistribution.randomXJunhao(avrgInDegree, random);
						if(possDeg == 0)
							possDeg = 1;
						possOutDegreePerHeadDs[i] = (double)possDeg;
					}
					
					ObjectDistribution<Integer> potentialInDegree = new ObjectDistribution<Integer>(objHeadIDs, possOutDegreePerHeadDs);
					OfferedItemByRandomProb<Integer> potentialDegreeProposer = new OfferedItemByRandomProb<Integer>(potentialInDegree, random);
					
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapPossIDegree = mapPossibleIDegreePerIEColo.get(edgeColo);
					if(mapPossIDegree == null){
						mapPossIDegree = new ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> ();
						mapPossibleIDegreePerIEColo.put(edgeColo, mapPossIDegree);
					}
					
					IOfferedItem<Integer> inDegreeProposer = mapPossIDegree.get(headColo);
					if(inDegreeProposer == null){
						mapPossIDegree.put(headColo, potentialDegreeProposer);
					}else{
						LOGGER.error("Something is seriously happening");
					}
				}
			}
		}
	}	
	
}

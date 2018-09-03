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
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
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
		
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		
		//compute potential degree for each of vertices
		computePotentialIODegreePerVert(origGrphs);
		
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

	public ColouredGraph generateGraph(){
		
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		
		for(BitSet tailColo: setVertColo){
			
			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
			if(mapHeadEdgeToGrpTriples == null)
				continue;
			
			for(BitSet headColo : setVertColo){
				
				if(mapHeadEdgeToGrpTriples.containsKey(headColo)){
					
					Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
					
					if(mapEdgeToGrpTriples == null) 
						continue;
					
					Set<BitSet> setEdgeColours = mapEdgeToGrpTriples.keySet();
					
					for(BitSet edgeColo: setEdgeColours){
						TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
						
						if(triple != null && triple.edgeIDs.size() > 0){
							double noOfEdges = triple.edgeIDs.size();

							ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailIdProposers = mapPossibleODegreePerOEColo.get(edgeColo);
							ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadIdProposers = mapPossibleIDegreePerIEColo.get(edgeColo);
							
							IntSet setOfRandomTailIds = getRandomVertices(triple.tailColour, triple.noOfTails, mapTailIdProposers);
							IntSet setOfRandomHeadIds = getRandomVertices(triple.headColour, triple.noOfHeads, mapHeadIdProposers);
							
							if((setOfRandomHeadIds == null || setOfRandomHeadIds.size() == 0 ) || 
									(setOfRandomTailIds == null || setOfRandomTailIds.size() == 0)){
								LOGGER.error("There exists no vertices in "  + triple.tailColour +" or " + triple.headColour +" colour");
								continue;
							}
							
							IOfferedItem<Integer> tailIdsProposer = mapTailIdProposers.get(triple.tailColour);
							IOfferedItem<Integer> headIdsProposer = mapHeadIdProposers.get(triple.headColour);
							
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
							
							int headId = -1;
							int i = 0 ; 
							while(i < noOfEdges){
								
								setOfRandomHeadIds = triple.headIDs.clone();

								// select a random tail
								int tailId = tailIdsProposer.getPotentialItem(setTailIds);
								
								int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray(); 
								for(int connectedHead: arrConnectedHeads){
									if(setOfRandomHeadIds.contains(connectedHead))
										setOfRandomHeadIds.remove(connectedHead);
								}
								
								if(setOfRandomHeadIds.size() == 0 ){
									LOGGER.warn("No heads any more! Consider another tail colour");
									continue;
								}
								Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setOfRandomHeadIds.toIntegerArrayList());
								
								headId = headIdsProposer.getPotentialItem(setFilteredHeadIDs);
								
								if(connectableVertices(tailId, headId, triple.edgeColour)){
									mMimicGraph.addEdge(tailId, headId, edgeColo);
									i++;
								}
							}// end while	
						}
					}
				}
			}
		}
		//printInfo();
		
		return mMimicGraph;
	}
	
	private IntSet getRandomVertices(BitSet vertColo, double iNoOfVertices, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapVertexIdsProposers){
		IntSet setVertices = mMapColourToVertexIDs.get(vertColo).clone();
		IOfferedItem<Integer> vertexIdsProposer= mapVertexIdsProposers.get(vertColo);
		if(setVertices != null && vertexIdsProposer!= null){
			
			int[] arrVertices= setVertices.toIntArray();
			
			IntSet res = new DefaultIntSet();
			
			if(iNoOfVertices >= arrVertices.length){
				iNoOfVertices = arrVertices.length;
				return setVertices;
			}
			
			int iCounter = 0 ;
			while(iNoOfVertices > 0){
				int vertId = vertexIdsProposer.getPotentialItem();
				if(!res.contains(vertId) && !mReversedMapClassVertices.containsKey(vertId)){
					res.add(vertId);
					iNoOfVertices --;
					setVertices.remove(vertId);
				}
				boolean havingMore = false;
				int[] arrAvailableVertices = setVertices.toIntArray();
				for(int availableId : arrAvailableVertices){
					if(!mReversedMapClassVertices.containsKey(availableId)){
						havingMore = true;
						break;
					}
				}
				
				if(!havingMore){
					LOGGER.warn("Could not get " + iNoOfVertices + " vertices of "+ vertColo);
					break;
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
								LOGGER.error("Found a triple missing of either tail, head or edges!");
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
					
					Set<BitSet> setHeadColours = mapHeadEdgeToGrpTriples.keySet();
					
					for (BitSet headColo : setHeadColours) {
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
		
		LOGGER.info("Done assing edges to grouped triples");
	}
	
	private void assignVerticesToGroupedTriple(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColo = mMapColourToEdgeIDs.keySet();
		
		for(BitSet tailColo : setVertColo){
			//get all tails
			IntSet setTails = mMapColourToVertexIDs.get(tailColo);
			// tail distribution
			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
			
			if(mapHeadEdgeToGrpTriples == null)
				continue;
			
			for(BitSet headColo: setVertColo){
				
				if(mapHeadEdgeToGrpTriples.containsKey(headColo)){
					
					// get all heads
					IntSet setHeads = mMapColourToVertexIDs.get(headColo);
					
					for(BitSet edgeColo: setEdgeColo){
						Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
						TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
						if(triple != null && triple.edgeIDs.size() > 0){
								
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
							
							triple.noOfEdges = triple.edgeIDs.size();
						}
					}
				}
			}
		}
	}
	
	private void computePotentialIODegreePerVert(ColouredGraph[] origGrphs){
		
		// compute for each vertex's colour, the average in-degree associated with a specific edge's colour
		AvrgOutDegreeDistBaseVEColo avrgOutDegreeAnalyzer = new AvrgOutDegreeDistBaseVEColo(origGrphs);
		
		// compute for each vertex's colour, the average out-degree associated with a specific edge's colour
		AvrgInDegreeDistBaseVEColo avrgInDegreeAnalyzer = new AvrgInDegreeDistBaseVEColo(origGrphs);
		
		
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		
		for(BitSet edgeColo : setEdgeColours){
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			
			//get only tail colours existing in the setVertexColours
			setTailColours.retainAll(setVertexColours);
			
			for(BitSet tailColo : setTailColours){
				
				Random random = new Random();
				
				double avrgOutDegree = avrgOutDegreeAnalyzer.getAvarageOutDegreeOf(tailColo, edgeColo);
				
				// get list tailIDs 
				int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
				double[] possOutDegreePerTailIDs = new double[arrTailIDs.length];
				Integer[] objTailIDs = new Integer[arrTailIDs.length];
				// for each tail id, we compute the potential out degree for it
				
				
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
			
			Set<BitSet> setHeadColours = mColourMapper.getHeadColoursFromEdgeColour(edgeColo);
			
			//get only tail colours existing in the setVertexColours
			setHeadColours.retainAll(setVertexColours);

			for(BitSet headColo : setHeadColours){
				
				Random random = new Random();
				
				double avrgInDegree = avrgInDegreeAnalyzer.getAvarageInDegreeOf(edgeColo, headColo);
				
				int [] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
				
				double[] possOutDegreePerHeadDs = new double[arrHeadIDs.length];
				Integer[] objHeadIDs = new Integer[arrHeadIDs.length];
				
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
	

	@Override
	public TripleBaseSingleID getProposedTriple(boolean isRandom){

		if(!isRandom){
			LOGGER.info("Using the override function getProposedTriple");
			// build a proposer of all cluster of triples
			
			Set<BitSet> setTailColours = mTrippleMapOfTailHeadEdgeRates.keySet();
			
			List<TripleBaseSetOfIDs> lstTripleGroups = new ArrayList<TripleBaseSetOfIDs>();
			List<Double> lstGapRequiredEdges = new ArrayList<Double>();
			for(BitSet tColo: setTailColours){
				Set<BitSet> setHeadColours = mTrippleMapOfTailHeadEdgeRates.get(tColo).keySet();
				for(BitSet hColo: setHeadColours){
					Set<BitSet> setEdgeColo = mTrippleMapOfTailHeadEdgeRates.get(tColo).get(hColo).keySet();
					for(BitSet eColo: setEdgeColo){
						TripleBaseSetOfIDs tripleGroup = mTrippleMapOfTailHeadEdgeRates.get(tColo).get(hColo).get(eColo);
						
						if(tripleGroup.edgeIDs.size() == 0 )
							continue;
						double gap = (tripleGroup.noOfHeads * tripleGroup.noOfTails) - tripleGroup.noOfEdges;
						
						
						
						if(gap > 0 ){
							lstTripleGroups.add(tripleGroup);
							lstGapRequiredEdges.add(gap);
							
							if(1> gap ){
								System.err.println("Break here");
							}
						}
					}
				}
			}
			
			if(lstTripleGroups.size() > 0 ){

				TripleBaseSetOfIDs[] arrGrpTriples = lstTripleGroups.toArray(new TripleBaseSetOfIDs[0]);
				double[] arrEdgeRatePerTriple = Doubles.toArray(lstGapRequiredEdges);
				
				ObjectDistribution<TripleBaseSetOfIDs> objDist = new ObjectDistribution<TripleBaseSetOfIDs>(arrGrpTriples, arrEdgeRatePerTriple);
				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(objDist);
				
				while(maxIterationFor1EdgeColo > 0 ){
					TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
					
					//get random a head and a tail to connect
					
					double gap = (offeredGrpTriple.noOfHeads * offeredGrpTriple.noOfTails) - offeredGrpTriple.noOfEdges;
					if(gap > 0 ){
						
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailIdProposers = 
														mapPossibleODegreePerOEColo.get(offeredGrpTriple.edgeColour);
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadIdProposers =
														mapPossibleIDegreePerIEColo.get(offeredGrpTriple.edgeColour);

						if(mapTailIdProposers == null || mapHeadIdProposers == null){
							continue;
						}
						
						
						IOfferedItem<Integer> tailIdsProposer = mapTailIdProposers.get(offeredGrpTriple.tailColour);
						IOfferedItem<Integer> headIdsProposer = mapHeadIdProposers.get(offeredGrpTriple.headColour);
						
						if(tailIdsProposer == null || headIdsProposer == null){
							continue;
						}
						
						Set<Integer> setTmpTails = new HashSet<Integer>(offeredGrpTriple.tailIDs.clone().toIntegerArrayList());
						int tId = tailIdsProposer.getPotentialItem(setTmpTails);
						
						int[] arrConnectedHeads = getConnectedHeads(tId, offeredGrpTriple.edgeColour).toIntArray();
						
						IntSet setAvailableHeads = offeredGrpTriple.headIDs.clone();
						
						for(int connectedHead : arrConnectedHeads){
							if(setAvailableHeads.contains(connectedHead)){
								setAvailableHeads.remove(connectedHead);
							}
						}
						
						if(setAvailableHeads.size() == 0){
							continue;
						}
						
						Set<Integer> setTmpHeads = new HashSet<Integer>(setAvailableHeads.toIntegerArrayList());
						int hId = headIdsProposer.getPotentialItem(setTmpHeads);
						
						
						TripleBaseSingleID singleTriple = new TripleBaseSingleID();
						singleTriple.edgeColour = offeredGrpTriple.edgeColour;
						//head
						singleTriple.headId = hId;
						singleTriple.headColour = offeredGrpTriple.headColour;
						
						//tail 
						singleTriple.tailId = tId;
						singleTriple.tailColour = offeredGrpTriple.tailColour;
						
						maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
						return singleTriple;
					}
					maxIterationFor1EdgeColo --;
				}
			}
		}
		
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		LOGGER.info("Using the base function getProposedTriple of abstract class");
		return super.getProposedTriple(true);
	}
	
}

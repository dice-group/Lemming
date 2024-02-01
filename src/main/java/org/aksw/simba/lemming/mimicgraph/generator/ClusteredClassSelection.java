/**
 * Topic: Mimiced RDF Graph Generation
 * An extension of Lemming Project
 *
 * https://github.com/dice-group/Lemming
 */
package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.TripleColourDistributionMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSetOfIDs;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.RandomUtil;
import org.apache.jena.ext.com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Graph generation based on the group of triple's colours.
 * A triple's colour includes tail's colour, head's colour and the colour of edge connecting them
 * @author nptsy
 */
public class GraphGenerationClusteringBased extends AbstractGraphGeneration
		implements IGraphGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationClusteringBased.class);
	
	private Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> mTrippleMapOfTailHeadEdgeRates;
	private List<TripleColourDistributionMetric> mLstEVColorMapping;
	private Map<Integer, List<BitSet>> mMapEdgeIdsToTripleColours;
	private Random mRandom ;
	/*
	 * Constructor
	 */
	public GraphGenerationClusteringBased(int iNumberOfVertices,
			ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		mRandom = new Random(this.seed);
		this.seed++;
		mTrippleMapOfTailHeadEdgeRates = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		mLstEVColorMapping = new ArrayList<TripleColourDistributionMetric>();
		mMapEdgeIdsToTripleColours = new HashMap<Integer, List<BitSet>>();
		/*
		 *  compute edges and vertices distribution over each triple's colour
		 */
		computeEVColoDist(origGrphs);
		
		//compute average distribution of edges/ vertices of each triple's colour
		computeAverageEVColoDistribution();
		
		// assign specific number of edges to each grouped triple
		computeNoOfEdgesInTriples();
		
		//assign specific number of vertices to each grouped triple
		computeNoOfVerticesInTriples();
	}
	
	/**
	 * compute edges and vertices distribution over each triple's colour
	 * 
	 * @param origGrphs
	 */
	private void computeEVColoDist(ColouredGraph[] origGrphs){
		for(ColouredGraph grph: origGrphs){
			TripleColourDistributionMetric colorMapping = new TripleColourDistributionMetric();
			colorMapping.applyWithSingleThread(grph);
			mLstEVColorMapping.add(colorMapping);
		}
	}
	
	/**
	 * compute average distribution of edges/ vertices of each triple's colour
	 */
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
	
	/**
	 * put information of tail, head, and edge into triple
	 * @param firstKey
	 * @param secondKey
	 * @param thirdKey
	 * @param val
	 * @param changedMap
	 */
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
	
	/**
	 * compute average number of edges in each triple
	 */
	private void computeNoOfEdgesInTriples() {
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
						
						if(!mapHeadEdgeToGrpTriples.containsKey(headColo))
							continue;
						
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
				
				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(objDist, seed);
				seed = grpTripleProposer.getSeed() + 1;
				IntSet setEdges = mMapColourToEdgeIDs.get(edgeColo);
				
				if(setEdges!=null && setEdges.size() >0){
			        for (int eId : setEdges) {
						TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
						offeredGrpTriple.edgeIDs.add(eId);
						
						// add to map fake edge ids and triple colours
						List<BitSet> setTripleColours = mMapEdgeIdsToTripleColours.get(eId);
						setTripleColours = new ArrayList<BitSet>();
						mMapEdgeIdsToTripleColours.put(eId, setTripleColours);
						
						setTripleColours.add(offeredGrpTriple.tailColour);
						setTripleColours.add(offeredGrpTriple.edgeColour);
						setTripleColours.add(offeredGrpTriple.headColour);
					}
				}
			}
		}
		
		LOGGER.info("Done assing edges to grouped triples");
	}
	
	/**
	 * compute possible number of vertices in triples
	 */
	private void computeNoOfVerticesInTriples(){
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
							double noOfTails = Math.round(triple.noOfTails * setTails.size() + 0.1);
							if(noOfTails > triple.edgeIDs.size()){
								noOfTails  = triple.edgeIDs.size();
							}
							
							if(noOfTails == 0)
								noOfTails = 1;
							
							triple.noOfTails = noOfTails;
							
							/// heads
							double noOfHeads = Math.round(triple.noOfHeads * setHeads.size() + 0.1);
							
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

	public ColouredGraph generateGraph(){
		
		if(mNumberOfThreads == 1){
			LOGGER.info("Run graph generation with single thread!");
			generateGraphSingleThread();
		}else{
			LOGGER.info("Run graph generation with "+mNumberOfThreads+ " threads!");
			//get vertices for tails and heads first
			assignVerticesToTriples();
			//generate graph
			generateGraphMultiThreads();
		}
		
		return mMimicGraph;
	}
	
	/**
	 * assign vertices to triples
	 */
	private void assignVerticesToTriples(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		
		for(BitSet tailColo: setVertColo){
			
			if(!mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)){
				continue;
			}
			
			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = 
								mTrippleMapOfTailHeadEdgeRates.get(tailColo);
			
			
			for(BitSet headColo : setVertColo){
				
				if(!mapHeadEdgeToGrpTriples.containsKey(headColo)){
					continue;
				}
				
				Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
				
				Set<BitSet> setEdgeColours = mapEdgeToGrpTriples.keySet();
				
				for(BitSet edgeColo: setEdgeColours){
					
					if(!mapEdgeToGrpTriples.containsKey(edgeColo)){
						continue;
					}
					
					TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
					
					if(triple.edgeIDs.size() > 0 ){
						double noOfEdges = triple.edgeIDs.size();
						IntSet setOfRandomTailIds = getRandomVertices(triple.tailColour, triple.noOfTails);
						IntSet setOfRandomHeadIds = getRandomVertices(triple.headColour, triple.noOfHeads);
						
						if(setOfRandomHeadIds == null || setOfRandomTailIds == null){
							LOGGER.warn("There exists no vertices in " + triple.headColour +" or " 
											+ triple.tailColour +" colour. Skip: " + noOfEdges +" edges!");
							continue;
						} 
						
						triple.tailIDs.addAll(setOfRandomTailIds);
						triple.headIDs.addAll(setOfRandomHeadIds);
						/*
						 *  standardize the amount of edges and vertices
						 *  this makes sure there is no pair of vertices are connected by 
						 *  2 edges in same colour 
						 */
						double totalEdges = (double)(setOfRandomTailIds.size() * setOfRandomHeadIds.size());
						
						if( totalEdges < noOfEdges){
							LOGGER.warn("Not generate " + (noOfEdges - totalEdges) 	+ " edges in "+ edgeColo );
							noOfEdges = totalEdges;
						}
						
						triple.noOfEdges = noOfEdges;
					}
				}
			}
		}
	}
	
	/**
	 * generate graph with multi-threads
	 */
	private void generateGraphMultiThreads(){
		//exploit all possible threads
		int iNumberOfThreads = mNumberOfThreads;
		//int iNumberOfThreads = 4;
		List<IntSet> lstAssignedEdges = getColouredEdgesForConnecting(iNumberOfThreads);
		ExecutorService service = Executors.newFixedThreadPool(iNumberOfThreads);
		
		LOGGER.info("Create "+lstAssignedEdges.size()+" threads for processing graph generation!");
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		
		for(int i = 0 ; i < lstAssignedEdges.size() ; i++){
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					Random rand = new Random(seed);
					seed++;
					//max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
					//track the index of previous iteration
					int iIndexOfProcessingEdge = -1;
					//set of process edges
					int[] lstEdgeIds = setOfEdges.toIntArray();

					//iterate through all edge
					int j = 0 ;
					while(j < lstEdgeIds.length){
						//get an edge id
						int fakeEdgeId = lstEdgeIds[j];
						BitSet edgeColo = getEdgeColour(fakeEdgeId);
						
						if(edgeColo == null){
							//skip the edge that has failed edge colour
							j++;
							continue;
						}
						
						
						if(iIndexOfProcessingEdge != j){
							maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
							iIndexOfProcessingEdge = j;
						}else{
							if(maxIterationFor1Edge == 0){
								LOGGER.error("Could not create an edge of "
										+ edgeColo
										+ " colour since it could not find any approriate vertices to connect.");						
								
								j++;
								continue;
							}
						}
						
						List<BitSet> tripleColours = mMapEdgeIdsToTripleColours.get(fakeEdgeId);
						if(tripleColours == null || tripleColours.size() !=3){
							j++;
							continue;
						}
						
						BitSet tailColo = tripleColours.get(0);
						BitSet checkedColo = tripleColours.get(1);
						BitSet headColo = tripleColours.get(2);
						
						if(checkedColo.equals(edgeColo)){
							
							Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
							
							if(mapHeadEdgeTriples == null){
								j++;
								continue;
							}
							
							Map<BitSet, TripleBaseSetOfIDs> mapEdgeTriples = mapHeadEdgeTriples.get(headColo);
							if(mapEdgeTriples == null){
								j++;
								continue;
							}
							
							TripleBaseSetOfIDs triples = mapEdgeTriples.get(edgeColo);
							
							if(triples == null){
								j++;
								continue;
							}
							
							int[] arrTailIDs = triples.tailIDs.toIntArray();
							
							
							// select a random tail
							int tailId = -1;
							int iAttemptToGetTailIds = 1000;
							while(iAttemptToGetTailIds > 0){
								tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
								mRandom.setSeed(seed);
								seed++;
								if(!mReversedMapClassVertices.containsKey(tailColo))
									break;
								tailId = -1;
								iAttemptToGetTailIds --;	
							}
							
							if(tailId ==-1){
								maxIterationFor1Edge--;
								continue;
							}
							
							IntSet setHeadIDs = new DefaultIntSet(triples.headIDs.size());
							setHeadIDs.addAll(triples.headIDs);
							
							IntSet tmpSetOfConnectedHeads = getConnectedHeads(tailId, edgeColo);
							if(tmpSetOfConnectedHeads!= null && tmpSetOfConnectedHeads.size() >0  ){
								//int[] arrConnectedHeads = tmpSetOfConnectedHeads.toIntArray(); 
						        for (int connectedHead: tmpSetOfConnectedHeads) {
									if(setHeadIDs.contains(connectedHead))
										setHeadIDs.remove(connectedHead);
								}
							}
							
							if(setHeadIDs.size() == 0 ){
								maxIterationFor1Edge--;
								continue;
							}
							
							int[] arrHeadIDs = setHeadIDs.toIntArray();
							
							int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
							mRandom.setSeed(seed);
							seed++;
							boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
							if(isFoundVerticesConnected){
								j++;
								continue;
							}
							
						}else{
							LOGGER.error("Not match edge colour: " + checkedColo + " and "+ edgeColo);
						}
						
						maxIterationFor1Edge--;
						
						if (maxIterationFor1Edge == 0) {
							LOGGER.error("Could not create "
									+ (lstEdgeIds.length - j)
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate vertices to connect.");						
							
							j++;
						}
						
					}//end iteration of edges
				}
			};
			tasks.add(Executors.callable(worker));
		}
		
		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor! Be carefule");
			e.printStackTrace();
		};
	}
	
	/**
	 * generate graph with single thread
	 */
	private void generateGraphSingleThread(){
		
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Random rand = new Random(seed);
		seed++;
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
						
						if(triple != null && triple.edgeIDs.size() > 0 ){
							double noOfEdges = triple.edgeIDs.size();
							
							IntSet setOfRandomTailIds = getRandomVertices(triple.tailColour, triple.noOfTails);
							IntSet setOfRandomHeadIds = getRandomVertices(triple.headColour, triple.noOfHeads);
							
							if(setOfRandomHeadIds == null || setOfRandomTailIds == null){
								LOGGER.warn("There exists no vertices in " + triple.headColour +" or " 
												+ triple.tailColour +" colour. Skip: " + noOfEdges +" edges!");
								continue;
							}
							
							
							int[] arrTailIDs = setOfRandomTailIds.toIntArray();
							triple.tailIDs.addAll(setOfRandomTailIds);
							int[] arrHeadIDs = setOfRandomHeadIds.toIntArray();
							triple.headIDs.addAll(setOfRandomHeadIds);
							/*
							 *  standardize the amount of edges and vertices
							 *  this makes sure there is no pair of vertices are connected by 
							 *  2 edges in same colour 
							 */
							if(arrHeadIDs.length * arrTailIDs.length < noOfEdges){
								LOGGER.warn("Not generate " + (noOfEdges - (arrHeadIDs.length * arrTailIDs.length)) + " edges in "+ edgeColo );
								noOfEdges = arrHeadIDs.length * arrTailIDs.length;
							}
							
							int headId = -1;
							int i = 0 ; 
							while(i < noOfEdges){
								
								setOfRandomHeadIds = new DefaultIntSet(triple.headIDs.size());
								setOfRandomHeadIds.addAll(triple.headIDs);
								
								// select a random tail
								int tailId = arrTailIDs[rand.nextInt(arrTailIDs.length)];
								
								int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray(); 
								for(int connectedHead: arrConnectedHeads){
									if(setOfRandomHeadIds.contains(connectedHead))
										setOfRandomHeadIds.remove(connectedHead);
								}
								
								if(setOfRandomHeadIds.size() == 0 ){
									LOGGER.warn("No heads any more! Consider another tail colour");
									continue;
								}
								
								arrHeadIDs = setOfRandomHeadIds.toIntArray();
								
								headId = arrHeadIDs[rand.nextInt(arrHeadIDs.length)];
								
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
	}
	
	private IntSet getRandomVertices(BitSet vertColo, double iNoOfVertices){
		IntSet setVertices = mMapColourToVertexIDs.get(vertColo);
		if(setVertices != null){
			
			IntSet res = new DefaultIntSet(Constants.DEFAULT_SIZE);
			
			if(iNoOfVertices >= setVertices.size()){
				iNoOfVertices = setVertices.size();
				return setVertices;
			}
			
			// store the array indices for which the vertID should not match in order to exclude these 
			// array indexes from the random number generation
			Set<Integer> keys = mReversedMapClassVertices.keySet();
			IntSet exclusionSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
			for(int e : mReversedMapClassVertices.keySet()) {
				if(setVertices.contains(e)) {
					exclusionSet.add(e);
				}
			}
			if(exclusionSet.size()>=setVertices.size()) {
				LOGGER.warn("No possible vertices to connect of "+vertColo);
				return null;
			}
			
			while(iNoOfVertices > 0 ){
				int vertId = RandomUtil.getRandomWithExclusion(mRandom, setVertices.size(), exclusionSet);
				mRandom.setSeed(seed);
				seed++;
				if(!res.contains(vertId)){
					res.add(vertId);
					iNoOfVertices --;
				}
				
				if(res.size() == (setVertices.size() -1)){
					if(iNoOfVertices!=0)
						LOGGER.warn("Could not get " + iNoOfVertices + " vertices of "+ vertColo);
					break;
				}
			}
			
			return res;
		}
		return null;
	}
	
	@Override
	public TripleBaseSingleID getProposedTriple(boolean isRandom){

		if(!isRandom){
			//LOGGER.info("Using the override function getProposedTriple");

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
							
							//if(1> gap ){
							//	System.err.println("Break here");
							//}
						}
					}
				}
			}
			
			if(lstTripleGroups.size() > 0 ){

				TripleBaseSetOfIDs[] arrGrpTriples = lstTripleGroups.toArray(new TripleBaseSetOfIDs[0]);
				double[] arrEdgeRatePerTriple = Doubles.toArray(lstGapRequiredEdges);
				
				ObjectDistribution<TripleBaseSetOfIDs> objDist = new ObjectDistribution<TripleBaseSetOfIDs>(arrGrpTriples, arrEdgeRatePerTriple);
				OfferedItemByRandomProb<TripleBaseSetOfIDs> grpTripleProposer = new OfferedItemByRandomProb<TripleBaseSetOfIDs>(objDist, seed);
				seed = grpTripleProposer.getSeed() + 1;
				TripleBaseSetOfIDs offeredGrpTriple = grpTripleProposer.getPotentialItem();
				
				//get random a head and a tail to connect
				
				double gap = (offeredGrpTriple.noOfHeads * offeredGrpTriple.noOfTails) - offeredGrpTriple.noOfEdges;
				if(gap > 0 ){
					int[] arrTailIds = offeredGrpTriple.tailIDs.toIntArray();
					int[] arrHeadIds = offeredGrpTriple.headIDs.toIntArray();
					for(int i = 0 ; i < arrTailIds.length ; i++){
						int tId = arrTailIds[i];
						
						IntSet setConnectedHeads = getConnectedHeads(tId, offeredGrpTriple.edgeColour);
						
						for(int j = 0 ; j< arrHeadIds.length ; j++){
							int hId = arrHeadIds[j];
							if(!setConnectedHeads.contains(hId)){
								TripleBaseSingleID singleTriple = new TripleBaseSingleID();
								singleTriple.edgeColour = offeredGrpTriple.edgeColour;
								//head
								singleTriple.headId = hId;
								singleTriple.headColour = offeredGrpTriple.headColour;
								
								//tail 
								singleTriple.tailId = tId;
								singleTriple.tailColour = offeredGrpTriple.tailColour;
								
								return singleTriple;
							}
						}
					}
				}
			}
		}
		
		LOGGER.info("Using the base function getProposedTriple of abstract class");
		return super.getProposedTriple(true);
	}
}

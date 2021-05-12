package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgInDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgOutDegreeDistBaseVEColo;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.TripleColourDistributionMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSetOfIDs;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.aksw.simba.lemming.util.RandomUtil;
import org.apache.jena.ext.com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphGenerationClusteringBased2 extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationClusteringBased2.class);
	
	private Map<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>> mTrippleMapOfTailHeadEdgeRates;
	private List<TripleColourDistributionMetric> mLstEVColorMapping;
	private Map<Integer, List<BitSet>> mMapEdgeIdsToTripleColours;
//	private long seed;
	
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
	
	public GraphGenerationClusteringBased2(int iNumberOfVertices, ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		
		mTrippleMapOfTailHeadEdgeRates = new HashMap<BitSet, Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>>>();
		mLstEVColorMapping = new ArrayList<TripleColourDistributionMetric>();
		mMapEdgeIdsToTripleColours = new HashMap<Integer, List<BitSet>>();
		
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
		computeNoOfEdgesInTriples();
		
		//assign specific number of vertices to each grouped triple
		computeNoOfVerticesInTriples();
	}

	public ColouredGraph generateGraph(){
		if(mNumberOfThreads == 1){
			LOGGER.info("Run graph generation with single thread!");
			generateGraphSingleThread();
		}else{
			LOGGER.info("Run graph generation with "+mNumberOfThreads+ " threads!");
			//get vertices for tails and heads first
			assignVerticesToTriples();
			
			//generate graph with multi threads
			generateGraphMultiThreads();
		}
		
		return mMimicGraph;
	}
	
	private void testInfo(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		for(BitSet tailColo: setVertColo){
					
			if(!mTrippleMapOfTailHeadEdgeRates.containsKey(tailColo)){
				continue;
			}

			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> 
					mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
			
			for(BitSet headColo : setVertColo){
				
				if(!mapHeadEdgeToGrpTriples.containsKey(headColo)){
					continue;
				}
				
				Map<BitSet, TripleBaseSetOfIDs> mapEdgeToGrpTriples = mapHeadEdgeToGrpTriples.get(headColo);
				
				Set<BitSet> setEdgeColours = mapEdgeToGrpTriples.keySet();
				
				for(BitSet edgeColo: setEdgeColours){
					
					TripleBaseSetOfIDs triple = mapEdgeToGrpTriples.get(edgeColo);
					
					if(triple == null){
						LOGGER.warn("Found an invalid triple of ("+tailColo+","+edgeColo+","+headColo+") colour");
						continue;
					}
					
					System.out.println("[T:"+tailColo+"]="+triple.tailIDs.size()+
							" [E:"+edgeColo+"]="+triple.edgeIDs.size()+ " [H:"+headColo+"]="+triple.headIDs.size());
				}
			}
		}
		
		
		System.exit(-1);
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
					//max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME1;
					//track the index of previous iteration
					int iIndexOfProcessingEdge = -1;
					//set of process edges
					int[] arrOfEdges = setOfEdges.toIntArray();
					
					/*
					 *  set of failed edge colours. A failed edge colour is 
					 *  the colour that are not used to connect any 
					 *  vertices
					 */
					Set<BitSet> failedEdgeColours = new HashSet<BitSet>();
					
					//iterate through all edge
					int j = 0 ;
					while(j < arrOfEdges.length){
						//get an edge id
						int fakeEdgeId = arrOfEdges[j];
						BitSet edgeColo = getEdgeColour(fakeEdgeId);
						
						if(edgeColo == null){
							//skip the edge that has failed edge colour
							j++;
							continue;
						}
						
						if(failedEdgeColours.contains(edgeColo)){
							//skip the edge that has failed edge colour
							j++;
							continue;
						}
						
						if(iIndexOfProcessingEdge != j){
							maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME1;
							iIndexOfProcessingEdge = j;
						}else{
							if(maxIterationFor1Edge == 0){
								LOGGER.error("Could not connect edge "+arrOfEdges[j]+" of"
										+ edgeColo
										+ " colour since there is no approriate vertices to connect.");						
								
								j++;
								continue;
							}
						}
						
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> 
										mapTailIdProposers = mapPossibleODegreePerOEColo.get(edgeColo);
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> 
										mapHeadIdProposers = mapPossibleIDegreePerIEColo.get(edgeColo);
						
						if(mapTailIdProposers == null || mapHeadIdProposers == null){
							LOGGER.error("The "
									+ edgeColo
									+ " edge colour does not hold any verties proposers.");
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
						
						List<BitSet> tripleColours = mMapEdgeIdsToTripleColours.get(fakeEdgeId);
						if(tripleColours == null || tripleColours.size() != 3){
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
							
							Set<Integer> setTailIds = new HashSet<Integer>();
							setTailIds.addAll(triples.tailIDs);
							
							IOfferedItem<Integer> tailIdsProposer = mapTailIdProposers.get(tailColo);
							IOfferedItem<Integer> headIdsProposer = mapHeadIdProposers.get(headColo);
							
							// select a random tail
							int tailId = -1;
							int iAttemptToGetTailIds = 1000;
							while(iAttemptToGetTailIds > 0){
								tailId = tailIdsProposer.getPotentialItem(setTailIds);
								if(!mReversedMapClassVertices.containsKey(tailColo))
									break;
								tailId = -1;
								iAttemptToGetTailIds --;	
							}
							
							if(tailId ==-1){
								maxIterationFor1Edge--;
								continue;
							}
							
							IntSet tmpSetOfConnectedHeads = getConnectedHeads(tailId, edgeColo);
							IntSet setHeadIDs = new DefaultIntSet(triples.headIDs.size());
							setHeadIDs.addAll(triples.headIDs);
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
							
							Set<Integer> setHeadIds = new HashSet<Integer>(setHeadIDs);
							int headId = headIdsProposer.getPotentialItem(setHeadIds);
							
							boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
							if(isFoundVerticesConnected){
								j++;
								continue;
							}
							
						}else{
							LOGGER.error("Not match edge colour: " + checkedColo + " and "+ edgeColo);
						}
						
						maxIterationFor1Edge--;
						
						if(maxIterationFor1Edge == 0){
							LOGGER.error("Could not connect edge "+arrOfEdges[j]+" of"
									+ edgeColo
									+ " colour since there is no approriate vertices to connect.");						
							
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
	
	private void generateGraphSingleThread(){
		
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
							
							IntSet setOfRandomTailIds = getRandomVerticesWithDegree(triple.tailColour, triple.noOfTails, mapTailIdProposers);
							IntSet setOfRandomHeadIds = getRandomVerticesWithDegree(triple.headColour, triple.noOfHeads, mapHeadIdProposers);
							
							if((setOfRandomHeadIds == null || setOfRandomHeadIds.size() == 0 ) || 
									(setOfRandomTailIds == null || setOfRandomTailIds.size() == 0)){
								LOGGER.error("There exists no vertices in "  + triple.tailColour +" or " + triple.headColour +" colour");
								continue;
							}
							
							IOfferedItem<Integer> tailIdsProposer = mapTailIdProposers.get(triple.tailColour);
							IOfferedItem<Integer> headIdsProposer = mapHeadIdProposers.get(triple.headColour);
							
							triple.headIDs.addAll(setOfRandomHeadIds);
							triple.tailIDs.addAll(setOfRandomTailIds);
							
							Set<Integer> setTailIds = new HashSet<Integer>();
							setTailIds.addAll(setOfRandomTailIds);
							Set<Integer> setHeadIds = new HashSet<Integer>();
							setHeadIds.addAll(setOfRandomHeadIds);
							
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
								
								setOfRandomHeadIds = new DefaultIntSet(triple.headIDs.size());
								setOfRandomHeadIds.addAll(triple.headIDs);

								// select a random tail
								int tailId = tailIdsProposer.getPotentialItem(setTailIds);
								
								int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray(); 
								for(int connectedHead: arrConnectedHeads){
									if(setOfRandomHeadIds.contains(connectedHead))
										setOfRandomHeadIds.remove(connectedHead);
								}
								
								if(setOfRandomHeadIds.size() == 0 ){
									LOGGER.warn("No heads any more! Consider another tail");
									continue;
								}
								Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setOfRandomHeadIds);
								
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
	}
	
	private IntSet getRandomVertices(BitSet vertColo, double iNoOfVertices){
		IntSet setVertices = mMapColourToVertexIDs.get(vertColo);
		if(setVertices != null){
			
			Random rand = new Random(seed);
			
			IntSet res = new DefaultIntSet(Constants.DEFAULT_SIZE);
			
			if(iNoOfVertices >= setVertices.size()){
				iNoOfVertices =  setVertices.size();
				return setVertices;
			}
			
			// store the array indexes for which the vertID should not match and exclude these array indexes from the 
			// random number generation
			IntSet exclusionSet = new DefaultIntSet(Constants.DEFAULT_SIZE);
			for(int e : mReversedMapClassVertices.keySet()) {
				if(setVertices.contains(e)) {
					exclusionSet.add(e);
				}
			}
			if(exclusionSet.size()>= setVertices.size()) {
				LOGGER.warn("No possible vertices to connect of "+vertColo);
				return null;
			}
			
			while(iNoOfVertices > 0 ){
				int vertId = RandomUtil.getRandomWithExclusion(rand, setVertices.size(), exclusionSet);
				rand.setSeed(seed);
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
	
	private IntSet getRandomVerticesWithDegree(BitSet vertColo, double iNoOfVertices, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapVertexIdsProposers){
		IntSet setVertices = new DefaultIntSet(mMapColourToVertexIDs.get(vertColo).size());
		setVertices.addAll(mMapColourToVertexIDs.get(vertColo));
		
		//invalid setVertices
		if(setVertices == null || setVertices.size() ==0)
			return null;
		
		Set<Integer> tmpSetOfVertices = new HashSet<Integer>(setVertices);
		IOfferedItem<Integer> vertexIdsProposer= mapVertexIdsProposers.get(vertColo);
		
		
		if(vertexIdsProposer!= null){
			int[] arrVertices= setVertices.toIntArray();
			IntSet res = new DefaultIntSet(Constants.DEFAULT_SIZE);
			
			if(iNoOfVertices >= arrVertices.length){
				iNoOfVertices = arrVertices.length;
				return setVertices;
			}
			
			while(iNoOfVertices > 0){
				int vertId = vertexIdsProposer.getPotentialItem(tmpSetOfVertices);
				if(!res.contains(vertId) && !mReversedMapClassVertices.containsKey(vertId)){
					res.add(vertId);
					iNoOfVertices --;
					tmpSetOfVertices.remove(vertId);
				}
				
				if(tmpSetOfVertices.size()==0){
					LOGGER.warn("Could not found more " + iNoOfVertices + " vertices of "+ vertColo);
					break;
				}
				
				
				boolean havingMore = false;
				Iterator<Integer> iter = tmpSetOfVertices.iterator();
				while(iter.hasNext()){
					int availableId = iter.next();
					if(!mReversedMapClassVertices.containsKey(availableId)
							&& !res.contains(availableId)){
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
		Set<BitSet> setVertColours = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		
		for (BitSet tailColo : setVertColours) {
			for (BitSet headColo : setVertColours) {
				for (BitSet edgeColo : setEdgeColours) {

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
					
					for(int eId: setEdges){
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
	
	private void computeNoOfVerticesInTriples(){
		Set<BitSet> setVertColours = mMapColourToVertexIDs.keySet();
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		
		for(BitSet tailColo : setVertColours){
			//get all tails
			IntSet setTails = mMapColourToVertexIDs.get(tailColo);
			// tail distribution
			Map<BitSet, Map<BitSet, TripleBaseSetOfIDs>> mapHeadEdgeToGrpTriples = mTrippleMapOfTailHeadEdgeRates.get(tailColo);
			
			if(mapHeadEdgeToGrpTriples == null)
				continue;
			
			for(BitSet headColo: setVertColours){
				
				if(mapHeadEdgeToGrpTriples.containsKey(headColo)){
					
					// get all heads
					IntSet setHeads = mMapColourToVertexIDs.get(headColo);
					
					for(BitSet edgeColo: setEdgeColours){
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
				
				Random random = new Random(seed);
				seed++;
				
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
				seed = potentialDegreeProposer.getSeed() + 1;
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
				
				Random random = new Random(seed);
				seed++;
				
				double avrgInDegree = avrgInDegreeAnalyzer.getAvarageInDegreeOf(edgeColo, headColo);
				
				int [] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
				
				double[] possInDegreePerHeadDs = new double[arrHeadIDs.length];
				Integer[] objHeadIDs = new Integer[arrHeadIDs.length];
				
				// for each head id, we compute the potential in degree for it
				for(int i = 0; i < arrHeadIDs.length ; i++){
					objHeadIDs[i] = arrHeadIDs[i];
					// generate a random in degree for each vertex in its set based on the computed average in-degree
					int possDeg = PoissonDistribution.randomXJunhao(avrgInDegree, random);
					if(possDeg == 0)
						possDeg = 1;
					possInDegreePerHeadDs[i] = (double)possDeg;
				}
				
				ObjectDistribution<Integer> potentialInDegree = new ObjectDistribution<Integer>(objHeadIDs, possInDegreePerHeadDs);
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
		int maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		
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
						
						Set<Integer> setTmpTails = new HashSet<Integer>(offeredGrpTriple.tailIDs);
						int tId = tailIdsProposer.getPotentialItem(setTmpTails);
						
						int[] arrConnectedHeads = getConnectedHeads(tId, offeredGrpTriple.edgeColour).toIntArray();
						
						IntSet setAvailableHeads = new DefaultIntSet(offeredGrpTriple.headIDs.size());
						setAvailableHeads.addAll(offeredGrpTriple.headIDs);
						
						for(int connectedHead : arrConnectedHeads){
							if(setAvailableHeads.contains(connectedHead)){
								setAvailableHeads.remove(connectedHead);
							}
						}
						
						if(setAvailableHeads.size() == 0){
							continue;
						}
						
						Set<Integer> setTmpHeads = new HashSet<Integer>(setAvailableHeads);
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

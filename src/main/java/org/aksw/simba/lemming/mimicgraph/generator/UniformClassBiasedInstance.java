package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.PoissonDistribution;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class GraphGenerationRandomly2 extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationRandomly2.class);
	private int maxIterationFor1EdgeColo ;
	private Random mRandom;
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
	
	
	public GraphGenerationRandomly2(int iNumberOfVertices,
			ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		mRandom = new Random(this.seed);
		// initilize variable
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		
		// extend step compared to the class GraphGenerationSimpleApproach
		computePotentialIODegreePerVert(origGrphs);
	}

	public ColouredGraph generateGraph(){
		if(mNumberOfThreads==1){
			LOGGER.info("Run graph generation with single thread!");
			generateGraphSingleThread();
		}else{
			LOGGER.info("Run graph generation with "+mNumberOfThreads+ " threads!");
			generateGraphMultiThreads();
		}
		return mMimicGraph;
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
			final Set<BitSet> setAvailableVertexColours = new HashSet<BitSet>(mMapColourToVertexIDs.keySet());
			
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					Random random = new Random(seed);
					seed++;
					//max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
					//track the index of previous iteration
					int iIndexOfProcessingEdge = -1;
					//set of process edges
					int[] arrOfEdges = setOfEdges.toIntArray();
					// set of failed edge colours
					Set<BitSet> failedEdgeColours = new HashSet<BitSet>();
					
					//iterate through each edge
					int j = 0;
					while( j < arrOfEdges.length ){
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
							maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
							iIndexOfProcessingEdge = j;
						}else{
							if(maxIterationFor1Edge == 0){
								LOGGER.error("Could not create an edge of "
										+ edgeColo
										+ " colour since it could not find any approriate vertices to connect.");						
								
								failedEdgeColours.add(edgeColo);
								j++;
								continue;
							}
						}
						
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadColoToIDProposer = mapPossibleIDegreePerIEColo.get(edgeColo);
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailColoToIDProposer = mapPossibleODegreePerOEColo.get(edgeColo);
						
						/*
						 * if not existing any proposers, then skip to the next id
						 */
						if(mapHeadColoToIDProposer == null || mapTailColoToIDProposer == null){
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
						
						//get potential tail colours
						Set<BitSet> setTailColours = 
								new HashSet<BitSet>(mColourMapper.getTailColoursFromEdgeColour(edgeColo));
						setTailColours.retainAll(setAvailableVertexColours);

						/*
						 * in case there is no tail colours => the edge colour should not 
						 * be considered again
						 */
						if(setTailColours.size() == 0){
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
						
						//get random a tail colour
						BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
						BitSet tailColo = arrTailColours[random.nextInt(arrTailColours.length)];	
						Set<BitSet> setRestrictedHeadColours = new HashSet<BitSet>(mColourMapper.getHeadColours(tailColo, edgeColo));
						
						if(setRestrictedHeadColours == null || setRestrictedHeadColours.size() ==0){
							maxIterationFor1Edge--;
							continue;
						}
						
						setRestrictedHeadColours.retainAll(setAvailableVertexColours);
						
						if(setRestrictedHeadColours.size() == 0){
							maxIterationFor1Edge--;
							continue;
						}
						
						BitSet [] arrHeadColours = setRestrictedHeadColours.toArray(new BitSet[0]);
						
						BitSet headColo = arrHeadColours[random.nextInt(arrHeadColours.length)];
						
						//add id proposers here
						IOfferedItem<Integer> tailIDProposer = mapTailColoToIDProposer.get(tailColo);
						IOfferedItem<Integer> headIDProposer = mapHeadColoToIDProposer.get(headColo);
						
						//since tailColo and headColo are selected randomly ==> restart select another
						if(tailIDProposer == null || headIDProposer == null){
							maxIterationFor1Edge--;
							continue;
						}
						
						int tailId = -1;
						int iAttemptToGetTailIds = 1000;
						//quite sure that we can always find a tail ID
						while(iAttemptToGetTailIds > 0 ){
							tailId = tailIDProposer.getPotentialItem();	
							if(!mReversedMapClassVertices.containsKey(tailId))
								break;
							tailId = -1;
							iAttemptToGetTailIds --;							
						}
						
						if(tailId == -1){
							maxIterationFor1Edge--;
							continue;
						}
						
						//get set of tail ids and head ids
						IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
						
						if(mMapColourToVertexIDs.containsKey(headColo)){
							setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
						}
						
						if(setHeadIDs == null || setHeadIDs.size() == 0 ){
							maxIterationFor1Edge--;
							continue;
						}
						
						IntSet tmpConnectedHeadIds = getConnectedHeads(tailId, edgeColo);
						if(tmpConnectedHeadIds!= null && tmpConnectedHeadIds.size() >0 ){
							for(int connectedHead: tmpConnectedHeadIds){
								if(setHeadIDs.contains(connectedHead))
									setHeadIDs.remove(connectedHead);
							}
						}
						
						if(setHeadIDs.size() == 0){
							maxIterationFor1Edge--;
							continue;
						}
						
						Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setHeadIDs);
						
						int headId = headIDProposer.getPotentialItem(setFilteredHeadIDs);
						
						boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
						if(isFoundVerticesConnected){
							j++;
							continue;
						}
						
						maxIterationFor1Edge--;
						
						if (maxIterationFor1Edge == 0) {
							LOGGER.error("Could not create "
									+ (arrOfEdges.length - j)
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate vertices to connect.");						
							
							failedEdgeColours.add(edgeColo);
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
		
		Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
		Set<BitSet> setAvailableVertexColours = mMapColourToVertexIDs.keySet();
		
		for(BitSet edgeColo : setEdgeColours){
			
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			setTailColours.retainAll(setAvailableVertexColours);
			
			BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
			
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadColoToIDProposer = mapPossibleIDegreePerIEColo.get(edgeColo);
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailColoToIDProposer = mapPossibleODegreePerOEColo.get(edgeColo);
			
			if(mapHeadColoToIDProposer != null && mapTailColoToIDProposer != null){
				
				/* 
				 * the setFakeEdgeIDs helps us to know how many edges existing
				 * of a specific edge's colour
				 */ 
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				int i = 0 ;
				while(i < setFakeEdgeIDs.size()){
					// use each edge to connect vertices
						
					boolean isFoundVerticesConnected = false;
					
					BitSet tailColo = arrTailColours[mRandom.nextInt(arrTailColours.length)];	
					Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					setHeadColours.retainAll(setAvailableVertexColours);
					
					if(setHeadColours.size() > 0){
						BitSet [] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
						BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
						
						IOfferedItem<Integer> tailIDProposer = mapTailColoToIDProposer.get(tailColo);
						IOfferedItem<Integer> headIDProposer = mapHeadColoToIDProposer.get(headColo);
								
						
						if(tailIDProposer!= null && headIDProposer != null){
							
							int tailId = tailIDProposer.getPotentialItem();
							if(mReversedMapClassVertices.containsKey(tailId)){
								continue;
							}
							
							IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
							if(mMapColourToVertexIDs.containsKey(headColo)){
								setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
							}
							
							if(setHeadIDs == null || setHeadIDs.size() == 0 ){
								continue;
							}
							
							int[] arrConnectedHeads = getConnectedHeads(tailId, edgeColo).toIntArray(); 
							for(int connectedHead: arrConnectedHeads){
								if(setHeadIDs.contains(connectedHead))
									setHeadIDs.remove(connectedHead);
							}
							
							if(setHeadIDs.size() == 0){
								continue;
							}
							
							Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setHeadIDs);
							
							int headId = headIDProposer.getPotentialItem(setFilteredHeadIDs);
							
							if(connectableVertices(tailId, headId, edgeColo)){
								mMimicGraph.addEdge(tailId, headId, edgeColo);
								isFoundVerticesConnected = true;
								i++;
							}
						}
//						else{
//							System.err.println("Could not find any vertices with the tail's or head's colours!");
//							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
//						}
					}
					
					if (!isFoundVerticesConnected) {
						maxIterationFor1EdgeColo--;
						if (maxIterationFor1EdgeColo == 0) {
							LOGGER.error("Could not create "
									+ (setFakeEdgeIDs.size() - i)
									+ " edges (" 
									+ setFakeEdgeIDs.size()
									+") in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							
							System.err.println("Could not create "
									+ (setFakeEdgeIDs.size() - i)
									+ " edges (" 
									+ setFakeEdgeIDs.size()
									+") in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							break;
						}
					}
				}
				
				maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
				
			}else{
				LOGGER.error("Could not consider the"
						+ edgeColo
						+ " edge's coloursince it could not find any distribution of edges.");
				
				System.err.println("Could not consider the"
						+ edgeColo
						+ " edge's coloursince it could not find any distribution of edges.");
			}
		}
	}
	
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		if(!isRandom){
			//System.out.println("using override function getProposedTriple(");
			
			Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
			BitSet[] arrVertexColours = setVertexColours.toArray(new BitSet[]{});
			
			while(true){
				// tail colour
				BitSet tailColo = arrVertexColours[mRandom.nextInt(arrVertexColours.length)];
				Set<BitSet> possOutEdgeColours = mColourMapper.getPossibleOutEdgeColours(tailColo);
				
				if(possOutEdgeColours != null && possOutEdgeColours.size() > 0){
					
					if(possOutEdgeColours.contains(mRdfTypePropertyColour)){
						possOutEdgeColours.remove(mRdfTypePropertyColour);
					}
					
					if(possOutEdgeColours.size() == 0){
						continue;
					}
					
					BitSet[] arrEdgeColours = possOutEdgeColours.toArray(new BitSet[]{});
					
					BitSet edgeColo = arrEdgeColours[mRandom.nextInt(arrEdgeColours.length)];
					Set<BitSet> possHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
					
					
					if(possHeadColours != null && possHeadColours.size() > 0 && mapTailColoToTailIDs!= null && mapHeadColoToHeadIDs!=null){
						BitSet[] arrHeadColours = possHeadColours.toArray(new BitSet[]{});
						BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
						
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						
						// get vertex's ids according to the vertex's colours
						if(tailIDsProposer != null && headIDsProposer != null){
							
							int tailId = tailIDsProposer.getPotentialItem();
							int headId = headIDsProposer.getPotentialItem();
							if(connectableVertices(tailId, headId, edgeColo)){
								
								// if the vertices can be connected via the edge colour => connect them 
								TripleBaseSingleID triple = new TripleBaseSingleID();
								triple.tailId = tailId;
								triple.tailColour = tailColo;
								triple.headId = headId;
								triple.headColour = headColo;
								triple.edgeColour = edgeColo;
								
								return triple;
							}
						}
					}
				}
			}
			
		}else{
			//System.out.println("using base function getProposedTriple(");
			return super.getProposedTriple(true);
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
					Random random = new Random(seed);
					seed++;
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
						LOGGER.error("Something is seriously happening for tail colours, since " + tailColo +" can not have more than 2 proposers");
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
					Random random = new Random(seed);
					seed++;
					
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
						LOGGER.error("Something is seriously happening for head colours, since " + headColo +" can not have more than 2 proposers");
					}
				}
			}
		}
	}
	
}

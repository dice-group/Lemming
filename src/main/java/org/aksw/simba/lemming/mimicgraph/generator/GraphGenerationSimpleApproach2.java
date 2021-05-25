package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredIEDistPerVColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredOEDistPerVColour;
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

public class GraphGenerationSimpleApproach2 extends AbstractGraphGeneration implements IGraphGeneration{
private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationSimpleApproach2.class);
	
	//key: out-edge's colours, the values are the distribution of edges per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgOutEdgeDistPerVertColo;
	
	//key: in-edge's colours, the values are the distribution of edges per vertex's colours
	//private Map<BitSet, ObjectDistribution<BitSet>> mMapAvrgInEdgeDistPerVertColo;

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


	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;

	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationSimpleApproach2(int iNumberOfVertices,
			ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		
		computeAvrgIOEdgeDistPerVertColo(origGrphs);
		
		// extend step compared to the class GraphGenerationSimpleApproach
		computePotentialIODegreePerVert(origGrphs);
	}

	public ColouredGraph generateGraph(){
		if(mNumberOfThreads<=1){
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
		final Set<BitSet> setAvailableVertexColours = new HashSet<BitSet>(mMapColourToVertexIDs.keySet());

		LOGGER.info("Create "+lstAssignedEdges.size()+" threads for processing graph generation!");
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		
		//for each set of edges ==> create a worker
		for(int i = 0 ; i < lstAssignedEdges.size() ; i++){
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			//worker
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					//max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
					//track the index of previous iteration
					int iIndexOfProcessingEdge = -1;
					//set of process edges
					int[] arrOfEdges = setOfEdges.toIntArray();

					/*
					 *  set of failed edge colours. A failed edge colour is 
					 *  the colour that has are not used to connect any 
					 *  vertices
					 */
					Set<BitSet> failedEdgeColours = new HashSet<BitSet>();
					
					int j = 0 ;
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
						
						//these proposers help to select a potential tail colour and a head colour
						IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
						IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
						
						if(headColourProposer == null || tailColourProposer == null){
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
						
						// these proposers provide a potential degree for each of tails and heads
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  
											mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
						ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  
											mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
						
						if(mapTailColoToTailIDs == null || mapHeadColoToHeadIDs == null){
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
						
						BitSet tailColo = tailColourProposer.getPotentialItem();
						
						if(tailColo == null){
							maxIterationFor1Edge--;
							continue;
						}
						// get a set of head colours associated with the edge colour and the tail colour
						Set<BitSet> setRestrictedHeadColours =
										new HashSet<BitSet>(mColourMapper.getHeadColours(tailColo, edgeColo));
						if(setRestrictedHeadColours== null || setRestrictedHeadColours.size() ==0){
							maxIterationFor1Edge--;
							continue;
						}
						
						setRestrictedHeadColours.retainAll(setAvailableVertexColours);
												
						if(setRestrictedHeadColours.size() == 0){
							maxIterationFor1Edge--;
							continue;
						}
						
						// base on the possible linked head's colours got from the tailColo we get the potential headColo
						BitSet headColo = headColourProposer.getPotentialItem(setRestrictedHeadColours);
						if(headColo == null){
							maxIterationFor1Edge--;
							continue;
						}
						
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						if(tailIDsProposer == null || headIDsProposer == null){
							maxIterationFor1Edge--;
							continue;
						}
						
						int tailId = -1;
						int iAttemptToGetTailIds = 1000;
						//quite sure that we can always find a tail ID
						while(iAttemptToGetTailIds > 0 ){
							tailId = tailIDsProposer.getPotentialItem();	
							if(!mReversedMapClassVertices.containsKey(tailId))
								break;
							tailId = -1;
							iAttemptToGetTailIds --;							
						}
						
						if(tailId ==-1){
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
						
						int headId = headIDsProposer.getPotentialItem(setFilteredHeadIDs);
						
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
		
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			//these proposers help to select a potential tail colour and a head colour
			IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
			IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
			
			// these proposers provide a potential degree for each of tails and heads
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
			
			if(headColourProposer != null && tailColourProposer!= null && mapTailColoToTailIDs != null && mapHeadColoToHeadIDs != null){
				
				// the setFakeEdgeIDs helps us to know how many edges existing in a specific edge's colour
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				
				int i = 0 ;
				while (i< setFakeEdgeIDs.size()){
						
					boolean isFoundVerticesConnected = false;
					
					BitSet tailColo = tailColourProposer.getPotentialItem();
					Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo);
					
					if(setPossHeadColours.size() == 0 )
						continue;
					
					BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
										
					if(headColo!=null && tailColo!= null){
						
						/*
						 *  this step is different to the GraphGenerationSimpleApproach
						 *  since it apply poisson distribution to compute potential degree 
						 *  for each vertex
						 */
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						if(tailIDsProposer !=null && headIDsProposer != null ){
							Integer tailId = tailIDsProposer.getPotentialItem();
							
							IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
							if(mMapColourToVertexIDs.containsKey(headColo)){
								setHeadIDs.addAll(mMapColourToVertexIDs.get(headColo));
							}
							if(setHeadIDs == null || setHeadIDs.size() == 0 ){
								continue;
							}
							
							int[] arrConnectedHeads = getConnectedHeads(tailId,edgeColo).toIntArray(); 
							for(int connectedHead: arrConnectedHeads){
								if(setHeadIDs.contains(connectedHead))
									setHeadIDs.remove(connectedHead);
							}
							
							if(setHeadIDs.size() == 0){
								continue;
							}
							
							Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setHeadIDs);
							
							Integer headId = headIDsProposer.getPotentialItem(setFilteredHeadIDs);
							if(tailId != null && headId != null && 
									connectableVertices(tailId, headId, edgeColo)){
								mMimicGraph.addEdge(tailId, headId, edgeColo);
								isFoundVerticesConnected = true;
								i++;
							}
						}
//						else{
//							System.err.println("Could not find any vertices with the tail's or head's colours!");
//							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
//						}
					}else{
						System.err.println("Could not find matching tail's and head's colours to connect!");
						LOGGER.warn("Could not find matching tail's and head's colours to connect!");
					}
					
					if(!isFoundVerticesConnected){
		    			maxIterationFor1EdgeColo--;
		    			if(maxIterationFor1EdgeColo == 0){
		    				LOGGER.warn("Could not create "
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
				/*
				 * this case seems to never happen since for an edge there are always vertices to be connected
				 */
				
				LOGGER.warn("Not process the"
						+ edgeColo
						+ " edge's colour since it could not find any approriate vertices to connect.");
				
				System.err.println("Not process the"
						+ edgeColo
						+ " edge's coloursince it could not find any approriate vertices to connect");
			}
		}
		System.out.println();
	}
	
	/**
	 * compute complex distribution
	 * 
	 * @param origGrphs list of all versions of graphs
	 */
	private void computeAvrgIOEdgeDistPerVertColo(ColouredGraph[] origGrphs){
		// out degree colour distribution associated with edge colours
		AvrgColouredOEDistPerVColour avrgOutEdgeDistPerVertColoMetric = new AvrgColouredOEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgOutEdgeDistPerVertColo = avrgOutEdgeDistPerVertColoMetric.getMapAvrgOutEdgeDist(mMapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
		
		Set<BitSet> outEdgeColours = avrgOutEdgeDistPerVertColo.keySet();
		for(BitSet edgeColo : outEdgeColours){
			ObjectDistribution<BitSet> outEdgeDistPerVertColo = avrgOutEdgeDistPerVertColo.get(edgeColo);
			if(outEdgeDistPerVertColo != null){
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(outEdgeDistPerVertColo, seed);
				seed = vertColoProposer.getSeed() + 1;
				mMapOEColoToTailColoProposer.put(edgeColo, vertColoProposer);
			}
		}
		
		// in degree colour distribution associated with edge colours
		AvrgColouredIEDistPerVColour avrgInEdgeDistPerVertColoMetric = new AvrgColouredIEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgInEdgeDistPerVertColo = avrgInEdgeDistPerVertColoMetric.getMapAvrgInEdgeDist(mMapColourToEdgeIDs.keySet(), mMapColourToVertexIDs.keySet());
		
		Set<BitSet> inEdgeColours = avrgInEdgeDistPerVertColo.keySet();
		for(BitSet edgeColo : inEdgeColours){
			ObjectDistribution<BitSet> inEdgeDistPerVertColo = avrgInEdgeDistPerVertColo.get(edgeColo);
			if(inEdgeDistPerVertColo != null){
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(inEdgeDistPerVertColo, seed);
				seed = vertColoProposer.getSeed() + 1;
				mMapIEColoToHeadColoProposer.put(edgeColo, vertColoProposer);
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
	
	
	
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		
		if(!isRandom){
			//LOGGER.info("Using the override function getProposedTriple");
			while(true){
				BitSet edgeColo = mEdgeColoProposer.getPotentialItem();
				if(edgeColo != null && !edgeColo.equals(mRdfTypePropertyColour)){
					IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
					IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
					
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapTailColoToTailIDs = mapPossibleODegreePerOEColo.get(edgeColo);
					ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>  mapHeadColoToHeadIDs = mapPossibleIDegreePerIEColo.get(edgeColo);
					
					
					if(tailColourProposer!=null && headColourProposer !=null && mapTailColoToTailIDs !=null && mapHeadColoToHeadIDs!=null ){
						
						BitSet tailColo = tailColourProposer.getPotentialItem();
						Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
						if(setPossHeadColours.size() == 0){
							continue;
						}
						
						BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
						
						IOfferedItem<Integer> tailIDsProposer = mapTailColoToTailIDs.get(tailColo);
						IOfferedItem<Integer> headIDsProposer = mapHeadColoToHeadIDs.get(headColo);
						
						if(tailIDsProposer !=null && headIDsProposer != null ){
							Integer tailId = tailIDsProposer.getPotentialItem();
							Integer headId = headIDsProposer.getPotentialItem();
							if(tailId != null && headId != null && 
									connectableVertices(tailId, headId, edgeColo)){
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
			LOGGER.info("Using the base function getProposedTriple of abstract class");
			return super.getProposedTriple(true);
		}
	}
}

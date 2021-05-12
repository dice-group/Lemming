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
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * The simplest approach, we randomly select a vertex in its set (the set with a
 * specific colour)
 * 
 * @author nptsy
 *
 */
public class GraphGenerationSimpleApproach extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationSimpleApproach.class);
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;
	
	private int maxIterationFor1EdgeColo ;
	private Random mRandom;
	
	public GraphGenerationSimpleApproach(int iNumberOfVertices,
			ColouredGraph[] origGrphs, int iNumberOfThreads, long seed) {
		super(iNumberOfVertices, origGrphs, iNumberOfThreads, seed);
		mRandom = new Random(this.seed);
		
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;
		
		computeAvrgIOEdgeDistPerVertColo(origGrphs);
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
					Random random = new Random(seed);
					seed++;
					//max iteration of 1 edge
					int maxIterationFor1Edge = Constants.MAX_EXPLORING_TIME;
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
					
					int j = 0 ; 
					while(j < arrOfEdges.length ){
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
						
						IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
						IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
						
						if(headColourProposer == null || tailColourProposer == null){
							failedEdgeColours.add(edgeColo);
							j++;
							continue;
						}
												
						// get tail colour based on distribution of the edge colour over the vertex colour 
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

						//get set of tail ids and head ids
						IntSet setTailIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
						IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
						
						if(mMapColourToVertexIDs.containsKey(tailColo)){
							setTailIDs = mMapColourToVertexIDs.get(tailColo);
						}
						
						if(mMapColourToVertexIDs.containsKey(headColo)){
							setHeadIDs = mMapColourToVertexIDs.get(headColo);
						}
						
						if(setTailIDs!= null && setTailIDs.size()> 0 && 
								setHeadIDs!=null && setHeadIDs.size()> 0){
							int[] arrTailIDs = setTailIDs.toIntArray();
							int tailId = -1;
							int iAttemptToGetTailIds = 1000;
							while(iAttemptToGetTailIds > 0){
								tailId = arrTailIDs[random.nextInt(arrTailIDs.length)];
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
							if(tmpSetOfConnectedHeads!= null && tmpSetOfConnectedHeads.size() >0  ){
								//int[] arrConnectedHeads = tmpSetOfConnectedHeads.toIntArray(); 
								for(int connectedHead:tmpSetOfConnectedHeads) {
									if(setHeadIDs.contains(connectedHead))
										setHeadIDs.remove(connectedHead);
								}
							}
							
							if(setHeadIDs.size() == 0 ){
								maxIterationFor1Edge--;
								continue;
							}
							
							int[] arrHeadIDs = setHeadIDs.toIntArray();
							
							int headId = arrHeadIDs[random.nextInt(arrHeadIDs.length)];
							boolean isFoundVerticesConnected = connectIfPossible(tailId, headId, edgeColo);
							if(isFoundVerticesConnected){
								j++;
								continue;
							}
						}
						
						maxIterationFor1Edge--;
						if (maxIterationFor1Edge == 0) {
							LOGGER.error("Could not create an edge of "
									+ edgeColo
									+ " colour since it could not find any approriate vertices to connect.");					
							
							failedEdgeColours.add(edgeColo);
							j++;
						}
					}//end of for of edge ids
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
		
		/*
		 * the map mMapColourToEdgeIDs just contains normal edges
		 * (not datatype property edges, or rdf:type edge) 
		 */
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
			IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
			
			if(tailColourProposer != null && headColourProposer!= null ){
				
				/* the setFakeEdgeIDs helps us to know how many edges existing
				 * in a specific edge's colour
				 */
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				
				// use each edge to connect vertices
				int i = 0 ; 
				while (i < setFakeEdgeIDs.size()){
						
					boolean isFoundVerticesConnected = false;
					
					// get tail colour based on distribution of the edge colour over the vertex colour 
					BitSet tailColo = tailColourProposer.getPotentialItem();
					// get a set of head colours associated with the edge colour and the tail colour
					Set<BitSet> setRestrictedHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					
					// base on the possible linked head's colours got from the tailColo we get the potential headColo
					BitSet headColo = headColourProposer.getPotentialItem(setRestrictedHeadColours);
					
					if(headColo!=null && tailColo!= null){
						// get tailIds based on the tailColo
						IntSet tailIDs = mMapColourToVertexIDs.get(tailColo);
			    		
			    		if(tailIDs!=null && !tailIDs.isEmpty()){
			    			int[] arrTailVertices = tailIDs.toIntArray();
				    		int tailId = arrTailVertices[mRandom.nextInt(arrTailVertices.length)];
				    		
				    		IntSet setHeadIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
							if(mMapColourToVertexIDs.containsKey(headColo)){
								setHeadIDs = mMapColourToVertexIDs.get(headColo);
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
							int[] arrHeadVertices = setHeadIDs.toIntArray();
							
				    		int headId = arrHeadVertices[mRandom.nextInt(arrHeadVertices.length)];
				    		
				    		if(connectableVertices(tailId, headId, edgeColo)){
								mMimicGraph.addEdge(tailId, headId, edgeColo);
								isFoundVerticesConnected = true;
								i++;
							}
			    		}
//			    		else{
//			    			System.err.println("Could not find any vertices with the tail's or head's colours!");
//							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
//			    		}
					}
					else{
						System.err.println("Could not find matching tail's and head's colours to connect!");
						LOGGER.error("Could not find matching tail's and head's colours to connect!");
					}
					
					if (!isFoundVerticesConnected) {
						// System.err.println("Could not link " + tailColo +
						// " - " + edgeColo + " - "+ headColo);
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
				
				/*
				 * this case may never happen since in RDF graph, an edge is always is used to connect 2 vertices 
				 */
				
				LOGGER.warn("Not process the"
						+ edgeColo
						+ " edge's colour since it could not find any approriate vertex's colours.");
				
				System.err.println("Not process the"
						+ edgeColo
						+ " edge's colours ince it could not find any approriate vertex's colours.");
			}
		}
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
	
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		
		if(!isRandom){
			
			while(true){
				BitSet edgeColo = mEdgeColoProposer.getPotentialItem();
				if(edgeColo != null && !edgeColo.equals(mRdfTypePropertyColour)){
					
					IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColo);
					IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColo);
					
					if(tailColourProposer!=null && headColourProposer !=null){
						
						BitSet tailColo = tailColourProposer.getPotentialItem();
						Set<BitSet> setPossHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
						
						if(setPossHeadColours.size() == 0 ){
							continue;
						}
						
						BitSet headColo = headColourProposer.getPotentialItem(setPossHeadColours);
						
						// get vertex's ids according to the vertex's colours
						if(tailColo != null && headColo != null
								&& mMapColourToVertexIDs.get(tailColo) != null && mMapColourToVertexIDs.get(headColo) != null
								&& mMapColourToVertexIDs.get(tailColo).size() > 0 
								&& mMapColourToVertexIDs.get(headColo).size() > 0 ){
							int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
							int[] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
							
							int tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
							int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
							
							
							if(connectableVertices(tailId, headId, edgeColo)){
								TripleBaseSingleID triple = new TripleBaseSingleID();
								triple.tailId = tailId;
								triple.tailColour = tailColo;
								triple.headId = headId;
								triple.headColour = headColo;
								triple.edgeColour = edgeColo;
								//LOGGER.info("Proposed added triple: ("+tailId+","+headId+","+edgeColo+")");
								return triple;
							}
						}
					}
				}
			}
		}else{
			//System.out.println("using base function getProposedTriple(");
			LOGGER.info("Using the base function getProposedTriple of abstract class");
			return super.getProposedTriple(true);
		}
	}
	
	
}

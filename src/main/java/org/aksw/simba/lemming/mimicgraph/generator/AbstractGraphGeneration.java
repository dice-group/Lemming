package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.mimicgraph.constraints.ColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.IColourMappingRules;
import org.aksw.simba.lemming.mimicgraph.constraints.TripleBaseSingleID;
import org.aksw.simba.lemming.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;

import grph.DefaultIntSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public abstract class AbstractGraphGeneration extends BasicGraphGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphGeneration.class);
	
	protected int mIDesiredNoOfVertices = 0;
	protected int mIDesiredNoOfEdges = 0;
	
	protected ObjectDistribution<BitSet> mVertColoDist;
	protected ObjectDistribution<BitSet> mEdgeColoDist;
	
	protected int mNumberOfThreads;
	/*
	 * the keys are the vertex's color and the values are the set of vertex's ids
	 */
	protected Map<BitSet, IntSet> mMapColourToVertexIDs = new ConcurrentHashMap<BitSet, IntSet>();
	
	/*
	 * the keys are the edge's color and the values are the set of edge's ids
	 * (note: fake id)
	 */
	protected Map<BitSet, IntSet> mMapColourToEdgeIDs = new ConcurrentHashMap<BitSet, IntSet>();

	protected ColouredGraph mMimicGraph;
	
	/*
	 * the mColourMapper is responsible for monitoring connection between vertex colours using edge colour 
	 */
	protected IColourMappingRules mColourMapper;
	
	/*
	 * this set manage a list of considered edge's colours can only exist in the mimic graph
	 * An edge colour can only exist if there are vertices in colours such that the edge colour can be use 
	 * to connect them
	 */
	private HashSet<BitSet> mSetOfRestrictedEdgeColours = new HashSet<BitSet>();
	
	/*
	 * 1st key: the edge colour, 2nd key is the tail id and the value is the set of already connected head id
	 */
	private Map<BitSet, Map<Integer, IntSet>> mMapEdgeColoursToConnectedVertices;
	
	protected IOfferedItem<BitSet> mEdgeColoProposer;
	
	private Random mRandom ;
	protected long seed;
	
	protected BitSet mRdfTypePropertyColour;
	
	protected Map<BitSet, Integer> mMapClassVertices;
	protected Map<Integer, BitSet> mReversedMapClassVertices;
	
	//protected final ObjectDoubleOpenHashMap<BitSet> mEdgeColoursThreshold;
	private Map<Integer, BitSet> mMapEdgeIdsToColour;
	
	
	public AbstractGraphGeneration(int iNumberOfVertices, ColouredGraph[] origGrphs, int iNumberOfThreads, long seed){
		//number of vertices
		mIDesiredNoOfVertices = iNumberOfVertices;
		this.seed = seed+1;
		// mimic grpah
		mMimicGraph = new ColouredGraph();
		//copy all colour palette to the mimic graph
		copyColourPalette(origGrphs, mMimicGraph);
		
		// random
		mRandom = new Random(this.seed);
		//number of threads 
		setNumberOfThreadsForGenerationProcess(iNumberOfThreads);
		
		mColourMapper = new ColourMappingRules();
		mColourMapper.analyzeRules(origGrphs);
		mMapEdgeIdsToColour = new HashMap<Integer, BitSet>();
		mMapEdgeColoursToConnectedVertices = new HashMap<BitSet, Map<Integer, IntSet>>();
		
		//compute average distribution of vertex's and edge's colors
		mVertColoDist = AvrgVertColoDistMetric.apply(origGrphs);
		mEdgeColoDist = AvrgEdgeColoDistMetric.apply(origGrphs);
		
		//mEdgeColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist);
		
		mMapClassVertices = new HashMap<BitSet, Integer>();
		mReversedMapClassVertices = new HashMap<Integer, BitSet>();
		
		//mEdgeColoursThreshold = new ObjectDoubleOpenHashMap<BitSet>();
		
		// colour of rdf:type edge
		mRdfTypePropertyColour = mMimicGraph.getRDFTypePropertyColour();
		
		//estimate potential number of edges
		mIDesiredNoOfEdges = estimateNoEdges(origGrphs, mIDesiredNoOfVertices);
		
		//assign colors to vertices
		paintVertices();
		
		//initialize edge colour proposer
		mEdgeColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist, mSetOfRestrictedEdgeColours, seed);
		seed = mEdgeColoProposer.getSeed() + 1;
		
		//assign colors to edges
		paintEdges();
		
		//defining type of resource vertices
		connectVerticesWithRDFTypeEdges();
	}

	public Map<BitSet,IntSet> getMappingColoursAndVertices(){
		return mMapColourToVertexIDs;
	}
	
	public Map<BitSet, IntSet> getMappingColoursAndEdges(){
		return mMapColourToEdgeIDs;
	}
	
	public IColourMappingRules getColourMapper() {
		return mColourMapper;
	}

	protected BitSet getProposedEdgeColour(BitSet headColour, BitSet tailColour){
		if(mColourMapper != null){
			Random rand = new Random(seed);
			seed++;
			Set<BitSet> possEdgeColours = mColourMapper.getPossibleLinkingEdgeColours(tailColour, headColour );
			if(possEdgeColours !=null && !possEdgeColours.isEmpty()){
				
				BitSet[] arrEdgeColours = possEdgeColours.toArray(new BitSet[0]);
				return arrEdgeColours[rand.nextInt(arrEdgeColours.length)];
			}
		}else{
			LOGGER.error("Could not get offered edge' colours since the colour mapper is invalid");
		}
		return null;
	}
	
	protected BitSet getProposedHeadColour(BitSet edgeColour, BitSet tailColour){
		if(mColourMapper != null){
			Random rand = new Random(seed);
			seed++;
			Set<BitSet> possHeadColours = mColourMapper.getHeadColours(tailColour, edgeColour);
			if(possHeadColours !=null && !possHeadColours.isEmpty()){
				BitSet[] arrHeadColours = possHeadColours.toArray(new BitSet[0]);
				return arrHeadColours[rand.nextInt(arrHeadColours.length)];
			}
		}else{
			LOGGER.error("Could not get offered head' colours since the colour mapper is invalid");
		}
		return null;
	}
	
	protected BitSet getProposedTailColour(BitSet headColour, BitSet edgeColour){
		if(mColourMapper != null){
			Random rand = new Random(seed);
			seed++;
			Set<BitSet> possTailColours = mColourMapper.getTailColours(headColour, edgeColour);
			if(possTailColours !=null && !possTailColours.isEmpty()){
				BitSet[] arrTailColours = possTailColours.toArray(new BitSet[0]);
				return arrTailColours[rand.nextInt(arrTailColours.length)];
			}
		}else{
			LOGGER.error("Could not get offered tail' colours since the colour mapper is invalid");
		}
		return null;
	}
	
	/**
	 * get a proposed triple of tail, head and their connection via edge
	 */
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		BitSet[] arrVertexColours = setVertexColours.toArray(new BitSet[]{});
		
		while(true){
			// get a random tail colour
			BitSet tailColo = arrVertexColours[mRandom.nextInt(arrVertexColours.length)];
			
			// get associated edge colour based on the tail colour
			Set<BitSet> possOutEdgeColours = mColourMapper.getPossibleOutEdgeColours(tailColo);
			
			//remove rdf:type edges
			if(possOutEdgeColours == null){
				continue;
			}
			
			if(possOutEdgeColours.contains(mRdfTypePropertyColour)){
				possOutEdgeColours.remove(mRdfTypePropertyColour);
			}
			
			if(possOutEdgeColours.size() ==0){
				continue;
			}
			
			/*
			 * it is supposed that there always exist at least one edge colour used to connect
			 * the vertices in the tail colour to other vertices
			 */
			//if(possOutEdgeColours != null && possOutEdgeColours.size() > 0){
			BitSet[] arrEdgeColours = possOutEdgeColours.toArray(new BitSet[]{});
			
			// chose a random edge colour
			BitSet edgeColo = arrEdgeColours[mRandom.nextInt(arrEdgeColours.length)];
			
			// get a set of head colours associated with the edgeColo and the tailColo
			Set<BitSet> possHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
			
			if(possHeadColours.size() == 0){
				continue;
			}
			
			/*
			 * it is supposed that there always exist at least one head colour that can be
			 * connected with the tail colour via the edge colour
			 */
			//if(possHeadColours != null && possHeadColours.size() > 0){
			BitSet[] arrHeadColours = possHeadColours.toArray(new BitSet[]{});
			BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
			
			// get vertex's ids according to the vertex's colours
			if(mMapColourToVertexIDs.get(headColo) != null){
				int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
				int[] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
				
				int tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
				int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
				if(connectableVertices(tailId, headId, edgeColo)){
					// if the vertices can be connected via the edge colour => connect them
					TripleBaseSingleID triple = new TripleBaseSingleID();
					triple.tailId = tailId;
					triple.tailColour = tailColo;
					triple.headId = headId;
					triple.headColour = headColo;
					triple.edgeColour = edgeColo;
					
					//LOGGER.info("Proposed added triple: ("+triple.tailId +","+triple.headId +","+ edgeColo +")");
					
					return triple;	
				}
//				else{
//					System.err.println("There existing an edge in "+edgeColo+" connect the vertices");
//				}
			}
		}
	}
	
	public ColouredGraph getMimicGraph(){
		return mMimicGraph;
	}
	
	/**
	 * generate a new graph
	 * @return a new mimic graph
	 */
	public ColouredGraph generateGraph(){
		return mMimicGraph;
	}
	
	/**
	 * assign colours to vertices based on the average distribution of vertices
	 * per colour over all versions of a graph
	 */
	private void paintVertices(){
		LOGGER.info("Assign colors to vertices.");
		IOfferedItem<BitSet> colorProposer = new OfferedItemByRandomProb<BitSet>(mVertColoDist, seed);
		seed = colorProposer.getSeed() + 1;
		//IOfferedItem<BitSet> colorProposer = new OfferedItemByErrorScore<BitSet>(mVertColoDist);
		for(int i = 0 ; i< mIDesiredNoOfVertices ; i++){
			BitSet offeredColor = (BitSet) colorProposer.getPotentialItem();
				
			int vertId = mMimicGraph.addVertex(offeredColor);
			IntSet setVertIDs = mMapColourToVertexIDs.get(offeredColor);
			if(setVertIDs == null){
				setVertIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToVertexIDs.put(offeredColor, setVertIDs);
			}
			setVertIDs.add(vertId);
		}
		
		/*
		 *  get restricted edge's colours can exist along with these created vertex's colours
		 */
		Set<BitSet> setVertColours = mMapColourToVertexIDs.keySet();
		
		for(BitSet tailColo: setVertColours){
			for(BitSet headColo : setVertColours){
				Set<BitSet> lstPossEdgeColours = mColourMapper.getPossibleLinkingEdgeColours(tailColo, headColo);
				if(lstPossEdgeColours != null && lstPossEdgeColours.size() >0){
					for(BitSet edgeColo : lstPossEdgeColours){
						mSetOfRestrictedEdgeColours.add(edgeColo);
					}
				}
			}
		}
		
		/*
		 * get a threshold number of edges for each edge colours 
		 */
//		for(BitSet edgeColo: mSetOfRestrictedEdgeColours){
//			for(BitSet tailColo: setVertColours){
//				Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
//				IntSet setTailIds = mMapColourToVertexIDs.get(tailColo);
//				if(setHeadColours!= null && setHeadColours.size() > 0){
//					for(BitSet headColo : setHeadColours){
//						if(setVertColours.contains(headColo)){
//							IntSet setHeadIds = mMapColourToVertexIDs.get(headColo);
//							mEdgeColoursThreshold.putOrAdd(edgeColo, 0,	0);
//							mEdgeColoursThreshold.putOrAdd(edgeColo, setTailIds.size() * setHeadIds.size(), 
//									setTailIds.size() * setHeadIds.size());
//						}
//					}
//				}
//			}
//		}
	}
	
	/**
	 * assign colours to edges based on the average distribution of edges per
	 * colour over all versions of a graph
	 */
	private void paintEdges(){
		if(mNumberOfThreads <= 1 ){
			paintEdgesSingleThread();
		}else{
			//paintEdgesMultiThreads();
			paintEdgesMultiThreads();
		}
	}
	
	private void paintEdgesMultiThreads(){

		List<IntSet> lstAssignedEdges = getLstTransparenEdgesForPainting();
		
		/*
		 * assign edges to each thread and run
		 */
		
		ExecutorService service = Executors.newFixedThreadPool(mNumberOfThreads);
		
		LOGGER.info("Create "+lstAssignedEdges.size()+" threads for painting edges !");
		
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		final Set<BitSet> setOfRestrictedEdgeColours = new HashSet<BitSet>(mSetOfRestrictedEdgeColours);
		setOfRestrictedEdgeColours.remove(mRdfTypePropertyColour);
		
		if(setOfRestrictedEdgeColours.size() == 0){
			LOGGER.error("Cound not find any edge colour except rdf:type edges!");
			return;
		}
		
		
		//concurrent hash map shared between multi-threads
		//final Map<BitSet, IntSet> mapColouredEdgeIds = new ConcurrentHashMap<BitSet, IntSet>();
		final Map<BitSet, AtomicInteger> mapEdgeColourCounter = new HashMap<BitSet, AtomicInteger>();
		
		for(BitSet eColo: setOfRestrictedEdgeColours){
			mapEdgeColourCounter.put(eColo, new AtomicInteger());
		}
		
		for(int i = 0 ; i < lstAssignedEdges.size() ; i++){
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			final int indexOfThread  = i+1;
			
			final IOfferedItem<BitSet> eColoProposer = 
					new OfferedItemByRandomProb<>( new ObjectDistribution<BitSet>(mEdgeColoDist.sampleSpace, mEdgeColoDist.values), seed);
			seed = eColoProposer.getSeed() + 1;
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					//set of edges for painting
					int[] arrOfEdges = setOfEdges.toIntArray();
					
					//LOGGER.info("Thread " + indexOfThread +" is painting " + arrOfEdges.length +" edges with "
					//						+ setOfRestrictedEdgeColours.size()+" colours... ");
					int j = 0 ; 
					
					
					while(j < arrOfEdges.length){
						BitSet offeredColor = (BitSet) eColoProposer.getPotentialItem(setOfRestrictedEdgeColours, true);
						
						if(offeredColor == null){
							LOGGER.warn("Skip edge "+ arrOfEdges[j]);
							j++;
							continue;
						}
						
						/**
						 * not add edge with the offered color to the graph
						 * since we have to determine the head and tail for the connection
						 * ==> just track the edge's color
						 */
						AtomicInteger counter =  mapEdgeColourCounter.get(offeredColor);
						counter.incrementAndGet();
						j++;
					}
				}
			};
			tasks.add(Executors.callable(worker));
		}
		
		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
			LOGGER.info("All threads are finised --> Copy result back to map");
			
			/*
			 * copy back to global variable
			 */
			Set<BitSet> setEdgeColours = mapEdgeColourCounter.keySet();
			int fakeEdgeID = 0 ;
			for(BitSet eColo: setEdgeColours){
				int j= 0;
				AtomicInteger counter = mapEdgeColourCounter.get(eColo);
				while(j < counter.get()){
					mMapEdgeIdsToColour.put(fakeEdgeID, eColo);
					IntSet setEdges = mMapColourToEdgeIDs.get(eColo);
					if(setEdges == null ){
						setEdges = new DefaultIntSet(Constants.DEFAULT_SIZE);
						mMapColourToEdgeIDs.put(eColo, setEdges);
					}
					setEdges.add(fakeEdgeID	);
					j++;
					fakeEdgeID++;
				}
			}
			
		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor!");
			e.printStackTrace();
		}
	}
	
	
	private void paintEdgesWithConcurrentHashMap_NG(){
		
		List<IntSet> lstAssignedEdges = getLstTransparenEdgesForPainting();
		
		/*
		 * assign edges to each thread and run
		 */
		
		ExecutorService service = Executors.newFixedThreadPool(mNumberOfThreads);
		
		LOGGER.info("Create "+lstAssignedEdges.size()+" threads for painting edges....!");
		
		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
		final Set<BitSet> setOfRestrictedEdgeColours = new HashSet<BitSet>(mSetOfRestrictedEdgeColours);
		setOfRestrictedEdgeColours.remove(mRdfTypePropertyColour);
		
		if(setOfRestrictedEdgeColours.size() == 0 ){
			LOGGER.error("Could not find any other edge colour except rdf:type edge colour");
			return ;
		}
		
		//concurrent hash map shared between multi-threads
		
		for(BitSet eColo: setOfRestrictedEdgeColours){
			IntSet setEdges = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mMapColourToEdgeIDs.put(eColo, setEdges);
		}
		
		for(int i = 0 ; i < lstAssignedEdges.size() ; i++){
			final IntSet setOfEdges = lstAssignedEdges.get(i);
			final int indexOfThread  = i+1;
			
			final IOfferedItem<BitSet> eColoProposer = 
					new OfferedItemByRandomProb<>( new ObjectDistribution<BitSet>(mEdgeColoDist.sampleSpace, mEdgeColoDist.values), seed);
			seed = eColoProposer.getSeed() + 1;
			//final ObjectDoubleOpenHashMap<BitSet> tmpEdgeThreshold = mEdgeColoursThreshold.clone();
			
			
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					//set of edges for painting
					int[] arrOfEdges = setOfEdges.toIntArray();
					
					//LOGGER.info("Thread " + indexOfThread +" is painting " + arrOfEdges.length +" edges with "
					//						+ setOfRestrictedEdgeColours.size()+" colours... ");
					int j = 0 ; 
					while(j < arrOfEdges.length){
						BitSet offeredColor = (BitSet) eColoProposer.getPotentialItem(setOfRestrictedEdgeColours, true);
						
						if(offeredColor == null){
							LOGGER.error("Skip edge "+ arrOfEdges[j] +" because of null colour!");
							j++;
							continue;
						}
						/**
						 * not add edge with the offered color to the graph
						 * since we have to determine the head and tail for the connection
						 * ==> just track the edge's color
						 */
						
						IntSet setEdgeIDs = mMapColourToEdgeIDs.get(offeredColor);
						setEdgeIDs.add(arrOfEdges[j]);
						//LOGGER.warn("Thread- "+indexOfThread+" painted edge "+ arrOfEdges[j] + "("+j+"/"+arrOfEdges.length+")");
						j++;
					}
				}
			};
			tasks.add(Executors.callable(worker));
		}
		
		try {
			service.invokeAll(tasks);
			service.shutdown();
			service.awaitTermination(48, TimeUnit.HOURS);
			LOGGER.info("All threads are finised --> Copy result back to map");
			
			/*
			 * copy back to global variable
			 */
			Set<BitSet> setEdgeColours = mMapColourToEdgeIDs.keySet();
			for(BitSet eColo: setEdgeColours){
				IntSet setEdgeIds = mMapColourToEdgeIDs.get(eColo);
				if(setEdgeIds!=null && setEdgeIds.size() > 0){
					for(int eId: mMapColourToEdgeIDs.get(eColo)) {
						mMapEdgeIdsToColour.put(eId,eColo);
					}
				}else{
					mMapColourToEdgeIDs.remove(eColo);
					LOGGER.warn("Remove " + eColo +" edge colour, since there is no edges having this colour!");
				}
			}
			
		} catch (InterruptedException e) {
			LOGGER.error("Could not shutdown the service executor!");
			e.printStackTrace();
		}
	}
	
//	
	
	private void paintEdgesSingleThread(){
		LOGGER.info("Assign colors to edges.");
		//IOfferedItem<BitSet> colorProposer = new OfferedItemByErrorScore<BitSet>(mEdgeColoDist);
		
		/*
		 * calculate number of [rdf:type] edges first. these edges will be used to define 
		 * classes of resources in vertices.
		 */
		int iNumberOfRdfTypeEdges = 0 ;
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		for(BitSet vColo: setVertexColours ){
			Set<BitSet> definedColours = mMimicGraph.getClassColour(vColo);
			IntSet setOfVertices = mMapColourToVertexIDs.get(vColo);
			if(definedColours!= null){
				iNumberOfRdfTypeEdges += definedColours.size() * setOfVertices.size();
			}
		}
		LOGGER.info("There are "+ iNumberOfRdfTypeEdges + " edges of rdf:type!");
		
		// fake edge's id
		/*
		 * 	process normal edges
		 */
			
		int iNumberOfOtherEdges = mIDesiredNoOfEdges - iNumberOfRdfTypeEdges;
		LOGGER.info("Assigning colours to "+iNumberOfOtherEdges + " .......");
		
		Set<BitSet> setOfRestrictedEdgeColours = new HashSet<BitSet>(mSetOfRestrictedEdgeColours);
		setOfRestrictedEdgeColours.remove(mRdfTypePropertyColour);
		for(int i = 0 ; i< iNumberOfOtherEdges ; ){
			BitSet offeredColor = (BitSet) mEdgeColoProposer.getPotentialItem(setOfRestrictedEdgeColours, true);
			
			if(offeredColor == null){
				continue;
			}
			
			/**
			 * not add edge with the offered color to the graph
			 * since we have to determine the head and tail for the connection
			 * ==> just track the edge's color
			 */
			
			IntSet setEdgeIDs = mMapColourToEdgeIDs.get(offeredColor);
			if(setEdgeIDs == null){
				setEdgeIDs = new DefaultIntSet(Constants.DEFAULT_SIZE);
				mMapColourToEdgeIDs.put(offeredColor, setEdgeIDs);
			}
				
			setEdgeIDs.add(i);
			mMapEdgeIdsToColour.put(i,offeredColor);
			i++;
			
		}
		LOGGER.info("DONE assigning colours to "+iNumberOfOtherEdges + "!");
	}
	
	/**
	 * connection typed resource vertices to its class with edge of rdf:type
	 * if a vertex has a colour, then it connect to some vertices with rdf:type edges.
	 * the number of connected heads is dependent on the number of colour the target has
	 */
	private void connectVerticesWithRDFTypeEdges(){
		
		/*
		 * filter colour and empty colour vertices
		 */
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		IntSet colourVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
		IntSet emptyColourVertices = new DefaultIntSet(Constants.DEFAULT_SIZE);
		for(BitSet vColo: setVertexColours){
			IntSet setVertices = mMapColourToVertexIDs.get(vColo);
			
			if(vColo.isEmpty()){
				//get vertices with empty colours
				emptyColourVertices.addAll(setVertices);
			}else{
				//get vertices with non-empty colour
				colourVertices.addAll(setVertices);
			}
		}
		
		int[] arrColourVertices = colourVertices.toIntArray();
		int[] arrEmptyColourVertices = emptyColourVertices.toIntArray(); 
		Set<Integer> trackedClassVertices = new HashSet<Integer>();
		//traverse through all coloured vertices and add classes to them
		for(int vId : arrColourVertices){
			BitSet vColo = mMimicGraph.getVertexColour(vId);
			Set<BitSet> setClassColours = mMimicGraph.getClassColour(vColo);
			
			for(BitSet classColo: setClassColours){					
				if(!mMapClassVertices.containsKey(classColo)){
					int hId = arrEmptyColourVertices[mRandom.nextInt(arrEmptyColourVertices.length)];
					
					while(trackedClassVertices.contains(hId) 
							&& trackedClassVertices.size() < arrEmptyColourVertices.length){
						hId = arrEmptyColourVertices[mRandom.nextInt(arrEmptyColourVertices.length)];
					}
					if(trackedClassVertices.size() <= arrEmptyColourVertices.length){
						trackedClassVertices.add(hId);
						mMapClassVertices.put(classColo, hId);
						mReversedMapClassVertices.put(hId, classColo);
						//connect the vId and hId using the edge rdf:type
						mMimicGraph.addEdge(vId, hId, mRdfTypePropertyColour);
					}else{
						LOGGER.warn("Cannot find any empty colour head for consideration as a class of resources!");
					}
				}else{
					int hId = mMapClassVertices.get(classColo);
					//connect the vId and hId using the edge rdf:type
					mMimicGraph.addEdge(vId, hId, mRdfTypePropertyColour);
				}
			}
		}
	}
	
	
	/**
	 * compute average distribution of vertex's and edge's colors
	 * 
	 * @param origGrphs the array of all versions of a graph
	 */
	
//	/**
//	 * just for testing the correctness of the constructor process
//	 */
//	private void printSimpleInfo(){
//		Set<BitSet> keyVertColo = mMapColourToVertexIDs.keySet();
//		int totalVertices = 0 ;
//		for(BitSet vertColo: keyVertColo){
//			IntSet setVertIDs = mMapColourToVertexIDs.get(vertColo);
//			totalVertices += setVertIDs.size();
//		}
//		
//		System.out.println("Number of painted vertices: " + totalVertices + " in total " + keyVertColo.size() +" colors");
//		
//		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
//		int totalEdges = 0 ;
//		for(BitSet edgeColo: keyEdgeColo){
//			IntSet setEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
//			totalEdges += setEdgeIDs.size();
//		}
//		
//		System.out.println("Number of painted edges: " + totalEdges + " in total " + keyEdgeColo.size() +" colors");
//	}
	
	
	public synchronized boolean connectIfPossible(int tailId, int headId, BitSet eColo) {
		if(connectableVertices(tailId, headId, eColo)) {
			// connect
			mMimicGraph.addEdge(tailId, headId, eColo);
			return true;
		}
		return false;
	}
	
	public boolean connectableVertices(int tailId, int headId, BitSet eColo){
		
		if(mReversedMapClassVertices.containsKey(headId)){
			return false;
		}
		boolean canConnect = false;
		
		Map<Integer, IntSet> mapTailToHeads = mMapEdgeColoursToConnectedVertices.get(eColo);
		if(mapTailToHeads == null){
			mapTailToHeads = new HashMap<Integer, IntSet>();
			mMapEdgeColoursToConnectedVertices.put(eColo, mapTailToHeads);
		}
		
		IntSet setOfHeads = mapTailToHeads.get(tailId);
		if(setOfHeads == null){
			setOfHeads = new DefaultIntSet(Constants.DEFAULT_SIZE);
			mapTailToHeads.put(tailId, setOfHeads);
		}
		
		if(!setOfHeads.contains(headId)){
			setOfHeads.add(headId);
			canConnect = true;
		}
		
		return canConnect;
	}
	
	public IntSet getConnectedHeads(int tailId, BitSet eColo){
		
		IntSet setOfHeads = new DefaultIntSet(Constants.DEFAULT_SIZE);
		Map<Integer, IntSet> mapTailToHeads = mMapEdgeColoursToConnectedVertices.get(eColo);
		if(mapTailToHeads != null && mapTailToHeads.containsKey(tailId)){
			setOfHeads = mapTailToHeads.get(tailId);
		}
		
		return setOfHeads;
	}
	
	public String getLiteralType(BitSet dteColo){
		return mMimicGraph.getLiteralType(dteColo);
	}
	
	public void setMimicGraph(ColouredGraph refinedGraph){
		mMimicGraph = refinedGraph;
	}
	
	public int getDefaultNoOfThreads(){
		return Runtime.getRuntime().availableProcessors() * 4;
	}
	
	public List<IntSet> getColouredEdgesForConnecting(int numberOfThreads){
		List<IntSet> lstAssingedEdges = new ArrayList<IntSet>();
		
		int iNoOfEdges = mMapEdgeIdsToColour.size();
		
		int iNoOfEdgesPerThread = 0;
		int iNoOfSpareEdges = 0;
		if(numberOfThreads == 1){
			iNoOfEdgesPerThread = iNoOfEdges;
		}else if(numberOfThreads > 1){
			if(iNoOfEdges % numberOfThreads == 0 ){
				iNoOfEdgesPerThread = iNoOfEdges/numberOfThreads;
			}else{
				iNoOfEdgesPerThread = iNoOfEdges/(numberOfThreads-1);
				iNoOfSpareEdges = iNoOfEdges - ( iNoOfEdgesPerThread * (numberOfThreads-1));
			}
		}
		
		if(iNoOfSpareEdges == 0 ){
			for(int i = 0 ; i< numberOfThreads ; i++){
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssingedEdges.add(tmpSetEdges);
			}
		}else{
			for(int i = 0 ; i< numberOfThreads -1 ; i++){
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssingedEdges.add(tmpSetEdges);
			}
			
			//add remaining edges
			int iEdgeId = (numberOfThreads -1) *  iNoOfEdgesPerThread;
			IntSet spareSetEdges = new DefaultIntSet(iNoOfEdges-iEdgeId);
			spareSetEdges.add(iEdgeId);
			while(iEdgeId < iNoOfEdges){
				iEdgeId ++;
				spareSetEdges.add(iEdgeId);
			}
			lstAssingedEdges.add(spareSetEdges);
		}
		
		return lstAssingedEdges;
	}
	
	protected BitSet getEdgeColour(int fakeEdgeId){
		return mMapEdgeIdsToColour.get(fakeEdgeId);
	}
	
	public void setNumberOfThreadsForGenerationProcess(int iNumberOfThreads){
		int iAvailableThreads = getDefaultNoOfThreads();
		
		if(iNumberOfThreads == 0 || iNumberOfThreads == 1){
			mNumberOfThreads = 1;
		}else if(iNumberOfThreads <= -1){
			mNumberOfThreads = iAvailableThreads;
		}else{
			mNumberOfThreads = iNumberOfThreads;
		}
		
		if(mNumberOfThreads > iAvailableThreads){
			mNumberOfThreads = iAvailableThreads;
		}
	}
	
	private List<IntSet> getLstTransparenEdgesForPainting(){
		/*
		 * calculate number of [rdf:type] edges first. these edges will be used to define 
		 * classes of resources in vertices.
		 */
		int iNumberOfRdfTypeEdges = 0 ;
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		for(BitSet vColo: setVertexColours ){
			Set<BitSet> definedColours = mMimicGraph.getClassColour(vColo);
			IntSet setOfVertices = mMapColourToVertexIDs.get(vColo);
			if(definedColours!= null){
				iNumberOfRdfTypeEdges += definedColours.size() * setOfVertices.size();
			}
		}
		
		LOGGER.info("There are "+ iNumberOfRdfTypeEdges + " edges of rdf:type!");
		
		/*
		 * 	process normal edges
		 */
			
		int iNumberOfOtherEdges = mIDesiredNoOfEdges - iNumberOfRdfTypeEdges;
		LOGGER.info("Assigning colours to "+iNumberOfOtherEdges + " .......");
		
		int iNoOfEdgesPerThread = 0;
		int iNoOfSpareEdges = 0;
		if(iNumberOfOtherEdges % mNumberOfThreads == 0 ){
			iNoOfEdgesPerThread = iNumberOfOtherEdges/mNumberOfThreads;
		}else{
			iNoOfEdgesPerThread = iNumberOfOtherEdges/(mNumberOfThreads-1);
			iNoOfSpareEdges = iNumberOfOtherEdges - ( iNoOfEdgesPerThread * (mNumberOfThreads-1));
		}
		
		List<IntSet> lstAssignedEdges = new ArrayList<IntSet>();
		
		if(iNoOfSpareEdges == 0 ){
			for(int i = 0 ; i< mNumberOfThreads ; i++){
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssignedEdges.add(tmpSetEdges);
			}
		}else{
			for(int i = 0 ; i< mNumberOfThreads -1 ; i++){
				IntSet tmpSetEdges = new DefaultIntSet(iNoOfEdgesPerThread);
				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
					tmpSetEdges.add(iEdgeId);
				}
				lstAssignedEdges.add(tmpSetEdges);
			}
			
			//add remaining edges
			int iEdgeId = (mNumberOfThreads -1) *  iNoOfEdgesPerThread;
			IntSet spareSetEdges = new DefaultIntSet(iNumberOfOtherEdges-iEdgeId);
			spareSetEdges.add(iEdgeId);
			while(iEdgeId < iNumberOfOtherEdges){
				iEdgeId ++;
				spareSetEdges.add(iEdgeId);
			}
			lstAssignedEdges.add(spareSetEdges);
		}
		
		return lstAssignedEdges;
	}

	/**
	 * @return the seed
	 */
	public long getSeed() {
		return seed;
	}
	
	
//	private void paintEdgesMultiThreads_orig(){
//		
//		/*
//		 * calculate number of [rdf:type] edges first. these edges will be used to define 
//		 * classes of resources in vertices.
//		 */
//		int iNumberOfRdfTypeEdges = 0 ;
//		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
//		for(BitSet vColo: setVertexColours ){
//			Set<BitSet> definedColours = mMimicGraph.getClassColour(vColo);
//			IntSet setOfVertices = mMapColourToVertexIDs.get(vColo);
//			if(definedColours!= null){
//				iNumberOfRdfTypeEdges += definedColours.size() * setOfVertices.size();
//			}
//		}
//		LOGGER.info("There are "+ iNumberOfRdfTypeEdges + " edges of rdf:type!");
//		
//		// fake edge's id
//		/*
//		 * 	process normal edges
//		 */
//			
//		int iNumberOfOtherEdges = mIDesiredNoOfEdges - iNumberOfRdfTypeEdges;
//		LOGGER.info("Assigning colours to "+iNumberOfOtherEdges + " .......");
//		
//		int iNoOfEdgesPerThread = 0;
//		int iNoOfSpareEdges = 0;
//		if(iNumberOfOtherEdges % mNumberOfThreads == 0 ){
//			iNoOfEdgesPerThread = iNumberOfOtherEdges/mNumberOfThreads;
//		}else{
//			iNoOfEdgesPerThread = iNumberOfOtherEdges/(mNumberOfThreads-1);
//			iNoOfSpareEdges = iNumberOfOtherEdges - ( iNoOfEdgesPerThread * (mNumberOfThreads-1));
//		}
//		
//		List<IntSet> lstAssignedEdges = new ArrayList<IntSet>();
//		
//		if(iNoOfSpareEdges == 0 ){
//			for(int i = 0 ; i< mNumberOfThreads ; i++){
//				IntSet tmpSetEdges = new DefaultIntSet();
//				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
//					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
//					tmpSetEdges.add(iEdgeId);
//				}
//				lstAssignedEdges.add(tmpSetEdges);
//			}
//		}else{
//			for(int i = 0 ; i< mNumberOfThreads -1 ; i++){
//				IntSet tmpSetEdges = new DefaultIntSet();
//				for(int j = 0 ; j < iNoOfEdgesPerThread ; j++){
//					int iEdgeId = (i*iNoOfEdgesPerThread) + j;
//					tmpSetEdges.add(iEdgeId);
//				}
//				lstAssignedEdges.add(tmpSetEdges);
//			}
//			IntSet spareSetEdges = new DefaultIntSet();
//			//add remaining edges
//			int iEdgeId = (mNumberOfThreads -1) *  iNoOfEdgesPerThread;
//			spareSetEdges.add(iEdgeId);
//			while(iEdgeId < iNumberOfOtherEdges){
//				iEdgeId ++;
//				spareSetEdges.add(iEdgeId);
//			}
//			lstAssignedEdges.add(spareSetEdges);
//		}
//		
//		//assign edges to each thread and run
//		
//		ExecutorService service = Executors.newFixedThreadPool(mNumberOfThreads);
//		
//		LOGGER.info("Create "+lstAssignedEdges.size()+" threads for painting edges !");
//		
//		List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
//		final Set<BitSet> setOfRestrictedEdgeColours = new HashSet<BitSet>(mSetOfRestrictedEdgeColours);
//		setOfRestrictedEdgeColours.remove(mRdfTypePropertyColour);
//		
//		for(int i = 0 ; i < lstAssignedEdges.size() ; i++){
//			final IntSet setOfEdges = lstAssignedEdges.get(i);
//			final int indexOfThread  = i+1;
//			
//			final IOfferedItem<BitSet> eColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist);
//			
//			Runnable worker = new Runnable() {
//				@Override
//				public void run() {
//					//set of edges for painting
//					int[] arrOfEdges = setOfEdges.toIntArray();
//					
//					LOGGER.info("Thread " + indexOfThread +" is painting " + arrOfEdges.length +" edges with "
//											+ setOfRestrictedEdgeColours.size()+" colours... ");
//					int j = 0 ; 
//					while(j < arrOfEdges.length){
//						BitSet offeredColor = (BitSet) eColoProposer.getPotentialItem(setOfRestrictedEdgeColours);
//						
//						/**
//						 * not add edge with the offered color to the graph
//						 * since we have to determine the head and tail for the connection
//						 * ==> just track the edge's color
//						 */
//						
//						synchronized(mMapColourToEdgeIDs){
//							IntSet setEdgeIDs = mMapColourToEdgeIDs.get(offeredColor);
//							if(setEdgeIDs == null){
//								setEdgeIDs = new DefaultIntSet();
//								mMapColourToEdgeIDs.put(offeredColor, setEdgeIDs);
//							}
//							
//							if(mEdgeColoursThreshold.containsKey(offeredColor) &&  
//									setEdgeIDs.size() < mEdgeColoursThreshold.get(offeredColor)){
//								
//								setEdgeIDs.add(arrOfEdges[j]);
//								mTmpColoureNormalEdges.put(arrOfEdges[j],offeredColor);
//								j++;
//							}
//						}
//					}
//					
//				}
//			};
//			tasks.add(Executors.callable(worker));
//		}
//		
//		try {
//			service.invokeAll(tasks);
//			service.shutdown();
//			service.awaitTermination(48, TimeUnit.HOURS);
//		} catch (InterruptedException e) {
//			LOGGER.error("Could not shutdown the service executor!");
//			e.printStackTrace();
//		}
//	}
	
}

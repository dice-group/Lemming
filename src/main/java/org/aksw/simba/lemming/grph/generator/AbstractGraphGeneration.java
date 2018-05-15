package org.aksw.simba.lemming.grph.generator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.IOfferedItem;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.metrics.dist.AvrgEdgeColoDistMetric;
import org.aksw.simba.lemming.metrics.dist.AvrgVertColoDistMetric;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.rules.ColourMappingRules;
import org.aksw.simba.lemming.rules.IColourMappingRules;
import org.aksw.simba.lemming.rules.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public abstract class AbstractGraphGeneration {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGraphGeneration.class);
	
	protected int mIDesiredNoOfVertices = 0;
	protected int mIDesiredNoOfEdges = 0;
	
	protected IOfferedItem<BitSet> mEdgeColoProposer;
	
	protected Random mRandom ;	
	/*
	 * the keys are the vertex's color and the values are the set of vertex's ids
	 */
	protected Map<BitSet, IntSet> mMapColourToVertexIDs = new HashMap<BitSet, IntSet>();
	/*
	 * the keys are the edge's color and the values are the set of edge's ids
	 * (note: fake id)
	 */
	protected Map<BitSet, IntSet> mMapColourToEdgeIDs = new HashMap<BitSet, IntSet>();
	
	protected ObjectDistribution<BitSet> mVertColoDist;
	protected ObjectDistribution<BitSet> mEdgeColoDist;
	
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
	
	protected ColouredGraph mMimicGraph;
	
	protected IColourMappingRules mColourMapper;
	
	private HashSet<BitSet> mSetOfRestrictedEdgeColours = new HashSet<BitSet>();
	
	public AbstractGraphGeneration(int iNumberOfVertices, ColouredGraph[] origGrphs){
		mIDesiredNoOfVertices = iNumberOfVertices;
		
		mMimicGraph = new ColouredGraph(); 
		
		// initilize variable
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		
		mColourMapper = new ColourMappingRules();
		mColourMapper.analyzeRules(origGrphs);
		
		mRandom = new Random();
		
		//estimate potential number of edges
		estimateNoEdges(origGrphs);
		
		//compute single distribution 
		computeAvrgEVColorDist(origGrphs);
		
		mEdgeColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist);
		
		//assign colors to vertices
		assignColorToVertices();
		
		//assign colors to edges
		assignColorToEdges();
		
		mEdgeColoProposer = new OfferedItemByRandomProb<>(mEdgeColoDist, mSetOfRestrictedEdgeColours);
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
			Random rand = new Random();
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
			Random rand = new Random();
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
			Random rand = new Random();
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
	
	public TripleBaseSingleID getProposedTriple(boolean isRandom){
		
		Set<BitSet> setVertexColours = mMapColourToVertexIDs.keySet();
		BitSet[] arrVertexColours = setVertexColours.toArray(new BitSet[]{});
		
		while(true){
			// tail colour
			BitSet tailColo = arrVertexColours[mRandom.nextInt(arrVertexColours.length)];
			
			Set<BitSet> possOutEdgeColours = mColourMapper.getPossibleOutEdgeColours(tailColo);
			
			if(possOutEdgeColours != null && possOutEdgeColours.size() > 0){
				BitSet[] arrEdgeColours = possOutEdgeColours.toArray(new BitSet[]{});
				
				BitSet edgeColo = arrEdgeColours[mRandom.nextInt(arrEdgeColours.length)];
				
				Set<BitSet> possHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
				if(possHeadColours != null && possHeadColours.size() > 0){
					BitSet[] arrHeadColours = possHeadColours.toArray(new BitSet[]{});
					BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
					
					// get vertex's ids according to the vertex's colours
					if(mMapColourToVertexIDs.get(tailColo) != null && mMapColourToVertexIDs.get(headColo) != null){
						int[] arrTailIDs = mMapColourToVertexIDs.get(tailColo).toIntArray();
						int[] arrHeadIDs = mMapColourToVertexIDs.get(headColo).toIntArray();
						
						int tailId = arrTailIDs[mRandom.nextInt(arrTailIDs.length)];
						int headId = arrHeadIDs[mRandom.nextInt(arrHeadIDs.length)];
						
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
	
	
	public ColouredGraph getMimicGraph(){
		return mMimicGraph;
	}
	
	/**
	 * generate a new graph
	 * @return a new mimic graph
	 */
	public ColouredGraph generateGraph(){
		return null;
	}
	
	/**
	 * assign colours to vertices based on the average distribution of vertices
	 * per colour over all versions of a graph
	 */
	private void assignColorToVertices(){
		LOGGER.info("Assign colors to vertices.");
		IOfferedItem<BitSet> colorProposer = new OfferedItemByRandomProb<BitSet>(mVertColoDist);
		//IOfferedItem<BitSet> colorProposer = new OfferedItemByErrorScore<BitSet>(mVertColoDist);
		for(int i = 0 ; i< mIDesiredNoOfVertices ; i++){
			BitSet offeredColor = (BitSet) colorProposer.getPotentialItem();
			int vertId = mMimicGraph.addVertex(offeredColor);
			IntSet setVertIDs = mMapColourToVertexIDs.get(offeredColor);
			if(setVertIDs == null){
				setVertIDs = new DefaultIntSet();
				mMapColourToVertexIDs.put(offeredColor, setVertIDs);
			}
			setVertIDs.add(vertId);
		}
		
		
		// get restricted edge's colours can exist along with these created vertex's colours
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
	}
	
	/**
	 * assign colours to edges based on the average distribution of edges per
	 * colour over all versions of a graph
	 */
	private void assignColorToEdges(){
		LOGGER.info("Assign colors to edges.");
		//IOfferedItem<BitSet> colorProposer = new OfferedItemByErrorScore<BitSet>(mEdgeColoDist);
		
		for(int i = 0 ; i< mIDesiredNoOfEdges ; i++){
			BitSet offeredColor = (BitSet) mEdgeColoProposer.getPotentialItem();
			
			if (!mSetOfRestrictedEdgeColours.contains(offeredColor)) {
				System.err.println("This edge colour: "
								+ offeredColor
								+ " won't be considered in the graph generation (since there is not approriate tail's colours and head's colours to connect)");
				i--;
				continue;
			}
			
			/**
			 * not add edge with the offered color to the graph
			 * since we have to determine the head and tail for the connection
			 * ==> just track the edge's color
			 */
			
			IntSet setEdgeIDs = mMapColourToEdgeIDs.get(offeredColor);
			if(setEdgeIDs == null){
				setEdgeIDs = new DefaultIntSet();
				mMapColourToEdgeIDs.put(offeredColor, setEdgeIDs);
			}
			// fake edge's id
			setEdgeIDs.add(i);
		}
	}
	
	/**
	 * draft estimation of number edges
	 * @param origGrphs
	 */
	private void estimateNoEdges(ColouredGraph[] origGrphs){
		LOGGER.info("Estimate the number of edges in the new graph.");
		if(origGrphs != null && origGrphs.length >0){
			int iNoOfVersions = origGrphs.length;
			double noEdges = 0;
			for(ColouredGraph graph: origGrphs){
				int iNoEdges = graph.getEdges().size();
				int iNoVertices = graph.getVertices().size();
				noEdges += iNoEdges/(iNoVertices *1.0); 
			}
			noEdges *= mIDesiredNoOfVertices;
			noEdges /= iNoOfVersions;
			mIDesiredNoOfEdges = (int)Math.round(noEdges);
			LOGGER.warn("Estimated the number of edges in the new graph is " + mIDesiredNoOfEdges);
		}else{
			LOGGER.warn("The array of original graphs is empty!");
		}
	}
	
	/**
	 * compute average distribution of vertex's and edge's colors
	 * 
	 * @param origGrphs the array of all versions of a graph
	 */
	private void computeAvrgEVColorDist(ColouredGraph[] origGrphs){
		LOGGER.info("Compute the average distribution for vertex's color and edge's color.");
		mVertColoDist = AvrgVertColoDistMetric.apply(origGrphs);
		mEdgeColoDist = AvrgEdgeColoDistMetric.apply(origGrphs);
	}
	
	/**
	 * just for testing the correctness of the constructor process
	 */
	private void printSimpleInfo(){
		Set<BitSet> keyVertColo = mMapColourToVertexIDs.keySet();
		int totalVertices = 0 ;
		for(BitSet vertColo: keyVertColo){
			IntSet setVertIDs = mMapColourToVertexIDs.get(vertColo);
			totalVertices += setVertIDs.size();
		}
		
		System.out.println("Number of painted vertices: " + totalVertices + " in total " + keyVertColo.size() +" colors");
		
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		int totalEdges = 0 ;
		for(BitSet edgeColo: keyEdgeColo){
			IntSet setEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
			totalEdges += setEdgeIDs.size();
		}
		
		System.out.println("Number of painted edges: " + totalEdges + " in total " + keyEdgeColo.size() +" colors");
	}
}

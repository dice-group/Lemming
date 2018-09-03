package org.aksw.simba.lemming.mimicgraph.generator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class GraphGenerationRandomly2 extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationRandomly2.class);
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
	
	
	public GraphGenerationRandomly2(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		maxIterationFor1EdgeColo = Constants.MAX_ITERATION_FOR_1_COLOUR;;
		
		// initilize variable
		mapPossibleIDegreePerIEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		mapPossibleODegreePerOEColo = new ObjectObjectOpenHashMap<BitSet, ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>>>();
		
		// extend step compared to the class GraphGenerationSimpleApproach
		computePotentialIODegreePerVert(origGrphs);
	}

	public ColouredGraph generateGraph(){
		
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
							
							IntSet setHeadIDs = new DefaultIntSet();
							if(mMapColourToVertexIDs.containsKey(headColo)){
								setHeadIDs = mMapColourToVertexIDs.get(headColo).clone();
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
							
							Set<Integer> setFilteredHeadIDs = new HashSet<Integer>(setHeadIDs.toIntegerArrayList());
							
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
		return mMimicGraph;
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
						LOGGER.error("Something is seriously happening for head colours, since " + headColo +" can not have more than 2 proposers");
					}
				}
			}
		}
	}
	
}

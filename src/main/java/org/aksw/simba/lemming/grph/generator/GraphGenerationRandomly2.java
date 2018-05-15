package org.aksw.simba.lemming.grph.generator;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.IOfferedItem;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.dist.utils.PoissonDistribution;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgInDegreeDistBaseVEColo;
import org.aksw.simba.lemming.metrics.dist.multi.AvrgOutDegreeDistBaseVEColo;
import org.aksw.simba.lemming.rules.TripleBaseSingleID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectObjectOpenHashMap;

public class GraphGenerationRandomly2 extends AbstractGraphGeneration implements IGraphGeneration{

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationRandomly2.class);
	private int maxIterationFor1EdgeColo ;
	
	public GraphGenerationRandomly2(int iNumberOfVertices,
			ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		maxIterationFor1EdgeColo = 10000;
		
		// extend step compared to the class GraphGenerationSimpleApproach
		computePotentialIODegreePerVert(origGrphs);
	}

	public ColouredGraph generateGraph(){
		
		Set<BitSet> keyEdgeColo = mMapColourToEdgeIDs.keySet();
		for(BitSet edgeColo : keyEdgeColo){
			
			Set<BitSet> setTailColours = mColourMapper.getTailColoursFromEdgeColour(edgeColo);
			BitSet[] arrTailColours = setTailColours.toArray(new BitSet[0]);
			
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapHeadColoToIDProposer = mapPossibleIDegreePerIEColo.get(edgeColo);
			ObjectObjectOpenHashMap<BitSet, IOfferedItem<Integer>> mapTailColoToIDProposer = mapPossibleODegreePerOEColo.get(edgeColo);
			
			if(mapHeadColoToIDProposer != null && mapTailColoToIDProposer != null){
				
				/* the setFakeEdgeIDs helps us to know how many edges existing
				 * in a specific edge's colour*/ 
				IntSet setFakeEdgeIDs = mMapColourToEdgeIDs.get(edgeColo);
				// use each edge to connect vertices
				for(int i = 0 ; i < setFakeEdgeIDs.size() ; i++){
					
					boolean isFoundVerticesConnected = false;
					
					BitSet tailColo = arrTailColours[mRandom.nextInt(arrTailColours.length)];	
					Set<BitSet> setHeadColours = mColourMapper.getHeadColours(tailColo, edgeColo);
					
					if(setHeadColours.size() > 0){
						BitSet [] arrHeadColours = setHeadColours.toArray(new BitSet[0]);
						BitSet headColo = arrHeadColours[mRandom.nextInt(arrHeadColours.length)];
						
						IOfferedItem<Integer> tailIDProposer = mapTailColoToIDProposer.get(tailColo);
						IOfferedItem<Integer> headIDProposer = mapHeadColoToIDProposer.get(headColo);
								
						
						if(tailIDProposer!= null && headIDProposer!=null){
							
							int tailId = tailIDProposer.getPotentialItem();
							int headId = headIDProposer.getPotentialItem();
							
							mMimicGraph.addEdge(tailId, headId, edgeColo);
							isFoundVerticesConnected = true;
						}else{
							System.err.println("Could not find any vertices with the tail's or head's colours!");
							LOGGER.warn("Could not find any vertices with the tail's or head's colours!");
						}
					}
					
					if (!isFoundVerticesConnected) {
						i--;
						maxIterationFor1EdgeColo--;
						if (maxIterationFor1EdgeColo == 0) {
							LOGGER.warn("Could not create "
									+ setFakeEdgeIDs.size()
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							
							System.err.println("Could not create "
									+ setFakeEdgeIDs.size()
									+ " edges in the "
									+ edgeColo
									+ " colour since it could not find any approriate tail and head to connect.");
							break;
						}
					}
				}
			}else{
				LOGGER.warn("Could not consider the"
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
			System.out.println("using override function getProposedTriple(");
			
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
			
		}else{
			System.out.println("using base function getProposedTriple(");
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

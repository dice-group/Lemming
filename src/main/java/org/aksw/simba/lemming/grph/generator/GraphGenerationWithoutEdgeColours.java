package org.aksw.simba.lemming.grph.generator;

import java.util.Random;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.dist.utils.OfferedItemByRandomProb;
import org.aksw.simba.lemming.dist.utils.PoissonDistribution;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.metrics.single.MaxVertexInDegreeMetric;
import org.aksw.simba.lemming.metrics.single.MaxVertexOutDegreeMetric;
import org.aksw.simba.lemming.util.MapUtil;

import toools.set.DefaultIntSet;
import toools.set.IntSet;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectDoubleOpenHashMap;

public class GraphGenerationWithoutEdgeColours extends AbstractGraphGeneration implements
		IGraphGeneration {
	
	private double mAvrgMaxOutDegree = 0 ;
	private double mAvrgMaxInDegree = 0 ;
	ObjectDoubleOpenHashMap<Integer> mapVertexPotentialOutDegree;
	ObjectDoubleOpenHashMap<Integer> mapVertexPotentialInDegree;
	
	
	public GraphGenerationWithoutEdgeColours(int iNumberOfVertices, ColouredGraph[] origGrphs) {
		super(iNumberOfVertices, origGrphs);
		
		mapVertexPotentialOutDegree = new ObjectDoubleOpenHashMap<Integer>();
		mapVertexPotentialInDegree = new ObjectDoubleOpenHashMap<Integer>();
		
		computeAvrgInOutDegree(origGrphs);
		computePotentialDegreeForEachVertex();
	}
	
	private void computeAvrgInOutDegree(ColouredGraph[] origGrphs){
		MaxVertexOutDegreeMetric maxOutDegreeMetric = new MaxVertexOutDegreeMetric();
		
		double avrgOutDegree = 0;
		double avrgInDegree = 0 ;
		for(ColouredGraph grph: origGrphs){
			IntSet setVertices = grph.getVertices();
			IntSet setEdges = grph.getEdges();
			
			int[] arrVertices = setVertices.toIntArray();
			IntSet setTails = new DefaultIntSet();
			IntSet setHeads = new DefaultIntSet();
			for(int vertId: arrVertices){
				IntSet setOutEdges = grph.getOutEdges(vertId);
				IntSet setInEdges = grph.getInEdges(vertId);
				
				if(setOutEdges!=null && setOutEdges.size() > 0){
					setTails.add(vertId);
				}
				
				
				if(setInEdges != null && setInEdges.size() > 0){
					setHeads.add(vertId);
				}
			}
			
			avrgOutDegree += setEdges.size()/setTails.size();
			avrgInDegree += setEdges.size()/setHeads.size();
			
		}
		
		mAvrgMaxOutDegree = avrgOutDegree / origGrphs.length;
		mAvrgMaxInDegree = avrgInDegree/ origGrphs.length;
	}
	
	
	private void computeAvrgInDegree(ColouredGraph[] origGrphs){
		MaxVertexInDegreeMetric maxInDegreeMetric = new MaxVertexInDegreeMetric();
		
		double avrgMaxInDegree =0;
		for(ColouredGraph grph: origGrphs){
			avrgMaxInDegree += maxInDegreeMetric.apply(grph);
		}
		mAvrgMaxInDegree = avrgMaxInDegree / origGrphs.length;
	}
	
	public ColouredGraph generateGraph(){
		
		IntSet setVertices = mMimicGraph.getVertices();
		int[] arrVertices = setVertices.toIntArray();
		
		ObjectDistribution<Integer> tailDistr = MapUtil.converta(mapVertexPotentialOutDegree);
		OfferedItemByRandomProb<Integer> tailProposer = new OfferedItemByRandomProb<Integer>(tailDistr);
		
		ObjectDistribution<Integer> headDistr = MapUtil.converta(mapVertexPotentialInDegree);
		OfferedItemByRandomProb<Integer> headProposer = new OfferedItemByRandomProb<Integer>(headDistr);
		
		for(int i = 0 ; i < mIDesiredNoOfEdges ; ){
			
			Integer tailId = tailProposer.getPotentialItem();
			BitSet tailColo = mMimicGraph.getVertexColour(tailId);
			
			Integer headId = headProposer.getPotentialItem();
			BitSet headColo = mMimicGraph.getVertexColour(headId);
			
			if(mColourMapper.canConnect(tailColo, headColo, null)){
				i++;
				mMimicGraph.addEdge(tailId, headId);
			}
		}
		
		
		return mMimicGraph;
	}
	
	private void computePotentialDegreeForEachVertex(){
		Set<BitSet> setVertColo = mMapColourToVertexIDs.keySet();
		Random randomOut = new Random();
		Random randomIn = new Random();
		for(BitSet vertColo : setVertColo){
			IntSet setVertices = mMapColourToVertexIDs.get(vertColo);
			
			int[] arrVertices = setVertices.toIntArray();
			for(int vertId : arrVertices){
				// potential out degree
				int possOutDegr = PoissonDistribution.randomXJunhao(mAvrgMaxOutDegree);
				mapVertexPotentialOutDegree.putOrAdd(vertId, possOutDegr, possOutDegr);
				// potential in degree
				int possInDegr = PoissonDistribution.randomXKnuth(mAvrgMaxInDegree);
				mapVertexPotentialInDegree.putOrAdd(vertId, possOutDegr, possOutDegr);
			}
		}
	}

}

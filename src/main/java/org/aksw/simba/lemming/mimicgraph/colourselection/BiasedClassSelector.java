package org.aksw.simba.lemming.mimicgraph.colourselection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphInitializer;
import org.aksw.simba.lemming.metrics.dist.ObjectDistribution;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredIEDistPerVColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.AvrgColouredOEDistPerVColour;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.IOfferedItem;
import org.aksw.simba.lemming.mimicgraph.colourmetrics.utils.OfferedItemByRandomProb;
import org.dice_research.ldcbench.generate.SeedGenerator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.carrotsearch.hppc.BitSet;

/**
 * 
 */
@Component("BCS")
@Scope(value = "prototype")
public class BiasedClassSelector implements IClassSelector {

	private GraphInitializer graphInit;
	private SeedGenerator seedGenerator;

	private Map<BitSet, IOfferedItem<BitSet>> mMapOEColoToTailColoProposer;
	private Map<BitSet, IOfferedItem<BitSet>> mMapIEColoToHeadColoProposer;

	public BiasedClassSelector(GraphInitializer graphInit) {
		this.graphInit = graphInit;
		this.seedGenerator = graphInit.getSeedGenerator();
		mMapOEColoToTailColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		mMapIEColoToHeadColoProposer = new HashMap<BitSet, IOfferedItem<BitSet>>();
		computeAvrgIOEdgeDistPerVertColo(graphInit.getOriginalGraphs());
	}

	@Override
	public IOfferedItem<BitSet>  getTailClass(BitSet edgeColour) {
		IOfferedItem<BitSet> tailColourProposer = mMapOEColoToTailColoProposer.get(edgeColour);
		return tailColourProposer;
	}

	@Override
	public IOfferedItem<BitSet>  getHeadClass(BitSet tailColour, BitSet edgeColour) {
		IOfferedItem<BitSet> headColourProposer = mMapIEColoToHeadColoProposer.get(edgeColour);
		return headColourProposer;
	}

	private void computeAvrgIOEdgeDistPerVertColo(ColouredGraph[] origGrphs) {
		// out degree colour distribution associated with edge colours
		AvrgColouredOEDistPerVColour avrgOutEdgeDistPerVertColoMetric = new AvrgColouredOEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgOutEdgeDistPerVertColo = avrgOutEdgeDistPerVertColoMetric
				.getMapAvrgOutEdgeDist(graphInit.getAvailableEdgeColours(), graphInit.getAvailableVertexColours());

		Set<BitSet> outEdgeColours = avrgOutEdgeDistPerVertColo.keySet();
		for (BitSet edgeColo : outEdgeColours) {
			ObjectDistribution<BitSet> outEdgeDistPerVertColo = avrgOutEdgeDistPerVertColo.get(edgeColo);
			if (outEdgeDistPerVertColo != null) {
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(outEdgeDistPerVertColo,
						graphInit.getSeedGenerator().getNextSeed());
				mMapOEColoToTailColoProposer.put(edgeColo, vertColoProposer);
			}
		}

		// in degree colour distribution associated with edge colours
		AvrgColouredIEDistPerVColour avrgInEdgeDistPerVertColoMetric = new AvrgColouredIEDistPerVColour(origGrphs);
		Map<BitSet, ObjectDistribution<BitSet>> avrgInEdgeDistPerVertColo = avrgInEdgeDistPerVertColoMetric
				.getMapAvrgInEdgeDist(graphInit.getAvailableEdgeColours(), graphInit.getAvailableVertexColours());
		Set<BitSet> inEdgeColours = avrgInEdgeDistPerVertColo.keySet();
		for (BitSet edgeColo : inEdgeColours) {
			ObjectDistribution<BitSet> inEdgeDistPerVertColo = avrgInEdgeDistPerVertColo.get(edgeColo);
			if (inEdgeDistPerVertColo != null) {
				IOfferedItem<BitSet> vertColoProposer = new OfferedItemByRandomProb<>(inEdgeDistPerVertColo,
						graphInit.getSeedGenerator().getNextSeed());
				mMapIEColoToHeadColoProposer.put(edgeColo, vertColoProposer);
			}
		}
	}
	
	@Override
	public BitSet getEdgeColourProposal() {
		ObjectDistribution<BitSet> edgeDistribution = graphInit.getEdgeColourDist();
		Set<BitSet> restrictedColours = graphInit.getSetOfRestrictedEdgeColours();
		IOfferedItem<BitSet> edgeProposer = new OfferedItemByRandomProb<BitSet>(edgeDistribution, restrictedColours,
				seedGenerator.getNextSeed());
		return edgeProposer.getPotentialItem();
	}

}

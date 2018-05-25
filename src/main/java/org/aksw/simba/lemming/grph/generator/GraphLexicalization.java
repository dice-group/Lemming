package org.aksw.simba.lemming.grph.generator;


import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.metrics.dist.LiteralProcessor;
import org.aksw.simba.lemming.rules.IColourMappingRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

public class GraphLexicalization {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphLexicalization.class);
	
	private IWord2Vec mWordProposer;
	private IColourMappingRules mColourMapper;
	private ColouredGraph mMimicGraph;
	private LiteralProcessor mLiteralProcessor;
	
	public GraphLexicalization(ColouredGraph[] origGrphs, ColouredGraph mimicGraph){
		mMimicGraph = mimicGraph;
		mLiteralProcessor = new LiteralProcessor(origGrphs);
	}
	
	public ColouredGraph lexicalizeGraph(){
		
		/* 
		 * get all words associated with the vectex's colours existing in the
		 * refined graph
		 */
		
		ObjectArrayList<BitSet> lstVColours = mMimicGraph.getVertexColours();
		BitSet[] arrVColours = lstVColours.buffer;
		for(BitSet vColo : arrVColours){
			// first get the data typed edge's colours associated to this vertColo
			Set<BitSet> setDTEColours = mColourMapper.getDataTypedEdgeColoursByVertexColour(vColo) ;
			for(BitSet dteColo : setDTEColours){
				// get a list of literals associated with the data typed edge's colours
				Set<String> setOfWords = mLiteralProcessor.getWords(dteColo);
				// get average number of words which the data typed edge's colour may contain
				double avrgNoOfWords = mLiteralProcessor.getAvrgNoOfWords(dteColo);
				
				// get a potential literal based on the word2vec model
				String literal = mWordProposer.getWords(setOfWords, (int)avrgNoOfWords);
				
				// add it to the mimic graph
				
			}
			
			 
			
		}
		// finally the mimic graph is reversed into RDF data set
			
		return mMimicGraph;
	}
	
}

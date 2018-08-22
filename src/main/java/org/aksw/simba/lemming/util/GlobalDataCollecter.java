package org.aksw.simba.lemming.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.algo.refinement.RefinementNode;
import org.aksw.simba.lemming.colour.ColourPalette;

import com.carrotsearch.hppc.BitSet;

public class GlobalDataCollecter {

	private static GlobalDataCollecter mInstance = new GlobalDataCollecter();
	private String mDatasetName = LocalDateTime.now().toString(); 
	private SortedSet<RefinementNode> mSetConstantExpressions;
	private ColouredGraph mMimicGraph;
	private ColouredGraph mRefinedMimicGraph;
	private Map<String, ColouredGraph> mMapOriginalGraphs;
	
	private ColourPalette mVertexPalette;
	private ColourPalette mDtEdgePalette;

	private Map<BitSet, Map<BitSet, String>> mMappingKeysAndColours ;
	
	private static BufferedWriter mWriter ;
	
	private List<Double> mScoreError;
	
	
	private GlobalDataCollecter(){
		mMapOriginalGraphs = new HashMap<String, ColouredGraph>();
		mSetConstantExpressions = new TreeSet<RefinementNode>();
		mMappingKeysAndColours = new HashMap<BitSet, Map<BitSet, String>>();
		mScoreError= new ArrayList<Double>();
	}
	
	public void addScoreError(double scoreErr){
		mScoreError.add(scoreErr);
	}
	
	
	public void setVertexPallete(ColourPalette vertexPalette){
		mVertexPalette = vertexPalette;
	}
	
	public void setDatatypeEdgePalette(ColourPalette dtePalette){
		mDtEdgePalette = dtePalette;
	}
	
	public void setDatasetName(String datasetName){
		mDatasetName = datasetName;
	}
	
	public String getKey (BitSet tColo, BitSet dteColo){
		String key = "";
		if(tColo != null && dteColo != null){
			Map<BitSet, String> mapTColoAndKeys = mMappingKeysAndColours.get(dteColo);
			if(mapTColoAndKeys != null){
				key = mapTColoAndKeys.get(tColo);
				if(key != null){
					return key;
				}else{
					key="";
				}
			}else{
				mapTColoAndKeys = new HashMap<BitSet, String>();
				mMappingKeysAndColours.put(dteColo, mapTColoAndKeys);
			}
			
			Set<String> propertyURIs = mDtEdgePalette.getURIs(dteColo, true);
			Set<String> resourceClassURIs = mVertexPalette.getURIs(tColo, false);
			
			
			if(resourceClassURIs != null && resourceClassURIs.size() >0){
				List<String> newResClassURIs = new ArrayList(new TreeSet(resourceClassURIs));
				
				for(String classUri : newResClassURIs){
					int indexOfLastFlash = classUri.lastIndexOf('/');
					
					if(indexOfLastFlash != -1){
						key+= classUri.substring(indexOfLastFlash+1, classUri.length());
					}else{
						key+= classUri;
					}
				}
			}
				
			if(propertyURIs != null && propertyURIs.size() > 0){
				String propURI = propertyURIs.iterator().next();
				int indexOfLastFlash = propURI.lastIndexOf('/');
				if(indexOfLastFlash!= -1){
					key+= propURI.substring(indexOfLastFlash+1, propURI.length());
				}else{
					key+= propURI;
				}
			}
			
			mapTColoAndKeys.put(tColo, key);
		}
			
		return key;
	}
	
	public String getDatasetName(){
		return mDatasetName;
	}
	
	public void addGraphs(String fileName, ColouredGraph grph){
		mMapOriginalGraphs.put(fileName, grph);
	}
	
	public static GlobalDataCollecter getInstance(){
		return mInstance;
	}

	public void addConstantExpressions(SortedSet<RefinementNode> exprss){
		mSetConstantExpressions.addAll(exprss);
	}
	
	public void setMimicGraph(ColouredGraph mimicGraph){
		mMimicGraph = mimicGraph;
	}
	
	public void setRefinedMimicGraph(ColouredGraph mimicGraph){
		mRefinedMimicGraph = mimicGraph;
	}
	
	public void printResult(){
		if(mMapOriginalGraphs != null && mMapOriginalGraphs.size()> 0){
			try{
				//open for appending content
				mWriter = new BufferedWriter( new FileWriter("LemmingEx.result", true));
				
				Set<String> setFileNames = mMapOriginalGraphs.keySet(); 

				int iCounter = 0;
				
				
				mWriter.write("--- Graph Generation: " + LocalDateTime.now().toString() +"---\n");
				mWriter.write("#Total number of input graphs: " + setFileNames.size() +"\n");
				
				
				
				//basic information of originals graph
				for(String fileName: setFileNames ){
					iCounter ++;
					ColouredGraph grph = mMapOriginalGraphs.get(fileName);
					
					if(grph !=null){
						//print the file name
						mWriter.write("#Graph "+ iCounter+": " + fileName +"\n");
						//number of vertices
						mWriter.write("\tNumber of vertices: "+ grph.getVertices().size() +"\n");
						//number of vertex colours
						mWriter.write("\tNumber of vertex colour: " + grph.getVertexPalette().getMapOfURIAndColour().size()+"\n");
						//number of edges
						mWriter.write("\tNumber of edges: "+ grph.getEdges().size() +"\n");
						//number of edge colours
						mWriter.write("\tNumber of edge colour: " + grph.getEdgePalette().getMapOfURIAndColour().size()+"\n");
						mWriter.write("\n");
					}
				}
				
				//basic information of mimic graph
				mWriter.write("#Mimic graph\n");
				//number of vertices
				mWriter.write("\tNumber of vertices: "+ mMimicGraph.getVertices().size() +"\n");
				//number of vertex colours
				mWriter.write("\tNumber of vertex colour: " + mMimicGraph.getVertexPalette().getMapOfURIAndColour().size()+"\n");
				//number of edges
				mWriter.write("\tNumber of edges: "+ mMimicGraph.getEdges().size() +"\n");
				//number of edge colours
				mWriter.write("\tNumber of edge colour: " + mMimicGraph.getEdgePalette().getMapOfURIAndColour().size()+"\n");
				mWriter.write("\n");
				
				//graph after being refined
				mWriter.write("#Refined Mimic graph\n");
				//number of vertices
				mWriter.write("\tNumber of vertices: "+ mRefinedMimicGraph.getVertices().size() +"\n");
				//number of vertex colours
				mWriter.write("\tNumber of vertex colour: " + mRefinedMimicGraph.getVertexPalette().getMapOfURIAndColour().size()+"\n");
				//number of edges
				mWriter.write("\tNumber of edges: "+ mRefinedMimicGraph.getEdges().size() +"\n");
				//number of edge colours
				mWriter.write("\tNumber of edge colour: " + mRefinedMimicGraph.getEdgePalette().getMapOfURIAndColour().size()+"\n\n");
				
				//constant expressions & values
				iCounter = 0 ;
				double constVal = 0. ;
				if(mSetConstantExpressions != null && mSetConstantExpressions.size()>0){
					for(RefinementNode n: mSetConstantExpressions){
						iCounter ++;
						//print the file name
						mWriter.write("#Expression "+ iCounter+": " + n.toString() +"\n");
						//number of vertices
						mWriter.write("\tFitness value: "+ n.getFitness() +"\n");
						//number of vertex colours
						mWriter.write("\tConstant values of graph(s) 1 -> " + setFileNames.size() +"\n");
						mWriter.write("\t\t[");
						int i = 0 ;
						for(String fileName: setFileNames){
							ColouredGraph grph = mMapOriginalGraphs.get(fileName);
							constVal = n.getExpression().getValue(grph);
							if(i < setFileNames.size()-1){
								mWriter.write(constVal +", ");
							}else{
								mWriter.write(constVal+"");
							}
						}
						mWriter.write("]\n");
						constVal = n.getExpression().getValue(mMimicGraph);
						mWriter.write("\tConstant value of mimic graph: "+constVal+"\n");
						constVal = n.getExpression().getValue(mRefinedMimicGraph);
						mWriter.write("\tConstant value of refined mimic graph: "+constVal+"\n\n");
					}
				}
				
				if(mScoreError != null && mScoreError.size() > 0){
					mWriter.write("#Score error: \n");
					mWriter.write("\t[");
					for(int i = 0 ; i < mScoreError.size() ; i++){
						if(i < mScoreError.size()-1){
							mWriter.write(mScoreError.get(i) +", ");
						}else{
							mWriter.write(mScoreError.get(i)+"");
						}
					}
					mWriter.write("]\n");
				}
				
				mWriter.write("\n\n\n");
				mWriter.close();
				
			}catch(Exception ex){
			}
		}
	}
}

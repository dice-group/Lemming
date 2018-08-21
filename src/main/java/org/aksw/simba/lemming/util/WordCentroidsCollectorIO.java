package org.aksw.simba.lemming.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WordCentroidsCollectorIO {

	private static WordCentroidsCollectorIO mInstance = new WordCentroidsCollectorIO();
	
	public static WordCentroidsCollectorIO getInstance(){
		return mInstance;
	}
	
	private Map<String, SingleWordData> mMapOfWordData;
	
	private WordCentroidsCollectorIO(){
		mMapOfWordData = new HashMap<String, SingleWordData>();
	}
	
	public void addWordData(String keyName, float[] means, float[] standardDeviation){
		
		SingleWordData data = new SingleWordData();
		data.arrMeans = means;
		data.arrStandardDeviation = standardDeviation;
		mMapOfWordData.put(keyName,data);
	}
	
	public void writeFiles(String datasetName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();

		ArrayNode data = mapper.createArrayNode();

		Set<String> setOfKeys = mMapOfWordData.keySet();
		for (String key : setOfKeys) {
			ObjectNode element = mapper.createObjectNode();
			SingleWordData word = mMapOfWordData.get(key);

			element.put("key", key);
			element.put("centroid", word.getStringMeans());
			element.put("sd", word.getStringStandardDeviation());

			data.add(element);
		}
		root.put("data", data);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		try {
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			writer.writeValue(new File(datasetName+".json"), root);
		} catch (Exception e) {
		}
	}
	
	
	protected class SingleWordData {
		protected float[] arrMeans;
		protected float[] arrStandardDeviation;
		
		public String getStringMeans(){
			if(arrMeans!= null && arrMeans.length > 0)
			{
				StringBuilder res = new StringBuilder("[");
				
				for(int i = 0 ; i <arrMeans.length ; i++){
					if(i < arrMeans.length -1){
						res.append(arrMeans[i]+", ");
					}else{
						res.append(arrMeans[i]+"");
					}
				}
				res.append("]");
				return res.toString();
			}
			return "[]";
		}
		
		public String getStringStandardDeviation(){
			if(arrStandardDeviation != null && arrStandardDeviation.length > 0  )
			{
				StringBuilder res = new StringBuilder("[");
				for(int i = 0 ; i< arrStandardDeviation.length ; i++){
					if(i < arrStandardDeviation.length -1){
						res.append(arrMeans[i]+", ");
					}else{
						res.append(arrMeans[i]+"");
					}
				}
				res.append("]");
				return res.toString();
			}
			return "[]";
		}
	}
}



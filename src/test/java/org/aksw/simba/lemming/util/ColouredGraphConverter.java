package org.aksw.simba.lemming.util;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * This class is used to import a RDF-file and generate a {@link ColouredGraph} for the imported RDF-file.
 * @author Zun Wang
 */
public class ColouredGraphConverter {

    /**
     * Convert a RDF-file to {@link ColouredGraph}
     * @param fileName: the fileName of RDF-file.
     * @return a ColouredGraph for the given RDF-file.
     */
    public static ColouredGraph convertFileToGraph(String fileName){
       Model model = ModelFactory.createDefaultModel();
       try{
           String filePath = ColouredGraphConverter.class.getClassLoader().getResource(fileName).getPath();
           model.read(filePath);
       }catch (Exception e){
           throw new RuntimeException("Couldn't find file from resource \"" + fileName + "\".", e);
       }
       GraphCreator creator = new GraphCreator();
       ColouredGraph graph = creator.processModel(model);
       return graph;
    }
}

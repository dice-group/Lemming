package org.aksw.simba.lemming.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class IOHelper {

    public static ColouredGraph readGraphFromFile(String file, String lang) {
        InputStream is = null;
        ColouredGraph result = null;
        try {
            is = new FileInputStream(file);
            result = readGraphFromResource(is, lang);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return result;
    }

    public static ColouredGraph readGraphFromResource(String resource, String lang) {
        return readGraphFromResource(IOHelper.class.getClassLoader(), resource, lang);
    }

    public static ColouredGraph readGraphFromResource(ClassLoader loader, String resource, String lang) {
        InputStream is = loader.getResourceAsStream(resource);
        ColouredGraph result = readGraphFromResource(is, lang);
        IOUtils.closeQuietly(is);
        return result;
    }

    public static ColouredGraph readGraphFromResource(InputStream is, String lang) {
        Model model = ModelFactory.createDefaultModel();
        model.read(is, null, lang);
        GraphCreator creator = new GraphCreator();
        return creator.processModel(model);
    }
}

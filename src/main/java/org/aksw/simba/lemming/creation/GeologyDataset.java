package org.aksw.simba.lemming.creation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeologyDataset extends AbstractDatasetManager implements IDatasetManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeologyDataset.class);

	public GeologyDataset() {
		super("Geology");
	}

	@Override
	public ColouredGraph[] readGraphsFromFiles(String dataFolderPath) {
		List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		GraphCreator creator = new GraphCreator();

		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			//build ontology model for Dataset
			OntModel ontModel = ModelFactory.createOntologyModel();
			ontModel.getDocumentManager().setProcessImports(false);
			ontModel.read("22-rdf-syntax-ns", "TTL");
			ontModel.read("rdf-schema", "TTL");
			File ontFolder = new File("geology");
			for(File file : ontFolder.listFiles()){
				ontModel.read(file.getAbsolutePath(), "ttl");
			}
			// sort files'name ascendently
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			Collections.sort(lstSortedFilesByName);

			Inferer inferer = new Inferer(true, ontModel);
			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);
				Model geologyModel = ModelFactory.createDefaultModel();
				geologyModel.read(file.getAbsolutePath(), "TTL");
				LOGGER.info("Read data to model - " + geologyModel.size() + " triples " + file.getName());
				// returns a new model with the added triples
				geologyModel = inferer.process(geologyModel);
				ColouredGraph graph = creator.processModel(geologyModel);
				if (graph != null) {
					LOGGER.info("Generated graph of " + geologyModel.size() + " triples");
					graphs.add(graph);
				}
			}
		} else {
			LOGGER.error("Find no files in \"" + folder.getAbsolutePath() + "\". Aborting.");
			System.exit(1);
		}

		return graphs.toArray(new ColouredGraph[graphs.size()]);
	}

//	public static void main(String[] args) {
//		String DATA_FOLDER_PATH = "GeologyGraphs/";
//		new GeologyDataset().readGraphsFromFiles(DATA_FOLDER_PATH);
//	}
}

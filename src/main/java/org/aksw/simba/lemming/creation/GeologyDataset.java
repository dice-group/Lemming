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
			OntModel ontModel = ModelFactory.createOntologyModel();
			ontModel.getDocumentManager().setProcessImports(false);
			ontModel.read("22-rdf-syntax-ns", "TTL");
			ontModel.read("rdf-schema", "TTL");
			ontModel.read("geology/void.ttl");
			ontModel.read("geology/foaf.ttl");
			ontModel.read("geology/skos.ttl");
			ontModel.read("geology/dcterms.ttl");
			ontModel.read("geology/owl.ttl");
			ontModel.read("geology/dc.ttl");
			ontModel.read("geology/geometry.ttl");
			ontModel.read("geology/geosparql.ttl");
			ontModel.read("geology/gts.ttl");
			ontModel.read("geology/gts-w3c.ttl");
			ontModel.read("geology/rank.ttl");
			ontModel.read("geology/sampling.ttl");
			ontModel.read("geology/sam-lite.ttl");
			ontModel.read("geology/sf.ttl");
			ontModel.read("geology/sosa.ttl");
			ontModel.read("geology/thors.ttl");
			ontModel.read("geology/time.ttl");
			ontModel.read("geology/basic.ttl");
			ontModel.read("geology/temporal.ttl");
			
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			// sort ascendently
			Collections.sort(lstSortedFilesByName);
			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);
				Model geologyModel = ModelFactory.createDefaultModel();
				geologyModel.read(file.getAbsolutePath(), "TTL");
				LOGGER.info("Read data to model - " + geologyModel.size() + " triples " + file.getName());
				Inferer inferer = new Inferer(true);
				// returns a new model with the added triples
				geologyModel = inferer.process(geologyModel, ontModel);
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

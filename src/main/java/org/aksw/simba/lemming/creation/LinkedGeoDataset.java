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

public class LinkedGeoDataset extends AbstractDatasetManager implements IDatasetManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedGeoDataset.class);

	public LinkedGeoDataset() {
		super("LinkedGeo");
	}

	@Override
	public ColouredGraph[] readGraphsFromFiles(String dataFolderPath) {
		List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		GraphCreator creator = new GraphCreator();

		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			// sort ascendently
			Collections.sort(lstSortedFilesByName);

			OntModel ontModel = ModelFactory.createOntologyModel();
			ontModel.getDocumentManager().setProcessImports(false);
			ontModel.read("22-rdf-syntax-ns", "TTL");
			ontModel.read("rdf-schema", "TTL");
			ontModel.read("lgeo/foaf.ttl");
			ontModel.read("lgeo/skos.ttl");
			ontModel.read("lgeo/purl_dcterms.ttl");
			ontModel.read("lgeo/owl.ttl");
			ontModel.read("lgeo/terms.ttl");
			ontModel.read("lgeo/wgs84_pos.ttl");
			ontModel.read("lgeo/2014-09-09-ontology.sorted.nt");
			ontModel.read("lgeo/geosparql.ttl");
			ontModel.read("lgeo/geovocab_geometry.ttl");
			ontModel.read("lgeo/geovocab_spatial.ttl");
			ontModel.read("lgeo/LGD-Dump-110406-Ontology.nt");
			ontModel.read("lgeo/rdfs-ns-void.rdf");
			ontModel.read("lgeo/custom_ontology.nt");

			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);

				if (file != null && file.isDirectory() && file.getTotalSpace() > 0) {
					Model geoModel = ModelFactory.createDefaultModel();
					for (File subFile : file.listFiles()) {
						// read file to model
						geoModel.read(subFile.getAbsolutePath(), "TTL");
					}
					LOGGER.info("Read data to model - " + geoModel.size() + " triples");
					Inferer inferer = new Inferer(true);
					// returns a new model with the added triples
					geoModel = inferer.process(geoModel, ontModel);
					ColouredGraph graph = creator.processModel(geoModel);
					if (graph != null) {
						LOGGER.info("Generated graph of " + geoModel.size() + " triples");
						graphs.add(graph);
					}
				}
			}
			
		} else {
			LOGGER.error("Find no files in \"" + folder.getAbsolutePath() + "\". Aborting.");
			System.exit(1);
		}

		return graphs.toArray(new ColouredGraph[graphs.size()]);
	}
	
//	public static void main(String[] args) {
//		String DATA_FOLDER_PATH = "LinkedGeoGraphs/";
//		new LinkedGeoDataset().readGraphsFromFiles(DATA_FOLDER_PATH);
//	}	
}

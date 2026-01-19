package org.aksw.simba.lemming.creation.datasets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.Inferer;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Linked Geo Data dataset manager.
 *
 * This class is responsible for reading graphs from files related to the
 * Linked Geo Data dataset, inferring based on the specified ontologies
 * and creating a {@link ColouredGraph} objects from them.
 * 
 */
@Component("lgeo")
@Scope(value = "prototype")
public class LinkedGeoDataset extends AbstractDatasetManager {
	/** Logging object */
	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedGeoDataset.class);

	/**
	 * Empty constructor.
	 */
	public LinkedGeoDataset(String folderPath) {
		super("LinkedGeo",folderPath);
	}

	@Override
	public String getDatasetPath() {
		return dataFolderPath;
	}

	@Override
	public ColouredGraph[] readGraphsFromFiles() {
		List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		GraphCreator creator = new GraphCreator(false);

		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			// sort ascendently
			Collections.sort(lstSortedFilesByName);

			OntModel ontModel = ModelFactory.createOntologyModel();
			ontModel.getDocumentManager().setProcessImports(false);
			ontModel.read("datasets/ontologies/22-rdf-syntax-ns", "TTL");
			ontModel.read("datasets/ontologies/rdf-schema", "TTL");
			File ontFolder = new File("datasets/ontologies/lgeo");
			for(File file : ontFolder.listFiles()){
				ontModel.read(file.getAbsolutePath(), "TTL");
			}

			Inferer inferer = new Inferer(true, ontModel);
			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);

				if (file != null && file.isDirectory() && file.getTotalSpace() > 0) {
					Model geoModel = ModelFactory.createDefaultModel();
					for (File subFile : file.listFiles()) {
						// read file to model
						geoModel.read(subFile.getAbsolutePath(), "TTL");
					}
					LOGGER.info("Read data to model - " + geoModel.size() + " triples");

					// returns a new model with the added triples
					geoModel = inferer.process(geoModel);
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
}

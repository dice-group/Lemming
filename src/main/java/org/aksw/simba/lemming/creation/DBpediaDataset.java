package org.aksw.simba.lemming.creation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component("dbp")
/**
 * TODO fix this
 */
public class DBpediaDataset extends AbstractDatasetManager implements IDatasetManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaDataset.class);

	@Value("${datasets.dbp.filepath}")
	private String dataFolderPath;

	public DBpediaDataset() {
		super("DBpedia");
	}

	@Override
	public String getDatasetPath() {
		return dataFolderPath;
	}

	@Override
	public ColouredGraph[] readGraphsFromFiles() {

		List<ColouredGraph> graphs = new ArrayList<ColouredGraph>();
		GraphCreator creator = new GraphCreator();

		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			// sort ascendently
			Collections.sort(lstSortedFilesByName);

			// key needs only the file name, whereas value needs the full path to the
			// corresponding Ontology
			Map<String, String> modelOntMap = new HashMap<>();
			modelOntMap.put("2022-12-01", "2022-12-01-194003-ontology--DEV_type=parsed.owl");
			modelOntMap.put("2022-03-01", "2022-03-04-070002-ontology--DEV_type=parsed.owl");
			modelOntMap.put("2021-12-01", "2021-12-01-180002-ontology_type=parsed.owl");
			modelOntMap.put("2021-03-01", "2021-03-12-142000-ontology--DEV_type=parsed.owl");
			modelOntMap.put("2020-10-01", "2020-10-01-031000-ontology--DEV_type=parsed.owl");
			modelOntMap.put("2020-05-01", "2020-06-10-181610-ontology_type=parsed.owl");

			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);

				if (file != null && file.isDirectory() && file.getTotalSpace() > 0) {
					Model model = ModelFactory.createDefaultModel();
					for (File subFile : file.listFiles()) {
						// read file to model
						model.read(subFile.getAbsolutePath(), "TTL");
					}
					LOGGER.info("Read data to model - " + model.size() + " triples");
					OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
					ontModel.getDocumentManager().setProcessImports(false);
					ontModel.read(modelOntMap.get(fileName));
					ontModel.read("22-rdf-syntax-ns", "TTL");
					ontModel.read("rdf-schema", "TTL");
					Inferer inferer = new Inferer(true, ontModel);
					model = inferer.process(model);
					ColouredGraph graph = creator.processModel(model);
					if (graph != null) {
						LOGGER.info("Generated graph of " + model.size() + " triples");
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
	
	public void setDataFolderPath(String dataFolderPath) {
		this.dataFolderPath = dataFolderPath;
	}
	
	
	public static void main(String[] args) {
		DBpediaDataset pg = new DBpediaDataset();
		pg.setDataFolderPath("DBpedia");
		pg.readGraphsFromFiles();
	}
}

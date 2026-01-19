package org.aksw.simba.lemming.creation.datasets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphWrapper;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.ColourPaletteWrapper;
import org.aksw.simba.lemming.creation.GraphCreator;
import org.aksw.simba.lemming.creation.GraphReverter;
import org.aksw.simba.lemming.util.PersHelper;
import org.aksw.simba.lemming.util.SerializationParser;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.ObjectArrayList;

public abstract class AbstractDatasetManager implements IDatasetManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatasetManager.class);
	protected String mDatasetName;
	protected String dataFolderPath;

	public AbstractDatasetManager(String datasetName, String folderPath) {
		mDatasetName = datasetName;
		this.dataFolderPath = folderPath;
	}

	public String getDatasetName() {
		return mDatasetName;
	}

	public void setDatasetName(String datasetName) {
		mDatasetName = datasetName;
	}
	
	public String getDatasetPath() {
		return dataFolderPath;
	}
	
	public String getSavedFileName(String folder) {
		new File(folder).mkdirs();
		String fileName = folder+"/Mimic_" + mDatasetName + ".ttl";
		String[] parts = new String[2];
		int index = fileName.lastIndexOf('.');
		parts[0] = fileName.substring(0, index);
		parts[1] = fileName.substring(index, fileName.length());

		Path path = Paths.get(fileName);
		int i = 1;
		while (Files.exists(path)) {
			LOGGER.warn("File already exists!");
			i++;
			path = Paths.get(parts[0] + "(" + i + ")" + parts[1]);
		}
		LOGGER.info("Output file: " + path.toString());
		return path.toString();
	}

	@Override
	public void writeGraphsToFile(ColouredGraph grph, String filePath) {
		Model datasetModel = ModelFactory.createDefaultModel();
		LOGGER.info("Converting and writing graph to file: "+filePath);
		try (Writer writerforOutModel = new FileWriter(filePath);) {
			// graph reverter: generate a new model from a coloured graph
			GraphReverter reverter = new GraphReverter(grph, datasetModel);
			Model newModel = reverter.processGraph();
			newModel.write(writerforOutModel, "TURTLE");
		} catch (Exception ex) {
			LOGGER.error("Failed to write to file: " + ex.getMessage());
			ex.printStackTrace();
		}
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

			for (String fileName : lstSortedFilesByName) {
				File file = new File(dataFolderPath + "/" + fileName);

				if (file != null && file.isDirectory() && file.getTotalSpace() > 0) {
					Model model = ModelFactory.createDefaultModel();
					for (File subFile : file.listFiles()) {
						model.read(subFile.getAbsolutePath(), "TTL");
					}
					LOGGER.info("Read data to model - " + model.size() + " triples");

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

	@Override
	public void persistIntResults(ColouredGraph curMimicGraph, String filePath) {
		try {
			FileOutputStream fileOut = new FileOutputStream(filePath);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);

			List<java.util.BitSet> serVertexColours = SerializationParser
					.parseBitSetArrayList(curMimicGraph.getVertexColours());
			List<java.util.BitSet> serEdgeColours = SerializationParser
					.parseBitSetArrayList(curMimicGraph.getEdgeColours());

			ColourPaletteWrapper vertexPaletteWrapper = PersHelper.convertCP(curMimicGraph.getVertexPalette());
			ColourPaletteWrapper edgePaletteWrapper = PersHelper.convertCP(curMimicGraph.getEdgePalette());
			ColourPaletteWrapper dtPaletteWrapper = PersHelper.convertCP(curMimicGraph.getDataTypedEdgePalette());

			ColouredGraphWrapper colouredGraphWrapper = new ColouredGraphWrapper(curMimicGraph.getGraph(),
					serVertexColours, serEdgeColours, vertexPaletteWrapper, edgePaletteWrapper, dtPaletteWrapper);

			out.writeObject(colouredGraphWrapper);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ColouredGraph readIntResults(String filePath) {
		ColouredGraph colouredGraph = null;
		try {
			Path path = Paths.get(filePath);

			if (Files.exists(path)) {
				FileInputStream fileIn = new FileInputStream(filePath);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				ColouredGraphWrapper colouredGraphWrapper = (ColouredGraphWrapper) in.readObject();
				in.close();
				fileIn.close();

				if (colouredGraphWrapper != null) {
					ColourPalette vertexPalette = PersHelper.convertCP(colouredGraphWrapper.getVertexPalette());
					ColourPalette edgePalette = PersHelper.convertCP(colouredGraphWrapper.getEdgePalette());
					ColourPalette dtEdgePalette = PersHelper.convertCP(colouredGraphWrapper.getDtEdgePalette());

					ObjectArrayList<BitSet> vertexColours = SerializationParser
							.parseBitSetArrayList(colouredGraphWrapper.getVertexColours());
					ObjectArrayList<BitSet> edgeColours = SerializationParser
							.parseBitSetArrayList(colouredGraphWrapper.getEdgeColours());

					colouredGraph = new ColouredGraph(colouredGraphWrapper.getGraph(), vertexPalette, edgePalette,
							dtEdgePalette);
					colouredGraph.setVertexColours(vertexColours);
					colouredGraph.setEdgeColours(edgeColours);
				}
			} else {
				LOGGER.warn("Specified file does not exist");
			}

		} catch (IOException i) {
			LOGGER.error("Could not read the file: " + i.getMessage());
			i.printStackTrace();
		} catch (ClassNotFoundException e) {
			LOGGER.error("Could not read the file: " + e.getMessage());
			e.printStackTrace();
		}

		return colouredGraph;
	}

	@Override
	public String toString() {
		return mDatasetName;
	}
	
	/**
	 * Reads a single graph from file and creates the respective
	 * {@link ColouredGraph}
	 * 
	 * @param file File path
	 * @return {@link ColouredGraph} instance of the graph
	 */
	public ColouredGraph readSingleGraphFromFile(String file) {
		GraphCreator creator = new GraphCreator(false);
		Model model = ModelFactory.createDefaultModel();
		model.read(file);
		return creator.processModel(model);
	}

	/**
	 * Reads all graphs from a folder into a single {@link ColouredGraph} object
	 * 
	 * @param dataFolderPath Path to the folder
	 * @return {@link ColouredGraph} instance of the graph
	 */
	public ColouredGraph readSingleGraphFromFolder(String dataFolderPath) {
		GraphCreator creator = new GraphCreator(false);
		ColouredGraph graph = null;
		File folder = new File(dataFolderPath);
		if (folder != null && folder.isDirectory() && folder.listFiles().length > 0) {
			List<String> lstSortedFilesByName = Arrays.asList(folder.list());
			Collections.sort(lstSortedFilesByName);
			Model model = ModelFactory.createDefaultModel();
			for (String fileName : lstSortedFilesByName) {
				System.out.println("Reading file: " + fileName);
				File file = new File(dataFolderPath + "/" + fileName);
				model.read(file.getAbsolutePath());
			}
			graph = creator.processModel(model);
		}
		return graph;
	}
	
	/**
	 * Reads all graphs from a folder into a single object or a single file,
	 * depending on the input data path.
	 * 
	 * @param filePath File path
	 * @return {@link ColouredGraph} instance of the graph
	 */
	public ColouredGraph readSingleGraphFromFileOrFolder() {
		File file = new File(dataFolderPath);
		if (file.exists()) {
			if (file.isFile()) {
				return readSingleGraphFromFile(dataFolderPath);
			} else if (file.isDirectory()) {
				return readSingleGraphFromFolder(dataFolderPath);
			} 
		}
		return null;
	}
}
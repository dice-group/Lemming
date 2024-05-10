package org.aksw.simba.lemming.creation;

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
import java.util.List;
import org.aksw.simba.lemming.ColouredGraph;
import org.aksw.simba.lemming.ColouredGraphWrapper;
import org.aksw.simba.lemming.colour.ColourPalette;
import org.aksw.simba.lemming.colour.ColourPaletteWrapper;
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

	public AbstractDatasetManager(String datasetName) {
		mDatasetName = datasetName;
	}

	public String getDatasetName() {
		return mDatasetName;
	}

	public void setDatasetName(String datasetName) {
		mDatasetName = datasetName;
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
		return null;
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
}
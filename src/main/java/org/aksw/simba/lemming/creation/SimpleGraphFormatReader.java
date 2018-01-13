package org.aksw.simba.lemming.creation;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.commons.io.IOUtils;


/**
 * This class models a very simple graph reader constructing {@link ColouredGraph}s from files which
 * are in the following format: Each line defines one edge and consists of two integers (the ids of
 * the nodes connected by the edge) separated by whitespace.
 * 
 * @author Alexander Hetzer
 *
 */
public class SimpleGraphFormatReader {


   private static final String COMMENT_LINE_PREFIX = "#";


   public static ColouredGraph readSimpleGraphFormatFileFromResources(String filePath) throws IOException {
      InputStream is = SimpleGraphFormatReader.class.getClassLoader().getResourceAsStream(filePath);
      List<String> lines = IOUtils.readLines(is);
      IOUtils.closeQuietly(is);

      String[] splitFilePath = filePath.split("//");
      String name = splitFilePath[splitFilePath.length - 1];
      return readSimpleGraphFromString(name, lines);
   }


   public static ColouredGraph readSimpleGraphFormatFile(String filePath) throws IOException {
      File file = new File(filePath);
      FileReader fileReader = new FileReader(file);
      List<String> lines = IOUtils.readLines(fileReader);
      IOUtils.closeQuietly(fileReader);
      return readSimpleGraphFromString(file.getName(), lines);
   }


   private static ColouredGraph readSimpleGraphFromString(String name, List<String> simpleGraphDefinition) {
      List<String> lines = simpleGraphDefinition;
      ColouredGraph graph = new ColouredGraph(name);
      try {
         for (String line : lines) {
            if (line.trim().length() > 1 && !line.startsWith(COMMENT_LINE_PREFIX)) {
               String[] splitLine = line.split("\\s");
               if (splitLine.length != 2) {
                  throw new RuntimeException(
                        String.format("Graph definition %s contains an error in line \n %s.", simpleGraphDefinition, line));
               }
               int from = Integer.parseInt(splitLine[0]);
               int to = Integer.parseInt(splitLine[1]);
               graph.addEdge(from, to);
            }
         }
      } catch (NumberFormatException numberFormatException) {
         throw new RuntimeException(String.format("Graph definition %s contains an error.", simpleGraphDefinition), numberFormatException);
      }
      return graph;
   }
}

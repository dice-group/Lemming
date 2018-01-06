package org.aksw.simba.lemming.creation;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.commons.io.IOUtils;


public class SimpleGraphFormatReader {


   public static ColouredGraph readSimpleGraphFormatFile(String filePath) throws IOException {
      InputStream is = SimpleGraphFormatReader.class.getClassLoader().getResourceAsStream(filePath);
      List<String> lines = IOUtils.readLines(is);
      IOUtils.closeQuietly(is);
      Set<Integer> createdVertices = new HashSet<>();
      Map<Integer, Integer> fileToGraphIds = new HashMap<>();
      ColouredGraph graph = new ColouredGraph();
      try {
         for (String line : lines) {
            if (line.trim().length() > 1) {
               String[] splitLine = line.split(" ");
               if (splitLine.length != 2) {
                  throw new RuntimeException(String.format("File %s contains an error in line \n %s.", filePath, line));
               }
               int from = Integer.parseInt(splitLine[0]);
               if (!createdVertices.contains(from)) {
                  createdVertices.add(from);
                  int fromId = graph.addVertex();
                  fileToGraphIds.put(from, fromId);
               }
               int to = Integer.parseInt(splitLine[1]);
               if (!createdVertices.contains(to)) {
                  createdVertices.add(to);
                  int toId = graph.addVertex();
                  fileToGraphIds.put(to, toId);
               }
               graph.addEdge(fileToGraphIds.get(from), fileToGraphIds.get(to));
            }
         }
      } catch (NumberFormatException numberFormatException) {
         throw new RuntimeException(String.format("File %s contains an error.", filePath), numberFormatException);
      }
      return graph;
   }
}

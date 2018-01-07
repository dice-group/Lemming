package org.aksw.simba.lemming.creation;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.aksw.simba.lemming.ColouredGraph;
import org.apache.commons.io.IOUtils;


public class SimpleGraphFormatReader {


   public static ColouredGraph readSimpleGraphFormatFile(String filePath) throws IOException {
      InputStream is = SimpleGraphFormatReader.class.getClassLoader().getResourceAsStream(filePath);
      List<String> lines = IOUtils.readLines(is);
      IOUtils.closeQuietly(is);
      ColouredGraph graph = new ColouredGraph();
      try {
         for (String line : lines) {
            if (line.trim().length() > 1) {
               String[] splitLine = line.split("\\s");
               if (splitLine.length != 2) {
                  throw new RuntimeException(String.format("File %s contains an error in line \n %s.", filePath, line));
               }
               int from = Integer.parseInt(splitLine[0]);
               int to = Integer.parseInt(splitLine[1]);
               graph.addEdge(from, to);
            }
         }
      } catch (NumberFormatException numberFormatException) {
         throw new RuntimeException(String.format("File %s contains an error.", filePath), numberFormatException);
      }
      return graph;
   }
}

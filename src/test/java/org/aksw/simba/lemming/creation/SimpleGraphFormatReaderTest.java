package org.aksw.simba.lemming.creation;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.aksw.simba.lemming.ColouredGraph;
import org.junit.Test;


public class SimpleGraphFormatReaderTest {

   @Test
   public void testReadingCorrectFile() throws IOException {
      ColouredGraph graph = SimpleGraphFormatReader.readSimpleGraphFormatFile("creation/correct_simple_format_file.txt");
      assertEquals(3, graph.getVertices().size());
      assertEquals(3, graph.getGraph().getEdges().size());
      assertEquals(Arrays.asList(1, 2), graph.getOutNeighbors(0).toIntegerArrayList());
      assertEquals(Arrays.asList(0), graph.getOutNeighbors(1).toIntegerArrayList());
      assertEquals(Arrays.asList(), graph.getOutNeighbors(2).toIntegerArrayList());
   }


   @Test(expected = RuntimeException.class)
   public void testReadingFileWithNumberFormatError() throws IOException {
      SimpleGraphFormatReader.readSimpleGraphFormatFile("creation/number_format_error_simple_format_file.txt");
   }


   @Test(expected = RuntimeException.class)
   public void testReadingFileWithGeneralFormatError() throws IOException {
      SimpleGraphFormatReader.readSimpleGraphFormatFile("creation/general_format_error_simple_format_file.txt");
   }
}

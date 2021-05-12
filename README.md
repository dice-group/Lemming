# Lemming
LEMMING is an ExaMple MImickiNg graph Generator

## Approach overview

1. **Load RDF graphs**  

The classes responsible to load the RDF graphs are under the package `org.aksw.simba.lemming.creation`. The graphs are first read from file and are then converted to coloured graphs by `GraphCreator.java`.

2. **Initialize a draft graph** 

The mimic graph is initialized based on the target graph's metrics. All the generator types are located under `org.aksw.simba.lemming.mimicgraph.generator`.

3. **Optimize the graph** 

In `GraphOptimization.java`, two graphs are created by adding and removing an edge from the generated graph. The error score is then computed for these two graphs and the one with the lowest error score is chosen for the next iteration until either the number of maximum iterations has been reached or no improvement is found on the graph for the past 5 000 iterations.

4. **Finalize the graph with semantic data** 

The optimized graph is finalized as a real-world RDF graph in `GraphLexicalization.java` by rendering all the resources' IRIs.

## Process overview
1. Run metric computation on all graphs
2. Run graph generation without the target graph in the file path

Place the files present in ``https://hobbitdata.informatik.uni-leipzig.de/lemming/resources.zip`` and in the ``Input graphs/`` folder of ``https://hobbitdata.informatik.uni-leipzig.de/lemming/Experiments_data.zip`` under lemming's directory.

### Metrics pre-computation
First, the metrics need to be computed on all available graphs of its corresponding dataset: ``Experiments_data/Input graphs``. The pre-computation can be achieved by indicating the dataset through:

```
mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.PrecomputingValues" -Dexec.args="pg" 
```

This will produce a file named ``value_store.val`` to be used during graph generation. It is recommended to move/rename the previous metrics store before re-running the store generation.

### Graphs generation

<table>
  <tr><th align="left">Parameter</th><th>Required</th><th>Default</th><th>Description</th></tr>
  <tr><th align="left">-ds</th><td>True</td><td>NA</td><td>Dataset {pg, swdf, lgeo, geology}</td></tr>
  <tr><th align="left">-nv</th><td>True</td><td>NA</td><td>Desired number of vertices in the generated graph (number of vertices of the target graph)</td></tr>
  <tr><th align="left">-t</th><td>False</td><td>R</td><td>Type of graph generator {R, RR, C, CD, D, DD}</td></tr>
  <tr><th align="left">-l</th><td>False</td><td>Initialized_MimicGraph.ser</td><td> File path where to save the initialized mimic graph. If a graph already exists there, the mimic graph generation will be skipped and loaded from file instead.</td></tr>
  <tr><th align="left">-s</th><td>False</td><td>System.currentTimeMillis()</td><td>Seed for results reproduction.</td></tr>
  <tr><th align="left">-thrs</th><td>False</td><td>availableProcessors*4</td><td>Number of threads</td></tr>
  <tr><th align="left">-op</th><td>False</td><td>50 000</td><td>Number of optimization iterations</td></tr>
</table>

To run the graph generation, you can use maven's plugin:

```
mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.GraphGenerationTest" -Dexec.args="-ds pg -nv 792923 -t R -op 30000" 
```

To run the graph generation for the baseline generator, use:

```
mvn exec:java -Dexec.mainClass="org.aksw.simba.lemming.tools.BuildBaselineGraph" -Dexec.args="-ds pg -nv 792923" 
```

You should move the target graph before starting the graph generation. The target graph is also called held-out graph, it's usually the latest graph of the versioned dataset.

From the metrics pre-computation step, you can get the number of vertices of the target graph. This will serve as an input to the graph generation. 
Below is a table with the currently accepted datasets and the number of vertices of its target graph.

<table>
  <tr><th align="left">Dataset</th><th align="center">No. vertices</th><th>Folder</th><th>Description</th><th>Target graph</th></tr>
  <tr><th align="left">pg</th><td align="center">792 923</td><td>PersonGraph/</td><td>Person Graph (subset of DBpedia)</td><td align="center">2016-10</td></tr>
  <tr><th align="left">swdf</th><td align="center">45 420</td><td>SemanticWebDogFood/</td><td>Semantic Web Dog Food</td><td align="center">2015</td></tr>
  <tr><th align="left">lgeo</th><td align="center">591 649</td><td>LinkedGeoGraphs/</td><td>Linked Geo Data</td><td align="center">2015</td></tr>
  <tr><th align="left">geology</th><td align="center">1 281</td><td>GeologyGraphs/</td><td> International Chronostratigraphic Chart</td><td align="center">2018-1</td></tr>
</table>

### Reproducing experiments
You can use our script to generate the graphs for all generator types by specifying the dataset: ``./run_dataset.sh pg``. Before starting/switching datasets, make sure you have the right ``value_store.val`` file.

The metrics and constant expressions values can be found in ``LemmingEx.result``. 

The triple stores benchmark was done through [IGUANA](https://github.com/dice-group/IGUANA) on Virtuoso, Apache Jena Fuseki, GraphDB and Blazegraph triple stores. You can find the queries used for each dataset under ``Experiments_data/IGUANA experiments/queries``. The benchmarking should be run for each of the generated graphs and the target graph. Please note that the target graph in this step should be the pre-processed one (after type inference and materialization).

IGUANA produces a N-Triple file with the metrics of interest: Query Mixes Per Hour (QMPH), No. Queries Per Hour (NoQPH) and Queries Per Second (QPS). 

<!-- commented
These can be collected through the results file:

```
 <http://iguana-benchmark.eu/recource/391/1/1/-395538669>  <http://iguana-benchmark.eu/properties/noOfQueriesPerHour> "2854.432211867693"^^<http://www.w3.org/2001/XMLSchema#double> . 
 <http://iguana-benchmark.eu/recource/391/1/1/-395538669>  <http://iguana-benchmark.eu/properties/queryMixes> "135.92534342227108"^^<http://www.w3.org/2001/XMLSchema#double> . 
 <http://iguana-benchmark.eu/recource/391/1/1/-395538669/sparql0>  <http://iguana-benchmark.eu/properties/queriesPerSecond> "70.97457627118645"^^<http://www.w3.org/2001/XMLSchema#double> . 
```
-->


We also have scripts to manage the lifecycle of the triple stores, as well as upload the graphs to the triple store and starting IGUANA. The scripts may need changes depending on the location of triple stores binary files/installation. 
To use them, you need to specify the folder where the graphs are located: 

```
./exec_all.sh /home/lemming/generated_graphs/
```

### Used data and software

Internally, Lemming is using the [Grph library](http://www.i3s.unice.fr/~hogie/software/index.php).

For testing, we are using the [email-Eu-core network](https://snap.stanford.edu/data/email-Eu-core.html) published by the Stanford University. It has been transformed into a simple RDF file.



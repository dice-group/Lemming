# Lemming
This is the repository of [LEMMING](https://doi.org/10.1109/ICSC50631.2021.00015), an ExaMple MImickiNg graph Generator.

## Prerequisites and Project Build
### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or later.
- **Apache Maven**: Version 3.6.

### Building the project
You can either use the pre-built JAR file provided in this repository, or build it yourself using Maven:

```
mvn clean package
```
The JAR file will be located in the target directory after the build process is complete.

## How to run
LEMMING currently supports 2 graph generation processes.

### 1. Versioned graph generation

 The first as presented in [LEMMING](https://doi.org/10.1109/ICSC50631.2021.00015) requires a versioned dataset as input.

```
java -jar lemming.jar graph -ds <dataset> -nv <num_vertices> -thrs <threads> -c <class_selection> -v <vertex_selection>

```

**Parameters**

<table>
  <tr><th align="left">Parameter</th><th>Required</th><th>Default</th><th>Description</th></tr>
  <tr><th align="left">-ds</th><td>True</td><td>NA</td><td>Dataset {dbp, pg, swdf, lgeo, geology}</td></tr>
  <tr><th align="left">-nv</th><td>True</td><td>NA</td><td>Desired number of vertices in the generated graph (number of vertices of the target graph)</td></tr>
  <tr><th align="left">-thrs</th><td>False</td><td>1</td><td>Number of threads</td></tr>
  <tr><th align="left">-s</th><td>False</td><td>System.currentTimeMillis()</td><td>Seed for results reproduction.</td></tr>
  <tr><th align="left">-m</th><td>False</td><td>Binary</td><td>Generation type {Binary, Simplex, Bl}</td></tr>
  <tr><th align="left">-c</th><td>False</td><td>UCS</td><td>Type of class selector {UCS, BCS, CCS}</td></tr>
  <tr><th align="left">-v</th><td>False</td><td>UCS</td><td>Type of vertex selector {UIS, BIS}</td></tr>
  <tr><th align="left">-sp</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex property sampling scheme {BPSI, UPSI}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex class sampling scheme {BCSI, UCSI}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used for baseline generators {BA, WS}</td></tr>
  <tr><th align="left">-op</th><td>False</td><td>0</td><td>Number of optimization iterations</td></tr>
</table>

### 2. Single-version graph generation

This mode requires only one graph version as input and skips the preprocessing and the optimization stage as a result.

```
java -jar lemming.jar single-graph -ds <dataset> -nv <num_vertices> -thrs <threads> -c <class_selection> -v <vertex_selection>

```

### Preprocessing stage 

LEMMING includes a preprocessing stage where invariant arithmetic expressions are learned for a given dataset. This stage runs by default if LEMMING does not find the path to the preprocessed data. The expressions are saved in ``value_store.val``. However, you can explicitly run it using:

 ```
 java -jar lemming.jar store -ds <dataset>
 ```

## Approach overview

1. **Load RDF graphs**  

The classes responsible to load the RDF graphs are under the package `org.aksw.simba.lemming.creation`. The graphs are first read from file and are then converted to coloured graphs by `GraphCreator.java`.

2. **Initialize a draft graph** 

The mimic graph is initialized based on the target graph's metrics. All the generator types are located under `org.aksw.simba.lemming.mimicgraph.generator`.

3. **Optimize the graph** 

In `GraphOptimization.java`, two graphs are created by adding and removing an edge from the generated graph. The error score is then computed for these two graphs and the one with the lowest error score is chosen for the next iteration until either the number of maximum iterations has been reached or no improvement is found on the graph for the past 5 000 iterations.

4. **Finalize the graph with semantic data** 

The optimized graph is finalized as a real-world RDF graph in `GraphLexicalization.java` by rendering all the resources' IRIs.


<!-- commented
Below is a table with the currently accepted datasets and the number of vertices of its target graph.

<table>
  <tr><th align="left">Dataset</th><th align="center">No. vertices</th><th>Folder</th><th>Description</th><th>Target graph</th></tr>
  <tr><th align="left">pg</th><td align="center">792 923</td><td>PersonGraph/</td><td>Person Graph (subset of DBpedia)</td><td align="center">2016-10</td></tr>
  <tr><th align="left">swdf</th><td align="center">45 420</td><td>SemanticWebDogFood/</td><td>Semantic Web Dog Food</td><td align="center">2015</td></tr>
  <tr><th align="left">lgeo</th><td align="center">591 649</td><td>LinkedGeoGraphs/</td><td>Linked Geo Data</td><td align="center">2015</td></tr>
  <tr><th align="left">geology</th><td align="center">1 281</td><td>GeologyGraphs/</td><td> International Chronostratigraphic Chart</td><td align="center">2018-1</td></tr>
</table>
-->

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



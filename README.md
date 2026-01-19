[![Codacy Badge](https://app.codacy.com/project/badge/Grade/abe2f703880e4a61b44790dc2fe0a534)](https://www.codacy.com/gh/dice-group/Lemming/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/Lemming&amp;utm_campaign=Badge_Grade)

<p align="center">
	<img src="https://files.dice-research.org/projects/Lemming/logo.png" height="300" />
</p>


# LEMMING: Example Mimicking Knowledge Graph Generators
This is the repository of [LEMMING](https://doi.org/10.1109/ICSC50631.2021.00015), an ExaMple MImickiNg graph Generator, and **SimplexKG**, A Simplex Approach to Synthetic Knowledge Graph Generation (Link to be added).

LEMMING contains Synthetic Knowledge Graph Generators based on instance data.

## Prerequisites and Project Build
### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or later.

- **Apache Maven**: Version 3.6.

### Building the project
You can either use the pre-built JAR file provided in this repository, or build it yourself using:

```
mvn clean package
```
The JAR file will be located in the target directory after the build process is complete.

#### Sample run

```
mvn clean package
java -jar target/lemming.jar single-graph -ds test -dp src/test/resources/snippet_linkedgeo.nt -nv 10
```


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
  <tr><th align="left">-dp</th><td>True</td><td>NA</td><td>Dataset path. Only required in single-graph mode and when the dataset is not present in application.properties. </td></tr>
  <tr><th align="left">-nv</th><td>True</td><td>NA</td><td>Desired number of vertices in the generated graph (number of vertices of the target graph)</td></tr>
  <tr><th align="left">-thrs</th><td>False</td><td>1</td><td>Number of threads</td></tr>
  <tr><th align="left">-s</th><td>False</td><td>System.currentTimeMillis()</td><td>Seed for results reproduction.</td></tr>
  <tr><th align="left">-m</th><td>False</td><td>Binary</td><td>Generation type {Binary, Simplex, Baseline}</td></tr>
  <tr><th align="left">-c</th><td>False</td><td>UCS</td><td>Type of class selector {UCS, BCS, CCS}</td></tr>
  <tr><th align="left">-v</th><td>False</td><td>UCS</td><td>Type of vertex selector {UIS, BIS}</td></tr>
  <tr><th align="left">-sp</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex property sampling scheme, either biased or uniform. {BP, UP}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex class sampling scheme, either biased or uniform. {BC, UC}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used for baseline generators. Barabási–Albert and Watts–Strogatz. {BA, WS}</td></tr>
  <tr><th align="left">-op</th><td>False</td><td>0</td><td>Number of optimization iterations</td></tr>
</table>

### 2. Single-version graph generation

This mode requires only one graph version as input and skips the preprocessing and the optimization stage as a result.

```
java -jar lemming.jar single-graph -ds <dataset> -dp <dataset-path> -nv <num_vertices> -thrs <threads> -c <class_selection> -v <vertex_selection>
```

### Preprocessing stage 

LEMMING includes a preprocessing stage where invariant arithmetic expressions are learned for a given dataset. This stage runs by default if LEMMING does not find the path to the preprocessed data. The expressions are saved in ``value_store.val``. However, you can explicitly run it using:

 ```
 java -jar lemming.jar store -ds <dataset>
 ```
 
 **Parameters**
 
<table>
  <tr><th align="left">Parameter</th><th>Required</th><th>Default</th><th>Description</th></tr>
  <tr><th align="left">-ds</th><td>True</td><td>NA</td><td>Dataset {dbp, pg, swdf, lgeo, geology}</td></tr>
  <tr><th align="left">-dp</th><td>True</td><td>NA</td><td>Dataset path. Only required when the dataset is not present in application.properties. </td></tr>
  <tr><th align="left">--min-fitness</th><td>False</td><td>100000.0</td><td>Minimum Fitness</td></tr>
  <tr><th align="left">---max-iterations</th><td>False</td><td>50</td><td>Maximum number of iterations</td></tr>
</table>
 

### Used data and software

Internally, Lemming is using the [Grph library](http://www.i3s.unice.fr/~hogie/software/index.php).

For testing, we are using the [email-Eu-core network](https://snap.stanford.edu/data/email-Eu-core.html) published by the Stanford University. It has been transformed into a simple RDF file.

The [Lemming logo](https://hobbitdata.informatik.uni-leipzig.de/lemming/logo.png) has been created by [TortugaAttack](https://github.com/TortugaAttack).


# Reproducing experiments
Download the datasets with:

```
wget https://files.dice-research.org/projects/Lemming/datasets.tar.gz && tar -xzf datasets.tar.gz --remove-files
```

Generate the graphs for all generator types for all datasets:
 
```
bash generate_graphs.sh swdf 32
bash generate_graphs.sh lgeo 32
bash generate_graphs.sh geology 32
```

The triple stores benchmark was done through [IGUANA](https://github.com/dice-group/IGUANA) on Tentris, Virtuoso, Apache Jena Fuseki, GraphDB and Blazegraph triple stores. 
The benchmarking should be run for each of the generated graphs and the target graph. Please note that the target graph in this step should be the pre-processed one (after materialization).
We have prepared scripts to manage the lifecycle of the triplestores, as well as upload the graphs to the triple store and starting IGUANA:


```
bash run_all.sh /home/lemming/generated_graphs/
```

## Files
You can find the original LEMMING files in [here](https://files.dice-research.org/projects/Lemming/ICSC_2021/) and the SimplexKG files [here](https://files.dice-research.org/projects/Lemming/WWW_2026/).

# How to cite
```
@inproceedings{roeder2021lemming,
  author = {R{\"o}der, Michael and Nguyen, Pham Thuy Sy and Conrads, Felix and da Silva, Ana Alexandra Morim and Ngomo, Axel-Cyrille Ngonga},
  booktitle = {Proceedings of the 15th IEEE International Conference on Semantic Computing (ICSC)},
  doi = {10.1109/ICSC50631.2021.00015},
  pages = {62-69},
  publisher = {IEEE Computer Society},
  title = {LEMMING -- Example-based Mimicking of Knowledge Graphs},
  url = {https://doi.org/10.1109/ICSC50631.2021.00015},
  year = 2021
}
```

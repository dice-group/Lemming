# Lemming
This is a fork of the original [LEMMING](https://github.com/dice-group/Lemming) project which is used to implement different triangle counting algorithms as part of the course "Foundations of Knowledge Graphs" at the  UPB during the winterterm 17/18. 

## Implemented Approaches
We implemented the following approaches for counting (node-) triangles in graphs: 

- forward ([paper]((https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study)))
- duolion ([paper](https://www.researchgate.net/publication/221654480_DOULION_Counting_triangles_in_massive_graphs_with_a_coin))
- matrix multiplication ([paper]((https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study)))
- node-iterator ([paper]((https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study)))
- node-iterator-core ([paper]((https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study)))
- ayz ([paper](https://www.researchgate.net/publication/225621879_Finding_and_Counting_Given_Length_Cycles))

## Running an Evaluation
For running an evaluation (in terms of runtime) of the approaches, the [EvaluationRunner](https://github.com/BlackHawkLex/Lemming/blob/master/src/main/java/org/aksw/simba/lemming/tools/EvaluationRunner.java) can be used. It is designed to evaluate the algorithms on graphs from both the SemanticWebDogFood dataset and [SNAP](https://snap.stanford.edu/index.html). Therefore it expects a folder named "SemanticWebDogFood", containing a folder for each year of the dataset including the associated RDF files. Furthermore it expects a folder named "evaluation_datasets" containing a folder called "snap". The class will read all files in this folder, assuming they are in the simple format, which most graphs from snap are in. This format simply gives one edge per line, where each line consists of two integers (the node ids that are connected by the edge) separated by whitespace.

The EvaluationRunner will run all approaches, which are defined in the according method, on each of the graphs contained in the folder structure detailed above. Futhermore for each of these graphs, the evaluation process constructs reference graphs based on the associated original graph, which the algorithms are evaluated on as well. These reference graphs are
- star 
- grid
- ring
- clique
- complete bipartite graph

In order to assure that no algorithm is able to permanently stop the evaluation process, the EvaluationRunner contains a timeout definition, which can be adjusted manually. Using this timeout, the evaluation runs each algorithm on each of the graphs (where a graph is either an original graph or a reference graph) with the given timeout. If the algorithms does not finish the computation within the given time limit, it is interrupted.

The results of the evaluation are printed to a text file in a folder called "results" (which will be created if necessary), where each line contains the runtime of one algorithm run on a graph and all of the associated reference graphs. The runtime (in seconds) on the original graph is the first element of the line, followed by the runtimes on the reference graphs in the same order they are listed above. If an approach was interrupted due to a timeout, the runtime will be given as -1.

## Acknowledgements & References
- We are using the [email-Eu-core network](https://snap.stanford.edu/data/email-Eu-core.html) from [SNAP](https://snap.stanford.edu/index.html) for unit testing the implementations of the triangle counting algorithms.
- For a clear overview of different approaches for triangle counting we refer the interested reader to the experimental study paper by Schank and Wagner: [paper](https://www.researchgate.net/publication/221131490_Finding_Counting_and_Listing_All_Triangles_in_Large_Graphs_an_Experimental_Study)





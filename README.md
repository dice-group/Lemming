# Graph generator simplexes-based approach
We propose changes to [Lemming Repo](https://github.com/dice-group/Lemming/tree/pgmerge) such that it uses simplexes.

## Overview (Röder et al., [2021](https://ieeexplore.ieee.org/document/9364380))
Lemming works in five steps to generate the future output graph. Our approach updates its two steps (Step 1 and Step 3).

**Step 1** reads previous versions of input graphs, analyzes simplexes in them, and computes various statistics.

**Step 2** evaluates input to determine expressions.

**Step 3** uses simplexes to generate the output graph.

**Step 4** modifies the output graph using expressions.

**Step 5** creates the final version of the output graph.


## Execution
Our approach is tested on two datasets [Semantic web dog food](https://old.datahub.io/dataset/semantic-web-dog-food) (SWDF) and [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052) (LGEO). We have created a class that sets the expressions mentioned in [Lemming](https://ieeexplore.ieee.org/document/9364380) for these datasets. Thus, Step 2 need not be executed. To generate the graph with the proposed approach, an instance of class **GraphGenerationTest** should be invoked with the following parameters, and it supports existing parameters of [Lemming Repo](https://github.com/dice-group/Lemming/tree/pgmerge).

<table>
  <tr><th align="left">Parameter</th><th>Description</th><th>Detailed Description</th></tr>
  <tr><th align="left">-ds</th><td>Input dataset name</td><td>This parameter should be set to "swdf" and "lgeo" for SWDF and LGEO datasets.</td></tr>
  <tr><th align="left">-nv</th><td>Number of vertices in the output graph</td><td>For our testing, this parameter was set to "45420" and "591649" for SWDF and LGEO datasets, respectively. We generated the future graph for the year 2015 for both datasets.</td></tr>
  <tr><th align="left">-t</th><td>Generator to use for creating the future graph</td><td>To test the proposed generators, this parameter should be set to "S1" or "S2" or "S3" or "S4". In this proposed thesis, the parameter "S1" corresponds to Generator 1. Similarly, Generator 2 is defined for parameter "S2", and so on.</td></tr>
  <tr><th align="left">-mi</th><td>Maximum number of retries</td><td>When the approach is not able to create a simplex, it retries in multiple iterations. This parameter sets the number of retries until the approach terminates. Its default value is 5000 if not provided as input.</td></tr>
</table>

## Results
We executed each generator three times for both datasets. Existing generators were also executed for comparison with the proposed generators. 
The generated result files for the performed execution can be found in the folder "generated_results". This folder also consists of benchmarking results and console logs for the proposed generators. 

### .result files
- The result files are available in [result_files.zip](https://github.com/atulpundir88/Lemming-Simplexes/blob/generator_using_simplexes/generated_results/result_files.zip). 
- The zip file has two parent folders: "Lemming" and "Simplex". "Lemming" contains results for existing generators, and the results of the proposed generators are available in "Simplex".
- The folders further consist of the sub-folders "SWDF" and "LGD" for the two datasets. The "SWDF" folder contains results about [Semantic web dog food](https://old.datahub.io/dataset/semantic-web-dog-food), whereas the "LGD" folder contains results for [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052).
- The files within this folder follow naming conventions such that they end with "_\<Generator execution parameter\>_r\<execution_id\>.result". For example, a file name ending with _R_r1.result denotes the result file for the generator invoked with parameter "R" (Existing generator) for the first execution. 
- Complete Example (Existing generator): `result_files > Lemming > LGD > LemmingEx_C_r1.result` denotes the result file for the first execution of the generator with parameter "C" for the [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052) dataset.
- Complete Example (Proposed generator): `result_files > Simplex > SWDF > LemmingEx_S1_r3.result` denotes the result file for the third execution of the generator with parameter "S1" for the [Semantic web dog food](https://old.datahub.io/dataset/semantic-web-dog-food) dataset.
- Note: The approach specified within these result files might differ, and the file name indicates the generator. To locate a result file for a specific generator, the file name should be used.

### console logs
- We have saved console logs for the proposed generators, and they are in [console_logs.zip](https://github.com/atulpundir88/Lemming-Simplexes/blob/generator_using_simplexes/generated_results/console_logs.zip).
- They follow the same hierarchy as that defined for .result files.
- Example: `console_logs > Simplex > LGD > lgeo_S1_r1.txt` denotes the console logs for the first execution of the generator with parameter "S1" for the [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052) dataset.

### Benchmarking 
- Benchmarking was performed using [IGUANA](https://link.springer.com/chapter/10.1007/978-3-319-68204-4_5), and the generated results are available in [benchmarking.zip](https://github.com/atulpundir88/Lemming-Simplexes/blob/generator_using_simplexes/generated_results/benchmarking.zip).
- The initial folder hierarchy is same as the previous files. The parent folder name indicates the generators. The dataset-specific folders are defined for them. Then, folders are defined for each generator's execution parameter, and the files are present for every execution run in these folders.
- General folder hierarchy: `benchmarking > <Generator> > <Dataset name> > <Generator execution parameter> > r<execution_id>`
- Example: `benchmarking > Lemming > LGD > R > r1` - The files in this folder are for the first execution of the existing generator with parameter "R" for the [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052) dataset.
- The files found in a specific folder consists of results for different triple stores evaluated using [IGUANA](https://link.springer.com/chapter/10.1007/978-3-319-68204-4_5).


# Graph generator simplexes-based approach
We propose changes to [Lemming Repo](https://github.com/dice-group/Lemming/tree/pgmerge) such that it uses simplexes.

## Overview (Röder et al., [2021](https://ieeexplore.ieee.org/document/9364380))
Lemming works in five steps to generate succeeding output graph. Our approach updates its two steps (Step 1 and Step 3).

**Step 1** reads previous versions of input graphs, analyzes simplexes in them and computes various statistics.

**Step 2** evaluates input to determine expressions.

**Step 3** uses simplexes to generate the output graph.

**Step 4** modifies the output graph using expressions.

**Step 5** creates the final version of the output graph.


## Execution
Our approach can be executed on two datasets [Semantic web dog food](https://old.datahub.io/dataset/semantic-web-dog-food) (SWDF) and [LinkedGeoData](https://content.iospress.com/articles/semantic-web/sw052) (LGEO). We have created a class that sets the expressions mentioned in [Lemming](https://ieeexplore.ieee.org/document/9364380) for these datasets. Thus, Step 2 need not be executed. To generate the graph with the proposed approach, a class **GraphGenerationTest** should be executed with the following parameters. It support existing parameters of [Lemming Repo](https://github.com/dice-group/Lemming/tree/pgmerge).

<table>
  <tr><th align="left">Parameter</th><th>Description</th><th>Detailed Description</th></tr>
  <tr><th align="left">-ds</th><td>Input dataset name</td><td>This parameter should be set to "swdf" and "lgeo" for SWDF and LGEO datasets.</td></tr>
  <tr><th align="left">-nv</th><td>Number of vertices in the output graph</td><td>For our testing, this parameter was set to "45420" and "591649" for SWDF and LGEO datasets. We generated the future graph for the year 2015 for both the datasets.</td></tr>
  <tr><th align="left">-t</th><td>Generator to use for creating the future graph</td><td>To test the proposed generators, this parameter should set to "S1" or "S2" or "S3" or "S4". In this proposed thesis, the parameter "S1" corresponds to Generator 1. Similarly, Generator 2 is defiend for parameter "S2" and so on.</td></tr>
  <tr><th align="left">-mi</th><td>Maximum number of retries</td><td>When the approach is not able to create a simplex, it retries in multiple iteration. This parameter sets the number of retries until the approach terminates. Its default value is 5000 if not provided as input.</td></tr>
</table>

## Results
We executed each generator three times for both the datasets. In this repository, we upload generated result files along with console logs.

Benchmarking was also performed using [IGUANA](https://link.springer.com/chapter/10.1007/978-3-319-68204-4_5), the obtained results are also uploaded.


# SimplexKG
Source code repository of SimplexKG.
We extend LEMMING with our simplex-based generators.

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

### 1. Versioned graph input

We require a versioned dataset as input.

```
java -jar jarfile.jar graph -ds <dataset> -nv <num_vertices> -thrs <threads> -m Simplex -sc <simplex_class_sample> -sp <simplex_property_sample>

```

**Parameters**

<table>
  <tr><th align="left">Parameter</th><th>Required</th><th>Default</th><th>Description</th></tr>
  <tr><th align="left">-ds</th><td>True</td><td>NA</td><td>Dataset {swdf, lgeo, geology}</td></tr>
  <tr><th align="left">-nv</th><td>True</td><td>NA</td><td>Desired number of vertices in the generated graph (number of vertices of the target graph)</td></tr>
  <tr><th align="left">-thrs</th><td>False</td><td>1</td><td>Number of threads</td></tr>
  <tr><th align="left">-s</th><td>False</td><td>System.currentTimeMillis()</td><td>Seed for results reproduction.</td></tr>
  <tr><th align="left">-m</th><td>False</td><td>Binary</td><td>Generation type {Binary, Simplex, Bl}</td></tr>
  <tr><th align="left">-sp</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex property sampling scheme {BPSI, UPSI}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used in Simplex mode. Simplex class sampling scheme {BCSI, UCSI}</td></tr>
  <tr><th align="left">-sc</th><td>False</td><td>UCS</td><td>Only used for baseline generators {BA, WS}</td></tr>
  <tr><th align="left">-op</th><td>False</td><td>0</td><td>Number of optimization iterations</td></tr>
</table>


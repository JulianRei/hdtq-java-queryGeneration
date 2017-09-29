# Query generation for [HDTQ](https://github.com/JulianRei/hdtq-java)

## Overview

The query generator generates random quad queries for a given RDF dataset. 

For this, the RDF data must first be imported into HDTQ, a Jena TDB store and a Virtuoso instance.

## Prerequisites

Add [HDTQ Performance Tests](https://github.com/JulianRei/hdtq-java-performanceTests) to your local Maven repository.

## Compiling

Use mvn install to let Apache Maven install the required jars in your system.

## Usage

1. Import the RDF data into HDTQ using annotated triples (AT) or annotated graphs (AG) or both.
2. Import the RDF data into a Jena TDB store.
3. Import the RDF data into a Virtuoso instance.
4. Create a folder named after your dataset (e.g. "bear") and move the artefacts from step 1-3 into it. Then name the files/folders as follows:
```
bear
│   bearAT.hdt /* HDTQ AT file. Ideally, also place index file (bearAT.hdt.index.v1-1) here */
│   bearAG.hdt /* HDTQ AG file. Ideally, also place index file (bearAG.hdt.index.v1-1) here */
│   bear.tdb   /* Jena folder. */
│   bear.vir   /* Virtuoso folder. */

```
5. Start the generator (e.g. via Eclipse or via terminal: java -jar queryGeneration.jar) to see input parameters.

## License

LPGL

## Authors

Julian Reindorf <julian.reindorf@gmailcom>
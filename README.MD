CBR4SSPlatform

## Setup

This projects uses [java-dotenv](https://github.com/cdimascio/java-dotenv) to load environment variables from a .env file, 
so you have to make a copy of `example.env`, name it `.env` and  configure the desire variables

```bash
$ cp example.env .env
```


### Variables

Commented variables (i.e. line starts with `#` ) are set with default value. Uncomment them to set another one.


#### Database Variables:
```
DATABASE_HOST: Host of the Database (default 127.0.0.1)
DATABASE_PORT: Port of the Database (default 27017)

DATABASE_KB_NAME: Name of the knowledge base Database (default KB)
DATABASE_QUERIES_NAME: Name of the queries Database (default queries)
DATABASE_RESULTS_PREFIX: Prefix of the name of the results Database (default retrieved_cases)
DATABASE_LEARNED_PREFIX: Prefix where the learned cases will be added (besides of KB) to inspect them (default learned)
```


#### Runtime Environment Variables
```
VERBOSE: Choose if you want to run it in verbose mode (default false)
K: The number of k to use in the knn algorithm (default 10)
SELECTION_RULE: The selection algorithm rule, one of KNN, WKNN and DWKNN (default KNN)
DISTANCE_THRESHOLD: Threshold distance value to retrain the CBR (when is not present, the CBR does not learn) 
```

#### SOAML parser settings:

[See](https://github.com/rapkyt/SoaMLparsers/blob/master/README.md) 


## Selections Rules

### KNN

This selection method is one of the oldest and simplest classifiers. The basic rationale is defined as follows: 
A query is labelled by a majority vote of its k-nearest neighbors, i.e. the solution of a Case will be the most 
predominant in its k-nearest neighbors, this is a simple majority vote.

### WKNN

Dudani first introduced a weighted voting method for KNN, called the distance-weighted knearest neighbor rule (WKNN).
In WKNN, the closer neighbors are weighted more heavily than the farther ones, using the distance-weighted function. 
The weight w^i for i-th nearest neighbor is defined as follow:

TODO: insert math formula

### DWKNN
DWKNN is based on WKNN: to give different weights to k nearest neighbors according to their distances, 
with closer neighbors having greater weights. Nevertheless, different from the weights in WKNN, we assign to the i-th
nearest neighbor xNNi of the query x a dual weight wi, defined by the dual distance-weighted function as below:

TODO: insert math formula


## Running
We provide a way to run experiments with a docker environment, to ensure the quality of the experiments, i.e. all are 
run in the same environment, regardless of who run it. And also to make it easyer to new ones getting into it.


### Setup Docker
In order to be ready to parse specifications into cases, or run the Case Based Reasoning you should run in a console:
```bash
$ docker-compose build
$ cp docker.env .env
```

This will install all required dependencies and let you with a pre-configured .env file for you to start with it.

All that remains is to do is:
- Get a list of stopwords separated by a newline character, and saves them into `WordNet/stoplist.txt`
- Get the dict of WordNet and save it into `WordNet/dict`.


### Parsing specifications into cases and saves them into a Collection
If you want to load specifications (OAS or WSDL) into a collection you must set the required SoaMLParsers' settings in 
.env and run `./load_cases.sh`.

This will iterate over the files defined in `OAS_PATH` and `WSDL_PATH`, parsing specifications and saving them into  the
collection defined in `DATABASE_NAME`.


### Running Case Based Reasoning
If you want to run the CBR you must set described settings in *Variables* and run `docker-compose up` in a console.


### Exporting the results
To get a nice .json file with the result of the experiments you should run `./export_results.sh` in a console.

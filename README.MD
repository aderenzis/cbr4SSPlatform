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

DROP_DB: Choose if you want to drop the Database before the import (defualt false)
```

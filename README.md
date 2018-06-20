# ADDB-Jedis

## Introduction
Add jedis commands into original jedis source

### Command list
* fpwrite

* fpscan

* metakeys

## Requirements

* Build ADDB(Redis+RocksDB), configure and run redis-server
* Install maven for Java

## How to build

```
mvn clean install
```

## Commands in detail
* fpwrite - command that set key-value when call "INSERT" statement in relation database(SparkSQL)
	parameter: CommandArgsObject(datakey, partitionInfo, #ofColumn, indexInfo, data)
	return: reply

* fpscan - command that get key-value pairs when call "SELECT" statement in relation database(SparkSQL)
	parameter: CommandArgsObject(datakey, required column indices)
	return: List<String>

* metakeys - command that get partition key filtering improper partition based on "WHERE" clause
	parameter: Filter string
	return: Set<String>

## ADDB-SRConnector



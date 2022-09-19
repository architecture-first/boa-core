# BOA - Getting Started

## Overview

- The BOA platform supports a Business Oriented Architecture. A deeper dive on the concepts can be found here.
- It can be developed as part of an Architecture-First strategy.
- The guide will walk through setting up a minimalist environment and actor.
- The environment consists of a Vicinity, an Actor and a Redis instance.

## Prerequisites
- Install Docker
- Install Java
- Install Maven

## Setup
### Setup Vicinity
- This step will download a basic Vicinity and the associated Redis instance.

```shell
kubectl apply -f http://architecture-first/boa/basic-vicinity.yml
```

### Setup Actor
- This step will get the Actor framework.
```shell
## Generate the service 
mvn archetype:generate -DarchetypeGroupId=com.architecture.first -DarchetypeArtifactId=boa-actor -DarchetypeVersion=2022.9.1 -DgroupId=com.architecture.first -DartifactId=my-actor


## Build the service:
mvn clean package

## Run the service:
java -jar target/my-actor-1.0-SNAPSHOT.jar
```

## Basic Tutorial

Install the Basic Guide Actor to support tutorials.

```shell
kubectl apply -f http://architecture-first/boa/basic-guide-actor.yml
```

### Self Message

### Say Message
### Respond to Message

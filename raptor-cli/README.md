Raptor CLI
---

Command line utility of Raptor

###Setup

Install with maven from the parent pom project

###Usage

Setup the search index and the storage.

**Note** Using the `--force` flag the preceding indeces and storage data may be removed (based on underlyng implementation)

`raptor setup # --force`

Index data and objects definitions with a batch of 1500 elements

`raptor index -d objects -t data -b 1500`

Launch the IoT Broker. To launch just one component at time use one flag

`raptor launch --broker --http`

##License

(C) CREATE-NET

Licensed under Apache2

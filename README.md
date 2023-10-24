# Nexus-Patho-to-Fhir
Kafka processor transforms Nexus pathology report data to FHIR resources meeting MII pathology profiling

Details about applied MII FHIR profiles can be reviewed [Medizininformatik Initiative - Modul Pathologie](https://simplifier.net/MedizininformatikInitiative-ModulPathologie/~introduction)

## Configuration
We provide default code mapping, which is located at `resources/mappings` folder. 
Mapping information is provided via CSV files, expected columns and order is `localCode, localShortName, snomedCode, snomedDisplayName`. 

You may provide your own mappings:

| Key                                       | Content                                |
|-------------------------------------------|----------------------------------------|
| mapping.location.specimenType             | extracted specimen with extraction method |
| mapping.location.specimenExtractionMethod | extraction method                      |

## Workflow (TODO)

* explain expected input
* explain created topics and their purpose

### processor overview (wip)
This is how components will be organized: ![image processor overview](./doc/pathologie-processor-overview.png)

# Requirements
* Kafka 3.0+

# Deployment (TODO)
* Kafka connect
* sample environment variable file
* expected topic configuration

# Build
You may build a docker image `nexus-patho-to-fhir:latest' via
```
./gradlew bootBuildImage
```

# Development (TODO)
* explain environment variables


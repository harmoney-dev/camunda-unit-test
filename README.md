# Harmoney Camunda DMN Runner Utility

This utility helps in preparing and validate the decisions for DMN files from Camunda.

## Data Preparation

### Installing npm Dependencies

Ensure you have the necessary npm dependencies installed by running:
```
npm install
```
This will install all required packages for the DMN Tester App.

### Preparing the DMN CSV Files

You need to prepare the CSV files in advance to start the DMN unit test.

### Data Format

The CSV file should contain:
```
decisionId
dmnFile
input:XXX,output:XXX
value,value
```
The decisionId and dmnFile have to be 100% match to the actual ones in dmns/ folders

### Data Example
```
decisionId,DecisionFraudRules
dmnFile,fraud-rules.dmn
input:isBankAccountHolderMissing,input:isBankAccountHolderUnavailable,input:fraudCheckRisk,output:fraudCheckResult
True,False,0,Failed
False,True,0.8,Failed
False,False,0,Successful
False,False,0.8,Successful
```

### Generate the unit test config files
```
npm run data
```
You can see the generated files in dmnConfigs/

## Docker Commands
Examples on how to run the App and Unit Tests with Docker
### DMN Tester App
Starts the Webserver with the DMN Tester App.

Open http://localhost:8883 in your Browser.
### Docker Run
```
docker run \
  --name camunda-dmn-tester \
   --rm \
   -it \
   -e TESTER_CONFIG_PATHS="/dmnConfigs" \
   -v $(pwd)/dmns:/opt/docker/dmns \
   -v $(pwd)/dmnConfigs:/opt/docker/dmnConfigs \
   -p 8883:8883 \
   pame/camunda-dmn-tester
```
### Running the DMN Unit Tests via CI
This creates automatically Unit Tests of your DMN Tests and runs them with `sbt`.

In the end you will have the Test Reports (`target/test-reports`) you can show, for example in your CI-Tool.

### Docker Run
```
docker run \
  --name camunda-dmn-tester-ci \
   --rm \
   -it \
   -e TESTER_CONFIG_PATHS="/dmnConfigs" \
   -v $(pwd)/dmnConfigs:/opt/workspace/dmnConfigs \
   -v $(pwd)/dmns:/opt/workspace/dmns \
   -v $(pwd)/target:/opt/workspace/target \
   -v $HOME/.ivy2:/root/.ivy2 \
   pame/camunda-dmn-tester-ci
```
Edgent-BME280 Application
=========================

This application does:

- read sensor data from BME280 device
- publish local sensor data to MQTT server
- subscribe sensor data from a peer node and then print the discrepancy
  between local and remote sensor data if it is above a threshold


Requirements:

- Java version 7 or above
- Maven version 3 or above (for compilation)


To compile:

```
mvn compile package
```

The above command generates a self-contained JAR file
`target/edgent-bme280-1.0-SNAPSHOT-uber.jar`.


To run:

```
java -cp target/edgent-bme280-1.0-SNAPSHOT-uber.jar org.uog.socs.EdgentBME280 <local-ID> <remote-ID> <MQTT-Host>
```

License: [MIT](LICENSE)

package org.uog.socs;

import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import java.util.concurrent.TimeUnit;
import org.apache.edgent.connectors.mqtt.MqttStreams;

public class EdgentBME280 {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("Usage: java EdgentBME280 <local-ID> <remote-ID> <mqtt-host>");
            System.exit(1);
        }

        String localID = args[0];
        String remoteID = args[1];
        String mqttHost = args[2];

        System.out.println("localID:" + localID + ", remoteID:" + remoteID + ", MQTT-Host:" + mqttHost);

        // BME 280 sensor driver
        BME280 bme280 = new BME280();
        Thread t = new Thread(bme280);
        t.start();    
        
        // create a provider
        DirectProvider dp = new DirectProvider();

        // create a topology
        Topology top = dp.newTopology();

        String mqttURL = "tcp://" + mqttHost + ":1883";
        MqttStreams mqttPub = new MqttStreams(top, mqttURL, null);
       
        // build the topology
        long period = 1; // in seconds
        TStream<String> temperatureStream = top.poll(() -> String.valueOf(bme280.getTemperature()), period, TimeUnit.SECONDS);
        TStream<String> humidityStream = top.poll(() -> String.valueOf(bme280.getHumidity()), period, TimeUnit.SECONDS);
        TStream<String> pressureStream = top.poll(() -> String.valueOf(bme280.getPressure()), period, TimeUnit.SECONDS);
        mqttPub.publish(temperatureStream, localID + "/temperature", 0, false);
        mqttPub.publish(humidityStream, localID + "/humidity", 0, false);
        mqttPub.publish(pressureStream, localID + "/pressure", 0, false);

        temperatureStream.map((s) -> "Local temperature: " + s + " C").print();
        humidityStream.map((s) -> "Local humidity: " + s + " %").print();
        pressureStream.map((s) -> "Local pressure: " + s + " hPa").print();

        new MqttStreams(top, mqttURL, null)
            .subscribe(remoteID + "/temperature", 0)
            .map((s) -> Double.valueOf(s) - bme280.getTemperature())
            .filter(d -> Math.abs(d) > 1)
            .map(d -> "Temperature differs by " + d + " C")
            .print();
        new MqttStreams(top, mqttURL, null)
            .subscribe(remoteID + "/humidity", 0)
            .map((s) -> Double.valueOf(s) - bme280.getHumidity())
            .filter(d -> Math.abs(d) > 3)
            .map(d -> "Humidity differs by " + d + " %")
            .print();
        new MqttStreams(top, mqttURL, null)
            .subscribe(remoteID + "/pressure", 0)
            .map((s) -> Double.valueOf(s) - bme280.getPressure())
            .filter(d -> Math.abs(d) > 4)
            .map(d -> "Pressure differs by " + d + " hPa")
            .print();

        // submit the topology
        dp.submit(top);
    }
}

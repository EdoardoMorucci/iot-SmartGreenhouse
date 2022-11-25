package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {

    public static void main(String[] args) throws SocketException, MqttException {

        CollectorCoapServer server = new CollectorCoapServer();
        CollectorMqtt collectorMqtt = new CollectorMqtt();
        //server.add(new CoAPResourceExample("hello"));
        server.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command = "";

        System.out.println("Choose a command!!:");

        while(true){
            try {
                command = reader.readLine();
                System.out.printf("%s \n", command);

                //server.printWaterLevel();


            } catch (IOException e) {
                System.out.println("Command not found!");
            }

        }

    }
}

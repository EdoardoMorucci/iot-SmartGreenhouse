package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {

    public static void main(String[] args) throws SocketException {

        CollectorCoapServer server = new CollectorCoapServer();
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

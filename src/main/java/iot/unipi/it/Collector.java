package iot.unipi.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Collector {

    public static void main(String[] args) {

        //CollectorCoapServer server = new CollectorCoapServer();

        //server.add(new CoAPResourceExample("hello"));
        //server.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command = "";

        while(true){
            try {

                command = reader.readLine();
                System.out.printf("%s!!!\n", command);

            } catch (IOException e) {
                System.out.println("Command not found, please retry!");
            }

        }

    }
}

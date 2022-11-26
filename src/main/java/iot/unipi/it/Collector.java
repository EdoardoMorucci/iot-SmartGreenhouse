package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {

    public static void main(String[] args) throws SocketException, MqttException {

        CollectorCoapServer coapServer = new CollectorCoapServer();
        //CollectorMqtt collectorMqtt = new CollectorMqtt();
        //server.add(new CoAPResourceExample("hello"));
        coapServer.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command = "";


        printCommands();

        while(true){
            try {
                command = reader.readLine();
                String[] splittedCommand = command.split(" ");

                switch (splittedCommand[0]) {
                    case "!help":
                        printCommands();
                        break;

                    case "!currentWater":
                        coapServer.getCurrentWater();

                    case "!waterLevelSensor":
                        coapServer.printWaterLevelSensor();

                    case "!currentpH":
                        coapServer.getCurrentpH();

                    case "!pHSensor":
                        coapServer.printpHSensor();

                    case "!printHistorypH":
                        if(checkNumberParameter(splittedCommand, 3)){
                            DatabaseManager.print_data("pH", splittedCommand[1], splittedCommand[2]);
                        }

                    case "!printHistoryWaterLevel":
                        if(checkNumberParameter(splittedCommand, 3)) {
                            DatabaseManager.print_data("water_level", splittedCommand[1], splittedCommand[2]);
                        }

                    default:
                        System.out.println("This command is not available.");
                        printCommands();
                        break;
                }


                System.out.printf("%s \n", command);

                //server.printWaterLevel();


            } catch (IOException e) {
                System.out.println("Command not found!");
            }

        }

    }

    private static boolean checkNumberParameter(String[] splittedCommand, int parameters){
        if(splittedCommand.length != parameters) {
            System.out.println("   insert the current number of parameter (" + parameters + ")!");
            return false;
        }
        return true;
    }


    private static void printCommands(){
        System.out.println(
                "----------   SMART GREENHOUSE APPPLICATION   ----------\n" +
                "Choose a command:\n" +
                "   !help \n" +
                "   !currentWater --> get the current value of the water tank \n" +
                "   !waterLevelSensor --> print the ip address of the water level sensor (if present) \n" +
                "   !currentpH --> get the current value of the water pH in the tank\n" +
                "   !pHSensor --> print the ip address of the pH sensor (if present)\n" +
                "   !printHistorypH <limit> <offset>\n " +
                "   !printHistoryWaterLevel <limit> <offset>\n "
                );
    }


}

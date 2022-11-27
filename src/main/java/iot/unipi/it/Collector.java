package iot.unipi.it;

import iot.unipi.it.coap.CoapClientHandler;
import iot.unipi.it.coap.CollectorCoapServer;
import iot.unipi.it.db.DatabaseManager;
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
                        CoapClientHandler.onePrintWater = true;
                        break;
                    case "!continuousWater":
                        CoapClientHandler.continuosPrintWater = true;
                        break;
                    case "!waterLevelSensor":
                        coapServer.printWaterLevelSensor();
                        break;
                    case "!currentpH":
                        coapServer.getCurrentpH();
                        CoapClientHandler.onePrintPH = true;
                        break;
                    case "!continuouspH":
                        CoapClientHandler.continuosPrintPH = true;
                        break;
                    case "!pHSensor":
                        coapServer.printpHSensor();
                        break;
                    case "!printHistorypH":
                        if(checkNumberParameter(splittedCommand, 3)){
                            DatabaseManager.print_data("ph", splittedCommand[1], splittedCommand[2]);
                        }
                        break;
                    case "!printHistoryWaterLevel":
                        if(checkNumberParameter(splittedCommand, 3)) {
                            DatabaseManager.print_data("waterlevel", splittedCommand[1], splittedCommand[2]);
                        }
                        break;
                    case "!stop":
                        CoapClientHandler.continuosPrintWater = false;
                        CoapClientHandler.continuosPrintPH = false;
                        break;
                    default:
                        System.out.println("This command is not available.");
                        printCommands();
                        break;
                }

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
                "Choose a command from the list:\n" +
                "   !help \n" +
                "   !currentWater --> get the current value of the water tank \n" +
                "   !continuousWater --> continuous print of water level received, stop using !stop\n" +
                "   !waterLevelSensor --> print the ip address of the water level sensor (if present) \n" +
                "   !currentpH --> get the current value of the water pH in the tank\n" +
                "   !continuousPH --> continuous print of pH received, stop using !stop\n" +
                "   !pHSensor --> print the ip address of the pH sensor (if present)\n" +
                "   !printHistorypH <limit> <offset>\n " +
                "   !printHistoryWaterLevel <limit> <offset>\n " +
                "   !stop --> stop continuous printing \n"
                );
    }


}

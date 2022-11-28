package iot.unipi.it;

import iot.unipi.it.coap.CoapClientHandler;
import iot.unipi.it.coap.CollectorCoapServer;
import iot.unipi.it.db.DatabaseManager;
import iot.unipi.it.mqtt.CollectorMqtt;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class Collector {

    public static void main(String[] args) throws SocketException, MqttException {

        CollectorCoapServer coapServer = new CollectorCoapServer();
        CollectorMqtt collectorMqtt = new CollectorMqtt();
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

                    case "!currentTemperature":
                        System.out.printf("The current temperature is %.1f\n",collectorMqtt.getCurrentTemp());
                        break;

                    case "!currentHumidity":
                        System.out.println("The current humidity is " + collectorMqtt.getCurrentHumidity());
                        break;

                    case "!printHistoryTemperature":
                        if(checkNumberParameter(splittedCommand, 3)) {
                            DatabaseManager.print_data("temperature", splittedCommand[1], splittedCommand[2]);
                        }
                        break;

                    case "!printHistoryHumidity":
                        if(checkNumberParameter(splittedCommand, 3)) {
                            DatabaseManager.print_data("humidity", splittedCommand[1], splittedCommand[2]);
                        }
                        break;

                    case "!continuousHumidity":
                        collectorMqtt.setContinuousHum(true);
                        break;

                    case "!continuousTemperature":
                        collectorMqtt.setContinuousTemp(true);
                        break;

                    case "!stop":
                        CoapClientHandler.continuosPrintWater = false;
                        CoapClientHandler.continuosPrintPH = false;
                        collectorMqtt.setContinuousTemp(false);
                        collectorMqtt.setContinuousHum(false);
                        break;

                    default:
                        printCommands();
                        System.out.printf("%s \n", command);
                        System.out.println("This command is not available.");
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
                "   !currentWater --> (COAP) get the current value of the water tank \n" +
                "   !currentpH --> (COAP) get the current value of the water pH in the tank\n" +
                "   !currentTemperature --> (MQTT) get the current temperature in thr greenhouse\n" +
                "   !currentHumidity --> (MQTT) get the current soil humidity expressed as percentage\n" +
                "   !continuousWater --> (COAP) continuous print of water level received, stop using !stop\n" +
                "   !continuousPH --> (COAP) continuous print of pH received, stop using !stop\n" +
                "   !continuousHumidity --> (MQTT) continuous print of Humidity received, stop using !stop\n" +
                "   !continuousTemperature --> (MQTT) continuous print of Temperature received, stop using !stop\n" +
                "   !waterLevelSensor --> (COAP) print the ip address of the water level sensor (if present)\n" +
                "   !pHSensor --> (COAP) print the ip address of the pH sensor (if present)\n" +
                "   !printHistorypH <limit> <offset>\n" +
                "   !printHistoryWaterLevel <limit> <offset>\n" +
                "   !printHistoryTemperature <limit> <offset>\n" +
                "   !printHistoryHumidity <limit> <offset>\n" +
                "   !printHistoryWaterLevel <limit> <offset>\n" +
                "   !stop --> stop continuous printing \n"
                );
    }


}

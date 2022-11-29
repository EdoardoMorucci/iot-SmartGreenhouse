package iot.unipi.it.coap;

import iot.unipi.it.db.DatabaseManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static java.lang.Thread.sleep;

public class CoapClientHandler {

    private CoapClient clientWaterLevel;
    private CoapClient clientLedWater;
    private CoapClient clientPH;
    private CoapClient clientLedPH;
    private CoapObserveRelation observeSensor;
    private CoapObserveRelation observeSensor2;

    private static CoapClientHandler instance = null;

    public static boolean onePrintPH = false;
    public static boolean onePrintWater = false;
    public static boolean continuosPrintPH = false;
    public static boolean continuosPrintWater = false;

    private static long startTime;

    public static CoapClientHandler getInstance() {
        if (instance == null) {
            instance = new CoapClientHandler();
            startTime = System.currentTimeMillis();
        }
        return instance;
    }


    public void registerWaterLevel(String ipAddress) {
        if(clientWaterLevel != null) {
            System.out.println("The water level sensor: " + ipAddress + " is ALREADY registered!!! \n");
            return;
        }
        System.out.println("The water level sensor: " + ipAddress + " is registered!!\n");
        clientWaterLevel = new CoapClient("coap://[" + ipAddress + "]/water_level_sensor");

        observeSensor = clientWaterLevel.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        handleWaterLevelResponse(coapResponse);
                    }

                    @Override
                    public void onError() {
                        System.out.println("OBSERVING WATER LEVEL FAILED");
                    }
                }
        );
    }

    private void handleWaterLevelResponse(CoapResponse coapResponse){
        try {
            JSONObject responseText = new JSONObject(coapResponse.getResponseText());
            String state_water_level;

            //System.out.printf("[WaterLevel]: %s\n", responseText.toString());

            if (responseText.has("water_level")) {
                int water_level = responseText.getInt("water_level");
                int secondsFromStart = responseText.getInt("timestamp");
                int int_state;
                if (water_level < 700) {
                    state_water_level = "low";
                    int_state = 0;
                } else if (water_level > 1400) {
                    state_water_level = "high";
                    int_state = 1;
                } else {
                    state_water_level = "medium";
                    int_state = 2;
                }

                // change led status, making a put on led actuator
                if(clientLedWater != null)
                    changeLedWater(String.valueOf(water_level));

                if (continuosPrintWater || onePrintWater) {
                    System.out.printf("The water level is: %d (%s)\n", water_level, state_water_level);
                    if (onePrintWater)
                        onePrintWater = false;
                }


                Timestamp timestamp = new Timestamp(startTime + (secondsFromStart * 1000L));
                DatabaseManager.insert_water_level(water_level, int_state, "liter", timestamp);
            }

        } catch (Exception e) {
            System.err.println("[ERROR]: Message received from water level sensor NOT VALID. \n" + e.getMessage());
            // e.printStackTrace();
        }
    }

    public void registerLedWater(String ipAddress) {
        if(clientLedWater != null) {
            System.out.println("The actuators leds of water: " + ipAddress + " is ALREADY registered!!! \n");
            return;
        }

        System.out.println("The actuators leds of water: " + ipAddress + " is registered!!! \n");
        clientLedWater = new CoapClient("coap://[" + ipAddress + "]/res_led_water");

        try {
            sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void changeLedWater(String responseString){
        clientLedWater.put(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                if (coapResponse != null) {
                    if (!coapResponse.isSuccess()){
                        System.out.println("Error on led water actuator.");
                    }
                }
            }

            @Override
            public void onError() {
                //System.err.println("[ERROR]: Led Water: " + clientRipeningNotifier.getURI() + "] ");
                System.err.println("[ERROR] led water actuator");
            }
        }, responseString, MediaTypeRegistry.TEXT_PLAIN);
    }

    public void registerPH(String ipAddress) {
        if(clientPH != null) {
            System.out.println("The pH sensor: " + ipAddress + " is ALREADY registered!!! \n");
            return;
        }

        System.out.println("The pH sensor: " + ipAddress + " is registered!!\n");
        clientPH = new CoapClient("coap://[" + ipAddress + "]/pH_sensor");

        observeSensor2 = clientPH.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        handlePHResponse(coapResponse);
                    }

                    @Override
                    public void onError() {
                        System.out.println("OBSERVING PH FAILED");
                    }
                }
        );
    }

    private void handlePHResponse(CoapResponse coapResponse){
        try {
            JSONObject responseText = new JSONObject(coapResponse.getResponseText());

            // System.out.printf("[PH] %s\n", responseText.toString());
            String state_pH;
            if (responseText.has("pH")) {
                float pH = responseText.getFloat("pH");
                int secondsFromStart = responseText.getInt("timestamp");
                int int_state;
                if (pH < 6.5 || pH > 7.5) {
                    if (pH < 6 || pH > 8) {
                        state_pH = "bad";
                        int_state = 0;
                    } else {
                        state_pH = "not so good";
                        int_state = 1;
                    }
                } else {
                    state_pH = "good";
                    int_state = 2;
                }

                // change led status, making a put on led actuator
                if (clientLedPH != null)
                    changeLedPH(String.valueOf(pH));

                if (continuosPrintPH || onePrintPH) {
                    System.out.printf("The pH is: %.1f (%s)\n", pH, state_pH);
                    if (onePrintPH)
                        onePrintPH = false;
                }

                // long now = System.currentTimeMillis();
                // Timestamp sqlTimestamp = new Timestamp(now);
                Timestamp timestamp = new Timestamp(startTime + (secondsFromStart * 1000L));
                DatabaseManager.insert_pH(pH, int_state, timestamp);
            }

        } catch (Exception e) {
            System.err.println("[ERROR]: Message received from pH sensor NOT VALID. err:" + e.getMessage() + "\n");
            // e.printStackTrace();
        }
    }

    public void registerLedPH(String ipAddress) {
        if(clientLedPH != null) {
            System.out.println("The actuators leds of pH: " + ipAddress + "is ALREADY registered!!! \n");
            return;
        }

        System.out.println("The actuators leds of water: " + ipAddress + "is registered!!! \n");
        clientLedPH = new CoapClient("coap://[" + ipAddress + "]/res_led_ph");

        try {
            sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void changeLedPH(String responseString){
        clientLedPH.put(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                if (coapResponse != null) {
                    if (!coapResponse.isSuccess()){
                        System.out.println("Error on led PH actuator.");
                    }
                }
            }

            @Override
            public void onError() {
                System.err.println("[ERROR] led ph actuator");
            }
        }, responseString, MediaTypeRegistry.TEXT_PLAIN);
    }

    public void printWaterLevelSensor() {
        if(clientWaterLevel != null) {
            System.out.printf("Water Level sensor: %s\n", clientWaterLevel.getURI());
        }
    }

    public void printpHSensor() {
        if(clientPH != null) {
            System.out.printf("pH sensor: %s\n", clientPH.getURI());
        }
    }

    public void getCurrentWater() {
        if(clientWaterLevel != null) {
            CoapResponse coapResponse = clientWaterLevel.get();
            handleWaterLevelResponse(coapResponse);
        }
    }

    public void getCurrentpH() {
        if(clientPH != null) {
            CoapResponse coapResponse = clientPH.get();
            handlePHResponse(coapResponse);
        }
    }

    public void deleteLedWater(){
        clientLedWater = null;
    }

    public void deleteWaterLevel(){
        clientWaterLevel = null;
    }

    public void deleteLedPH(){
        clientLedPH = null;
    }

    public void deletePH(){
        clientPH = null;
    }

}

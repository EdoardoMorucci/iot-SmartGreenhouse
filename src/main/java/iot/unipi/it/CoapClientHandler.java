package iot.unipi.it;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;


import javax.xml.crypto.Data;
import java.awt.geom.RectangularShape;
import java.sql.Timestamp;

import static java.lang.Thread.sleep;

public class CoapClientHandler {

    private CoapClient clientWaterLevel;
    private CoapClient clientLedWater;
    private CoapObserveRelation observeSensor;

    private String state_water_level;

    private static CoapClientHandler instance = null;

    public static CoapClientHandler getInstance() {
        if (instance == null) {
            instance = new CoapClientHandler();
        }
        return instance;
    }

    public void registerLedWater(String ipAddress) {
        System.out.println("The actuators leds of water: " + ipAddress + "is registered!!! \n");
        clientLedWater = new CoapClient("coap://[" + ipAddress + "]/res_led_water");
        //System.out.println("URI: "+clientLedWater.getURI()+ "\n");
        //System.out.println("ping: "+ clientLedWater.ping() + "\n");
        //System.out.println("ping: "+ clientLedWater. + "\n");

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

    public void registerWaterLevel(String ipAddress) {

        /*
        if(clientWaterLevel != null) {
            System.out.println("Water level sensor already registered on: " + ipAddress + ".\n");
            return;
        }*/
        System.out.println("The water level sensor: " + ipAddress + " is registered!!\n");
        clientWaterLevel = new CoapClient("coap://[" + ipAddress + "]/water_level_sensor");

        observeSensor = clientWaterLevel.observe(
                new CoapHandler() {
                    @Override
                    public void onLoad(CoapResponse coapResponse) {
                        System.out.println("success: " + coapResponse.isSuccess()+ "\n");
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

            System.out.printf("TESTO RICEVUTO: %s\n", responseText.toString());

            if (responseText.has("water_level")) {
                int water_level = responseText.getInt("water_level");
                int timestamp = responseText.getInt("timestamp");
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
                changeLedWater(String.valueOf(water_level));

                System.out.printf("The water level is: %s\n", state_water_level);

                long now = System.currentTimeMillis();
                Timestamp sqlTimestamp = new Timestamp(now);
                //System.out.println("currentTimeMillis     : " + now);
                //System.out.println("SqlTimestamp          : " + sqlTimestamp);
                //System.out.println("SqlTimestamp.getTime(): " + sqlTimestamp.getTime());

                DatabaseManager.insert_water_level(water_level, int_state, "liter", sqlTimestamp);
            }

        } catch (Exception e) {
            System.err.println("[ERROR]: Message received from water level sensor NOT VALID. \n" + e.getMessage());
            // e.printStackTrace();
        }
    }

    public void printWaterLevelSensor() {
        System.out.printf("Water Level sensor: %s\n", clientWaterLevel.getURI());
    }





}

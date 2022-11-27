package iot.unipi.it.mqtt;

import iot.unipi.it.db.DatabaseManager;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

import java.sql.Timestamp;


public class CollectorMqtt implements MqttCallback {

    private final String broker = "tcp://127.0.0.1:1883";
    private final String clientId = "JavaApp";
    private final String temperatureSens = "temperature";
    private final String humiditySens = "humidity";
    private final String windowAct = "window";
    private final String lightAct = "light";
    private final String humidityAct = "humidityActuator";

    //status of the actuators
    private String windowStatus = "Closed";
    private String lightStatus = "Off";
    private String tapStatus = "Closed";

    private boolean continuousTemp = false;
    private boolean continuousHum = false;

    //UB and LB of temperature and humidity + current measurements
    private final float MAX_TEMP = 25.1f;
    private final float MIN_TEMP = 10.1f;
    private final int MAX_HUMIDITY = 100;
    private final int MIN_HUMIDITY = 5;
    private float currentTemp;
    private int currentHumidity;
    private MqttClient mqttClient = null;
    public CollectorMqtt() throws MqttException {
        do{
            try{
                Thread.sleep(5000);
                this.mqttClient = new MqttClient(this.broker, this.clientId);
                this.mqttClient.setCallback(this);
                this.mqttClient.connect();
                //Now we can subscribe to the topics
                this.mqttClient.subscribe(this.temperatureSens);
                System.out.printf("Subscribed to %s topic!\n", this.temperatureSens);
                this.mqttClient.subscribe(this.humiditySens);
                System.out.printf("Subscribed to %s topic!\n", this.humiditySens);
            }catch(MqttException | InterruptedException mqtte){
                System.out.println("Error during the connection");
            }
        }while(!this.mqttClient.isConnected());

    }

    private void publishMessage(String topic, String message){
        try{
            MqttMessage messageToSend = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, messageToSend);
            System.out.printf("Message sent on topic %s\n", topic);
        }catch(MqttException mqtte){
            mqtte.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection lost, attempting to reconnect...");
        int reconnWindow = 3000;
        while(!mqttClient.isConnected()){
            try{
                System.out.println("Reconnecting in " + reconnWindow/1000 + " seconds");
                Thread.sleep(reconnWindow);
                System.out.println("Reconnecting . . .");
                mqttClient.connect();
                reconnWindow *= 2;
                //Now we can subscribe to the topics
                mqttClient.subscribe(temperatureSens);
                System.out.printf("Resubscribed to %s topic!\n", temperatureSens);
                mqttClient.subscribe(humiditySens);
                System.out.printf("Resubscribed to %s topic!\n", humiditySens);
            }catch(MqttException | InterruptedException mqtte){
                System.out.println("Error during the reconnection " + throwable.getCause().getMessage());
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        byte[] payload = message.getPayload();
        JSONObject jsonPayload = new JSONObject(new String(payload));
        if(topic.equals(temperatureSens)){
            if (jsonPayload.has("temperature") && jsonPayload.has("timestamp")) {
                if(continuousTemp) {
                    System.out.printf("[%s] %s\n", topic, new String(payload));
                }
                currentTemp = jsonPayload.getFloat("temperature");
                int time = jsonPayload.getInt("timestamp");

                DatabaseManager.insert_temperature(currentTemp, "Celsius", time);

                if (currentTemp < MAX_TEMP && currentTemp > MIN_TEMP) {
                    if(continuousTemp) {
                        System.out.println("The temperature is ok");
                    }
                    if (windowStatus.equals("Open")) {
                        if(continuousTemp) {
                            System.out.println("We need to close the window");
                        }
                        publishMessage(windowAct, "close");
                        windowStatus = "Closed";
                    }
                    if (lightStatus.equals("On")) {
                        if(continuousTemp) {
                            System.out.println("We need to switch off the light");
                        }
                        publishMessage(lightAct, "off");
                        lightStatus = "Off";
                    }
                } else if (currentTemp > MAX_TEMP) {
                    if (windowStatus.equals("Closed")) {
                        if(continuousTemp) {
                            System.out.println("Temperature too high, we need to open the window");
                        }
                        publishMessage(windowAct, "open");
                        windowStatus = "Open";
                    }
                } else if (currentTemp < MIN_TEMP) {
                    if (lightStatus.equals("Off")) {
                        if(continuousTemp) {
                            System.out.println("Temperature too low, we need to switch on the light");
                        }
                        publishMessage(lightAct, "on");
                        lightStatus = "On";
                    }
                }
            }else{
                System.out.println("Message not correct!");
            }
        } else if (topic.equals(humiditySens)) {
            if (jsonPayload.has("humidity") && jsonPayload.has("timestamp")) {
                if(continuousTemp) {
                    System.out.printf("[%s] %s\n", topic, new String(payload));
                }
                currentHumidity = jsonPayload.getInt("humidity");
                int time = jsonPayload.getInt("timestamp");

                DatabaseManager.insert_humidity(currentHumidity, "%", time);

                if (currentHumidity < MIN_HUMIDITY) {
                    if (tapStatus.equals("Closed")) {
                        if(continuousTemp) {
                            System.out.println("We need to switch on the irrigation system");
                        }
                        tapStatus = "Open";
                        publishMessage(humidityAct, "open");
                    }
                } else if (currentHumidity == MAX_HUMIDITY) {
                    if (tapStatus.equals("Open")) {
                        if(continuousTemp) {
                            System.out.println("We need to switch off the irrigation system");
                        }
                        tapStatus = "Closed";
                        publishMessage(humidityAct, "close");
                    }
                }
            }else{
                System.out.println("Message not correct!");
            }
        }
    }

    public float getCurrentTemp(){
        return currentTemp;
    }

    public int getCurrentHumidity(){
        return currentHumidity;
    }

    public void setContinuousTemp(boolean status){
        continuousTemp = status;
    }

    public void setContinuousHum(boolean status){
        continuousHum = status;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery is complete!");
    }
}

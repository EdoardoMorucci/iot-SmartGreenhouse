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
    private final int MAX_HUMIDITY = 90;
    private final int MIN_HUMIDITY = 20;
    private float currentTemp;
    private int currentHumidity;
    private MqttClient mqttClient = null;

    private static long startTime;

    public CollectorMqtt() throws MqttException {
        do{
            try{
                startTime = System.currentTimeMillis();
                Thread.sleep(5000);
                this.mqttClient = new MqttClient(this.broker, this.clientId);
                this.mqttClient.setCallback(this);
                this.mqttClient.connect();
                //Now we can subscribe to the topics
                this.mqttClient.subscribe(this.temperatureSens);
                System.out.printf("Subscribed to %s topic!\n", this.temperatureSens);
                this.mqttClient.subscribe(this.humiditySens);
                System.out.printf("Subscribed to %s topic!\n", this.humiditySens);
            }catch(MqttException | InterruptedException mqtte) {
                System.out.println("Error during the connection");
            }
        }while(!this.mqttClient.isConnected());

    }

    private void publishMessage(String topic, String message){
        try{
            MqttMessage messageToSend = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, messageToSend);
            //System.out.printf("Message sent on topic %s\n", topic);
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

                currentTemp = jsonPayload.getFloat("temperature");
                int secondsFromStart = jsonPayload.getInt("timestamp");

                Timestamp timestamp = new Timestamp(startTime + (secondsFromStart * 1000L));
                DatabaseManager.insert_temperature(currentTemp, "Celsius", timestamp);

                if (currentTemp < MAX_TEMP && currentTemp > MIN_TEMP) {
                    if(continuousTemp) {
                        System.out.printf("[%s] Temperature is %.2f (The temperature is ok)\n", topic, currentTemp);
                    }
                    if (windowStatus.equals("Open") && currentTemp < 20) {
                        if(continuousTemp) {
                            System.out.printf("[%s] (We need to close the window)\n", topic);
                        }
                        publishMessage(windowAct, "close");
                        windowStatus = "Closed";
                    }
                    if (lightStatus.equals("On") && currentTemp > 18) {
                        if(continuousTemp) {
                            System.out.printf("[%s] (We need to switch off the light)\n", topic);
                        }
                        publishMessage(lightAct, "off");
                        lightStatus = "Off";
                    }
                } else if (currentTemp > MAX_TEMP) {
                    if (windowStatus.equals("Closed")) {
                        if(continuousTemp) {
                            System.out.printf("[%s] (Temperature too high, we need to open the window)\n", topic);
                        }
                        publishMessage(windowAct, "open");
                        windowStatus = "Open";
                    }
                } else if (currentTemp < MIN_TEMP) {
                    if (lightStatus.equals("Off")) {
                        if(continuousTemp) {
                            System.out.printf("[%s] (Temperature too low, we need to switch on the light)\n", topic);
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

                currentHumidity = jsonPayload.getInt("humidity");
                int secondsFromStart = jsonPayload.getInt("timestamp");

                if(continuousHum && (currentHumidity != MIN_HUMIDITY && currentHumidity != MAX_HUMIDITY)) {
                    System.out.printf("[%s] Humidity percentage is %d%% (Humidity is ok)\n", topic, currentHumidity);
                }

                Timestamp timestamp = new Timestamp(startTime + (secondsFromStart * 1000L));
                DatabaseManager.insert_humidity(currentHumidity, "%", timestamp);

                if (currentHumidity < MIN_HUMIDITY) {
                    if (tapStatus.equals("Closed")) {
                        if(continuousHum) {
                            System.out.printf("[%s] (We need to switch on the irrigation system)\n", topic);
                        }
                        tapStatus = "Open";
                        publishMessage(humidityAct, "open");
                    }
                } else if (currentHumidity > MAX_HUMIDITY) {
                    if (tapStatus.equals("Open")) {
                        if(continuousHum) {
                            System.out.printf("[%s] (We need to switch off the irrigation system)\n", topic);
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
        //System.out.println("Delivery is complete!");
    }
}

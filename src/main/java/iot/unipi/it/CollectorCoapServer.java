package iot.unipi.it;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class CollectorCoapServer extends CoapServer {

    private static CoapClientHandler coapClientHandler = CoapClientHandler.getInstance();

    public CollectorCoapServer() throws SocketException {
        this.add(new CoapRegisterResource());
    }

    public void printWaterLevel() {
        coapClientHandler.printWaterLevelSensor();
    }

    class CoapRegisterResource extends CoapResource {

        public CoapRegisterResource() {
            super("registration");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            String deviceType = exchange.getRequestText();
            String ipAddress = exchange.getSourceAddress().getHostAddress();
            boolean success = true;
            switch (deviceType) {
                case "water_level_sensor":
                    coapClientHandler.registerWaterLevel(ipAddress);
                    break;
                case "led_water_actuator":
                    coapClientHandler.registerLedWater(ipAddress);
                    break;
                default:
                    success = false;
            }

            if(success) {
                exchange.respond(CoAP.ResponseCode.CREATED, "Registration Completed!".getBytes(StandardCharsets.UTF_8));
            } else {
                exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE, "Registration not allowed!".getBytes(StandardCharsets.UTF_8));
            }
        }
        /*
        @Override
        public void handleDELETE(CoapExchange exchange) {
            String deviceType = exchange.getRequestText();
            String ipAddress = exchange.getSourceAddress().getHostAddress();
            boolean success = true;
            switch (deviceType) {
                case "water_level_sensor":
                    coapClientHandler.registerWaterLevel(ipAddress);
                    break;
                default:
                    success = false;
            }

            if(success) {
                exchange.respond(CoAP.ResponseCode.CREATED, "Registration Completed!".getBytes(StandardCharsets.UTF_8));
            } else {
                exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE, "Registration not allowed!".getBytes(StandardCharsets.UTF_8));
            }
        }
        */

    }

}

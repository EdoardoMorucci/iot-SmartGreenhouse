#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"


#define LOG_MODULE "Led water"
#define LOG_LEVEL LOG_LEVEL_APP

static void put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_led_water,
         "title=\"Water led actuator\";rt=\"Control\"",
         NULL,
         NULL,
         put_handler,
         NULL);


int water_level = 0;

static void
put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    size_t len = 0;
  const uint8_t* payload = NULL;
  int success = 1;
	// leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));

  
  if((len = coap_get_payload(request, &payload))) {
		char data[20];
		strncpy(data, (char*)payload, len);	
		data[len] = '\0';	
		//LOG_INFO("Received the message: %s\n", data);
		
		water_level = atoi(data);
		//LOG_INFO("Water level: %f\n", water_level);
        
		if(water_level < 700){ 
			LOG_INFO("Water level: %d, set LED RED\n", water_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}
		else if(water_level > 1400){ 
			LOG_INFO("Water level: %d, set LED GREEN\n", water_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}else{
			LOG_INFO("Water level: %d, set LED YELLOW\n", water_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
		}
	}  

  if(!success) 
    coap_set_status_code(response, BAD_REQUEST_4_00);  
}

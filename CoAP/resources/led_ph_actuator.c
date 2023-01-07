#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"



#define LOG_MODULE "Led ph"
#define LOG_LEVEL LOG_LEVEL_APP

static void put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_led_ph,
         "title=\"ph led actuator\";rt=\"Control\"",
         NULL,
         NULL,
         put_handler,
         NULL);


float ph_level = 0;

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
		ph_level = atof(data);
		//LOG_INFO("ph level: %f\n", ph_level);
        
		if(ph_level < 6 || ph_level > 8){ 
			LOG_INFO("ph level: %.1f, set LED RED\n", ph_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}
		else if(ph_level < 6.5 || ph_level > 7.5){ 
			LOG_INFO("ph level: %.1f, set LED YELLOW\n", ph_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_YELLOW));
		}else{
			LOG_INFO("ph level: %.1f, set LED GREEN\n", ph_level);
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}
	}  

  if(!success) 
    coap_set_status_code(response, BAD_REQUEST_4_00);  
}

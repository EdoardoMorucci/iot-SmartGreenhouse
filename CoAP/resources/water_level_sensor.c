#include <stdio.h>
#include <stdlib.h>
#include "coap-engine.h"
#include <time.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "WL sensor"
#define LOG_LEVEL LOG_LEVEL_APP

static void get_water_level_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void water_level_event_handler(void);
static void simulate_water_level(void);



EVENT_RESOURCE(water_level_sensor,
        "title=\"Water Level sensor\";obs",
        get_water_level_handler,
        NULL,
        NULL,
        NULL,
        water_level_event_handler);

static unsigned int water_level = 1000;

static void get_water_level_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    simulate_water_level();

	coap_set_header_content_format(response, APPLICATION_JSON);
    sprintf((char *)buffer, "{\"water_level\": %d, \"timestamp\": %lu}", water_level, clock_seconds());
	coap_set_payload(response, buffer, strlen((char*)buffer));
    LOG_INFO("Message sent: %s\n", buffer);

    
}


static void water_level_event_handler(void)
{
    coap_notify_observers(&water_level_sensor);
}


static void simulate_water_level() 
{
    water_level = water_level - 100;

    if(water_level < 300) {
        water_level = 2000;
    }
}
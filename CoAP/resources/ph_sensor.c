#include <stdio.h>
#include <stdlib.h>
#include "coap-engine.h"
#include <time.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "pH sensor"
#define LOG_LEVEL LOG_LEVEL_APP

#define MIN_PH 6
#define MAX_PH 8

static void get_ph_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void ph_event_handler(void);
static void simulate_pH(void);



EVENT_RESOURCE(pH_sensor,
        "title=\"pH sensor\";obs",
        get_ph_handler,
        NULL,
        NULL,
        NULL,
        ph_event_handler);

static float pH = 7;
static bool ascending = true;

static void get_ph_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    simulate_pH();

	coap_set_header_content_format(response, APPLICATION_JSON);
    sprintf((char *)buffer, "{\"pH\": %.1f, \"timestamp\": %lu}", pH, clock_seconds());
	coap_set_payload(response, buffer, strlen((char*)buffer));
    LOG_INFO("Message sent: %s\n", buffer);

}


static void ph_event_handler(void)
{
    coap_notify_observers(&pH_sensor);
}


static void simulate_pH() 
{
    if (ascending && pH > 8) {
        ascending = false;
        pH = pH - 0.1;
    } else if (ascending) {
        pH = pH + 0.1;
    } else if (!ascending && pH < 6) {
        ascending = true;
        pH = pH + 0.1; 
    } else if (!ascending) {
        pH = pH - 0.1;
    }
}
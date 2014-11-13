/*
 * Copyright 2014-2014 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.camel.component.hippo;

import net.sf.json.JSONObject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.GuavaHippoEventBus;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class HippoEventConsumerTest extends CamelTestSupport {

    private static final transient Logger LOG = LoggerFactory.getLogger(HippoEventConsumerTest.class);

    private HippoEventBus eventBus;

    @Override
    public void setUp() throws Exception {
        HippoServiceRegistry.registerService(new GuavaHippoEventBus(), HippoEventBus.class);
        eventBus = HippoServiceRegistry.getService(HippoEventBus.class);
        assertNotNull(eventBus);

        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        HippoServiceRegistry.unregisterService(eventBus, HippoEventBus.class);

        super.tearDown();
    }

    @Test
    public void testHippoEventConsumer() throws Exception {
        // start consumer thread first
        HippoEventConsumerThread consumerThread = new HippoEventConsumerThread();
        consumerThread.start();
        // wait until the consumer thread has tried to receive event at least once
        while (consumerThread.getReceiveTrialTimes() < 1) {
            Thread.sleep(10L);
        }

        // now post a HippoEvent
        HippoEvent event = new HippoEvent("application1");
        event.action("action1");
        event.category("category1");
        event.message("message1");
        event.result("result1");
        event.user("user1");
        eventBus.post(event);

        // wait until the consumer thread captures an event
        consumerThread.join();

        Exchange exchange = consumerThread.getExchange();
        assertNotNull(exchange);

        Message message = exchange.getIn();
        assertNotNull(message);
        assertTrue(message instanceof HippoEventMessage);
        JSONObject eventJson = (JSONObject) message.getBody();
        assertNotNull(eventJson);

        assertEquals(HippoEvent.class.getName(), eventJson.get("_eventClassName"));
        assertEquals("action1", eventJson.get("action"));
        assertEquals("category1", eventJson.get("category"));
        assertEquals("message1", eventJson.get("message"));
        assertEquals("result1", eventJson.get("result"));
        assertEquals("user1", eventJson.get("user"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                String uri = "hippoevent://localhost/";
                from(uri).to("direct:a");
            }
        };
    }

    private class HippoEventConsumerThread extends Thread {

        private Exchange exchange;
        private int receiveTrialTimes;

        public void run() {
            while (exchange == null) {
                exchange = consumer.receive("direct:a", 10L);
                ++receiveTrialTimes;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }

            LOG.debug("HippoEventConsumerThread receive exchange, {} after {} trials", exchange, receiveTrialTimes);
        }

        public Exchange getExchange() {
            return exchange;
        }

        public int getReceiveTrialTimes() {
            return receiveTrialTimes;
        }
    }
}

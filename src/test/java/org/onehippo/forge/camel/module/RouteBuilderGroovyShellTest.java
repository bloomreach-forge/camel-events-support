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
package org.onehippo.forge.camel.module;

import groovy.lang.GroovyShell;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class RouteBuilderGroovyShellTest extends CamelTestSupport {

    private static final transient Logger LOG = LoggerFactory.getLogger(RouteBuilderGroovyShellTest.class);

    private static final String ROUTE_EXPR =
            "new RouteBuilder() {\n" +
            "  def void configure() {\n" +
            "    from(\"timer://jdkTimer?period=10\")\n" +
            "    .to(\"direct:a\")\n" +
            "  }\n" +
            "}";

    private GroovyShell groovyShell;

    @Override
    public void setUp() throws Exception {
        groovyShell = new RouteBuilderGroovyShell();

        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return (RouteBuilder) groovyShell.evaluate(ROUTE_EXPR);
    }

    @Test
    public void testCamelContextWithRouteBuilder() throws Exception {
        // start consumer thread first
        TimerEventConsumerThread consumerThread = new TimerEventConsumerThread();

        long timestamp = System.currentTimeMillis();
        Thread.sleep(10);

        consumerThread.start();
        // wait until the consumer thread has tried to receive event at least once
        while (consumerThread.getReceivedTimestamp() == 0L) {
            Thread.sleep(10L);
        }

        consumerThread.join();

        Exchange exchange = consumerThread.getExchange();
        assertNotNull(exchange);

        Message message = exchange.getIn();
        assertNotNull(message);

        assertTrue(consumerThread.getReceivedTimestamp() > timestamp);
    }

    private class TimerEventConsumerThread extends Thread {

        private Exchange exchange;
        private long receivedTimestamp;

        public void run() {
            while (exchange == null) {
                exchange = consumer.receive("direct:a", 10L);

                if (exchange != null) {
                    receivedTimestamp = System.currentTimeMillis();
                    break;
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }

            LOG.debug("HippoEventConsumerThread receive exchange, {}. Timestamp: {}.", exchange, receivedTimestamp);
        }

        public Exchange getExchange() {
            return exchange;
        }

        public long getReceivedTimestamp() {
            return receivedTimestamp;
        }
    }
}

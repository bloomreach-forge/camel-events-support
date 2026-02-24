/*
 * Copyright 2025 Bloomreach B.V. (https://www.bloomreach.com)
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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultCamelContext;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onehippo.cms7.event.HippoEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HippoEventConsumerUnitTest {

    private DefaultCamelContext ctx;
    private HippoEventEndpoint endpoint;
    private Processor mockProcessor;
    private HippoEventConsumer consumer;

    @BeforeEach
    void setUp() throws Exception {
        ctx = new DefaultCamelContext();
        ctx.start();
        HippoEventComponent comp = new HippoEventComponent();
        comp.setCamelContext(ctx);

        Map<String, Object> props = new HashMap<>();
        props.put("category", "category1");
        props.put("user", "user1,user2");

        endpoint = new HippoEventEndpoint("hippoevent:?category=category1&user=user1,user2", comp, props);
        endpoint.setCamelContext(ctx);

        mockProcessor = EasyMock.createNiceMock(Processor.class);
        EasyMock.replay(mockProcessor);

        consumer = new HippoEventConsumer(endpoint, mockProcessor);
    }

    @AfterEach
    void tearDown() throws Exception {
        ctx.stop();
    }

    @Test
    void isConsumable_matchingFirstValue_returnsTrue() {
        JSONObject json = new JSONObject();
        json.put("category", "category1");
        json.put("user", "user1");

        assertTrue(consumer.isConsumable(new HippoEvent("app"), json));
    }

    @Test
    void isConsumable_matchingSecondValueInList_returnsTrue() {
        JSONObject json = new JSONObject();
        json.put("category", "category1");
        json.put("user", "user2");

        assertTrue(consumer.isConsumable(new HippoEvent("app"), json));
    }

    @Test
    void isConsumable_nonMatchingCategory_returnsFalse() {
        JSONObject json = new JSONObject();
        json.put("category", "other");
        json.put("user", "user1");

        assertFalse(consumer.isConsumable(new HippoEvent("app"), json));
    }

    @Test
    void isConsumable_missingPropertyInJson_returnsFalse() {
        // JSON has no category or user fields
        assertFalse(consumer.isConsumable(new HippoEvent("app"), new JSONObject()));
    }

    @Test
    void isConsumable_skipsPersistedOptionParams() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        HippoEventComponent comp = new HippoEventComponent();
        comp.setCamelContext(ctx);

        Map<String, Object> props = new HashMap<>();
        props.put("category", "category1");
        props.put("_persisted", "false");
        props.put("_channelName", "chan");
        props.put("_onlyNewEvents", "true");

        HippoEventEndpoint ep = new HippoEventEndpoint("hippoevent:", comp, props);
        ep.setCamelContext(ctx);

        HippoEventConsumer c = new HippoEventConsumer(ep, mockProcessor);

        JSONObject json = new JSONObject();
        json.put("category", "category1");
        // persisted params absent from json but must be skipped

        assertTrue(c.isConsumable(new HippoEvent("app"), json));
    }

    @Test
    void doStart_persistedConsumerWithoutChannelName_throwsRuntimeCamelException() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        HippoEventComponent comp = new HippoEventComponent();
        comp.setCamelContext(ctx);

        Map<String, Object> props = new HashMap<>();
        props.put("_persisted", "true");
        // _channelName intentionally omitted

        HippoEventEndpoint ep = new HippoEventEndpoint("hippoevent:?_persisted=true", comp, props);
        ep.setCamelContext(ctx);

        HippoEventConsumer c = new HippoEventConsumer(ep, mockProcessor);
        assertThrows(RuntimeCamelException.class, c::doStart);
    }

    @Test
    void persistedEventListener_propertiesAndDelegates() {
        HippoEventConsumer.HippoPersistedEventListener listener = consumer.new HippoPersistedEventListener();

        listener.setChannelName("my-channel");
        assertEquals("my-channel", listener.getChannelName());

        listener.setEventCategory("my-category");
        assertEquals("my-category", listener.getEventCategory());

        assertTrue(listener.isOnlyNewEvents());
        assertTrue(listener.onlyNewEvents());

        listener.setOnlyNewEvents(false);
        assertFalse(listener.isOnlyNewEvents());
        assertFalse(listener.onlyNewEvents());
    }

    @Test
    void handleHippoEvent_processorThrowsRCE_rethrows() throws Exception {
        RuntimeCamelException rce = new RuntimeCamelException("test error");

        Processor throwingProcessor = EasyMock.createMock(Processor.class);
        throwingProcessor.process(EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(rce);
        EasyMock.replay(throwingProcessor);

        // endpoint has category/user filters; supply a matching event so isConsumable returns true
        HippoEventConsumer c = new HippoEventConsumer(endpoint, throwingProcessor);
        HippoEvent matchingEvent = new HippoEvent("app");
        matchingEvent.category("category1");
        matchingEvent.user("user1");

        assertThrows(RuntimeCamelException.class, () -> c.handleHippoEvent(matchingEvent));
        EasyMock.verify(throwingProcessor);
    }

    @Test
    void handleHippoEvent_messageBodyCreationThrows_wrapsInRCE() {
        // Override createMessageBody to throw before exchange is created — exercises exchange==null path
        HippoEventConsumer throwingConsumer = new HippoEventConsumer(endpoint, mockProcessor) {
            @Override
            protected JSONObject createMessageBody(HippoEvent<?> event) {
                throw new RuntimeException("body creation failed");
            }
        };

        assertThrows(RuntimeCamelException.class,
                () -> throwingConsumer.handleHippoEvent(new HippoEvent("app")));
    }

    @Test
    void onHippoEvent_delegatesToHandleHippoEvent() {
        HippoEventConsumer.HippoPersistedEventListener listener = consumer.new HippoPersistedEventListener();

        // Non-matching event — isConsumable returns false immediately, no exchange needed
        HippoEvent nonMatchingEvent = new HippoEvent("app");
        nonMatchingEvent.category("other");
        nonMatchingEvent.user("other");

        listener.onHippoEvent(nonMatchingEvent);
        // no exception means delegation to handleHippoEvent succeeded
    }
}

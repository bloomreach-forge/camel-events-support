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
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HippoEventEndpointTest {

    private HippoEventEndpoint endpoint;
    private HippoEventEndpoint endpointNullProps;

    @BeforeEach
    void setUp() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        HippoEventComponent comp = new HippoEventComponent();
        comp.setCamelContext(ctx);

        Map<String, Object> props = new HashMap<>();
        props.put("category", "category1");
        props.put("user", "user1,user2");

        endpoint = new HippoEventEndpoint("hippoevent:?category=category1&user=user1,user2", comp, props);
        endpoint.setCamelContext(ctx);

        endpointNullProps = new HippoEventEndpoint("hippoevent:", comp, null);
        endpointNullProps.setCamelContext(ctx);
    }

    @Test
    void getProperty_existingKey_returnsValue() {
        assertEquals("category1", endpoint.getProperty("category"));
        assertEquals("user1,user2", endpoint.getProperty("user"));
    }

    @Test
    void getProperty_missingKey_returnsNull() {
        assertNull(endpoint.getProperty("unknown"));
    }

    @Test
    void getProperty_nullPropertiesMap_returnsNull() {
        assertNull(endpointNullProps.getProperty("category"));
    }

    @Test
    void hasProperty_existingKey_returnsTrue() {
        assertTrue(endpoint.hasProperty("category"));
        assertTrue(endpoint.hasProperty("user"));
    }

    @Test
    void hasProperty_missingKey_returnsFalse() {
        assertFalse(endpoint.hasProperty("unknown"));
    }

    @Test
    void hasProperty_nullPropertiesMap_returnsFalse() {
        assertFalse(endpointNullProps.hasProperty("category"));
    }

    @Test
    void getPropertyNameSet_returnsAllKeys() {
        Set<String> names = endpoint.getPropertyNameSet();
        assertEquals(Set.of("category", "user"), names);
    }

    @Test
    void getPropertyNameSet_nullPropertiesMap_returnsEmptySet() {
        assertTrue(endpointNullProps.getPropertyNameSet().isEmpty());
    }

    @Test
    void isSingleton_returnsTrue() {
        assertTrue(endpoint.isSingleton());
    }

    @Test
    void createProducer_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, endpoint::createProducer);
    }

    @Test
    void createConsumer_returnsHippoEventConsumer() throws Exception {
        Processor mockProcessor = EasyMock.createNiceMock(Processor.class);
        EasyMock.replay(mockProcessor);

        Consumer consumer = endpoint.createConsumer(mockProcessor);

        assertNotNull(consumer);
        assertTrue(consumer instanceof HippoEventConsumer);
    }
}

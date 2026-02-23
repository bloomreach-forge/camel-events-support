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

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HippoEventComponentTest {

    private HippoEventComponent component;

    @BeforeEach
    void setUp() {
        DefaultCamelContext ctx = new DefaultCamelContext();
        component = new HippoEventComponent();
        component.setCamelContext(ctx);
    }

    @Test
    void createEndpoint_nullProperties_returnsEndpointWithEmptyProps() throws Exception {
        Endpoint ep = component.createEndpoint("hippoevent:", "", null);

        assertNotNull(ep);
        assertTrue(ep instanceof HippoEventEndpoint);
        assertTrue(((HippoEventEndpoint) ep).getPropertyNameSet().isEmpty());
    }

    @Test
    void createEndpoint_withProperties_copiesAndClearsOriginal() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put("category", "cat1");
        props.put("user", "user1");

        Endpoint ep = component.createEndpoint("hippoevent:?category=cat1&user=user1", "", props);

        assertNotNull(ep);
        HippoEventEndpoint hippoeEp = (HippoEventEndpoint) ep;
        assertEquals("cat1", hippoeEp.getProperty("category"));
        assertEquals("user1", hippoeEp.getProperty("user"));
        assertTrue(props.isEmpty(), "original properties map must be cleared");
    }
}

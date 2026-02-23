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

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HippoEventMessageTest {

    private DefaultCamelContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new DefaultCamelContext();
    }

    @Test
    void toString_withBody_includesBodyContent() {
        HippoEventMessage message = new HippoEventMessage(ctx);
        message.setBody(new JSONObject("{\"key\":\"value\"}"));

        assertTrue(message.toString().startsWith("HippoEventMessage[event:"));
    }

    @Test
    void toString_withoutBody_includesIdentityHash() {
        HippoEventMessage message = new HippoEventMessage(ctx);

        assertTrue(message.toString().startsWith("HippoEventMessage@"));
    }

    @Test
    void copyFrom_sameInstance_isNoOp() {
        HippoEventMessage message = new HippoEventMessage(ctx);
        JSONObject json = new JSONObject();
        json.put("k", "v");
        message.setBody(json);
        message.setHeader("h1", "v1");

        message.copyFrom(message);

        assertSame(json, message.getBody());
        assertEquals("v1", message.getHeader("h1"));
    }

    @Test
    void copyFrom_jsonObjectBody_serialisedToString() {
        HippoEventMessage src = new HippoEventMessage(ctx);
        JSONObject json = new JSONObject();
        json.put("key", "value");
        src.setBody(json);

        HippoEventMessage dest = new HippoEventMessage(ctx);
        dest.copyFrom(src);

        assertTrue(dest.getBody() instanceof String);
        assertTrue(((String) dest.getBody()).contains("value"));
    }

    @Test
    void copyFrom_nonJsonObjectBody_setDirectly() {
        HippoEventMessage src = new HippoEventMessage(ctx);
        src.setBody("plain string");

        HippoEventMessage dest = new HippoEventMessage(ctx);
        dest.copyFrom(src);

        assertEquals("plain string", dest.getBody());
    }

    @Test
    void copyFrom_nullBody_setDirectly() {
        HippoEventMessage src = new HippoEventMessage(ctx);
        src.setBody(null);

        HippoEventMessage dest = new HippoEventMessage(ctx);
        dest.setBody("old");
        dest.copyFrom(src);

        assertEquals(null, dest.getBody());
    }

    @Test
    void copyFrom_copiesHeaders() {
        HippoEventMessage src = new HippoEventMessage(ctx);
        src.setBody("body");
        src.setHeader("h1", "v1");
        src.setHeader("h2", "v2");

        HippoEventMessage dest = new HippoEventMessage(ctx);
        dest.copyFrom(src);

        assertEquals("v1", dest.getHeader("h1"));
        assertEquals("v2", dest.getHeader("h2"));
    }

    @Test
    void constructor_withExchange_createsMessage() {
        Exchange exchange = new DefaultExchange(ctx);
        HippoEventMessage msg = new HippoEventMessage(exchange);
        assertNotNull(msg);
        assertSame(exchange, msg.getExchange());
    }
}

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
package org.onehippo.forge.camel.converter.json;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test that exercises type converter lambdas in the generated
 * JSONConverterLoader and RepositoryJobExecutionContextConverterLoader
 * via Camel's type conversion infrastructure.
 */
class CamelTypeConverterIntegrationTest extends CamelTestSupport {

    @Test
    void convertJsonObjectToString() {
        JSONObject json = new JSONObject();
        json.put("key", "value");

        String result = context.getTypeConverter().convertTo(String.class, json);

        assertNotNull(result);
        assertTrue(result.contains("key"));
        assertTrue(result.contains("value"));
    }

    @Test
    void convertJsonObjectToInputStream() throws Exception {
        JSONObject json = new JSONObject();
        json.put("key", "value");

        InputStream result = context.getTypeConverter().convertTo(InputStream.class, json);

        assertNotNull(result);
        String content = new String(result.readAllBytes(), StandardCharsets.UTF_8);
        assertTrue(content.contains("key"));
        assertTrue(content.contains("value"));
    }

    @Test
    void convertStringToJsonObject() {
        String jsonStr = "{\"key\":\"value\",\"num\":42}";

        JSONObject result = context.getTypeConverter().convertTo(JSONObject.class, jsonStr);

        assertNotNull(result);
        assertEquals("value", result.getString("key"));
        assertEquals(42, result.getInt("num"));
    }

    @Test
    void convertRepositoryJobExecutionContextToJsonObject() {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttributeNames()).andReturn(List.of("attr1", "attr2")).anyTimes();
        EasyMock.expect(ctx.getAttribute("attr1")).andReturn("val1").anyTimes();
        EasyMock.expect(ctx.getAttribute("attr2")).andReturn("val2").anyTimes();
        EasyMock.replay(ctx);

        JSONObject result = context.getTypeConverter().convertTo(JSONObject.class, ctx);

        assertNotNull(result);
        assertEquals("val1", result.getString("attr1"));
        assertEquals("val2", result.getString("attr2"));
        EasyMock.verify(ctx);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
            }
        };
    }
}

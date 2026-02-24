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

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JSONConverterTest {

    @Test
    void toString_returnsJsonString() {
        JSONObject json = new JSONObject();
        json.put("key", "value");

        String result = JSONConverter.toString(json);

        assertNotNull(result);
        assertTrue(result.contains("\"key\""));
        assertTrue(result.contains("\"value\""));
    }

    @Test
    void toInputStream_returnsReadableStream() throws Exception {
        JSONObject json = new JSONObject();
        json.put("key", "value");

        InputStream stream = JSONConverter.toInputStream(json);
        String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(content.contains("\"key\""));
        assertTrue(content.contains("\"value\""));
    }

    @Test
    void toJSON_parsesJsonString() {
        String jsonString = "{\"key\":\"value\",\"num\":42}";

        JSONObject result = JSONConverter.toJSON(jsonString);

        assertEquals("value", result.getString("key"));
        assertEquals(42, result.getInt("num"));
    }
}

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
package org.onehippo.forge.camel.converter.hippo;

import java.util.List;

import org.easymock.EasyMock;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryJobExecutionContextConverterTest {

    @Test
    void toJSON_convertsAllAttributes() {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttributeNames()).andReturn(List.of("key1", "key2"));
        EasyMock.expect(ctx.getAttribute("key1")).andReturn("value1");
        EasyMock.expect(ctx.getAttribute("key2")).andReturn("value2");
        EasyMock.replay(ctx);

        JSONObject json = RepositoryJobExecutionContextConverter.toJSON(ctx);

        assertEquals("value1", json.getString("key1"));
        assertEquals("value2", json.getString("key2"));
        EasyMock.verify(ctx);
    }

    @Test
    void toJSON_emptyContext_returnsEmptyJSONObject() {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttributeNames()).andReturn(List.of());
        EasyMock.replay(ctx);

        JSONObject json = RepositoryJobExecutionContextConverter.toJSON(ctx);

        assertTrue(json.isEmpty());
        EasyMock.verify(ctx);
    }
}

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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.standardworkflow.FolderWorkflowEvent;
import org.junit.Test;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoSecurityEvent;
import org.onehippo.repository.events.HippoWorkflowEvent;

/**
 * HippoEventConverterTest
 */
public class HippoEventConverterTest {

    @Test
    public void testHippoEvent() throws Exception {
        final long timestamp = System.currentTimeMillis();

        HippoEvent event = new HippoEvent("application1");
        event.action("action1");
        event.category("category1");
        event.message("message1");
        event.result("result1");
        event.system(true);
        event.timestamp(timestamp);
        event.user("user1");
        event.set("attr1", "value1");
        event.set("attr2", "value2");
        event.sealEvent();

        JSONObject json = HippoEventConverter.toJSONObject(event);
        assertEquals(HippoEvent.class.getName(), json.get("_eventClassName"));
        assertEquals("application1", json.get("application"));
        assertEquals("action1", json.get("action"));
        assertEquals("category1", json.get("category"));
        assertEquals("message1", json.get("message"));
        assertEquals("result1", json.get("result"));
        assertEquals(true, json.get("system"));
        assertEquals(timestamp, json.get("timestamp"));
        assertEquals("user1", json.get("user"));
        assertEquals("value1", json.get("attr1"));
        assertEquals("value2", json.get("attr2"));
        assertEquals(true, json.get("sealed"));
    }

    @Test
    public void testHippoSecurityEvent() throws Exception {
        final long timestamp = System.currentTimeMillis();

        HippoSecurityEvent event = new HippoSecurityEvent("application1");
        event.action("action1");
        event.category("category1");
        event.message("message1");
        event.result("result1");
        event.system(true);
        event.timestamp(timestamp);
        event.user("user1");
        event.set("attr1", "value1");
        event.set("attr2", "value2");
        event.success(true);
        event.sealEvent();

        JSONObject json = HippoEventConverter.toJSONObject(event);
        assertEquals(HippoSecurityEvent.class.getName(), json.get("_eventClassName"));
        assertEquals("application1", json.get("application"));
        assertEquals("action1", json.get("action"));
        assertEquals("category1", json.get("category"));
        assertEquals("message1", json.get("message"));
        assertEquals("result1", json.get("result"));
        assertEquals(true, json.get("system"));
        assertEquals(timestamp, json.get("timestamp"));
        assertEquals("user1", json.get("user"));
        assertEquals("value1", json.get("attr1"));
        assertEquals("value2", json.get("attr2"));
        assertEquals(true, json.get("success"));
        assertEquals(true, json.get("sealed"));
    }

    @Test
    public void testHippoWorkflowEvent() throws Exception {
        final long timestamp = System.currentTimeMillis();
        final List<String> arguments = Arrays.asList("arg1", "arg2");
        final Exception exception = new Exception();

        HippoWorkflowEvent event = new HippoWorkflowEvent();
        event.action("action1");
        event.message("message1");
        event.result("result1");
        event.system(true);
        event.timestamp(timestamp);
        event.user("user1");
        event.set("attr1", "value1");
        event.set("attr2", "value2");
        event.arguments(arguments);
        event.className("example.Workflow");
        event.exception(exception);
        event.interaction("interaction1");
        event.interactionId("interactionId1");
        event.returnType("returnType1");
        event.returnValue("returnValue1");
        event.subjectId("subjectId1");
        event.subjectPath("subjectPath1");
        event.workflowCategory("workflowCategory1");
        event.workflowName("workflowName1");
        event.sealEvent();

        JSONObject json = HippoEventConverter.toJSONObject(event);
        assertEquals(HippoWorkflowEvent.class.getName(), json.get("_eventClassName"));
        assertEquals("repository", json.get("application"));
        assertEquals("action1", json.get("action"));
        assertEquals("workflow", json.get("category"));
        assertEquals("message1", json.get("message"));
        assertEquals("result1", json.get("result"));
        assertEquals(true, json.get("system"));
        assertEquals(timestamp, json.get("timestamp"));
        assertEquals("user1", json.get("user"));
        assertEquals("value1", json.get("attr1"));
        assertEquals("value2", json.get("attr2"));
        assertEquals("arg1,arg2", StringUtils.join(json.getJSONArray("arguments"), ","));
        assertEquals("example.Workflow", json.get("className"));
        assertEquals(exception.toString(), json.get("exception"));
        assertEquals("interaction1", json.get("interaction"));
        assertEquals("interactionId1", json.get("interactionId"));
        assertEquals("returnType1", json.get("returnType"));
        assertEquals("returnValue1", json.get("returnValue"));
        assertEquals("subjectId1", json.get("subjectId"));
        assertEquals("subjectPath1", json.get("subjectPath"));
        assertEquals("workflowCategory1", json.get("workflowCategory"));
        assertEquals("workflowName1", json.get("workflowName"));
        assertEquals(true, json.get("sealed"));
    }

    @Test
    public void testFolderWorkflowEvent() throws Exception {
        final long timestamp = System.currentTimeMillis();

        HippoEvent event = new HippoEvent("application1");
        event.action("action1");
        event.category("category1");
        event.message("message1");
        event.result("result1");
        event.system(true);
        event.timestamp(timestamp);
        event.user("user1");
        event.set("attr1", "value1");
        event.set("attr2", "value2");
        event.sealEvent();

        FolderWorkflowEvent folderEvent = new FolderWorkflowEvent(event);
        folderEvent.set("event-type", FolderWorkflowEvent.Type.DELETED);
        folderEvent.sealEvent();

        JSONObject json = HippoEventConverter.toJSONObject(folderEvent);
        assertEquals(FolderWorkflowEvent.class.getName(), json.get("_eventClassName"));
        assertEquals("application1", json.get("application"));
        assertEquals("action1", json.get("action"));
        assertEquals("category1", json.get("category"));
        assertEquals("message1", json.get("message"));
        assertEquals("result1", json.get("result"));
        assertEquals(true, json.get("system"));
        assertEquals(timestamp, json.get("timestamp"));
        assertEquals("user1", json.get("user"));
        assertEquals("value1", json.get("attr1"));
        assertEquals("value2", json.get("attr2"));
        assertEquals("DELETED", json.get("type"));
        assertEquals(true, json.get("sealed"));
    }
}

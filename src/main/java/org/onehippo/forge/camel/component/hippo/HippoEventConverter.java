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

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hippoecm.repository.standardworkflow.FolderWorkflowEvent;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.event.HippoSecurityEvent;
import org.onehippo.repository.events.HippoWorkflowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public final class HippoEventConverter {

    private static Logger log = LoggerFactory.getLogger(HippoEventConverter.class);

    private HippoEventConverter() {
    }

    public static JSONObject toJSONObject(final HippoEvent event) {
        JSONObject eventJson = new JSONObject();

        if (event != null) {
            eventJson.put("_eventClassName", event.getClass().getName());

            eventJson.put("action", event.action());
            eventJson.put("application", event.application());
            eventJson.put("category", event.category());
            eventJson.put("sealed", event.isSealed());
            eventJson.put("message", event.message());
            eventJson.put("result", event.result());
            eventJson.put("system", event.system());
            eventJson.put("timestamp", event.timestamp());
            eventJson.put("user", event.user());

            if (event instanceof HippoSecurityEvent) {
                HippoSecurityEvent secEvent = (HippoSecurityEvent) event;
                eventJson.put("success", secEvent.success());
            }

            if (event instanceof HippoWorkflowEvent) {
                HippoWorkflowEvent wfEvent = (HippoWorkflowEvent) event;

                List<String> arguments = wfEvent.arguments();
                JSONArray jsonArgs = new JSONArray();
                if (arguments != null) {
                    jsonArgs.addAll(arguments);
                }

                eventJson.put("arguments", jsonArgs);
                eventJson.put("className", wfEvent.className());

                if (wfEvent.exception() != null) {
                    eventJson.put("exception", wfEvent.exception().toString());
                }

                eventJson.put("interaction", wfEvent.interaction());
                eventJson.put("interactionId", wfEvent.interactionId());
                eventJson.put("methodName", wfEvent.methodName());
                eventJson.put("returnType", wfEvent.returnType());
                eventJson.put("returnValue", wfEvent.returnValue());
                eventJson.put("subjectId", wfEvent.subjectId());
                eventJson.put("subjectPath", wfEvent.subjectPath());
                eventJson.put("workflowCategory", wfEvent.workflowCategory());
                eventJson.put("workflowName", wfEvent.workflowName());

                try {
                    eventJson.put("documentType", wfEvent.documentType());
                } catch (Throwable th) {
                    log.warn("HippoWorkflowEvent#documentType() is not supported in the current module.");
                }

                // Add deprecated properties as well for now.
                eventJson.put("documentPath", wfEvent.documentPath());
                eventJson.put("handleUuid", wfEvent.handleUuid());

                if (wfEvent instanceof FolderWorkflowEvent) {
                    FolderWorkflowEvent fwfEvent = (FolderWorkflowEvent) wfEvent;
                    eventJson.put("type", fwfEvent.type().toString());
                }
            }
        }

        return eventJson;
    }
}

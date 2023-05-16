/*
 * Copyright 2014-2023 Bloomreach (http://www.bloomreach.com)
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

import net.sf.json.JSONObject;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.util.ObjectHelper;

public class HippoEventMessage extends DefaultMessage {

    public HippoEventMessage(final JSONObject eventJson, Exchange exchange) {
        super(exchange);
        setBody(eventJson);
    }

    @Override
    public String toString() {
        if (getBody() != null) {
            return "HippoEventMessage[event: " + getBody() + "]";
        }

        return "HippoEventMessage@" + ObjectHelper.getIdentityHashCode(this);
    }

    @Override
    public void copyFrom(Message that) {
        if (that == this) {
            // the same instance so do not need to copy
            return;
        }
        AttachmentMessage attachmentMessage = (AttachmentMessage) that;
        // must initialize headers before we set the JmsMessage to avoid Camel
        // populating it before we do the copy
        getHeaders().clear();

        // copy body and fault flag`

        Object body = attachmentMessage.getBody();

        if (body instanceof JSONObject) {
            setBody(JSONObject.fromObject(body));
        } else {
            setBody(body);
        }

//        setFault(that.isFault());

        // we have already cleared the headers
        if (attachmentMessage.hasHeaders()) {
            getHeaders().putAll(attachmentMessage.getHeaders());
        }

        getExchange().getMessage(AttachmentMessage.class).getAttachments().clear();

        if (attachmentMessage.hasAttachments()) {
            getExchange().getMessage(AttachmentMessage.class).getAttachments().putAll(attachmentMessage.getAttachments());
        }
    }
}

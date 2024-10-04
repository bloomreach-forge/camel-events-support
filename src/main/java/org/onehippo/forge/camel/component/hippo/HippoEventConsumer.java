/*
 * Copyright 2024 Bloomreach B.V. (https://www.bloomreach.com)
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.SuspendableService;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.support.DefaultEndpoint;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.eventbus.HippoEventListenerRegistry;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.onehippo.repository.events.PersistedHippoEventListener;
import org.onehippo.repository.events.PersistedHippoEventListenerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * HippoEventConsumer.
 */
public class HippoEventConsumer extends DefaultConsumer implements SuspendableService {

    private static final Logger LOG = LoggerFactory.getLogger(HippoEventConsumer.class);

    public static final String PERSISTED_LISTENER_FLAG = "_persisted";

    public static final String PERSISTED_LISTENER_CHANNEL_NAME = "_channelName";

    public static final String PERSISTED_LISTENER_ONLY_NEW_EVENTS_FLAG = "_onlyNewEvents";

    private static final Set<String> PERSISTED_LISTENER_OPTION_PARAM_NAME_SET = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    PERSISTED_LISTENER_FLAG,
                    PERSISTED_LISTENER_CHANNEL_NAME,
                    PERSISTED_LISTENER_ONLY_NEW_EVENTS_FLAG)));

    private final HippoEventEndpoint endpoint;
    private final Processor processor;

    private HippoLocalEventListener localEventListener;
    private HippoPersistedEventListener persistedEventListener;

    public HippoEventConsumer(HippoEventEndpoint endpoint, Processor processor) {
        super(endpoint, processor);

        this.endpoint = endpoint;
        this.processor = processor;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        final boolean persistedEventConsumer = BooleanUtils.toBoolean((String) endpoint.getProperty(PERSISTED_LISTENER_FLAG));

        if (persistedEventConsumer) {
            LOG.info("Registering a persisted event consumer because the _persisted parameter set to true.");

            HippoPersistedEventListener listener = new HippoPersistedEventListener();

            final String channelName = (String) endpoint.getProperty(PERSISTED_LISTENER_CHANNEL_NAME);

            if (StringUtils.isEmpty(channelName)) {
                throw new RuntimeCamelException("Channel name must be specified for a persisted event consumer with '_channelName' parameter!");
            }

            listener.setChannelName(channelName);

            final String [] eventCategories = StringUtils.split((String) endpoint.getProperty("category"), ",");

            if (ArrayUtils.isNotEmpty(eventCategories) && StringUtils.isNotEmpty(eventCategories[0])) {
                listener.setEventCategory(eventCategories[0]);
            }

            if (endpoint.hasProperty(PERSISTED_LISTENER_ONLY_NEW_EVENTS_FLAG)) {
                listener.setOnlyNewEvents(BooleanUtils.toBoolean((String) endpoint.getProperty(PERSISTED_LISTENER_ONLY_NEW_EVENTS_FLAG)));
            }

            PersistedHippoEventListenerRegistry.get().register(listener);

            persistedEventListener = listener;

        } else {
            LOG.info("Registering a local event consumer because the _persisted parameter unspecified or set to false.");

            HippoLocalEventListener listener = new HippoLocalEventListener();
            HippoEventListenerRegistry.get().register(listener);
            localEventListener = listener;

        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        if (persistedEventListener != null) {
            PersistedHippoEventListenerRegistry.get().unregister(persistedEventListener);
        }

        if (localEventListener != null) {
            HippoEventListenerRegistry.get().unregister(localEventListener);
        }
    }

    protected JSONObject createMessageBody(HippoEvent<?> event) {
        return HippoEventConverter.toJSONObject(event);
    }

    protected Exchange createExchange(HippoEvent<?> event, JSONObject messageBody) {
        Exchange exchange = ((DefaultEndpoint) getEndpoint()).createExchange();
        HippoEventMessage hippoEventMessage = new HippoEventMessage(getEndpoint().getCamelContext());
        hippoEventMessage.setBody(messageBody);
        exchange.setIn(hippoEventMessage);
        return exchange;
    }

    protected boolean isConsumable(final HippoEvent<?> event, final JSONObject messageBody) {
        String [] availableValues;
        String value;

        for (String propName : endpoint.getPropertyNameSet()) {
            if (PERSISTED_LISTENER_OPTION_PARAM_NAME_SET.contains(propName)) {
                continue;
            }

            availableValues = StringUtils.split((String) endpoint.getProperty(propName), ",");
            value = null;

            if (messageBody.has(propName)) {
                value = messageBody.getString(propName);
            }

            if (value == null && ArrayUtils.isNotEmpty(availableValues)) {
                return false;
            }

            if (!ArrayUtils.contains(availableValues, value)) {
                return false;
            }
        }

        return true;
    }

    protected void handleHippoEvent(HippoEvent<?> event) {
        RuntimeCamelException rce = null;

        Exchange exchange = null;

        try {
            JSONObject messageBody = createMessageBody(event);

            if (!isConsumable(event, messageBody)) {
                return;
            }

            exchange = createExchange(event, messageBody);
            processor.process(exchange);
        } catch (Exception e) {
            if (exchange != null) {
                exchange.setException(e);
            } else {
                rce = new RuntimeCamelException(e);
            }
        }

        if (exchange != null) {
            rce = exchange.getException(RuntimeCamelException.class);
        }

        if (rce != null) {
            throw rce;
        }
    }

    public class HippoLocalEventListener {

        @Subscribe
        public void handleEvent(HippoEvent<?> event) {
            handleHippoEvent(event);
        }
    }

    public class HippoPersistedEventListener implements PersistedHippoEventListener {

        private String channelName;
        private String eventCategory;
        private boolean onlyNewEvents = true;

        @Override
        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(final String channelName) {
            this.channelName = channelName;
        }

        @Override
        public String getEventCategory() {
            return eventCategory;
        }

        public void setEventCategory(final String eventCategory) {
            this.eventCategory = eventCategory;
        }

        @Override
        public boolean onlyNewEvents() {
            return isOnlyNewEvents();
        }

        public boolean isOnlyNewEvents() {
            return onlyNewEvents;
        }

        public void setOnlyNewEvents(boolean onlyNewEvents) {
            this.onlyNewEvents = onlyNewEvents;
        }

        @Override
        public void onHippoEvent(HippoEvent event) {
            handleHippoEvent(event);
        }
    }

}

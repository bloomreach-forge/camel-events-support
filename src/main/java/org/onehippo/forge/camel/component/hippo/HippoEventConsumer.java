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

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.SuspendableService;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultEndpoint;
import org.onehippo.cms7.event.HippoEvent;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.cms7.services.eventbus.HippoEventBus;
import org.onehippo.cms7.services.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class HippoEventConsumer extends DefaultConsumer implements SuspendableService {

    private static final transient Logger LOG = LoggerFactory.getLogger(HippoEventConsumer.class);

    private final HippoEventEndpoint endpoint;
    private final Processor processor;
    private final Map<String, Object> properties;

    private HippoEventListener eventListener;

    public HippoEventConsumer(HippoEventEndpoint endpoint, Processor processor, Map<String, Object> properties) {
        super(endpoint, processor);

        this.endpoint = endpoint;
        this.processor = processor;
        this.properties = properties;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        eventListener = new HippoEventListener();
        HippoServiceRegistry.registerService(eventListener, HippoEventBus.class); 
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        HippoServiceRegistry.unregisterService(eventListener, HippoEventBus.class);
    }

    protected Exchange createExchange(HippoEvent<?> event) {
        Exchange exchange = ((DefaultEndpoint) getEndpoint()).createExchange();
        exchange.setIn(new HippoEventMessage(HippoEventConverter.toJSONObject(event)));
        return exchange;
    }

    public class HippoEventListener {

        @Subscribe
        public void handleEvent(HippoEvent<?> event) {
            Exchange exchange = createExchange(event);
            RuntimeCamelException rce = null;

            try {
                processor.process(exchange);
            } catch (Exception e) {
                exchange.setException(e);
            }

            rce = exchange.getException(RuntimeCamelException.class);

            if (rce != null) {
                throw rce;
            }
        }
    }
}

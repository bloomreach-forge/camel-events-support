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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * 
 */
public class HippoEventEndpoint extends DefaultEndpoint {

    private final Map<String, Object> properties;

    protected HippoEventEndpoint(final String endpointUri, final HippoEventComponent component, final Map<String, Object> properties) {
        super(endpointUri, component);
        this.properties = properties;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        HippoEventConsumer answer = new HippoEventConsumer(this, processor);
        configureConsumer(answer);
        return answer;
    }

    @Override
    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("Producer is not supported.");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Set<String> getPropertyNameSet() {
        if (properties == null) {
            return Collections.emptySet();
        }

        return Collections.unmodifiableSet(properties.keySet());
    }

    public Object getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }

        return null;
    }

    public boolean hasProperty(String name) {
        if (properties == null) {
            return false;
        }

        return properties.containsKey(name);
    }
}

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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class HippoEventComponent extends DefaultComponent {

    private static final transient Logger LOG = LoggerFactory.getLogger(HippoEventComponent.class);

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> properties) throws Exception {
        Map<String, Object> endPointProps;

        if (properties == null) {
            endPointProps = new HashMap<String, Object>();
        } else {
            endPointProps = new HashMap<String, Object>(properties);
            properties.clear();
        }

        HippoEventEndpoint endpoint = new HippoEventEndpoint(uri, this, endPointProps);

        return endpoint;
    }
}

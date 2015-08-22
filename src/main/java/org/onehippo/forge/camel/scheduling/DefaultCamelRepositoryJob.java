/*
 * Copyright 2015-2015 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.forge.camel.scheduling;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.camel.util.CamelContextUtils;
import org.onehippo.repository.scheduling.RepositoryJob;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link RepositoryJob} implementation invoking Camel endpoint.
 * <P>
 * This default {@link RepositoryJob} implementation reads attributes like the following from {@link RepositoryJobExecutionContext}:
 * </P>
 * <ul>
 * <li><code>camel.endpoint.uri</code>: Camel Endpoint URI to invoke. This is required.</li>
 * <li><code>camel.context.id</code>: Camel Context ID. If not provided, it retrieves the first one found.</li>
 * </ul>
 */
public class DefaultCamelRepositoryJob implements RepositoryJob {

    private static Logger log = LoggerFactory.getLogger(DefaultCamelRepositoryJob.class);

    public static final String CAMEL_ENDPOINT_URI_ATTR = "camel.endpoint.uri";

    public static final String CAMEL_CONTEXT_ID_ATTR = "camel.context.id";

    @Override
    public void execute(RepositoryJobExecutionContext context) throws RepositoryException {
        String endpointUri = context.getAttribute(CAMEL_ENDPOINT_URI_ATTR);

        if (StringUtils.isBlank(endpointUri)) {
            throw new RepositoryException("Invalid Camel Endpoint URI: '" + endpointUri + "'.");
        }

        String camelContextId = context.getAttribute(CAMEL_CONTEXT_ID_ATTR);

        CamelContextUtils.invokeManagedCamelContextMBean(camelContextId,
                                                         "sendBody",
                                                         new Object[] { endpointUri, context },
                                                         new String[] { "java.lang.String", "java.lang.Object" });
    }

}

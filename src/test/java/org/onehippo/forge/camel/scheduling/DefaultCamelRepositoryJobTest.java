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
package org.onehippo.forge.camel.scheduling;

import javax.jcr.RepositoryException;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultCamelRepositoryJobTest {

    @Test
    void execute_blankEndpointUri_throwsRepositoryException() throws Exception {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttribute(DefaultCamelRepositoryJob.CAMEL_ENDPOINT_URI_ATTR)).andReturn("  ");
        EasyMock.replay(ctx);

        assertThrows(RepositoryException.class, () -> new DefaultCamelRepositoryJob().execute(ctx));

        EasyMock.verify(ctx);
    }

    @Test
    void execute_nullEndpointUri_throwsRepositoryException() throws Exception {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttribute(DefaultCamelRepositoryJob.CAMEL_ENDPOINT_URI_ATTR)).andReturn(null);
        EasyMock.replay(ctx);

        assertThrows(RepositoryException.class, () -> new DefaultCamelRepositoryJob().execute(ctx));

        EasyMock.verify(ctx);
    }

    @Test
    void execute_validUri_noRegisteredCamelContext_throwsRuntimeException() throws Exception {
        RepositoryJobExecutionContext ctx = EasyMock.createMock(RepositoryJobExecutionContext.class);
        EasyMock.expect(ctx.getAttribute(DefaultCamelRepositoryJob.CAMEL_ENDPOINT_URI_ATTR)).andReturn("direct:test");
        EasyMock.expect(ctx.getAttribute(DefaultCamelRepositoryJob.CAMEL_CONTEXT_ID_ATTR)).andReturn(null);
        EasyMock.replay(ctx);

        // no Camel MBean registered in this JVM, so CamelContextUtils throws RuntimeException
        assertThrows(RuntimeException.class, () -> new DefaultCamelRepositoryJob().execute(ctx));

        EasyMock.verify(ctx);
    }
}

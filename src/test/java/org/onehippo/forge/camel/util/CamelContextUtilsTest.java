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
package org.onehippo.forge.camel.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CamelContextUtilsTest {

    @Test
    void invokeManagedCamelContextMBean_namedContextNotFound_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                CamelContextUtils.invokeManagedCamelContextMBean(
                        "nonexistent-context-xyz",
                        "sendBody",
                        new Object[]{"direct:a", "body"},
                        new String[]{"java.lang.String", "java.lang.Object"}));

        assertTrue(ex.getMessage().contains("No CamelContext found by name"));
    }

    @Test
    void invokeManagedCamelContextMBean_noContextId_noRegisteredContext_throwsRuntimeException() {
        // null/blank camelContextId uses wildcard ObjectName; no Camel MBean is registered in this JVM
        assertThrows(RuntimeException.class, () ->
                CamelContextUtils.invokeManagedCamelContextMBean(
                        "sendBody",
                        new Object[]{"direct:a", "body"},
                        new String[]{"java.lang.String", "java.lang.Object"}));
    }

    @Test
    void invokeManagedCamelContextMBean_malformedContextId_throwsRuntimeException() {
        // "valid,extra" creates "org.apache.camel:name=valid,extra" — the "extra" key has no '=' value
        // pair which is illegal per JMX ObjectName spec → MalformedObjectNameException
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                CamelContextUtils.invokeManagedCamelContextMBean(
                        "valid,extra",
                        "sendBody",
                        new Object[]{"direct:a", "body"},
                        new String[]{"java.lang.String", "java.lang.Object"}));

        assertTrue(ex.getMessage().contains("MalformedObjectNameException"));
    }
}

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
package org.onehippo.forge.camel.util;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.StringValueExp;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class to invoke CamelContext.
 */
public class CamelContextUtils {

    private static final String CAMEL_CONTEXT_MBEAN_INSTANCE_TYPE = "org.apache.camel.management.mbean.ManagedCamelContext";

    /**
     * Finds {@code ManagedCamelContextMBean}s from the platform MBean server and invokes the {@code operation}
     * with {@code params} of which signature is like {@code signature} on the first found {@code ManagedCamelContextMBean} MBean.
     * <p>Example code:</p>
     * <pre>
     * CamelContextUtils.invokeManagedCamelContextMBean("sendBody", new Object [] { "direct:test", body }, new String {} { String.class.getName(), Object.class.getName() });;
     * </pre>
     * @param operation operation name
     * @param params parameters array
     * @param signature signature of parameters
     * @return operation return
     */
    public static Object invokeManagedCamelContextMBean(final String operation, Object [] params, String [] signature) {
        return invokeManagedCamelContextMBean(null, operation, params, signature);
    }

    /**
     * Finds the {@code ManagedCamelContextMBean} having {@code camelContextId} from the platform MBean server and invokes the {@code operation}
     * with {@code params} of which signature is like {@code signature} on the found {@code ManagedCamelContext} MBean.
     * If a blank {@code camelContextId} provided, then it invokes the operation on the first found {@code ManagedCamelContextMBean} MBean.
     * <p>Example code:</p>
     * <pre>
     * CamelContextUtils.invokeManagedCamelContextMBean("camel-1", "sendBody", new Object [] { "direct:test", body }, new String {} { String.class.getName(), Object.class.getName() });;
     * </pre>
     * @param camelContextId camel context ID
     * @param operation operation name
     * @param params parameters array
     * @param signature signature of parameters
     * @return operation return
     */
    public static Object invokeManagedCamelContextMBean(final String camelContextId, final String operation, Object [] params, String [] signature) {
        Object ret = null;

        try {
            QueryExp expr = Query.isInstanceOf(new StringValueExp(CAMEL_CONTEXT_MBEAN_INSTANCE_TYPE));
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName;
            if (StringUtils.isNotBlank(camelContextId)) {
                objectName = new ObjectName("org.apache.camel:name=" + camelContextId);
            } else {
                objectName = new ObjectName("org.apache.camel:*");
            }

            Set<ObjectName> objectNames = mbeanServer.queryNames(objectName, expr);

            if (!objectNames.isEmpty()) {
                final ObjectName firstObjectName = objectNames.iterator().next();
                ret = mbeanServer.invoke(firstObjectName, operation, params, signature);
            } else {
                throw new RuntimeException("No CamelContext found by name: " + objectName);
            }
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("MalformedObjectNameException: " + e, e);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException("InstanceNotFoundException: " + e, e);
        } catch (ReflectionException e) {
            throw new RuntimeException("ReflectionException: " + e, e);
        } catch (MBeanException e) {
            throw new RuntimeException("MBeanException: " + e, e);
        }

        return ret;
    }
}

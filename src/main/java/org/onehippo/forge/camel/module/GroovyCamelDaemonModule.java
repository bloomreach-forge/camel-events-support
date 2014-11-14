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
package org.onehippo.forge.camel.module;

import groovy.lang.GroovyShell;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.StringUtils;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class GroovyCamelDaemonModule extends AbstractReconfigurableDaemonModule {

    private static Logger log = LoggerFactory.getLogger(GroovyCamelDaemonModule.class);

    private CamelContext camelContext;
    private String routeBuilderExpression;
    private GroovyShell groovyShell;

    public GroovyCamelDaemonModule() {
        groovyShell = new RouteBuilderGroovyShell();
    }

    @Override
    protected void doConfigure(Node moduleConfig) throws RepositoryException {

        if (moduleConfig.hasProperty("routebuilder.expression")) {
            setRouteBuilderExpression(StringUtils.trim(moduleConfig.getProperty("routebuilder.expression").getString()));
        }

        if (getCamelContext() != null) {
            disposeCamelContext();

            try {
                setCamelContext(createNewCamelContext());
                getCamelContext().start();
            } catch (Throwable th) {
                log.error("Failed to create a camel context.", th);
            }
        }
    }

    @Override
    protected void doInitialize(Session session) throws RepositoryException {
        disposeCamelContext();

        try {
            setCamelContext(createNewCamelContext());
            getCamelContext().start();
        } catch (Throwable th) {
            log.error("Failed to create a camel context.", th);
        }
    }

    @Override
    protected void doShutdown() {
        disposeCamelContext();
    }

    protected void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    protected CamelContext getCamelContext() {
        return camelContext;
    }

    protected void setRouteBuilderExpression(String routeBuilderExpression) {
        this.routeBuilderExpression = routeBuilderExpression;
    }

    protected String getRouteBuilderExpression() {
        return routeBuilderExpression;
    }

    protected GroovyShell getGroovyShell() {
        return groovyShell;
    }

    protected void setGroovyShell(GroovyShell groovyShell) {
        this.groovyShell = groovyShell;
    }

    protected CamelContext createNewCamelContext() throws Exception {
        CamelContext context = null;

        if (StringUtils.isNotEmpty(getRouteBuilderExpression())) {
            RouteBuilder routeBuilder = (RouteBuilder) getGroovyShell().evaluate(getRouteBuilderExpression());
            context = new DefaultCamelContext();
            context.addRoutes(routeBuilder);
        }

        return context;
    }

    protected void disposeCamelContext() {
        if (getCamelContext() != null) {
            try {
                getCamelContext().stop();
            } catch (Throwable th) {
                log.error("Failed to stop camel context.", th);
            } finally {
                setCamelContext(null);
            }
        }
    }
}

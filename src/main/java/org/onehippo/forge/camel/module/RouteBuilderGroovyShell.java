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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Arrays;

import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

/**
 * 
 */
public class RouteBuilderGroovyShell extends GroovyShell {

    private static final String[] defaultImports = {
            "org.apache.camel", "org.apache.camel.builder"
    };

    private static final String[] importsBlacklist = {
            "java.io.File", "java.io.FileDescriptor", "java.io.FileInputStream",
            "java.io.FileOutputStream", "java.io.FileWriter", "java.io.FileReader"
    };

    private static final String[] starImportsBlacklist = {
            "java.nio.file", "java.net", "javax.net", "javax.net.ssl", "java.lang.reflect"
    };

    public RouteBuilderGroovyShell() {
        this(new Binding());
    }

    public RouteBuilderGroovyShell(final Binding binding) {
        this(RouteBuilderGroovyShell.class.getClassLoader(), binding);
    }

    public RouteBuilderGroovyShell(final ClassLoader classLoader, final Binding binding) {
        super(classLoader, binding, createCompilerConfiguration());
    }

    private static CompilerConfiguration createCompilerConfiguration() {
        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        compilerConfiguration.addCompilationCustomizers(createImportCustomizer(), createSecurityCustomizer());
        return compilerConfiguration;
    }

    private static CompilationCustomizer createImportCustomizer() {
        final ImportCustomizer importCustomizer = new ImportCustomizer();
        importCustomizer.addStarImports(defaultImports);
        return importCustomizer;
    }

    private static CompilationCustomizer createSecurityCustomizer() {
        final SecureASTCustomizer securityCustomizer = new SecureASTCustomizer();
        securityCustomizer.setImportsBlacklist(Arrays.asList(importsBlacklist));
        securityCustomizer.setStarImportsBlacklist(Arrays.asList(starImportsBlacklist));
        securityCustomizer.setIndirectImportCheckEnabled(true);
        securityCustomizer.addExpressionCheckers(new RouterBuilderExpressionChecker());
        return securityCustomizer;
    }

    private static final class RouterBuilderExpressionChecker implements SecureASTCustomizer.ExpressionChecker {

        @Override
        public boolean isAuthorized(final Expression expression) {
            if (isSystemExitCall(expression)) {
                return false;
            }

            if (isRuntimeCall(expression)) {
                return false;
            }

            if (isClassForNameCall(expression)) {
                return false;
            }

            return true;
        }

        private boolean isSystemExitCall(final Expression expression) {
            if (expression instanceof MethodCallExpression) {
                final Expression objectExpression = ((MethodCallExpression) expression).getObjectExpression();

                if (objectExpression instanceof ClassExpression) {
                    if (objectExpression.getType().getName().equals(System.class.getName())) {
                        if (((MethodCallExpression) expression).getMethodAsString().equals("exit")) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        private boolean isRuntimeCall(final Expression expression) {
            if (expression instanceof MethodCallExpression) {
                final Expression objectExpression = ((MethodCallExpression) expression).getObjectExpression();

                if (objectExpression instanceof ClassExpression) {
                    if (objectExpression.getType().getName().equals(Runtime.class.getName())) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean isClassForNameCall(final Expression expression) {
            if (expression instanceof MethodCallExpression) {
                final Expression objectExpression = ((MethodCallExpression) expression).getObjectExpression();

                if (objectExpression instanceof ClassExpression) {
                    if (objectExpression.getType().getName().equals(Class.class.getName())) {
                        if (((MethodCallExpression) expression).getMethodAsString().equals("forName")) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }
}

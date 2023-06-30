package org.bloomreach.xm.cms;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:custom-camel-${routes.mode:file}.xml"})
public class CustomConfiguration {
}
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
package com.bloomreach.forge.camel.demo.beans;

import org.hippoecm.hst.content.beans.standard.HippoHtml;

/**
 * Interface for documents which can be (un)published to search engines.
 */
public class BaseHippoDocument extends BaseDocument {

    private final static String TITLE = "camelhippoevtdemo:title";
    private final static String INTRODUCTION = "camelhippoevtdemo:introduction";
    private final static String CONTENT = "camelhippoevtdemo:content";

    /**
     * Get the title of the document.
     *
     * @return the title
     */
    public String getTitle() {
        return getSingleProperty(TITLE);
    }

    /**
     * Get the introduction of the document.
     *
     * @return the introduction
     */
    public String getIntroduction() {
        return getSingleProperty(INTRODUCTION);
    }

    /**
     * Get the main content of the document.
     *
     * @return the content
     */
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    public String getContentString() {
        final HippoHtml html = getContent();

        if (html != null) {
            return html.getContent();
        }

        return null;
    }

}

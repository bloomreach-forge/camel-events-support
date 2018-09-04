package org.onehippo.forge.camel.demo.beans;
/*
 * Copyright 2014-2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

@HippoEssentialsGenerated(internalName = "camelhippoevtdemo:eventsdocument")
@Node(jcrType="camelhippoevtdemo:eventsdocument")
public class EventsDocument extends BaseHippoDocument {

    /**
     * The document type of the events document.
     */
    public static final String DOCUMENT_TYPE = "camelhippoevtdemo:eventsdocument";

    private static final String TITLE = "camelhippoevtdemo:title";
    private static final String DATE = "camelhippoevtdemo:date";
    private static final String INTRODUCTION = "camelhippoevtdemo:introduction";
    private static final String IMAGE = "camelhippoevtdemo:image";
    private static final String CONTENT = "camelhippoevtdemo:content";
    private static final String LOCATION = "camelhippoevtdemo:location";
    private static final String END_DATE = "camelhippoevtdemo:enddate";

    /**
     * Get the title of the document.
     *
     * @return the title
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:title")
    public String getTitle() {
        return getProperty(TITLE);
    }

    /**
     * Get the date of the document, i.e. the start date of the event.
     *
     * @return the (start) date
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:date")
    public Calendar getDate() {
        return getProperty(DATE);
    }

    /**
     * Get the introduction of the document.
     *
     * @return the introduction
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:introduction")
    public String getIntroduction() {
        return getProperty(INTRODUCTION);
    }

    /**
     * Get the image of the document.
     *
     * @return the image
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:image")
    public HippoGalleryImageSet getImage() {
        return getLinkedBean(IMAGE, HippoGalleryImageSet.class);
    }

    /**
     * Get the main content of the document.
     *
     * @return the content
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:content")
    public HippoHtml getContent() {
        return getHippoHtml(CONTENT);
    }

    /**
     * Get the location of the document.
     *
     * @return the location
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:location")
    public String getLocation() {
        return getProperty(LOCATION);
    }

    /**
     * Get the end date of the document, i.e. the end date of the event.
     *
     * @return the end date
     */
    @HippoEssentialsGenerated(internalName = "camelhippoevtdemo:enddate")
    public Calendar getEndDate() {
        return getProperty(END_DATE);
    }

}

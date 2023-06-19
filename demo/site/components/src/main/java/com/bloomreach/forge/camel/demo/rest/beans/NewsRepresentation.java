/*
 * Copyright 2014-2023 Bloomreach B.V. (https://www.bloomreach.com)
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
package com.bloomreach.forge.camel.demo.rest.beans;

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlRootElement;

import org.hippoecm.hst.jaxrs.model.content.HippoDocumentRepresentation;
import org.hippoecm.hst.jaxrs.model.content.HippoHtmlRepresentation;

import com.bloomreach.forge.camel.demo.beans.NewsDocument;

@XmlRootElement(name = "news")
public class NewsRepresentation extends HippoDocumentRepresentation {

    private String title;
    private String author;
    private Calendar date;
    private String introduction;
    private HippoHtmlRepresentation contentRep;

    public NewsRepresentation() {
        super();
    }

    public NewsRepresentation represent(NewsDocument newsBean) throws RepositoryException {
        super.represent(newsBean);

        setTitle(newsBean.getTitle());
        setAuthor(newsBean.getAuthor());
        setDate(newsBean.getDate());
        setIntroduction(newsBean.getIntroduction());
        contentRep = new HippoHtmlRepresentation().represent(newsBean.getContent());

        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getContent() {
        return contentRep.getContent();
    }

    public void setContent(String content) {
        contentRep.setContent(content);
    }

}

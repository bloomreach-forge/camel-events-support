/*
 * Copyright 2014-2024 Bloomreach B.V. (https://www.bloomreach.com)
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
package com.bloomreach.forge.camel.demo.rest.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomreach.forge.camel.demo.beans.BaseHippoDocument;

import net.sf.json.JSONObject;

/**
 * ElasticSearch REST-based Search Engine Update REST Service implementation.
 * <p>
 * This class simply posts JSON HTTP body to ElasticSearch Update Service REST URL
 * to either add a document or remove a document.
 * </p>
 * <p>
 * See {@link #createDocumentAddPayload(BaseHippoDocument)} and {@link #createDocumentDeletePayload(BaseHippoDocument)}
 * methods for detail on how the JSON payloads are created.
 * </p>
 */
@Path("/es/update/")
public class ElasticSearchRestUpdateResource extends AbstractRestUpdateResource {

    private static Logger log = LoggerFactory.getLogger(ElasticSearchRestUpdateResource.class);

    /**
     * ElasticSearch index name property name to be internally used in the payload JSON object.
     */
    public static final String INDEX_PROP = "_index";

    /**
     * ElasticSearch document type name property name to be internally used in the payload JSON object.
     */
    public static final String TYPE_PROP = "_type";

    /**
     * Default Index name.
     * <p>
     * <em>Note:</em> Index name can be overriden by adding {@link #INDEX_PROP} property in the payload JSON object.
     * </p>
     */
    private String defaultIndexName = "pages";

    /**
     * Default Document type name.
     * <p>
     * <em>Note:</em> Document type name can be overriden by adding {@link #TYPE_PROP} property in the payload JSON object.
     * </p>
     */
    private String defaultTypeName = "page";

    /**
     * Returns default index name.
     * @return
     */
    public String getDefaultIndexName() {
        return defaultIndexName;
    }

    /**
     * Sets default index name.
     * @param defaultIndexName
     */
    public void setDefaultIndexName(String defaultIndexName) {
        this.defaultIndexName = defaultIndexName;
    }

    /**
     * Returns default type name.
     * @return
     */
    public String getDefaultTypeName() {
        return defaultTypeName;
    }

    /**
     * Sets default type name.
     * @param defaultTypeName
     */
    public void setDefaultTypeName(String defaultTypeName) {
        this.defaultTypeName = defaultTypeName;
    }

    /**
     * Creates document addition JSON payload.
     * <p>
     * Example payload looks like this:
     * <pre>
     * {
     *   "id":"cafebabe-cafe-babe-cafe-babecafebabe",
     *   "url": "http://www.onehippo.org/",
     *   "title":"Enterprise Open Source Java cms - Hippo CMS",
     *   "description":"Hippo Open Source Enterprise Content Management",
     *   "text":"We are big believers in open source. We use and contribute to many open source components - and our entire core is available under the Apache License."
     * }
     * </pre>
     * </p>
     * @param document The hippo document to be updated in Solr Search index.
     * @return
     */
    @Override
    protected JSONObject createDocumentAddPayload(final BaseHippoDocument document) {
        List<String> urls = getAvailablePageURLs(document);

        if (urls.isEmpty()) {
            return null;
        }

        JSONObject doc = new JSONObject();
        doc.put("id", document.getCanonicalHandleUUID());
        doc.put("url", urls.get(0));
        doc.put("title", document.getTitle());
        doc.put("description", document.getIntroduction());
        doc.put("text", document.getContentString());
        return doc;
    }

    /**
     * Creates document deletion JSON payload.
     * <p>
     * Example payload looks like this:
     * <pre>
     * {
     *   "id":"cafebabe-cafe-babe-cafe-babecafebabe"
     * }
     * </pre>
     * </p>
     * @param id
     * @return
     */
    @Override
    protected JSONObject createDocumentDeletePayload(final String id) {
        JSONObject doc = new JSONObject();
        doc.put("id", id);
        return doc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequestBase createHttpRequest(final String action, final JSONObject payload) throws IOException {
        HttpRequestBase request = null;
        final String indexTypePath = getIndexTypePath(payload);

        if (INDEX_ACTION.equals(action)) {
            request = new HttpPut(getBaseUrl() + "/" + indexTypePath + "/" + payload.get("id"));
            request.setHeader("Content-Type", "application/json; charset=UTF-8");
            HttpEntity entity = new StringEntity(payload.toString(), "UTF-8");
            ((HttpPut) request).setEntity(entity);
        } else if (DELETE_ACTION.equals(action)) {
            request = new HttpDelete(getBaseUrl() + "/" + indexTypePath + "/" + payload.get("id"));
        }

        return request;
    }

    protected String getIndexTypePath(final JSONObject payload) {
        List<String> tokens = new ArrayList<String>();

        if (payload.has(INDEX_PROP) && StringUtils.isNotEmpty(payload.getString(INDEX_PROP))) {
            tokens.add(payload.getString(INDEX_PROP));
        } else if (StringUtils.isNotEmpty(getDefaultIndexName())) {
            tokens.add(getDefaultIndexName());
        }

        if (payload.has(TYPE_PROP) && StringUtils.isNotEmpty(payload.getString(TYPE_PROP))) {
            tokens.add(payload.getString(TYPE_PROP));
        } else if (StringUtils.isNotEmpty(getDefaultTypeName())) {
            tokens.add(getDefaultTypeName());
        }

        if (tokens.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return StringUtils.join(tokens, "/");
    }
}

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
package com.bloomreach.forge.camel.demo.rest.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Path;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import com.bloomreach.forge.camel.demo.beans.BaseHippoDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solr REST-based Search Engine Update REST Service implementation.
 * <p>
 * This class simply posts JSON HTTP body to Solr Update Service REST URL
 * to either add a document or remove a document.
 * </p>
 * <p>
 * See {@link #createDocumentAddPayload(BaseHippoDocument)} and {@link #createDocumentDeletePayload(BaseHippoDocument)}
 * methods for detail on how the JSON payloads are created.
 * </p>
 */
@Path("/solr/update/")
public class SolrRestUpdateResource extends AbstractRestUpdateResource {

    private static Logger log = LoggerFactory.getLogger(SolrRestUpdateResource.class);

    /**
     * Solr core name to update.
     */
    private String coreName = "collection1";

    /**
     * Returns Solr core name to update.
     * @return
     */
    public String getCoreName() {
        return coreName;
    }

    /**
     * Sets Solr core name to update.
     * @param solrCoreName
     */
    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpRequestBase createHttpRequest(final String action, final JSONObject payload) throws IOException {
        HttpPost request = new HttpPost(getBaseUrl() + "/" + getCoreName() + "/update?wt=json");
        request.setHeader("Content-Type", "application/json; charset=UTF-8");
        HttpEntity entity = new StringEntity(payload.toString(), "UTF-8");
        request.setEntity(entity);
        return request;
    }

    /**
     * Creates document addition JSON payload.
     * <p>
     * Example payload looks like this:
     * <pre>
     * {
     *   "add": {
     *     "doc": {
     *       "id":"cafebabe-cafe-babe-cafe-babecafebabe",
     *       "url": "http://www.onehippo.org/",
     *       "title":"Enterprise Open Source Java cms - Hippo CMS",
     *       "description":"Hippo Open Source Enterprise Content Management",
     *       "text":"We are big believers in open source. We use and contribute to many open source components - and our entire core is available under the Apache License."
     *     },
     *     "boost":1.0,
     *     "overwrite":true,
     *     "commitWithin",1000
     *   },
     *   "commit":{}
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
        // NOTE: It will be needed to update all the available link URLs from the document,
        //       but for demonstration purpose for now, just set the first URL only in this example.
        doc.put("url", urls.get(0));
        doc.put("title", document.getTitle());
        doc.put("description", document.getIntroduction());
        doc.put("text", document.getContentString());

        JSONObject add = new JSONObject();
        add.put("doc", doc);
        add.put("boost", 1.0);
        add.put("overwrite", true);
        add.put("commitWithin", 1000);

        JSONObject body = new JSONObject();
        body.put("add", add);
        body.put("commit", new JSONObject());

        return body;
    }

    /**
     * Creates document deletion JSON payload.
     * <p>
     * Example payload looks like this:
     * <pre>
     * {
     *   "delete": {
     *     "id":"cafebabe-cafe-babe-cafe-babecafebabe",
     *   },
     *   "commit":{}
     * }
     * </pre>
     * </p>
     * @param id
     * @return
     */
    @Override
    protected JSONObject createDocumentDeletePayload(final String id) {
        JSONObject delete = new JSONObject();
        delete.put("id", id);

        JSONObject body = new JSONObject();
        body.put("delete", delete);
        body.put("commit", new JSONObject());

        return body;
    }
}

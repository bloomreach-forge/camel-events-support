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
import java.util.LinkedList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.jaxrs.services.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomreach.forge.camel.demo.beans.BaseHippoDocument;
import com.bloomreach.forge.camel.demo.util.HstLinkCreatorUtils;

import org.json.JSONObject;

/**
 * Abstract REST-based Search Engine Update REST Service class.
 * <p>
 * This class simply posts JSON HTTP body to Search Engine Update Service REST URL
 * to either add a document or remove a document.
 * </p>
 * <p>
 * See implementations of {@link #createDocumentAddPayload(BaseHippoDocument)} and {@link #createDocumentDeletePayload(BaseHippoDocument)}
 * methods in child classes for detail on how the JSON payloads are created
 * for the specific search engine REST service.
 * </p>
 */
public abstract class AbstractRestUpdateResource extends AbstractResource {

    private static Logger log = LoggerFactory.getLogger(AbstractRestUpdateResource.class);

    /**
     * Document action constact for indexing.
     */
    public static final String INDEX_ACTION = "index";

    /**
     * Document action constact for deleting.
     */
    public static final String DELETE_ACTION = "delete";

    /**
     * REST-based Search Engine Update Service base URL.
     */
    private String baseUrl;

    /**
     * Http Client to invoke the Solr Update Service with.
     */
    private CloseableHttpClient httpClient;

    /**
     * Returns Update Service base URL.
     * @return
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets Update Service base URL.
     * @param baseUrl
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns Http Client to invoke the Solr Update Service with.
     * @return
     */
    public CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }

        return httpClient;
    }

    /**
     * Sets Http Client to invoke the Solr Update Service with.
     * @param httpClient
     */
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lifecycle method to be invoked when this bean is being destroyed by the IoC container.
     */
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error("Exception occurred during closing httpClient.", e);
            }
        }
    }

    /**
     * Invoke Solr Update Service based on the given <code>action</code> and <code>id</code>.
     * @param action Update action name. It should be either 'addOrReplace' or 'delete'.
     * @param handleUuid The document handle identifier.
     * @return
     */
    @POST
    @Path("/")
    public Response update(@QueryParam("action") @DefaultValue(INDEX_ACTION) String action, @QueryParam("id") String handleUuid) {

        log.info("Updating ('{}') document from search index: {}", action, handleUuid);

        if (StringUtils.isNotEmpty(handleUuid)) {
            try {
                HstRequestContext requestContext = RequestContextProvider.get();

                if (INDEX_ACTION.equals(action)) {

                    Node node = requestContext.getSession().getNodeByIdentifier(handleUuid);
                    HippoBean bean = (HippoBean) getObjectConverter(requestContext).getObject(node);

                    if (bean instanceof BaseHippoDocument) {
                        BaseHippoDocument document = (BaseHippoDocument) bean;
                        JSONObject payload = createDocumentAddPayload(document);

                        if (payload != null) {
                            HttpResponse httpResponse = invokeUpdateService(action, payload);

                            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                                return Response.status(httpResponse.getStatusLine().getStatusCode()).build();
                            }
                        }
                    } else {
                        log.warn("The bean from '{}' is not a BaseHippoDocument.", handleUuid);
                    }

                } else if (DELETE_ACTION.equals(action)) {

                    JSONObject payload = createDocumentDeletePayload(handleUuid);
                    HttpResponse httpResponse = invokeUpdateService(action, payload);

                    final int status = httpResponse.getStatusLine().getStatusCode();

                    if (status >= HttpServletResponse.SC_BAD_REQUEST) {
                        if (status == HttpServletResponse.SC_NOT_FOUND || status == HttpServletResponse.SC_GONE) {
                            log.info("The document is not found or no more exists: '{}'.", handleUuid);
                        } else if (status != HttpServletResponse.SC_OK) {
                            return Response.status(httpResponse.getStatusLine().getStatusCode()).build();
                        }
                    }

                } else {

                    log.warn("Unknown action: '{}'.", action);

                }
            } catch (ItemNotFoundException e) {
                log.warn("The news is not found by the identifier: '{}'", handleUuid);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.warn("Failed to find news by identifier.", e);
                } else {
                    log.warn("Failed to find news by identifier. {}", e.toString());
                }

                throw new WebApplicationException(e, buildServerErrorResponse(e));
            }
        }

        return Response.ok().build();
    }

    /**
     * Creates document addition JSON payload.
     * @param document The hippo document to be updated in Solr Search index.
     * @return
     */
    abstract protected JSONObject createDocumentAddPayload(final BaseHippoDocument document);

    /**
     * Creates document deletion JSON payload.
     * @param id
     * @return
     */
    abstract protected JSONObject createDocumentDeletePayload(final String id);

    /**
     * Invokes Solr Update Service REST service with the given <code>payload</code>.
     * @param action Update action name. It should be either 'addOrReplace' or 'delete'.
     * @param payload JSON payload to be used as HTTP POST message body.
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    protected HttpResponse invokeUpdateService(final String action, final JSONObject payload) throws ClientProtocolException, IOException {
        HttpClient client = getHttpClient();
        HttpRequestBase request = createHttpRequest(action, payload);
        log.info("Invoking Search Engine Updater Service: '{}', '{}'.", request.getURI(), payload);

        HttpResponse response = client.execute(request);
        log.info("Response Status: {}", response.getStatusLine());

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            log.info("Response Entity: {}", EntityUtils.toString(entity));
        }

        return response;
    }

    /**
     * Creates {@link HttpRequestBase} to be used in Search Engine Update REST Service calls.
     * @param action Update action name. It should be either 'addOrReplace' or 'delete'.
     * @param payload
     * @return
     * @throws IOException
     */
    abstract protected HttpRequestBase createHttpRequest(final String action, final JSONObject payload) throws IOException;

    /**
     * Creates a generic JAX-RS Error Response from the given error.
     * @param th
     * @return
     */
    protected Response buildServerErrorResponse(Throwable th) {
        return Response.serverError().entity(th.getCause() != null ? th.getCause().toString() : th.toString()).build();
    }

    /**
     * Returns all the available page link URLs from the document {@code bean}.
     * @param bean
     * @return
     */
    protected List<String> getAvailablePageURLs(final HippoBean bean) {
        final List<String> urls = new LinkedList<>();

        final HstRequestContext requestContext = RequestContextProvider.get();
        List<HstLink> links = HstLinkCreatorUtils.createAllLinks(bean.getNode());

        for (HstLink link : links) {
            urls.add(link.toUrlForm(requestContext, true));
        }

        return urls;
    }

}

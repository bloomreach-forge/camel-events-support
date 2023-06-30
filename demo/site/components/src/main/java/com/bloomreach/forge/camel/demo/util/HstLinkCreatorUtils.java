/*
 *  Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.bloomreach.forge.camel.demo.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.sitemap.HstSiteMap;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * HstLinkCreator utility to support creating all the available links.
 */
public final class HstLinkCreatorUtils {

    private HstLinkCreatorUtils() {
    }

    /**
     * Creates all the possible {@code HstLink}s on the given {@code node}.
     * @param node the context document node
     * @return all the available {@code HstLink}s from the context node
     */
    public static List<HstLink> createAllLinks(final Node node) {
        return createAllLinks(node, null);
    }

    /**
     * Creates all the possible {@code HstLink}s on the given {@code node} for {@code hostGroupName}.
     * @param node the context document node
     * @param hostGroupName the target host group name against which links are generated
     * @return all the available {@code HstLink}s from the context node
     */
    public static List<HstLink> createAllLinks(final Node node, final String hostGroupName) {
        return createAllLinks(node, hostGroupName, null);
    }

    /**
     * Creates all the possible {@code HstLink}s on the given {@code node}
     * for {@code hostGroupName} and {@code type}.
     * @param node the context document node
     * @param hostGroupName the target host group name against which links are generated
     * @param type the mount type against which links are generated
     * @return all the available {@code HstLink}s from the context node
     */
    public static List<HstLink> createAllLinks(final Node node, final String hostGroupName, final String type) {
        final List<HstLink> links = new LinkedList<>();

        final HstRequestContext requestContext = RequestContextProvider.get();
        final HstLinkCreator linkCreator = requestContext.getHstLinkCreator();

        List<HstLink> canonicalLinks =
                linkCreator.createAllAvailableCanonicals(node, requestContext, type, hostGroupName);

        for (HstLink canonicalLink : canonicalLinks) {
            addAllAvailableLinks(links, canonicalLink, linkCreator, node);
        }

        return removeDuplicates(links, requestContext);
    }

    private static void addAllAvailableLinks(final List<HstLink> links, final HstLink canonicalLink,
                                     final HstLinkCreator hstLinkCreator, final Node newsNode) {
        links.add(canonicalLink);

        final Mount mount = canonicalLink.getMount();
        final HstSiteMap siteMap = mount.getHstSite().getSiteMap();

        for (HstSiteMapItem siteMapItem : siteMap.getSiteMapItems()) {
            traverseSiteMapItemAndAddLink(links, siteMapItem, hstLinkCreator, mount, newsNode);
        }
    }

    private static void traverseSiteMapItemAndAddLink(final List<HstLink> links,
                                                      final HstSiteMapItem siteMapItem,
                                                      final HstLinkCreator hstLinkCreator, final Mount mount,
                                                      final Node newsNode) {
        final HstLink hstLink = hstLinkCreator.create(newsNode, mount, siteMapItem, false);

        if (!hstLink.isNotFound()) {
            links.add(hstLink);

            for (HstSiteMapItem childSiteMapItem : siteMapItem.getChildren()) {
                traverseSiteMapItemAndAddLink(links, childSiteMapItem, hstLinkCreator, mount, newsNode);
            }
        }
    }

    private static List<HstLink> removeDuplicates(final List<HstLink> allLinks,
                                                  final HstRequestContext requestContext) {
        HashMap<String, HstLink> links = new LinkedHashMap<>();

        for (HstLink link : allLinks) {
            links.put(link.toUrlForm(requestContext, false), link);
        }

        return new LinkedList<>(links.values());
    }

}
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
package org.onehippo.forge.camel.demo.container;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.core.linking.AbstractResourceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overriding the default {@code ResourceContainer} to prepend the resource's last modification
 * timestamp to the generated path.
 * <p>
 * For example, by default, binary image link is generated like the following:
 * <ul>
 * <li>/site/binaries/content/gallery/camelhippoevtdemo/samples/coffee-206142_150.jpg</li>
 * </ul>
 * But, if you configure this {@code ResourceContainer} and {@link #revisionTimestampPrependingEnabled} is true,
 * then you will get the following instead:
 * <ul>
 * <li>/site/binaries/_ht_1384250940000/content/gallery/camelhippoevtdemo/samples/coffee-206142_150.jpg</li>
 * </ul>
 * </p>
 */
public class RevisionTimePrefixedHippoGalleryImageSetContainer extends AbstractResourceContainer {

    private static Logger log = LoggerFactory.getLogger(RevisionTimePrefixedHippoGalleryImageSetContainer.class);

    private static final String REVISION_TIMESTAMP_PREFIX = "/_ht_";
    private static final String REVISION_TIMESTAMP_PATH_REGEX = "^/_ht_\\d+";

    /**
     * Flag whether or not the revision timestamp prepending should be enabled.
     */
    private boolean revisionTimestampPrependingEnabled;

    public boolean isRevisionTimestampPrependingEnabled() {
        return revisionTimestampPrependingEnabled;
    }

    public void setRevisionTimestampPrependingEnabled(boolean revisionTimestampPrependingEnabled) {
        this.revisionTimestampPrependingEnabled = revisionTimestampPrependingEnabled;
    }

    @Override
    public String getNodeType() {
        return "hippogallery:imageset";
    }

    @Override
    public String resolveToPathInfo(Node resourceContainerNode, Node resourceNode, Mount mount) {
        String pathInfo = super.resolveToPathInfo(resourceContainerNode, resourceNode, mount);

        if (isRevisionTimestampPrependingEnabled() && pathInfo != null) {
            try {
                Calendar lastModified = resourceNode.getProperty("jcr:lastModified").getDate();
                pathInfo = new StringBuilder(pathInfo.length() + 20)
                    .append(REVISION_TIMESTAMP_PREFIX)
                    .append(lastModified.getTimeInMillis())
                    .append(pathInfo)
                    .toString();
            } catch (RepositoryException e) {
                log.warn("RepositoryException while prepending lastModified timestamp.", e);
            }
        }

        return pathInfo;
    }

    public Node resolveToResourceNode(Session session, String pathInfo) {
        return super.resolveToResourceNode(session, pathInfo.replaceFirst(REVISION_TIMESTAMP_PATH_REGEX, ""));
    }

}

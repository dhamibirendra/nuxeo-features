/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */

package org.nuxeo.ecm.platform.tag;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.storage.sql.DatabaseH2;
import org.nuxeo.ecm.core.storage.sql.DatabaseHelper;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.runtime.api.Framework;

public class TestTagService extends SQLRepositoryTestCase {

    protected static final Log log = LogFactory.getLog(TestTagService.class);

    private TagService tagService;

    private DocumentModel tagRoot;

    private DocumentModel tag1;

    private DocumentModel file1;

    public boolean disableTests() {
        return !DatabaseHelper.DATABASE.getClass().equals(DatabaseH2.class);
    }

    @Override
    public void setUp() throws Exception {
        if (disableTests()) {
            log.error("test disabled for non-H2");
            return;
        }

        super.setUp();

        deployBundle("org.nuxeo.ecm.core");
        deployBundle("org.nuxeo.ecm.core.api");
        deployBundle("org.nuxeo.ecm.core.schema");
        deployBundle("org.nuxeo.ecm.core.persistence");
        deployBundle("org.nuxeo.ecm.platform.comment.core");
        deployBundle("org.nuxeo.ecm.platform.tag");
        deployBundle("org.nuxeo.ecm.platform.tag.tests");

        openSession();

        TagServiceImpl service = (TagServiceImpl) Framework.getLocalService(TagService.class);
        service.updateSchema();
    }


    @Override
    public void tearDown() throws Exception {
        if (disableTests()) {
            return;
        }
        closeSession();
        super.tearDown();

    }

    public TagService getTagService() {
        return Framework.getLocalService(TagService.class);
    }

    public void testServiceTagInitialization() throws Exception {
        if (disableTests()) {
            return;
        }
        TagService tagService = getTagService();
        assertNotNull("Failed to get tag service.", tagService);
        RepositoryManager repoService = Framework.getLocalService(RepositoryManager.class);
        assertNotNull("Failed to get repo service", repoService);
        Repository repo = repoService.getRepository("test");
        assertNotNull("Failed to acess to test repo", repo);
    }

    public void testTagCreation() throws Exception {
        if (disableTests()) {
            return;
        }
        TagService tagService = getTagService();
        DocumentModel tagRoot = tagService.getRootTag(session);
        assertNotNull(tagRoot);
        tagService.getOrCreateTag(session, tagRoot, "tag1", true);
        session.save();
        DocumentModel tag = null;
        for (DocumentModel tagChild : session.getChildren(tagRoot.getRef())) {
            if (tagChild.getProperty("tag", "label").toString().equals("tag1")) {
                tag = tagChild;
                break;
            }
        }
        assertNotNull(
                "Unable to find created tag. Probably service failing on creation.",
                tag);
        assertTrue("Private flag is not correctly set.",
                ((Long) tag.getProperty("tag", "private")) != 0);
        assertEquals(tag.getProperty("dublincore", "creator"),
                session.getPrincipal().getName());
    }

    protected void createAndTagDocument() throws ClientException {
        tagService = getTagService();
        tagRoot = tagService.getRootTag(session);
        assertNotNull(tagRoot);
        tag1 = tagService.getOrCreateTag(session, tagRoot, "tag1", true);
        file1 = session.createDocumentModel("/", "0006", "File");
        file1.setPropertyValue("dc:title", "File1");
        file1 = session.createDocument(file1);
        file1 = session.saveDocument(file1);
        session.save();
        tagService.tagDocument(session, file1, tag1.getId(), false);
    }

    public void testDetachedDocumentList() throws ClientException {
        if (disableTests()) {
            return;
        }
        createAndTagDocument();
        ((DocumentModelImpl) file1).detach(true);
        List<Tag> tags = tagService.listTagsAppliedOnDocument(session, file1);
        assertNotNull(tags);
        assertEquals(1, tags.size());
    }

    public void testEnabled() throws ClientException {
        if (disableTests()) {
            return;
        }
        TagService tagService = getTagService();
        assertTrue(tagService.isEnabled());
    }

}

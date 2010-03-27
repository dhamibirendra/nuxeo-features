/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Nuxeo
 */

package org.nuxeo.ecm.platform.publisher.test;

import org.hsqldb.jdbc.jdbcDataSource;
import org.nuxeo.common.jndi.NamingContextFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.helper.RootSectionsManager;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyTree;
import org.nuxeo.ecm.platform.publisher.impl.service.PublisherServiceImpl;
import org.nuxeo.runtime.api.Framework;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.List;

/**
 *
 * Test the {@link PublicationTree} implementations
 *
 * @author tiry
 *
 */
public class TestServiceWithCore extends SQLRepositoryTestCase {

    protected DocumentModel doc2Publish;

    public TestServiceWithCore(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        NamingContextFactory.setAsInitial();

        jdbcDataSource ds = new jdbcDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:jena");
        ds.setUser("sa");
        ds.setPassword("");
        Context context = new InitialContext();
        context.rebind("java:comp/env/jdbc/nxrelations-default-jena", ds);
        Framework.getProperties().setProperty(
                "org.nuxeo.ecm.sql.jena.databaseType", "HSQL");
        Framework.getProperties().setProperty(
                "org.nuxeo.ecm.sql.jena.databaseTransactionEnabled", "false");

        deployBundle("org.nuxeo.ecm.core.api");
        deployBundle("org.nuxeo.ecm.platform.content.template");
        deployBundle("org.nuxeo.ecm.platform.types.api");
        deployBundle("org.nuxeo.ecm.platform.types.core");
        deployBundle("org.nuxeo.ecm.platform.versioning.api");
        deployBundle("org.nuxeo.ecm.platform.versioning");
        deployBundle("org.nuxeo.ecm.relations");
        deployBundle("org.nuxeo.ecm.relations.jena");
        deployContrib("org.nuxeo.ecm.platform.publisher.test",
                "OSGI-INF/relations-default-jena-contrib.xml");

        deployBundle("org.nuxeo.ecm.platform.publisher.core.contrib");
        deployBundle("org.nuxeo.ecm.platform.publisher.core");

        openSession();
        fireFrameworkStarted();
    }

    @Override
    public void tearDown() throws Exception {
        NamingContextFactory.revertSetAsInitial();
        super.tearDown();
    }

    protected void createInitialDocs() throws Exception {

        DocumentModel wsRoot = session.getDocument(new PathRef(
                "default-domain/workspaces"));

        DocumentModel ws = session.createDocumentModel(
                wsRoot.getPathAsString(), "ws1", "Workspace");
        ws.setProperty("dublincore", "title", "test WS");
        ws = session.createDocument(ws);

        DocumentModel sectionsRoot = session.getDocument(new PathRef(
                "default-domain/sections"));

        DocumentModel section1 = session.createDocumentModel(
                sectionsRoot.getPathAsString(), "section1", "Section");
        section1.setProperty("dublincore", "title", "section1");
        section1 = session.createDocument(section1);

        DocumentModel section2 = session.createDocumentModel(
                sectionsRoot.getPathAsString(), "section2", "Section");
        section2.setProperty("dublincore", "title", "section2");
        section2 = session.createDocument(section2);

        DocumentModel section11 = session.createDocumentModel(
                section1.getPathAsString(), "section11", "Section");
        section11.setProperty("dublincore", "title", "section11");
        section11 = session.createDocument(section11);

        doc2Publish = session.createDocumentModel(ws.getPathAsString(), "file",
                "File");
        doc2Publish.setProperty("dublincore", "title", "MyDoc");

        Blob blob = new StringBlob("SomeDummyContent");
        blob.setFilename("dummyBlob.txt");
        blob.setMimeType("text/plain");
        doc2Publish.setProperty("file", "content", blob);

        doc2Publish = session.createDocument(doc2Publish);

        session.save();
    }

    public void testCorePublishing() throws Exception {

        createInitialDocs();

        // check service config
        PublisherService service = Framework.getLocalService(PublisherService.class);
        List<String> treeNames = service.getAvailablePublicationTree();
        assertEquals(1, treeNames.size());

        // check publication tree
        PublicationTree tree = service.getPublicationTree(
                treeNames.get(0), session, null);
        assertNotNull(tree);
        assertEquals("label.publication.tree.local.sections", tree.getTreeTitle());
        assertEquals("RootSectionsPublicationTree", tree.getTreeType());
        assertTrue(tree.getConfigName().startsWith("DefaultSectionsTree"));

        Boolean isRemotable = false;
        if (tree instanceof ProxyTree) {
            ProxyTree rTree = (ProxyTree) tree;
            isRemotable = true;
        }
        assertTrue(isRemotable);
        List<PublicationNode> nodes = tree.getChildrenNodes();

        assertEquals(2, nodes.size());
        assertEquals("section1", nodes.get(0).getTitle());
        assertEquals("section2", nodes.get(1).getTitle());
        List<PublicationNode> subnodes = nodes.get(0).getChildrenNodes();
        assertEquals(1, subnodes.size());
        assertEquals("section11", subnodes.get(0).getTitle());

        PublicationNode targetNode = subnodes.get(0);

        // check treeconfigName propagation
        assertEquals(tree.getConfigName(), tree.getTreeConfigName());
        assertEquals(tree.getConfigName(), nodes.get(1).getTreeConfigName());

        assertEquals(tree.getSessionId(), nodes.get(1).getSessionId());

        // check publishing
        PublishedDocument pubDoc = tree.publish(doc2Publish, targetNode);
        assertNotNull(pubDoc);
        assertEquals(1, tree.getExistingPublishedDocument(
                new DocumentLocationImpl(doc2Publish)).size());
        session.save();

        assertEquals("test", pubDoc.getSourceRepositoryName());
        DocumentModel publishedDocVersion = session.getDocument(pubDoc.getSourceDocumentRef());
        assertNotNull(publishedDocVersion);
        assertTrue(publishedDocVersion.isVersion());
        assertEquals(doc2Publish.getRef().toString(),
                publishedDocVersion.getSourceId());

        // check tree features about proxy detection
        List<PublishedDocument> detectedProxies = tree.getExistingPublishedDocument(new DocumentLocationImpl(
                publishedDocVersion));
        assertTrue(detectedProxies.size() == 1);

        detectedProxies = tree.getPublishedDocumentInNode(nodes.get(0));
        assertTrue(detectedProxies.size() == 0);

        detectedProxies = tree.getPublishedDocumentInNode(subnodes.get(0));
        assertTrue(detectedProxies.size() == 1);
        assertEquals(publishedDocVersion.getRef(),
                detectedProxies.get(0).getSourceDocumentRef());

        // check publishing 2
        PublicationNode targetNode2 = nodes.get(0);
        PublishedDocument pubDoc2 = tree.publish(doc2Publish, targetNode2);
        assertNotNull(pubDoc2);
        assertEquals(2, tree.getExistingPublishedDocument(
                new DocumentLocationImpl(doc2Publish)).size());
        session.save();

        assertEquals("test", pubDoc2.getSourceRepositoryName());
        DocumentModel publishedDocVersion2 = session.getDocument(pubDoc2.getSourceDocumentRef());
        assertNotNull(publishedDocVersion2);
        assertTrue(publishedDocVersion2.isVersion());
        assertEquals(doc2Publish.getRef().toString(),
                publishedDocVersion2.getSourceId());

        // check tree features about proxy detection
        detectedProxies = tree.getExistingPublishedDocument(new DocumentLocationImpl(
                publishedDocVersion));
        assertTrue(detectedProxies.size() == 2);

        detectedProxies = tree.getPublishedDocumentInNode(nodes.get(0));
        assertTrue(detectedProxies.size() == 1);
        assertEquals(publishedDocVersion2.getRef(),
                detectedProxies.get(0).getSourceDocumentRef());

        detectedProxies = tree.getPublishedDocumentInNode(subnodes.get(0));
        assertTrue(detectedProxies.size() == 1);

    }

    public void testCleanUp() throws Exception {
        createInitialDocs();

        deployContrib("org.nuxeo.ecm.platform.publisher.test",
                "OSGI-INF/publisher-remote-contrib-test.xml");

        assertEquals(0, PublisherServiceImpl.getLiveTreeCount());

        PublisherService service = Framework.getLocalService(PublisherService.class);

        // get a local tree
        PublicationTree ltree = service.getPublicationTree(
                "DefaultSectionsTree-default-domain", session, null);
        assertEquals(1, PublisherServiceImpl.getLiveTreeCount());

        // get a remote tree
        PublicationTree rtree = service.getPublicationTree("ClientRemoteTree",
                session, null);
        assertEquals(3, PublisherServiceImpl.getLiveTreeCount());

        // release local tree
        ltree.release();
        assertEquals(2, PublisherServiceImpl.getLiveTreeCount());

        // release remote tree
        rtree.release();
        assertEquals(0, PublisherServiceImpl.getLiveTreeCount());

    }

    public void testWrapToPublicationNode() throws Exception {
        createInitialDocs();

        PublisherService service = Framework.getLocalService(PublisherService.class);

        PublicationTree tree = service.getPublicationTree(
                service.getAvailablePublicationTree().get(0), session, null);

        DocumentModel ws1 = session.getDocument(new PathRef(
                "default-domain/workspaces/ws1"));
        assertFalse(tree.isPublicationNode(ws1));

        DocumentModel section1 = session.getDocument(new PathRef(
                "default-domain/sections/section1"));
        assertTrue(tree.isPublicationNode(section1));

        PublicationNode targetNode = service.wrapToPublicationNode(section1,
                session);
        assertNotNull(targetNode);

        PublishedDocument pubDoc = tree.publish(doc2Publish, targetNode);
        assertNotNull(pubDoc);
        assertEquals(1, tree.getExistingPublishedDocument(
                new DocumentLocationImpl(doc2Publish)).size());
    }

    public void testWithRootSections() throws Exception {
        createInitialDocs();

        RootSectionsManager rootSectionsManager = new RootSectionsManager(
                session);

        DocumentModel section1 = session.getDocument(new PathRef(
                "default-domain/sections/section1"));
        DocumentModel ws1 = session.getDocument(new PathRef(
                "default-domain/workspaces/ws1"));

        assertTrue(rootSectionsManager.canAddSection(section1, ws1));

        rootSectionsManager.addSection(section1.getId(), ws1);
        String[] sectionIdsArray = (String[]) ws1.getPropertyValue(RootSectionsManager.SECTIONS_PROPERTY_NAME);
        assertEquals(1, sectionIdsArray.length);

        PublisherService service = Framework.getLocalService(PublisherService.class);

        PublicationTree tree = service.getPublicationTree(
                service.getAvailablePublicationTree().get(0), session, null, doc2Publish);
        assertNotNull(tree);

        List<PublicationNode> nodes = tree.getChildrenNodes();
        assertEquals(1, nodes.size());

        rootSectionsManager.removeSection(section1.getId(), ws1);
        sectionIdsArray = (String[]) ws1.getPropertyValue(RootSectionsManager.SECTIONS_PROPERTY_NAME);
        assertEquals(0, sectionIdsArray.length);

        DocumentModel section2 = session.getDocument(new PathRef(
                "default-domain/sections/section2"));
        DocumentModel section11 = session.getDocument(new PathRef(
                "default-domain/sections/section1/section11"));

        rootSectionsManager.addSection(section2.getId(), ws1);
        rootSectionsManager.addSection(section11.getId(), ws1);

        // "hack" to reset the RootSectionsFinder used by the tree
        // implementation
        tree.setCurrentDocument(doc2Publish);
        nodes = tree.getChildrenNodes();
        assertEquals(2, nodes.size());

        PublicationNode node = nodes.get(1);
        assertEquals(0, node.getChildrenNodes().size());

        assertNotNull(node.getParent());
        assertEquals(tree.getPath(), node.getParent().getPath());
    }

}

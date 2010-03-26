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

package org.nuxeo.ecm.platform.publisher.impl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublicationTreeNotAvailable;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocumentFactory;
import org.nuxeo.ecm.platform.publisher.api.PublisherException;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.api.RemotePublicationTreeManager;
import org.nuxeo.ecm.platform.publisher.descriptors.PublicationTreeConfigDescriptor;
import org.nuxeo.ecm.platform.publisher.descriptors.PublicationTreeDescriptor;
import org.nuxeo.ecm.platform.publisher.descriptors.PublishedDocumentFactoryDescriptor;
import org.nuxeo.ecm.platform.publisher.helper.PublicationRelationHelper;
import org.nuxeo.ecm.platform.publisher.rules.ValidatorsRule;
import org.nuxeo.ecm.platform.publisher.rules.ValidatorsRuleDescriptor;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * POJO implementation of the publisher service Implements both
 * {@link PublisherService} and {@link RemotePublicationTreeManager}.
 *
 * @author tiry
 */
public class PublisherServiceImpl extends DefaultComponent implements
        PublisherService, RemotePublicationTreeManager, FrameworkListener {

    protected static Map<String, PublicationTreeDescriptor> treeDescriptors = new HashMap<String, PublicationTreeDescriptor>();

    protected static Map<String, PublishedDocumentFactoryDescriptor> factoryDescriptors = new HashMap<String, PublishedDocumentFactoryDescriptor>();

    protected static Map<String, PublicationTreeConfigDescriptor> treeConfigDescriptors = new HashMap<String, PublicationTreeConfigDescriptor>();

    protected static Map<String, ValidatorsRuleDescriptor> validatorsRuleDescriptors = new HashMap<String, ValidatorsRuleDescriptor>();

    protected static Map<String, PublicationTreeConfigDescriptor> pendingDescriptors = new HashMap<String, PublicationTreeConfigDescriptor>();

    private static final Log log = LogFactory.getLog(PublisherServiceImpl.class);

    protected static Map<String, PublicationTree> liveTrees = new HashMap<String, PublicationTree>();

    public static final String TREE_EP = "tree";

    public static final String TREE_CONFIG_EP = "treeInstance";

    public static final String VALIDATORS_RULE_EP = "validatorsRule";

    public static final String FACTORY_EP = "factory";

    protected static final String ROOT_PATH_KEY = "RootPath";

    protected static final String RELATIVE_ROOT_PATH_KEY = "RelativeRootPath";

    @Override
    public void activate(ComponentContext context) throws Exception {
        context.getRuntimeContext().getBundle().getBundleContext()
                .addFrameworkListener(this);
        liveTrees = new HashMap<String, PublicationTree>();
        treeDescriptors = new HashMap<String, PublicationTreeDescriptor>();
        factoryDescriptors = new HashMap<String, PublishedDocumentFactoryDescriptor>();
        treeConfigDescriptors = new HashMap<String, PublicationTreeConfigDescriptor>();
        validatorsRuleDescriptors = new HashMap<String, ValidatorsRuleDescriptor>();
        pendingDescriptors = new HashMap<String, PublicationTreeConfigDescriptor>();
    }

    // for testing cleanup
    public static int getLiveTreeCount() {
        return liveTrees.size();
    }

    public static PublicationTree getTreeBySid(String sid) {
        return liveTrees.get(sid);
    }

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {

        log.debug("Registry contribution for EP " + extensionPoint);

        if (TREE_EP.equals(extensionPoint)) {
            PublicationTreeDescriptor desc = (PublicationTreeDescriptor) contribution;
            treeDescriptors.put(desc.getName(), desc);
        } else if (TREE_CONFIG_EP.equals(extensionPoint)) {
            PublicationTreeConfigDescriptor desc = (PublicationTreeConfigDescriptor) contribution;
            registerTreeConfig(desc);
        } else if (FACTORY_EP.equals(extensionPoint)) {
            PublishedDocumentFactoryDescriptor desc = (PublishedDocumentFactoryDescriptor) contribution;
            factoryDescriptors.put(desc.getName(), desc);
        } else if (VALIDATORS_RULE_EP.equals(extensionPoint)) {
            ValidatorsRuleDescriptor desc = (ValidatorsRuleDescriptor) contribution;
            validatorsRuleDescriptors.put(desc.getName(), desc);
        }
    }

    protected void registerTreeConfig(PublicationTreeConfigDescriptor desc) {
        if (desc.getParameters().get("RelativeRootPath") != null) {
            pendingDescriptors.put(desc.getName(), desc);
        } else {
            treeConfigDescriptors.put(desc.getName(), desc);
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor)
            throws Exception {
    }

    protected String computeTreeSessionId(String treeConfigName,
            CoreSession coreSession) {
        return treeConfigName + coreSession.getSessionId();
    }

    public List<String> getAvailablePublicationTree() {
        List<String> treeConfigs = new ArrayList<String>();
        treeConfigs.addAll(treeConfigDescriptors.keySet());
        return treeConfigs;
    }

    public Map<String, String> getAvailablePublicationTrees() {
        Map<String, String> trees = new HashMap<String, String>();
        for (PublicationTreeConfigDescriptor desc : treeConfigDescriptors.values()) {
            String title = desc.getTitle() == null ? desc.getName()
                    : desc.getTitle();
            trees.put(desc.getName(), title);
        }
        return trees;
    }

    public PublicationTree getPublicationTree(String treeName,
            CoreSession coreSession, Map<String, String> params)
            throws ClientException, PublicationTreeNotAvailable {
        return getPublicationTree(treeName, coreSession, params, null);
    }

    public PublicationTree getPublicationTree(String treeName,
            CoreSession coreSession, Map<String, String> params,
            DocumentModel currentDocument) throws ClientException,
            PublicationTreeNotAvailable {
        PublicationTree tree = getOrBuildTree(treeName, coreSession, params);
        if (tree == null) {
            return null;
        }
        if (currentDocument != null) {
            tree.setCurrentDocument(currentDocument);
        }
        return new ProxyTree(tree, tree.getSessionId());
    }

    public Map<String, String> initRemoteSession(String treeConfigName,
            Map<String, String> params) throws Exception {
        RepositoryManager rm = Framework.getService(RepositoryManager.class);
        CoreSession coreSession = null;
        if (rm != null) {
            coreSession = rm.getDefaultRepository().open();
        }

        PublicationTree tree = getPublicationTree(treeConfigName, coreSession,
                params);

        Map<String, String> res = new HashMap<String, String>();
        res.put("sessionId", tree.getSessionId());
        res.put("title", tree.getTitle());
        res.put("nodeType", tree.getNodeType());
        res.put("treeName", tree.getConfigName());
        res.put("path", tree.getPath());

        return res;
    }

    public void release(String sid) {
        PublicationTree tree;

        if (liveTrees.containsKey(sid)) {
            tree = liveTrees.get(sid);
            tree.release();
            liveTrees.remove(sid);
        }
    }

    protected PublicationTree getOrBuildTree(String treeConfigName,
            CoreSession coreSession, Map<String, String> params)
            throws PublicationTreeNotAvailable {
        String key = computeTreeSessionId(treeConfigName, coreSession);
        PublicationTree tree;
        if (liveTrees.containsKey(key)) {
            tree = liveTrees.get(key);
        } else {
            tree = buildTree(key, treeConfigName, coreSession, params);
            if (tree != null)
                liveTrees.put(key, tree);
        }
        return tree;
    }

    protected PublicationTree buildTree(String sid, String treeConfigName,
            CoreSession coreSession, Map<String, String> params)
            throws PublicationTreeNotAvailable {
        try {
            PublicationTreeConfigDescriptor config = getPublicationTreeConfigDescriptor(treeConfigName);
            Map<String, String> allParameters = computeAllParameters(config,
                    params);
            PublicationTreeDescriptor treeDescriptor = getPublicationTreeDescriptor(config);
            PublishedDocumentFactory publishedDocumentFactory = getPublishedDocumentFactory(
                    config, treeDescriptor, coreSession, allParameters);
            return getPublicationTree(treeDescriptor, sid, coreSession,
                    allParameters, publishedDocumentFactory, config.getName(),
                    config.getTitle());
        } catch (PublisherException e) {
            log.error("Unable to build PublicationTree", e);
            return null;
        }
    }

    protected Map<String, String> computeAllParameters(
            PublicationTreeConfigDescriptor config, Map<String, String> params) {
        final Map<String, String> allParameters = config.getParameters();
        if (params != null) {
            allParameters.putAll(params);
        }
        return allParameters;
    }

    protected PublishedDocumentFactory getPublishedDocumentFactory(
            PublicationTreeConfigDescriptor config,
            PublicationTreeDescriptor treeDescriptor, CoreSession coreSession,
            Map<String, String> params) throws PublisherException {
        PublishedDocumentFactoryDescriptor factoryDesc = getPublishedDocumentFactoryDescriptor(
                config, treeDescriptor);
        ValidatorsRule validatorsRule = getValidatorsRule(factoryDesc);

        PublishedDocumentFactory factory;
        try {
            factory = factoryDesc.getKlass().newInstance();
        } catch (Exception e) {
            throw new PublisherException("Error while creating factory "
                    + factoryDesc.getName(), e);
        }

        try {
            factory.init(coreSession, validatorsRule, params);
        } catch (Exception e) {
            throw new PublisherException("Error during Factory init", e);
        }
        return factory;
    }

    protected ValidatorsRule getValidatorsRule(
            PublishedDocumentFactoryDescriptor factoryDesc)
            throws PublisherException {
        String validatorsRuleName = factoryDesc.getValidatorsRuleName();
        ValidatorsRule validatorsRule = null;
        if (validatorsRuleName != null) {
            ValidatorsRuleDescriptor validatorsRuleDesc = validatorsRuleDescriptors.get(validatorsRuleName);
            if (validatorsRuleDesc == null) {
                throw new PublisherException("Unable to find validatorsRule"
                        + validatorsRuleName);
            }
            try {
                validatorsRule = validatorsRuleDesc.getKlass().newInstance();
            } catch (Exception e) {
                throw new PublisherException(
                        "Error while creating validatorsRule "
                                + validatorsRuleName, e);
            }
        }
        return validatorsRule;
    }

    protected PublishedDocumentFactoryDescriptor getPublishedDocumentFactoryDescriptor(
            PublicationTreeConfigDescriptor config,
            PublicationTreeDescriptor treeDescriptor) throws PublisherException {
        String factoryName = config.getFactory();
        if (factoryName == null) {
            factoryName = treeDescriptor.getFactory();
        }

        PublishedDocumentFactoryDescriptor factoryDesc = factoryDescriptors.get(factoryName);
        if (factoryDesc == null) {
            throw new PublisherException("Unable to find factory" + factoryName);
        }
        return factoryDesc;
    }

    protected PublicationTreeConfigDescriptor getPublicationTreeConfigDescriptor(
            String treeConfigName) throws PublisherException {
        if (!treeConfigDescriptors.containsKey(treeConfigName)) {
            throw new PublisherException("Unknow treeConfig :" + treeConfigName);
        }
        return treeConfigDescriptors.get(treeConfigName);
    }

    protected PublicationTreeDescriptor getPublicationTreeDescriptor(
            PublicationTreeConfigDescriptor config) throws PublisherException {
        String treeImplName = config.getTree();
        if (!treeDescriptors.containsKey(treeImplName)) {
            throw new PublisherException("Unknow treeImplementation :"
                    + treeImplName);
        }
        return treeDescriptors.get(treeImplName);
    }

    protected PublicationTree getPublicationTree(
            PublicationTreeDescriptor treeDescriptor, String sid,
            CoreSession coreSession, Map<String, String> parameters,
            PublishedDocumentFactory factory, String configName,
            String treeTitle) throws PublisherException,
            PublicationTreeNotAvailable {
        PublicationTree treeImpl;
        try {
            treeImpl = treeDescriptor.getKlass().newInstance();
        } catch (Exception e) {
            throw new PublisherException(
                    "Error while creating tree implementation", e);
        }

        try {
            treeImpl.initTree(sid, coreSession, parameters, factory,
                    configName, treeTitle);
        } catch (Exception e) {
            throw new PublicationTreeNotAvailable("Error during tree init", e);
        }
        return treeImpl;
    }

    public PublishedDocument publish(DocumentModel doc,
            PublicationNode targetNode) throws ClientException {
        return publish(doc, targetNode, null);
    }

    public PublishedDocument publish(DocumentModel doc,
            PublicationNode targetNode, Map<String, String> params)
            throws ClientException {

        PublicationTree tree = liveTrees.get(targetNode.getSessionId());
        if (tree != null) {
            return tree.publish(doc, targetNode, params);
        } else {
            throw new ClientException(
                    "Calling getChildrenNodes on a closed tree");
        }
    }

    public void unpublish(DocumentModel doc, PublicationNode targetNode)
            throws ClientException {
        PublicationTree tree = liveTrees.get(targetNode.getSessionId());
        if (tree != null) {
            tree.unpublish(doc, targetNode);
        } else {
            throw new ClientException(
                    "Calling getChildrenNodes on a closed tree");
        }
    }

    public void unpublish(String sid, PublishedDocument publishedDocument)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            tree.unpublish(publishedDocument);
        } else {
            throw new ClientException(
                    "Calling getChildrenNodes on a closed tree");
        }
    }

    public List<PublishedDocument> getChildrenDocuments(PublicationNode node)
            throws ClientException {

        PublicationTree tree = liveTrees.get(node.getSessionId());
        if (tree != null) {
            return tree.getPublishedDocumentInNode(tree.getNodeByPath(node.getPath()));
        } else {
            throw new ClientException(
                    "Calling getChildrenDocuments on a closed tree");
        }
    }

    protected List<PublicationNode> makeRemotable(List<PublicationNode> nodes,
            String sid) throws ClientException {
        List<PublicationNode> remoteNodes = new ArrayList<PublicationNode>();

        for (PublicationNode node : nodes) {
            remoteNodes.add(new ProxyNode(node, sid));
        }

        return remoteNodes;
    }

    public List<PublicationNode> getChildrenNodes(PublicationNode node)
            throws ClientException {
        String sid = node.getSessionId();
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return makeRemotable(
                    tree.getNodeByPath(node.getPath()).getChildrenNodes(), sid);
        } else {
            throw new ClientException(
                    "Calling getChildrenNodes on a closed tree");
        }
    }

    public PublicationNode getParent(PublicationNode node) {
        String sid = node.getSessionId();
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            PublicationNode liveNode;
            try {
                liveNode = tree.getNodeByPath(node.getPath()).getParent();
                if (liveNode == null) {
                    return null;
                }
                return new ProxyNode(liveNode, sid);
            } catch (ClientException e) {
                log.error("Error while getting Parent", e);
                return null;
            }
        } else {
            log.error("Calling getParent on a closed tree");
            return null;
        }
    }

    public PublicationNode getNodeByPath(String sid, String path)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return new ProxyNode(tree.getNodeByPath(path), sid);
        } else {
            throw new ClientException("Calling getNodeByPath on a closed tree");
        }
    }

    public List<PublishedDocument> getExistingPublishedDocument(String sid,
            DocumentLocation docLoc) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.getExistingPublishedDocument(docLoc);
        } else {
            throw new ClientException("Calling getNodeByPath on a closed tree");
        }
    }

    public List<PublishedDocument> getPublishedDocumentInNode(
            PublicationNode node) throws ClientException {
        String sid = node.getSessionId();
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.getPublishedDocumentInNode(tree.getNodeByPath(node.getPath()));
        } else {
            throw new ClientException(
                    "Calling getPublishedDocumentInNode on a closed tree");
        }
    }

    public void setCurrentDocument(String sid, DocumentModel currentDocument)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            tree.setCurrentDocument(currentDocument);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public void validatorPublishDocument(String sid,
            PublishedDocument publishedDocument, String comment) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            tree.validatorPublishDocument(publishedDocument, comment);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public void validatorRejectPublication(String sid,
            PublishedDocument publishedDocument, String comment)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            tree.validatorRejectPublication(publishedDocument, comment);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public boolean canPublishTo(String sid, PublicationNode publicationNode)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.canPublishTo(publicationNode);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public boolean canUnpublish(String sid, PublishedDocument publishedDocument)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.canUnpublish(publishedDocument);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public boolean canManagePublishing(String sid,
            PublishedDocument publishedDocument) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.canManagePublishing(publishedDocument);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public boolean isPublishedDocument(DocumentModel documentModel) {
        return PublicationRelationHelper.isPublished(documentModel);
    }

    public PublicationTree getPublicationTreeFor(DocumentModel doc,
            CoreSession coreSession) throws ClientException {
        PublicationTree tree = null;
        try {
            tree = PublicationRelationHelper.getPublicationTreeUsedForPublishing(
                    doc, coreSession);
        } catch (ClientException e) {
            log.debug("Unable to get PublicationTree for "
                    + doc.getPathAsString()
                    + ". Fallback on first PublicationTree accepting this document.");
            for (String treeName : treeConfigDescriptors.keySet()) {
                tree = getPublicationTree(treeName, coreSession, null);
                if (tree.isPublicationNode(doc)) {
                    break;
                }
            }
        }
        return tree;
    }

    public boolean hasValidationTask(String sid,
            PublishedDocument publishedDocument) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.hasValidationTask(publishedDocument);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public PublishedDocument wrapToPublishedDocument(String sid,
            DocumentModel documentModel) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.wrapToPublishedDocument(documentModel);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public boolean isPublicationNode(String sid, DocumentModel documentModel)
            throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.isPublicationNode(documentModel);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public PublicationNode wrapToPublicationNode(String sid,
            DocumentModel documentModel) throws ClientException {
        PublicationTree tree = liveTrees.get(sid);
        if (tree != null) {
            return tree.wrapToPublicationNode(documentModel);
        } else {
            throw new ClientException(
                    "Calling validatorPublishDocument on a closed tree");
        }
    }

    public PublicationNode wrapToPublicationNode(DocumentModel documentModel,
            CoreSession coreSession) throws ClientException,
            PublicationTreeNotAvailable {
        for (String name : getAvailablePublicationTree()) {
            PublicationTree tree = getPublicationTree(name, coreSession, null);
            PublicationTreeConfigDescriptor config = treeConfigDescriptors.get(tree.getConfigName());
            if (!config.islocalSectionTree()) {
                // ignore all non local section tree
                continue;
            }
            if (tree.isPublicationNode(documentModel)) {
                return tree.wrapToPublicationNode(documentModel);
            }
        }
        return null;
    }

    @Override
    public void deactivate(ComponentContext context) throws Exception {
        // this is doing nothing if listener was not registered
        context.getRuntimeContext().getBundle().getBundleContext().removeFrameworkListener(
                this);
    }

    public void frameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == FrameworkEvent.STARTED) {
            ClassLoader jbossCL = Thread.currentThread().getContextClassLoader();
            ClassLoader nuxeoCL = PublisherServiceImpl.class.getClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(nuxeoCL);
                log.info("Publisher Service initialization");
                registerPendingDescriptors();
            } catch (Exception e) {
                log.error("Unable to register pending descriptors", e);
            } finally {
                Thread.currentThread().setContextClassLoader(jbossCL);
                log.debug("JBoss ClassLoader restored");
            }
        }
    }

    protected void registerPendingDescriptors() throws Exception {
        // TODO what to do with multiple repositories?
        RepositoryManager repositoryManager = Framework.getService(RepositoryManager.class);
        String repositoryName = repositoryManager.getDefaultRepository().getName();
        List<DocumentModel> domains = new DomainsFinder(repositoryName).getDomains();
        for (DocumentModel domain : domains) {
            registerTreeConfigFor(domain);
        }
    }

    public void registerTreeConfigFor(DocumentModel domain)
            throws ClientException {
        for (PublicationTreeConfigDescriptor desc : pendingDescriptors.values()) {
            PublicationTreeConfigDescriptor newDesc = new PublicationTreeConfigDescriptor(
                    desc);
            String newTreeName = desc.getName() + "-" + domain.getName();
            newDesc.setName(newTreeName);
            Path newPath = domain.getPath();
            Map<String, String> parameters = newDesc.getParameters();
            newPath = newPath.append(parameters.remove(RELATIVE_ROOT_PATH_KEY));
            parameters.put(ROOT_PATH_KEY, newPath.toString());
            parameters.put(PublisherService.DOMAIN_NAME_KEY, domain.getTitle());
            treeConfigDescriptors.put(newDesc.getName(), newDesc);
        }
    }

    public void unRegisterTreeConfigFor(DocumentModel domain) {
        for (PublicationTreeConfigDescriptor desc : pendingDescriptors.values()) {
            String treeName = desc.getName() + "-" + domain.getName();
            treeConfigDescriptors.remove(treeName);
            for (Iterator<String> it = liveTrees.keySet().iterator(); it.hasNext();) {
                String entry = it.next();
                if (entry.startsWith(treeName)) {
                    it.remove();
                }
            }
        }
    }

    public Map<String, String> getParametersFor(String treeConfigName) {
        PublicationTreeConfigDescriptor desc = treeConfigDescriptors.get(treeConfigName);
        Map<String, String> parameters = new HashMap<String, String>();
        if (desc != null) {
            parameters.putAll(desc.getParameters());
        }
        return parameters;
    }
}

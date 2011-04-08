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
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.nuxeo.ecm.platform.syndication.serializer;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.platform.syndication.translate.TranslationHelper;
import org.nuxeo.ecm.platform.syndication.workflow.DashBoardItem;
import org.nuxeo.ecm.platform.usermanager.NuxeoPrincipalImpl;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

import com.intalio.user.UserPreference;

public class DMJSONSerializer extends AbstractDocumentModelSerializer implements
        DashBoardItemSerializer {

    @Override
    public String serialize(ResultSummary summary, DocumentModelList docList,
            List<String> columnsDefinition, HttpServletRequest req)
            throws ClientException {
        return serialize(summary, docList, columnsDefinition, req, null, null);
    }

    public String serialize(ResultSummary summary, DocumentModelList docList,
            List<String> columnsDefinition, HttpServletRequest req,
            List<String> labels, String lang) throws ClientException {

        if (docList == null) {
            return EMPTY_LIST;
        }

        Map<String, Object> all = new HashMap<String, Object>();

        all.put("summary", summary);

        List<Map<String, String>> struct = new ArrayList<Map<String, String>>();

        // get the timezone from current user
        Principal principal = req.getUserPrincipal();
        UserPreference preference = null;
        TimeZone timezone = null;
        
        if (principal instanceof NuxeoPrincipalImpl) {
        	preference = ((NuxeoPrincipalImpl)principal).getPreference();
        	timezone = TimeZone.getTimeZone(preference.getTimeZone());
        }
        
        for (DocumentModel doc : docList) {
            Map<String, String> resDoc = new HashMap<String, String>();

            resDoc.put("id", doc.getId());

            for (String colDef : columnsDefinition) {
                ResultField res = getDocumentProperty(doc, colDef, timezone);
                resDoc.put(res.getName(), res.getValue());
            }
            struct.add(resDoc);
        }

        all.put("data", struct);

        // add translations if asked for
        if (lang != null && labels != null) {
            Map<String, String> translations = new HashMap<String, String>();
            for (String key : labels) {
                translations.put(key, TranslationHelper.getLabel(key, lang));
            }
            all.put("translations", translations);
        }

        return makeJSON(all);
    }

    protected static String makeJSON(Map<String, Object> all) {
        JSON jsonRes = JSONSerializer.toJSON(all);
        if (jsonRes instanceof JSONObject) {
            JSONObject jsonOb = (JSONObject) jsonRes;
            return jsonOb.toString(2);
        } else if (jsonRes instanceof JSONArray) {
            JSONArray jsonOb = (JSONArray) jsonRes;
            return jsonOb.toString(2);
        } else {
            return null;
        }
    }

    @Override
    public void serialize(ResultSummary summary, DocumentModelList docList,
            String columnsDefinition, Response res, HttpServletRequest req)
            throws ClientException {
        String json = serialize(summary, docList, columnsDefinition, req);
        res.setEntity(json, MediaType.TEXT_PLAIN);
    }

    @Override
    public void serialize(ResultSummary summary, DocumentModelList docList,
            String columnsDefinition, Response res, HttpServletRequest req,
            List<String> labels, String lang) throws ClientException {
        List<String> cols = new ArrayList<String>();
        if (columnsDefinition != null) {
            cols = Arrays.asList(columnsDefinition.split(colDefinitonDelimiter));
        }
        String json = serialize(summary, docList, cols, req, labels, lang);
		StringRepresentation output = new StringRepresentation(json,
				MediaType.TEXT_PLAIN, null, new CharacterSet("UTF-8"));        
        res.setEntity(output);
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    protected String serialize(ResultSummary summary,
            List<DashBoardItem> workItems, String columnsDefinition,
            List<String> labels, String lang) throws ClientException {
        if (workItems == null) {
            workItems = Collections.emptyList();
        }

        Map<String, List<Map<String, String>>> data = new HashMap<String, List<Map<String, String>>>();
        for (DashBoardItem item : workItems) {
            String cat = item.getDirective();
            if (cat == null) {
                cat = "workflowDirectiveValidation"; // XXX NXP-4224
            }
            List<Map<String, String>> category = data.get(cat);
            if (category == null) {
                data.put(cat, category = new ArrayList<Map<String, String>>());
            }

            Map<String, String> m = new HashMap<String, String>();
            m.put("id", item.getId().toString());
            m.put("name", item.getName());
            if (lang != null && item.getName() != null) {
                m.put("nameI18n", TranslationHelper.getLabel(
                        "label.workflow.task." + item.getName(), lang));
            }
            m.put("directive", item.getDirective());
            if (lang != null && item.getDirective() != null) {
                m.put("directiveI18n", TranslationHelper.getLabel(
                        item.getDirective(), lang));
            }
            m.put("description", item.getDescription());
            m.put("title", item.getDocument().getTitle());
            m.put("link", item.getDocumentLink());
            String currentLifeCycle;
            try {
                currentLifeCycle = item.getDocument().getCurrentLifeCycleState();
            } catch (ClientException e) {
                currentLifeCycle = "";
            }
            m.put("currentDocumentLifeCycle", currentLifeCycle);

            if (item.getDueDate() != null) {
                m.put("dueDate", DATE_FORMAT.format(item.getDueDate()));
            }
            if (item.getStartDate() != null) {
                m.put("startDate", DATE_FORMAT.format(item.getStartDate()));
            }
            if (item.getComment() != null) {
                m.put("comment", item.getComment());
            }
            category.add(m);
        }

        Map<String, Object> all = new HashMap<String, Object>();
        all.put("data", data);
        all.put("summary", summary);

        if (lang != null && labels != null) {
            Map<String, String> translations = new HashMap<String, String>();
            for (String key : labels) {
                translations.put(key, TranslationHelper.getLabel(key, lang));
            }
            all.put("translations", translations);
        }

        return makeJSON(all);
    }

    public void serialize(ResultSummary summary, List<DashBoardItem> workItems,
            String columnsDefinition, Response res, HttpServletRequest req)
            throws ClientException {
        serialize(summary, workItems, columnsDefinition, null, null, res, req);
    }

    public void serialize(ResultSummary summary, List<DashBoardItem> workItems,
            String columnsDefinition, List<String> labels, String lang,
            Response res, HttpServletRequest req) throws ClientException {
        String json = serialize(summary, workItems, columnsDefinition, labels,
                lang);
		StringRepresentation output = new StringRepresentation(json,
				MediaType.TEXT_PLAIN, null, new CharacterSet("UTF-8"));        
        res.setEntity(output);
    }

}

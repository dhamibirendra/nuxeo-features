package org.nuxeo.opensocial.container.client.view.rest;

import org.nuxeo.opensocial.container.client.JsLibrary;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;

public class NXRestAPI {

    private static final String HTTP = "http://";

    private final static String QUERY_PATH = "/nuxeo/restAPI/execQueryModel/";

    private final static String NXPICS_PATH = "/nuxeo/nxpicsfile/default/";

    private final static String FORMAT_JSON = "&format=JSON";

    private final static String PAGE = "&page=";

    private final static String MEDIUM_VIEW = "/Medium:content";

    public static void queryDocType(String type, double page,
            RequestCallback callback) {
        StringBuilder sb = new StringBuilder(HTTP);
        sb.append(Window.Location.getHost());
        sb.append(QUERY_PATH);
        sb.append("TYPE_SEARCH?QP1=");
        sb.append(type);
        sb.append(FORMAT_JSON);
        sb.append(PAGE);
        sb.append(page);
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
                sb.toString());
        rb.setCallback(callback);
        try {
            rb.send();
        } catch (RequestException e) {
            JsLibrary.error("Error : NXRestAPI - queryDocType");
        }

    }

    public static void queryCurrentDocChildren(String docId,
            RequestCallback callback) {
        StringBuilder sb = new StringBuilder(HTTP);
        sb.append(Window.Location.getHost());
        sb.append(QUERY_PATH);
        sb.append("CURRENT_DOC_CHILDREN?QP1=");
        sb.append(docId);
        sb.append(FORMAT_JSON);
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET,
                sb.toString());
        rb.setCallback(callback);
        try {
            rb.send();
        } catch (RequestException e) {
            JsLibrary.error("Error : NXRestAPI - queryDocType");
        }

    }

    public static String getImageUrl(String docId) {
        StringBuilder sb = new StringBuilder(HTTP);
        sb.append(Window.Location.getHost());
        sb.append(NXPICS_PATH);
        sb.append(docId);
        sb.append(MEDIUM_VIEW);
        return sb.toString();
    }

}

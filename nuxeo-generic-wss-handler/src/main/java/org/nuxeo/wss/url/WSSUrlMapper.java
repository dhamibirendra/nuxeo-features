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
 *     Thierry Delprat
 */
package org.nuxeo.wss.url;

import java.io.UnsupportedEncodingException;

import org.nuxeo.common.utils.Path;
import org.nuxeo.wss.WSSConfig;
import org.nuxeo.wss.servlet.WSSRequest;

public class WSSUrlMapper {

    public static String getLocationFromFullUrl(WSSRequest request, String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("[")) {
            url = url.substring(1, url.length() - 2);
        }
        String location = url.replace(request.getBaseUrl(), "");

        return location;
    }

    public static WSSUrlMapping getWebMapping(WSSRequest request, String location) {

        String siteUrl = "/";
        String resourceUrl = location;

        if (location.startsWith("http")) {
            siteUrl = request.getBaseUrl();
            resourceUrl = location.replace(siteUrl, "");
        } else {

            String ctxPath = getFirstSegment(WSSConfig.instance().getContextPath());
            String resourcePath = getFirstSegment(resourceUrl);

            if (ctxPath != null && ctxPath.equals(resourcePath)) {
                resourceUrl = resourceUrl.replace(resourcePath, "");
                siteUrl = resourcePath;
            }
            if (!siteUrl.startsWith("/")) {
                siteUrl = "/" + siteUrl;
            }
            while (resourceUrl.startsWith("/")) {
                resourceUrl = resourceUrl.substring(1);
            }
        }
        return new WSSUrlMapping(siteUrl, resourceUrl);
    }

    public static String getUrlWithSitePath(WSSRequest request, String location) {

        String sitePath = request.getSitePath();
        
        String newLocation = location;
        if (location != null) {
        	boolean leadingSlash = location.startsWith("/");
        	
	        // decode the location
	        Path decodePath = new Path(location);
	        String[] segs = new String[decodePath.segmentCount()];
	        
	        for (int i = 0; i < decodePath.segmentCount(); i++) {
	        	try {
					segs[i] = java.net.URLDecoder.decode(decodePath.segment(i), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					segs[i] = decodePath.segment(i);
				}
	        }
	        
	        newLocation = Path.createFromSegments(segs).toString();
	        if ((leadingSlash) && (!newLocation.startsWith("/"))) {
	        	newLocation = "/" + newLocation;
	        } else if((!leadingSlash) && (newLocation.startsWith("/"))) {
	        	newLocation = newLocation.substring(1);
	        }
        }
        
        //location = getCleanUrl(location);
        if (sitePath != null && !"".equals(sitePath)) {
            String fullPath = sitePath;
            if (newLocation.startsWith("/")) {
                fullPath = fullPath + newLocation;
            } else {
                fullPath = fullPath + "/" + newLocation;
            }
            return fullPath;
        } else {
            return newLocation;
        }
    }

    private static String getCleanUrl(String location) {

        if ("".equals(location) || location == null) {
            return "/";
        }

        return location;
        /*if (location.startsWith(WSSConfig.instance().getContextPath())) {
            return location.replace(WSSConfig.instance().getContextPath(), "");
        } else {
            return location;
        }*/
    }

    public static String getFirstSegment(String path) {
        if (path == null) {
            return null;
        }
        if (path.trim().equals("")) {
            return "";
        }
        String[] parts = path.split("/");

        if (!"".equals(parts[0].trim())) {
            return parts[0];
        }
        return parts[1];
    }

}

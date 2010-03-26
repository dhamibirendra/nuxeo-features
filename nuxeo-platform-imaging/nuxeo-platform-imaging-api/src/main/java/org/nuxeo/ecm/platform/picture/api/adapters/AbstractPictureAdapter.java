/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 * $Id$
 */

package org.nuxeo.ecm.platform.picture.api.adapters;

import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.CONVERSION_FORMAT;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.JPEG_CONVERSATION_FORMAT;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPERATION_CROP;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPERATION_RESIZE;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPTION_CROP_X;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPTION_CROP_Y;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPTION_RESIZE_DEPTH;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPTION_RESIZE_HEIGHT;
import static org.nuxeo.ecm.platform.picture.api.ImagingConvertConstants.OPTION_RESIZE_WIDTH;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_BY_LINE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_BY_LINE_TITLE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_CAPTION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_CATEGORY;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_CITY;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_COLORSPACE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_COMMENT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_COPYRIGHT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_COPYRIGHT_NOTICE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_COUNTRY_OR_PRIMARY_LOCATION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_CREDIT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_DATE_CREATED;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_DESCRIPTION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_EQUIPMENT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_EXPOSURE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_FNUMBER;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_FOCALLENGTH;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_HEADLINE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_HEIGHT;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_HRESOLUTION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ICCPROFILE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ISOSPEED;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_KEYWORDS;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_LANGUAGE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_OBJECT_NAME;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ORIENTATION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ORIGINALDATE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ORIGINAL_TRANSMISSION_REFERENCE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_ORIGINATING_PROGRAM;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_PIXEL_XDIMENSION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_PIXEL_YDIMENSION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_PROVINCE_OR_STATE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_RECORD_VERSION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_RELEASE_DATE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_RELEASE_TIME;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_SOURCE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_SPECIAL_INSTRUCTIONS;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_SUPPLEMENTAL_CATEGORIES;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_TIME_CREATED;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_URGENCY;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_VRESOLUTION;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_WHITEBALANCE;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_WIDTH;
import static org.nuxeo.ecm.platform.picture.api.MetadataConstants.META_WRITER;

import java.awt.Point;
import java.awt.color.ICC_Profile;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.picture.api.ImageInfo;
import org.nuxeo.ecm.platform.picture.api.ImagingService;
import org.nuxeo.runtime.api.Framework;

public abstract class AbstractPictureAdapter implements PictureResourceAdapter {

    private static final Log log = LogFactory.getLog(PictureResourceAdapter.class);

    public static final String VIEWS_PROPERTY = "picture:views";

    public static final String CONTENT_XPATH = "picture:views/view[%d]/content";

    public static final String FIELD_HEADLINE = "headline";

    public static final String FIELD_SUBHEADLINE = "subheadline";

    public static final String FIELD_BYLINE = "byline";

    public static final String FIELD_DATELINE = "dateline";

    public static final String FIELD_SLUGLINE = "slugline";

    public static final String FIELD_CREDIT = "credit";

    public static final String FIELD_LANGUAGE = "language";

    public static final String FIELD_SOURCE = "source";

    public static final String FIELD_ORIGIN = "origin";

    public static final String FIELD_GENRE = "genre";

    public static final String FIELD_CAPTION = "caption";

    public static final String FIELD_TYPAGE = "typage";

    public static final String SCHEMA_NAME = "picture";

    public static final int MEDIUM_SIZE = 550;

    public static final int THUMB_SIZE = 100;

    protected DocumentModel doc;

    protected Integer width;

    protected Integer height;

    protected Integer depth;

    protected String description;

    protected String type;

    protected File file;

    protected Blob fileContent;

    private CoreSession session;

    private ImagingService imagingService;

    private ConversionService converionService;

    public void setDocumentModel(DocumentModel doc) {
        this.doc = doc;
    }

    protected ImagingService getImagingService() {
        if (imagingService == null) {
            try {
                imagingService = Framework.getService(ImagingService.class);
            } catch (Exception e) {
                log.error("Unable to get Imaging Service.", e);
            }

        }
        return imagingService;
    }

    protected ConversionService getConversionService() throws ClientException {
        if (converionService == null) {
            try {
                converionService = Framework.getService(ConversionService.class);
            } catch (Exception e) {
                log.error("Unable to get converion Service.", e);
                throw new ClientException(e);
            }
        }
        return converionService;
    }

    protected CoreSession getSession() {
        if (session == null) {
            if (doc == null) {
                return null;
            }
            String sid = doc.getSessionId();
            session = CoreInstance.getInstance().getSession(sid);
        }

        return session;
    }

    protected void setMetadata() throws IOException, ClientException {
        boolean imageInfoUsed = false;
        ImageInfo imageInfo = getImagingService().getImageInfo(fileContent);
        if (imageInfo != null) {
            width = imageInfo.getWidth();
            height = imageInfo.getHeight();
            depth = imageInfo.getDepth();
            imageInfoUsed = true;
        }
        Map<String, Object> metadata = getImagingService().getImageMetadata(
                fileContent);
        description = (String) metadata.get(META_DESCRIPTION);
        if (!imageInfoUsed) {
            width = (Integer) metadata.get(META_WIDTH);
            height = (Integer) metadata.get(META_HEIGHT);
        }
        doc.setPropertyValue("picture:" + FIELD_BYLINE,
                (String) metadata.get(META_BY_LINE));
        doc.setPropertyValue("picture:" + FIELD_CAPTION,
                (String) metadata.get(META_CAPTION));
        doc.setPropertyValue("picture:" + FIELD_CREDIT,
                (String) metadata.get(META_CREDIT));
        if (metadata.containsKey(META_DATE_CREATED)) {
            doc.setPropertyValue("picture:" + FIELD_DATELINE, metadata.get(
                    META_DATE_CREATED).toString());
        }
        doc.setPropertyValue("picture:" + FIELD_HEADLINE,
                (String) metadata.get(META_HEADLINE));
        doc.setPropertyValue("picture:" + FIELD_LANGUAGE,
                (String) metadata.get(META_LANGUAGE));
        doc.setPropertyValue("picture:" + FIELD_ORIGIN,
                (String) metadata.get(META_OBJECT_NAME));
        doc.setPropertyValue("picture:" + FIELD_SOURCE,
                (String) metadata.get(META_SOURCE));

        // Set EXIF info
        doc.setPropertyValue("imd:image_description",
                (String) metadata.get(META_DESCRIPTION));
        doc.setPropertyValue("imd:user_comment",
                (String) metadata.get(META_COMMENT));
        doc.setPropertyValue("imd:equipment",
                (String) metadata.get(META_EQUIPMENT));
        Date dateTimeOriginal = (Date) metadata.get(META_ORIGINALDATE);
        if (dateTimeOriginal != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateTimeOriginal);
            doc.setPropertyValue("imd:date_time_original", calendar);
        }
        doc.setPropertyValue("imd:xresolution",
                (Integer) metadata.get(META_HRESOLUTION));
        doc.setPropertyValue("imd:yresolution",
                (Integer) metadata.get(META_VRESOLUTION));
        doc.setPropertyValue("imd:pixel_xdimension",
                (Integer) metadata.get(META_PIXEL_XDIMENSION));
        doc.setPropertyValue("imd:pixel_ydimension",
                (Integer) metadata.get(META_PIXEL_YDIMENSION));
        doc.setPropertyValue("imd:copyright",
                (String) metadata.get(META_COPYRIGHT));
        doc.setPropertyValue("imd:exposure_time",
                (String) metadata.get(META_EXPOSURE));
        doc.setPropertyValue("imd:iso_speed_ratings",
                (String) metadata.get(META_ISOSPEED));
        doc.setPropertyValue("imd:focal_length",
                (Double) metadata.get(META_FOCALLENGTH));
        doc.setPropertyValue("imd:color_space",
                (String) metadata.get(META_COLORSPACE));
        doc.setPropertyValue("imd:white_balance",
                (String) metadata.get(META_WHITEBALANCE));
        ICC_Profile iccProfile = (ICC_Profile) metadata.get(META_ICCPROFILE);
        if (iccProfile != null) {
            doc.setPropertyValue("imd:icc_profile", iccProfile.toString());
        }
        doc.setPropertyValue("imd:orientation",
                (String) metadata.get(META_ORIENTATION));
        doc.setPropertyValue("imd:fnumber", (Double) metadata.get(META_FNUMBER));

        // Set IPTC info
        doc.setPropertyValue("iptc:by_line",
                (String) metadata.get(META_BY_LINE));
        doc.setPropertyValue("iptc:by_line_title",
                (String) metadata.get(META_BY_LINE_TITLE));
        doc.setPropertyValue("iptc:caption",
                (String) metadata.get(META_CAPTION));
        doc.setPropertyValue("iptc:category",
                (String) metadata.get(META_CATEGORY));
        doc.setPropertyValue("iptc:city", (String) metadata.get(META_CITY));
        doc.setPropertyValue("iptc:copyright_notice",
                (String) metadata.get(META_COPYRIGHT_NOTICE));
        doc.setPropertyValue("iptc:country_or_primary_location",
                (String) metadata.get(META_COUNTRY_OR_PRIMARY_LOCATION));
        doc.setPropertyValue("iptc:credit", (String) metadata.get(META_CREDIT));
        Date dateCreated = (Date) metadata.get(META_DATE_CREATED);
        if (dateCreated != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(dateCreated);
            doc.setPropertyValue("iptc:date_created", calendar);
        }
        doc.setPropertyValue("iptc:headline",
                (String) metadata.get(META_HEADLINE));
        doc.setPropertyValue("iptc:keywords",
                (String) metadata.get(META_KEYWORDS));
        doc.setPropertyValue("iptc:language",
                (String) metadata.get(META_LANGUAGE));
        doc.setPropertyValue("iptc:object_name",
                (String) metadata.get(META_OBJECT_NAME));
        doc.setPropertyValue("iptc:original_transmission_ref",
                (String) metadata.get(META_ORIGINAL_TRANSMISSION_REFERENCE));
        doc.setPropertyValue("iptc:originating_program",
                (String) metadata.get(META_ORIGINATING_PROGRAM));
        doc.setPropertyValue("iptc:province_or_state",
                (String) metadata.get(META_PROVINCE_OR_STATE));
        doc.setPropertyValue("iptc:record_version",
                (String) metadata.get(META_RECORD_VERSION));
        Date releaseDate = (Date) metadata.get(META_RELEASE_DATE);
        if (releaseDate != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(releaseDate);
            doc.setPropertyValue("iptc:release_date", calendar);
        }
        doc.setPropertyValue("iptc:release_time",
                (String) metadata.get(META_RELEASE_TIME));
        doc.setPropertyValue("iptc:source", (String) metadata.get(META_SOURCE));
        doc.setPropertyValue("iptc:special_instructions",
                (String) metadata.get(META_SPECIAL_INSTRUCTIONS));
        doc.setPropertyValue("iptc:supplemental_categories",
                (String) metadata.get(META_SUPPLEMENTAL_CATEGORIES));
        doc.setPropertyValue("iptc:time_created",
                (String) metadata.get(META_TIME_CREATED));
        doc.setPropertyValue("iptc:urgency",
                (String) metadata.get(META_URGENCY));
        doc.setPropertyValue("iptc:writer", (String) metadata.get(META_WRITER));
    }

    protected void clearViews() throws ClientException {
        List<Map<String, Object>> viewsList = new ArrayList<Map<String, Object>>();
        doc.getProperty(VIEWS_PROPERTY).setValue(viewsList);
    }

    protected void addViews(List<Map<String, Object>> pictureTemplates,
            String filename, String title) throws IOException, ClientException {
        doc.setProperty("dublincore", "title", title);
        if (pictureTemplates != null) {
            // Use PictureBook Properties
            for (Map<String, Object> view : pictureTemplates) {
                Integer maxsize;
                if (view.get("maxsize") == null) {
                    maxsize = MEDIUM_SIZE;
                } else {
                    maxsize = ((Long) view.get("maxsize")).intValue();
                }
                createPictureimpl((String) view.get("description"),
                        (String) view.get("tag"), (String) view.get("title"),
                        maxsize, filename, width, height, depth, fileContent);
            }
        } else {
            // Default properties When PictureBook doesn't exist
            createPictureimpl("Medium Size", "medium", "Medium", MEDIUM_SIZE,
                    filename, width, height, depth, fileContent);
            createPictureimpl(description, "original", "Original", null,
                    filename, width, height, depth, fileContent);
            createPictureimpl("Thumbnail Size", "thumb", "Thumbnail",
                    THUMB_SIZE, filename, width, height, depth, fileContent);
            createPictureimpl("Original Picture in JPEG format", "originalJpeg", "OriginalJpeg", null,
                    filename, width, height, depth, fileContent);

        }
    }

    @SuppressWarnings( { "unchecked" })
    public void createPictureimpl(String description, String tag, String title,
            Integer maxsize, String filename, Integer width, Integer height, Integer depth,
            Blob fileContent) throws IOException, ClientException {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("title", title);
        map.put("description", description);
        map.put("filename", filename);
        map.put("tag", tag);
        if ("Original".equals(title)) {
            map.put("width", width);
            map.put("height", height);
            FileBlob fileBlob = new FileBlob(file, type);
            fileBlob.setFilename(title + "_" + filename);
            map.put("content", fileBlob);
        } else if ("OriginalJpeg".equals(title)) {
            map.put("width", width);
            map.put("height", height);
            Map<String, Serializable> options = new HashMap<String, Serializable>();
            options.put(OPTION_RESIZE_WIDTH, width);
            options.put(OPTION_RESIZE_HEIGHT, height);
            options.put(OPTION_RESIZE_DEPTH, depth);
            // always convert to jpeg
            options.put(CONVERSION_FORMAT, JPEG_CONVERSATION_FORMAT);
            BlobHolder bh = new SimpleBlobHolder(fileContent);
            bh = getConversionService().convert(OPERATION_RESIZE, bh, options);
            Blob blob = bh.getBlob() != null ? bh.getBlob() : new FileBlob(
                    file, type);
            String viewFilename = computeViewFilename(filename, JPEG_CONVERSATION_FORMAT);
            blob.setFilename(title + "_" + viewFilename);
            map.put("content", blob);
        } else {
            Point size = new Point(width, height);
            size = getSize(size, maxsize);
            map.put("width", size.x);
            map.put("height", size.y);
            Map<String, Serializable> options = new HashMap<String, Serializable>();
            options.put(OPTION_RESIZE_WIDTH, size.x);
            options.put(OPTION_RESIZE_HEIGHT, size.y);
            options.put(OPTION_RESIZE_DEPTH, depth);
            // use the registered conversion format for 'Medium' and 'Thumbnail' views
            options.put(CONVERSION_FORMAT, imagingService.getConfigurationValue(CONVERSION_FORMAT,
                JPEG_CONVERSATION_FORMAT));
            BlobHolder bh = new SimpleBlobHolder(fileContent);
            bh = getConversionService().convert(OPERATION_RESIZE, bh, options);
            Blob blob = bh.getBlob() != null ? bh.getBlob() : new FileBlob(
                    file, type);
            String viewFilename = computeViewFilename(filename, JPEG_CONVERSATION_FORMAT);
            blob.setFilename(title + "_" + viewFilename);
            map.put("content", blob);
        }
        Serializable views = doc.getPropertyValue(VIEWS_PROPERTY);
        List<Map<String, Object>> viewsList = (List<Map<String, Object>>) views;
        viewsList.add(map);
        doc.getProperty(VIEWS_PROPERTY).setValue(viewsList);
    }

    protected static Point getSize(Point current, int max) {
        int x = current.x;
        int y = current.y;
        int newx;
        int newy;
        if (x > y) { // landscape
            newy = (y * max) / x;
            newx = max;
        } else { // portrait
            newx = (x * max) / y;
            newy = max;
        }
        if (newx > x || newy > y) {
            return current;
        }
        return new Point(newx, newy);
    }

    protected String computeViewFilename(String filename, String format) {
        int index = filename.lastIndexOf(".");
        if (index == -1) {
            return filename + "." + format;
        } else {
            return filename.substring(0, index + 1) + format;
        }
    }

    protected Blob getContentFromViews(Integer i) throws ClientException {
        return (Blob) doc.getPropertyValue(String.format(CONTENT_XPATH, i));
    }

    protected FileBlob crop(Blob blob, Map<String, Serializable> coords)
            throws ClientException {
        try {
            BlobHolder bh = new SimpleBlobHolder(blob);
            String type = blob.getMimeType();

            Map<String, Serializable> options = new HashMap<String, Serializable>();
            options.put(OPTION_CROP_X, coords.get("x"));
            options.put(OPTION_CROP_Y, coords.get("y"));
            options.put(OPTION_RESIZE_HEIGHT, coords.get("h"));
            options.put(OPTION_RESIZE_WIDTH, coords.get("w"));

            if (type != "image/png") {
                bh = getConversionService().convert(OPERATION_CROP, bh, options);
                return new FileBlob(bh.getBlob().getStream(), type);
            }
        } catch (Exception e) {
            throw new ClientException("Crop failed", e);
        }
        return null;
    }

}

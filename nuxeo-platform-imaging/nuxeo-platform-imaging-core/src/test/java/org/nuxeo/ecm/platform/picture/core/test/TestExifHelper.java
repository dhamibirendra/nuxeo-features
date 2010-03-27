/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.ecm.platform.picture.core.test;

import java.util.Arrays;
import java.util.Map;

import junit.framework.TestCase;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.platform.picture.ExifHelper;
import org.nuxeo.ecm.platform.picture.api.MetadataConstants;
import org.nuxeo.ecm.platform.picture.core.mistral.MistralMetadataUtils;

/**
 * 
 * @author btatar
 * 
 */
public class TestExifHelper extends TestCase {

	MistralMetadataUtils service;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		service = new MistralMetadataUtils();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		service = null;
	}

	public void testExtractBytes() {
		// ASCII string as an byte array
		byte[] bytes = new byte[] { 65, 83, 67, 73, 73, 0, 0, 0 };
		byte[] extractedBytes = ExifHelper.extractBytes(bytes, 0, 3);
		String s = new String(new byte[] { 65, 83, 67, 73, 73 });
		assertEquals("ASCII", s);
		assertTrue(Arrays.equals(new byte[] { 65, 83, 67 }, extractedBytes));
	}

	public void testDecodeUndefined() {
		byte[] rawBytes = new byte[] { 65, 83, 67, 73, 73, 0, 0, 0, 66, 65, 66,
				65 };
		String decodedString = ExifHelper.decodeUndefined(rawBytes);
		String rawString = new String(rawBytes);
		assertNotSame(decodedString, rawString);
	}

	public void testUserCommentExifDataType() {

		// picture from:
		// http://www.flickr.com/photos/paulobrandao/2788050844/sizes/o/
		// by paulo brandao
		// distributed under
		// http://creativecommons.org/licenses/by-sa/2.0/deed.en
		Blob blob = StreamingBlob.createFromURL((this.getClass()
				.getClassLoader().getResource("images/statue.jpg")));
		Map<String, Object> metadatas = service.getImageMetadata(blob);
		String userComment = ((String) metadatas
				.get(MetadataConstants.META_COMMENT)).trim();
		assertNotSame("ASCII", userComment);
		assertEquals("(C) PAULO BRANDA", userComment);
	}
}

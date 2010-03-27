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
 * $Id: TestTransformDocumentWithKnownExtensionBBB.java 24475 2007-09-03 06:03:57Z janguenot $
 */
package org.nuxeo.ecm.platform.transform;

import java.io.File;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.transform.document.TransformDocumentImpl;

public class TestTransformDocumentWithKnownExtensionBBB extends AbstractTransformDocumentTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        File f = FileUtils.getResourceFileFromContext("test-data/hello.doc");
        FileBlob blob = new FileBlob(f);
        document = new TransformDocumentImpl(blob, "application/msword");
    }

}

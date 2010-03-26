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

package org.nuxeo.wss.fprpc.tests.resources;

import java.io.InputStream;

import javax.servlet.Filter;

import junit.framework.TestCase;

import org.nuxeo.wss.fprpc.tests.fake.FakeRequest;
import org.nuxeo.wss.fprpc.tests.fake.FakeRequestBuilder;
import org.nuxeo.wss.fprpc.tests.fake.FakeResponse;
import org.nuxeo.wss.handlers.resources.ResourcesHandler;
import org.nuxeo.wss.servlet.WSSFilter;

public class TestResources extends TestCase {


    public void testResourceStream() {

        InputStream is = ResourcesHandler.getResourceAsStream("icons/folder.gif");
        assertNotNull(is);

        is = ResourcesHandler.getResourceAsStream("icons/toto.gif");
        assertNull(is);

        is = ResourcesHandler.getResourceAsStream("ResourceHandler.class");
        assertNull(is);

        is = ResourcesHandler.getResourceAsStream("list-documents.ftl");
        assertNull(is);

    }

    public void testGetResource() throws Exception {
        Filter filter=new WSSFilter();
        filter.init(null);

        FakeRequest request = FakeRequestBuilder.buildFromResource("GetResources.dump");
        FakeResponse response = new FakeResponse();

        filter.doFilter(request, response, null);

        String result= response.getOutput();

        assertTrue(result.startsWith("GIF89"));

    }

}

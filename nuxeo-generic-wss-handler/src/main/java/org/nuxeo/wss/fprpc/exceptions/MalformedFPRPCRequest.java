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
package org.nuxeo.wss.fprpc.exceptions;

import org.nuxeo.wss.WSSException;

public class MalformedFPRPCRequest extends WSSException {

    private static final long serialVersionUID = 1L;

    public MalformedFPRPCRequest(String message) {
        super(message);
    }

    public MalformedFPRPCRequest(String message, Throwable t) {
        super(message, t);
    }

}

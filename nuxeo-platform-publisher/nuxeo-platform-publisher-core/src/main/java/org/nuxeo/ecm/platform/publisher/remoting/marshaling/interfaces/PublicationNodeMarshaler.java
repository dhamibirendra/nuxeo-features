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

package org.nuxeo.ecm.platform.publisher.remoting.marshaling.interfaces;

import org.nuxeo.ecm.platform.publisher.api.PublicationNode;

/**
 * Interface for {@link PublicationNode} marshaler.
 *
 * @author tiry
 */
public interface PublicationNodeMarshaler {

    String marshalPublicationNode(PublicationNode node)
            throws PublishingMarshalingException;;

    PublicationNode unMarshalPublicationNode(String data)
            throws PublishingMarshalingException;;
}

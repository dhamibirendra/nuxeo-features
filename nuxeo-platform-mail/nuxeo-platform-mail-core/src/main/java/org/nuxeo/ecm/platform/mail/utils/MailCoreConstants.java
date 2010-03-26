/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.nuxeo.ecm.platform.mail.utils;

/**
 * Constants specific to Mail Core.
 *
 * @author <a href="mailto:cbaican@nuxeo.com">Catalin Baican</a>
 */
public final class MailCoreConstants {

    public static final String MAIL_MESSAGE_TYPE = "MailMessage";

    public static final String MAIL_FOLDER_TYPE = "MailFolder";

    public static final String SENDER_PROPERTY_NAME = "mail:sender";

    public static final String SENDING_DATE_PROPERTY_NAME = "mail:sending_date";

    public static final String RECIPIENTS_PROPERTY_NAME = "mail:recipients";

    public static final String CC_RECIPIENTS_PROPERTY_NAME = "mail:cc_recipients";

    public static final String TEXT_PROPERTY_NAME = "mail:text";

    public static final String MESSAGE_ID_PROPERTY_NAME = "mail:messageId";

    public static final String EMAIL_PROPERTY_NAME = "prot:email";

    public static final String PASSWORD_PROPERTY_NAME = "prot:password";

    public static final String PROTOCOL_TYPE_PROPERTY_NAME = "prot:protocol_type";

    public static final String HOST_PROPERTY_NAME = "prot:host";

    public static final String PORT_PROPERTY_NAME = "prot:port";

    public static final String SOCKET_FACTORY_FALLBACK_PROPERTY_NAME = "prot:socket_factory_fallback";

    public static final String SOCKET_FACTORY_PORT_PROPERTY_NAME = "prot:socket_factory_port";

    public static final String STARTTLS_ENABLE_PROPERTY_NAME = "prot:starttls_enable";

    public static final String SSL_PROTOCOLS_PROPERTY_NAME = "prot:ssl_protocols";

    public static final String EMAILS_LIMIT_PROPERTY_NAME = "prot:emails_limit";

    public static final String MAIL_RECEIVED_EVENT = "MailReceivedEvent";

    public static final String CORE_SESSION_ID_KEY = "sessionId";

    public static final String PARENT_PATH_KEY = "parentPath";

    public static final String MIMETYPE_SERVICE_KEY = "mimetypeService";

    public static final String SUBJECT_KEY = "subject";

    public static final String SENDER_KEY = "sender";

    public static final String SENDER_EMAIL_KEY = "senderEmail";

    public static final String SENDING_DATE_KEY = "sendingDate";

    public static final String RECIPIENTS_KEY = "recipients";

    public static final String CC_RECIPIENTS_KEY = "ccRecipients";

    public static final String ATTACHMENTS_KEY = "attachments";

    public static final String TEXT_KEY = "text";

    public static final String MESSAGE_ID_KEY = "messageId";

    public static final String IMAP = "imap";

    public static final String POP3 = "pop3";

    public static final String IMAPS = "imaps";

    public static final String POP3S = "pop3s";

    public static final String PROTOCOL_TYPE_KEY = "protocolType";

    public static final String LEAVE_ON_SERVER_KEY = "leaveOnServer";

    private MailCoreConstants() {
    }

}

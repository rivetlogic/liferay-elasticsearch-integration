/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.rivetlogic.portal.messaging.sender;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.messaging.sender.MessageSender;

/**
 * The Class PluginMessageSender.
 */
public class PluginMessageSender implements MessageSender {

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.messaging.sender.MessageSender#send(java.lang.String, com.liferay.portal.kernel.messaging.Message)
     */
    public void send(String destinationName, Message message) {
        MessageBusUtil.sendMessage(destinationName, message);
    }

}

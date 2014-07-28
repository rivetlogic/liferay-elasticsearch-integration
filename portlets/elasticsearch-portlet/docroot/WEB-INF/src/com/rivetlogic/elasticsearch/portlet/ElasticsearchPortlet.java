/**
 * Copyright (C) 2005-2014 Rivet Logic Corporation.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.rivetlogic.elasticsearch.portlet;

import java.io.IOException;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;
import com.rivetlogic.elasticsearch.portlet.exception.ElasticsearchAutocompleteException;
import com.rivetlogic.elasticsearch.portlet.util.ElasticsearchPortletHelper;

/**
 * The Class ElasticsearchPortlet.
 */
public class ElasticsearchPortlet extends MVCPortlet {

    /*
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#init(javax.portlet.PortletConfig)
     */
    @Override
    public void init(PortletConfig portletConfig) {
        try {
            super.init(portletConfig);

            portletHelper = new ElasticsearchPortletHelper();
            portletHelper.createClient();
        } catch (PortletException e) {
            _log.error("Error:" + e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.portlet.GenericPortlet#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        portletHelper.destroyClient();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.liferay.util.bridges.mvc.MVCPortlet#serveResource(javax.portlet.
     * ResourceRequest, javax.portlet.ResourceResponse)
     */
    @Override
    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) {

        try {
            portletHelper.fetchAutoSuggestions(resourceRequest, resourceResponse);
        } catch (IOException ioex) {
            _log.error(ioex.getMessage());
        } catch (ElasticsearchAutocompleteException esex) {
            _log.error(esex.getMessage());
        }
    }

    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchPortlet.class);

    /** The portlet helper. */
    private ElasticsearchPortletHelper portletHelper;
}

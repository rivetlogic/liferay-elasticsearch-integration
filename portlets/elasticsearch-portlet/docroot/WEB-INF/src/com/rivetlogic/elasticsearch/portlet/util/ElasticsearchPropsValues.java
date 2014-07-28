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

package com.rivetlogic.elasticsearch.portlet.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.util.portlet.PortletProps;

/**
 * The Class ElasticsearchPropsValues.
 * 
 */
public class ElasticsearchPropsValues {

    /**
     * Gets the property.
     * 
     * @param key
     *            the key
     * @return the property
     */
    public String getPortletProperty(String key) {
        return PortletProps.get(key);
    }

    /**
     * Gets the property.
     * 
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the property
     */
    public int getPortletProperty(String key, int defaultValue) {
        return GetterUtil.getInteger(getPortletProperty(key), defaultValue);
    }

    /**
     * Gets the suggestions size.
     * 
     * @return the suggestions size
     */
    public int getSuggestionsSize() {
        return suggestionsSize;
    }

    /**
     * Gets the suggestions length.
     * 
     * @return the suggestions length
     */
    public int getSuggestionsLength() {
        return suggestionsLength;
    }

    /**
     * Gets the es server home.
     * 
     * @return the es server home
     */
    public String getEsServerHome() {
        return esServerHome;
    }

    /**
     * Gets the es cluster name.
     *
     * @return the es cluster name
     */
    public String getEsClusterName() {
        return esClusterName;
    }
    
    /**
     * Gets the suggestion query fields.
     * 
     * @return the suggestion query fields
     */
    public Map<String, Float> getSuggestionQueryFields() {
        return suggestionQueryFields;
    }

    /**
     * Gets the suggestion excluded types.
     * 
     * @return the suggestion excluded types
     */
    public Set<String> getSuggestionExcludedTypes() {
        return suggestionExcludedTypes;
    }

    /**
     * Gets the suggestions query max hits.
     *
     * @return the suggestions query max hits
     */
    public int getSuggestionsQueryMaxHits() {
        return suggestionsQueryMaxHits;
    }


    /**
     * This method loads the needed properties from Elastic.props files and
     * makes necessary conversion using utility methods.
     */
    public void loadESProperties() {

        /** Load Elastic server home and clustername from Portal level props*/
        this.esServerHome = PropsUtil.get(ElasticsearchPortletConstants.ES_KEY_HOME_PATH);
        this.esClusterName = PropsUtil.get(ElasticsearchPortletConstants.ES_KEY_CLUSTERNAME);

        /** Load other suggestion related props from Portlet properties*/
        this.suggestionsSize = getPortletProperty(ElasticsearchPortletConstants.SUGGESTIONS_SIZE_KEY,
                ElasticsearchPortletConstants.SUGGESTIONS_SIZE_DEFAULT_VALUE);

        this.suggestionsLength = getPortletProperty(ElasticsearchPortletConstants.SUGGESTIONS_LENGTH_KEY,
                ElasticsearchPortletConstants.SUGGESTIONS_LENGTH_DEFAULT_VALUE);

        this.suggestionsQueryMaxHits = getPortletProperty(ElasticsearchPortletConstants.SUGGESTION_QUERY_MAX_HITS_KEY,
                ElasticsearchPortletConstants.SUGGESTIONS_QUERY_MAX_HITS_DEFAULT_VALUE);


        setSuggestionQueryFields();
        setSuggestionExcludedTypes();
    }

    /**
     * Gets the transport hosts.
     * 
     * @return the transport hosts
     */
    public InetSocketTransportAddress[] getTransportHosts() {
        
        String csNodeList = PropsUtil.get(ElasticsearchPortletConstants.ES_KEY_NODE);
        InetSocketTransportAddress[] transportAddresses = null;
        if (Validator.isNotNull(csNodeList)) {
            String[] nodeList = csNodeList.split(StringPool.COMMA);
            transportAddresses = new InetSocketTransportAddress[nodeList.length];
            
            /** Prepare a list of Hosts */
            for (int i = 0; i < nodeList.length; i++) {
                String[] hostnames = nodeList[i].split(StringPool.COLON);
                InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(hostnames[0],
                        Integer.parseInt(hostnames[1]));
                transportAddresses[i] = transportAddress;
            }
        } else {
            _log.error("Elastic search nodes are missing from properties...");
        }
        
        return transportAddresses;
    }

    /**
     * Sets the suggestion excluded types.
     */
    private void setSuggestionExcludedTypes() {
        suggestionExcludedTypes = new HashSet<String>();
        String csExcludedTypes = getPortletProperty(ElasticsearchPortletConstants.SUGGESTION_EXCLUDED_TYPES_KEY);
        if (Validator.isNotNull(csExcludedTypes)) {
            String[] excludedTypes = csExcludedTypes.split(StringPool.COMMA);
            /** Iterate over the fields and add to Querybuilder */
            for (String type : excludedTypes) {
                String elasticsearchType = type.replace(StringPool.PERIOD, StringPool.UNDERLINE);
                suggestionExcludedTypes.add(elasticsearchType);
            }
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug("Suggestion query excluded index types are not defined.....");
            }
        }
    }

    /**
     * Sets the suggestion query fields.
     */
    private void setSuggestionQueryFields() {
        suggestionQueryFields = new HashMap<String, Float>();
        String csFields = getPortletProperty(ElasticsearchPortletConstants.SUGGESTION_QUERY_FIELDS_KEY);

        if (Validator.isNotNull(csFields)) {
            String[] fieldList = csFields.split(StringPool.COMMA);
            /** Iterate over the fields and add to Querybuilder */
            for (String field : fieldList) {
                String[] fieldWithBoostValue = field.split(ElasticsearchPortletConstants.CARET_SPLITCHAR);
                String filedName = fieldWithBoostValue[0] + ElasticsearchPortletConstants.NGRAMS_WITH_PERIOD;
                float boost = ElasticsearchPortletConstants.FLOAT_ZERO_VALUE;
                if (fieldWithBoostValue.length > ElasticsearchPortletConstants.INTEGER_ONE_VALUE) {
                    boost = GetterUtil.getFloat(fieldWithBoostValue[1], boost);
                }
                suggestionQueryFields.put(filedName, boost);
            }
        } else {
            _log.error("Query fields to get suggestions is missing from properties...");
        }
    }

    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchPropsValues.class);

    /** The suggestions size. */
    private int suggestionsSize;

    /** The suggestions length. */
    private int suggestionsLength;

    /** The suggestions query max hits. */
    private int suggestionsQueryMaxHits;

    /** The es server home. */
    private String esServerHome;

    /** The es cluster name. */
    private String esClusterName;

    /** The suggestion query fields. */
    private Map<String, Float> suggestionQueryFields;

    /** The suggestion excluded types. */
    private Set<String> suggestionExcludedTypes;

}

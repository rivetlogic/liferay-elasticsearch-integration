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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.rivetlogic.elasticsearch.portlet.exception.ElasticsearchAutocompleteException;

/**
 * The Class ElasticsearchHelper.
 */
public class ElasticsearchPortletHelper {

    /**
     * Creates a transport client to interact with Elasticsearch servers.
     */
    public void createClient() {

        propsValues = new ElasticsearchPropsValues();
        propsValues.loadESProperties();
        String esServerHome = propsValues.getEsServerHome();
        String esClusterName = propsValues.getEsClusterName();
        
        if (Validator.isNotNull(esServerHome) && !esServerHome.isEmpty() && propsValues.getTransportHosts() != null) {

            /** Create a settings object with custom attributes and build */
            ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder().classLoader(Settings.class.getClassLoader())
                    .put(ElasticsearchPortletConstants.ES_SETTING_PATH_HOME, esServerHome);
            
            if (Validator.isNotNull(esClusterName) && !esClusterName.isEmpty() && !ElasticsearchPortletConstants.ELASTIC_SEARCH.equalsIgnoreCase(esClusterName)) {
                
                settingsBuilder.put(ElasticsearchPortletConstants.ES_SETTING_CLUSTERNAME, esClusterName);
                if(_log.isDebugEnabled()){
                    _log.debug("Elasticsearch cluster name is not configured to default:" + esClusterName);
                }
            }
            
            client = new TransportClient(settingsBuilder.build()).addTransportAddresses(propsValues.getTransportHosts());

            if (_log.isDebugEnabled()) {
                _log.debug("Transport client created successfully");
            }

            createESRequestBuilder();
        } else {
            _log.error("Elasticsearch server home path and at least one node is required to proceed......");
        }
    }

    /**
     * A method to closes client properly to avoid memory leaks.
     */
    public void destroyClient() {
        if (Validator.isNotNull(client)) {
            client.close();
            if (_log.isDebugEnabled()) {
                _log.debug("Closed client successfully......");
            }

        }
    }

    /**
     * A method to create filtered query builder with appropriate filters to fetch auto suggestions.
     *
     * @param resourceRequest the resource request
     * @param resourceResponse the resource response
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ElasticsearchAutocompleteException the elasticsearch autocomplete exception
     */
    public void fetchAutoSuggestions(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
            throws IOException, ElasticsearchAutocompleteException {

        if (Validator.isNull(client)) {
            throw new ElasticsearchAutocompleteException("Client object cannot be null.......");
        }
        if (_log.isDebugEnabled()) {
            _log.debug("About to get suggestions from elasticsearch server.......");
        }
        String searchTerm = ParamUtil.getString(resourceRequest, ElasticsearchPortletConstants.SEARCH_TEXT);

        QueryStringQueryBuilder stringQueryBuilder = QueryBuilders.queryString(searchTerm);
        appendQueryFieldsToBuilder(stringQueryBuilder);
        
        
        FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(stringQueryBuilder, prepareBoolFilterBuilder(resourceRequest));
        
        requestBuilder.setQuery(filteredQueryBuilder);

        SearchResponse response = requestBuilder.execute().actionGet();

        PrintWriter out = resourceResponse.getWriter();
        out.println(parseResponse(response));

        if (_log.isDebugEnabled()) {
            _log.debug("Suggestions retreived from elasticsearch server....");
        }
    }

    /**
     * This method prepares a Boolean filter builder with respective facet selections from Resource request object.
     *
     * @param request the request
     * @return the bool filter builder
     */
    private BoolFilterBuilder prepareBoolFilterBuilder(ResourceRequest request) {
        
        /**Set a filter to get suggestions with Status Approved (0) */
        BoolFilterBuilder boolFilterBuilder = FilterBuilders.boolFilter()
                .must(FilterBuilders.termFilter(ElasticsearchPortletConstants.STATUS, 
                        WorkflowConstants.STATUS_APPROVED));
        
        /** Iterate over Suggestion excluded index types and add to Boolfilter. Since these are excluded, mustNot filter should be used */
        for (Iterator<String> iterator = propsValues.getSuggestionExcludedTypes().iterator(); iterator.hasNext();) {
            boolFilterBuilder.mustNot(FilterBuilders.typeFilter(iterator.next()));
        } 
        
        /** Process facet selections and apply appropriate filters here
            Apply UserId filter */
        long userId = ParamUtil.getLong(request, WorkflowConstants.CONTEXT_USER_ID);
        if(userId > ElasticsearchPortletConstants.INTEGER_ZERO_VALUE) {
            boolFilterBuilder.must(FilterBuilders.termFilter(WorkflowConstants.CONTEXT_USER_ID, userId));
        }

        /** Apply modified filter */
        String selectedRange = ParamUtil.getString(request, ElasticsearchPortletConstants.FILTER_MODIFIED);
        if(Validator.isNotNull(selectedRange) && !selectedRange.isEmpty()){
            String[] rangeArray = fetchFromToValuesInRage(selectedRange);
            boolFilterBuilder.must(FilterBuilders.rangeFilter(ElasticsearchPortletConstants.FILTER_MODIFIED_DATE)
                                    .from(rangeArray[0].trim()).to(rangeArray[1].trim()));
        }
        
        /**  Apply AssetCategoryIds filter */
        long assetCategoryIds = ParamUtil.getLong(request, ElasticsearchPortletConstants.FILTER_ASSET_CATEGORY);
        if(assetCategoryIds > ElasticsearchPortletConstants.INTEGER_ZERO_VALUE) {
            boolFilterBuilder.must(FilterBuilders.termFilter(ElasticsearchPortletConstants.FILTER_ASSET_CATEGORY, assetCategoryIds));
        }


        /**  Apply FolderId filter */
        long folderId = ParamUtil.getLong(request, ElasticsearchPortletConstants.FILTER_FOLDERID);
        if(folderId > ElasticsearchPortletConstants.INTEGER_ZERO_VALUE) {
            boolFilterBuilder.must(FilterBuilders.termFilter(ElasticsearchPortletConstants.FILTER_FOLDERID, folderId));
        }

        /** Apply Site id filter */
        long groupId = ParamUtil.getLong(request, WorkflowConstants.CONTEXT_GROUP_ID);
        if(groupId != WorkflowConstants.DEFAULT_GROUP_ID){
            boolFilterBuilder.must(FilterBuilders.termFilter(ElasticsearchPortletConstants.FILTER_SCOPE_GROUPID, groupId))
                             .must(FilterBuilders.termFilter(WorkflowConstants.CONTEXT_GROUP_ID, groupId));
        }

        /**  Entryclassname is a special case since object is directly mapped to Index type in Elasticsearch.
         So instead of applying a filter, we use respective Entryclassname type */
        String selectedClassName = ParamUtil.getString(request, ElasticsearchPortletConstants.ENTRY_CLASSNAME);
        if(Validator.isNotNull(selectedClassName)){
            /** Convert selectedClassName to index type by replacing . with _*/
            selectedClassName = selectedClassName.replace(StringPool.PERIOD, StringPool.UNDERLINE);
            boolFilterBuilder.must(FilterBuilders.typeFilter(selectedClassName));
        }
        return boolFilterBuilder;
    }
    
    /**
     * Creates the Elasticsearch request builder with all necessary settings.
     */
    private void createESRequestBuilder() {

        requestBuilder = client
                .prepareSearch(ElasticsearchPortletConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                .setFrom(ElasticsearchPortletConstants.INTEGER_ZERO_VALUE)
                .setSize(propsValues.getSuggestionsQueryMaxHits())
                .setHighlighterNumOfFragments(ElasticsearchPortletConstants.INTEGER_ONE_VALUE)
                .setHighlighterRequireFieldMatch(true)
                .setHighlighterFragmentSize(propsValues.getSuggestionsLength())
                .setHighlighterPreTags(StringPool.BLANK)
                .setHighlighterPostTags(StringPool.BLANK)
                .setFetchSource(ElasticsearchPortletConstants.ENTRY_CLASSNAME, StringPool.BLANK);
                
        
        /** Iterate over the fields and add to Requestbuilder */
        for(Map.Entry<String, Float> entry : propsValues.getSuggestionQueryFields().entrySet()) {
            requestBuilder.addHighlightedField(entry.getKey());
        }
    }

    /**
     * A utility method to append query fields to ES Request builder.
     *
     * @param builder the builder
     */
    private void appendQueryFieldsToBuilder(QueryStringQueryBuilder builder) {

        /** Iterate over the fields and add to Querybuilder */
        for(Map.Entry<String, Float> entry : propsValues.getSuggestionQueryFields().entrySet()) {
            if(entry.getValue() > ElasticsearchPortletConstants.FLOAT_ZERO_VALUE) {
                builder = builder.field(entry.getKey(), entry.getValue());
            } else {
                builder = builder.field(entry.getKey());
            }
        }
    }

    /**
     * A utility method which parses the ES Query response and returns a JSON array string object.
     * If the response is empty, return empty JSON empty array
     *
     * @param response the response
     * @return the string
     */
    private String parseResponse(SearchResponse response) {

        JSONArray arraySuggestions = JSONFactoryUtil.createJSONArray();
        Set<String> nonDupliateSuggestions = new HashSet<String>();

        for (Iterator<SearchHit> iterator = response.getHits().iterator(); iterator.hasNext();) {
            SearchHit hit = iterator.next();
            JSONObject jsonSuggestion = JSONFactoryUtil.createJSONObject();
            /** Get all the highlights from a hit and put them in a Map */
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            /** Get the highlighted fields and check for NULL, if so skip the hit */
            if (Validator.isNull(highlightFields)) {
                /** if highlight is missing, then there is nothing to process in the hit */
                continue;
            } else {
                HighlightField[] highlightArray = new HighlightField[ElasticsearchPortletConstants.INTEGER_ONE_VALUE];
                highlightArray = highlightFields.values().toArray(highlightArray);
                HighlightField field = highlightArray[ElasticsearchPortletConstants.INTEGER_ZERO_VALUE];
                String ngramField = field.getName();
                String fieldName = ngramField.substring(ElasticsearchPortletConstants.INTEGER_ZERO_VALUE,
                        ngramField.indexOf(ElasticsearchPortletConstants.NGRAMS_WITH_PERIOD));
                /** Since the fragment size is set to 1, there can only be one fragment at any time */
                String fragment = field.fragments()[ElasticsearchPortletConstants.INTEGER_ZERO_VALUE].string();
                
                /**Check if the suggestion is unique and doesn't exists in the set...*/
                if(!nonDupliateSuggestions.contains(fragment)){
                    
                    nonDupliateSuggestions.add(fragment);
                    /** Update the JSON suggestion with highlight fragment */
                    jsonSuggestion.put(fieldName, fragment);

                    /** Now add Entryclassname from the hit which helps to identify the type of each result in front end */
                    Map<String, Object> hitValuesAsMap = hit.sourceAsMap();
                    String entryClassname = (String) hitValuesAsMap.get(ElasticsearchPortletConstants.ENTRY_CLASSNAME);
                    if (Validator.isNotNull(entryClassname)) {
                        jsonSuggestion.put(ElasticsearchPortletConstants.ENTRY_CLASSNAME, entryClassname);
                    }
                }
            }
            /** Add the JSON suggestion object to array only if it has some content */
            if(jsonSuggestion.length() > ElasticsearchPortletConstants.INTEGER_ZERO_VALUE) {
                arraySuggestions.put(jsonSuggestion);
            }
            
            /** Check if the Suggestions size have reached desired count, if so break; */
            if(nonDupliateSuggestions.size() == propsValues.getSuggestionsSize()) {
                break;
            }
        }
        return arraySuggestions.toString();
    }
    
    /**
     * A utility method to fetch FROM and TO values in range string.
     *
     * @param fromToFormatRange the from to format range
     * @return the string[]
     */
    private String[] fetchFromToValuesInRage(String fromToFormatRange){
        String[] fromToArray = null;
        fromToArray = fromToFormatRange.substring(1, fromToFormatRange.length()-1).split(ElasticsearchPortletConstants.FILTER_TO);
        return fromToArray;
    }

    /** The request builder. */
    private SearchRequestBuilder requestBuilder = null;
    
    /** The client. */
    private Client client = null;
    
    /** The props values. */
    private ElasticsearchPropsValues propsValues;
    
    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchPortletHelper.class);
}

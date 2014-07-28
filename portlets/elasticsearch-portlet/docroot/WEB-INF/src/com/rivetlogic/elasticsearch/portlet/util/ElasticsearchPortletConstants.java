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

/**
 * The Class ElasticsearchPortletConstants.
 * 
 */
public class ElasticsearchPortletConstants {

    /** Elasticsearch build settings */    
    public static final String ES_SETTING_PATH_HOME = "path.home";
    public static final String ES_SETTING_CLUSTERNAME = "cluster.name";
    public static final String ES_SETTING_CLIENT_SNIFF = "client.transport.sniff";

    /** Elasticsearch portal property keys */    
    public static final String ES_KEY_CLUSTERNAME = "elasticsearch.clusterName";
    public static final String ES_KEY_NODE = "elasticsearch.node";
    public static final String ES_KEY_HOME_PATH = "elasticsearch.homeFile";

    /** Portlet property keys */
    public static final String SUGGESTIONS_SIZE_KEY = "autoSuggestionsSize";
    public static final String SUGGESTIONS_LENGTH_KEY = "autoSuggestionLength";
    public static final String SUGGESTION_QUERY_FIELDS_KEY = "suggestionQueryFields";
    public static final String SUGGESTION_EXCLUDED_TYPES_KEY = "suggestionQueryExcludedType";
    public static final String SUGGESTION_QUERY_MAX_HITS_KEY = "autoSuggestionsQueryMaxHits";

    /** Other constants required in plugin */
    public static final int SUGGESTIONS_SIZE_DEFAULT_VALUE = 4;
    public static final int SUGGESTIONS_LENGTH_DEFAULT_VALUE = 50;
    public static final int SUGGESTIONS_QUERY_MAX_HITS_DEFAULT_VALUE = 10;
    public static final int INTEGER_ZERO_VALUE = 0;
    public static final int INTEGER_ONE_VALUE = 1;
    public static final float FLOAT_ZERO_VALUE = 0;

    public static final String ELASTIC_SEARCH = "elasticsearch";
    public static final String ELASTIC_SEARCH_LIFERAY_INDEX = "liferay";
    public static final String SEARCH_TEXT = "searchText";
    public static final String TITLE = "title";
    public static final String USERNAME = "userName";
    public static final String CONTENT = "content";
    public static final String SUGGESTION = "suggestion";
    public static final String AUTHOR = "author";
    public static final String ENTRY_CLASSNAME = "entryClassName";
    public static final String NGRAMS_WITH_PERIOD = ".ngrams";
    public static final String CARET_SPLITCHAR = "\\^";
    public static final String STATUS = "status";

    
    public static final String FILTER_SCOPE_GROUPID = "scopeGroupId";
    public static final String FILTER_ASSET_CATEGORY = "assetCategoryIds";
    public static final String FILTER_FOLDERID = "folderId";
    public static final String FILTER_MODIFIED = "modified";
    public static final String FILTER_MODIFIED_DATE = "modified.modified_date";
    public static final String FILTER_TO = "TO";
}

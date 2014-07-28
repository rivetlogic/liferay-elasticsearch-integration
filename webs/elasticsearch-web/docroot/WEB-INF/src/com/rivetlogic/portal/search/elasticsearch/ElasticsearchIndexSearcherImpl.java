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
package com.rivetlogic.portal.search.elasticsearch;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexSearcher;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Query;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.Sort;
import com.rivetlogic.portal.search.elasticsearch.util.ElasticsearchHelper;

/**
 * The Class ElasticsearchIndexSearcherImpl.
 */
public class ElasticsearchIndexSearcherImpl extends BaseIndexSearcher {


    /**
     * Gets the es search helper.
     *
     * @return the _esSearchHelper
     */
	public ElasticsearchHelper getEsSearchHelper() {
		return _esSearchHelper;
	}

	/**
	 * Sets the es search helper.
	 *
	 * @param esSearchHelper the new es search helper
	 */
	public void setEsSearchHelper(ElasticsearchHelper esSearchHelper) {
		this._esSearchHelper = esSearchHelper;
	}



    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.IndexSearcher#search(com.liferay.portal.kernel.search.SearchContext, com.liferay.portal.kernel.search.Query)
     */
    @Override
    public Hits search(SearchContext searchContext, Query query) throws SearchException {
    	if (_log.isInfoEnabled()) {
    		_log.info("Search against elasticsearch indexes");
    	}

    	return _esSearchHelper.getSearchHits(searchContext, query);
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.IndexSearcher#search(java.lang.String, long, com.liferay.portal.kernel.search.Query, com.liferay.portal.kernel.search.Sort[], int, int)
     */
    @Override
    public Hits search(String searchEngineId, long companyId, Query query, Sort[] sort, int start, int end)
            throws SearchException {
    	if (_log.isInfoEnabled()) {
    		_log.info("Search with sort and ranges against elasticsearch indexes");
    	}
        return _esSearchHelper.getSearchHits(searchEngineId, companyId, query, sort, start, end);
    }

    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchIndexSearcherImpl.class);
    
    /** The _es search helper. */
    private ElasticsearchHelper _esSearchHelper;
}

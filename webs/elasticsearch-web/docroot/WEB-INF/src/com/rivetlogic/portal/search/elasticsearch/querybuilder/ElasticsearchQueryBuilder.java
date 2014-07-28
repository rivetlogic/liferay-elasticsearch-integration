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

package com.rivetlogic.portal.search.elasticsearch.querybuilder;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Query;

/**
 * Query builder that returns QueryStringQueryBuilder that uses the same query object from Liferay
 * which is based on Lucene index search
 * 
 *
 */
public class ElasticsearchQueryBuilder {
	private final static Log _log = LogFactoryUtil.getLog(ElasticsearchQueryBuilder.class);
	
	/**
	 * Since all the search implementation from elasticsearch are based on Lucene search,
	 * we could just use the same query object from Liferay which used Lucene search as base
	 * 
	 * @param query
	 * @return
	 */
	public QueryStringQueryBuilder doSearch(Query query) {
		if (_log.isInfoEnabled()) {
			_log.info("Searching against Elasticsearch Search Engine using QueryStringQueryBuilder...");
		}
		return QueryBuilders.queryString(query.toString());
	}
}

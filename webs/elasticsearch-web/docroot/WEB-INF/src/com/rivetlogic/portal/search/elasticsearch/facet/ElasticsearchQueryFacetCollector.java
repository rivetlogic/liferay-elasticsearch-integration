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

package com.rivetlogic.portal.search.elasticsearch.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.search.facet.collector.FacetCollector;
import com.liferay.portal.kernel.search.facet.collector.TermCollector;

/**
 * The Class ElasticsearchQueryFacetCollector.
 */
public class ElasticsearchQueryFacetCollector implements FacetCollector {

    /**
     * Instantiates a new elasticsearch query facet collector.
     *
     * @param fieldName the field name
     * @param facetResults the facet results
     */
    public ElasticsearchQueryFacetCollector(String fieldName, Map<String, Integer> facetResults) {
        this._fieldName = fieldName;

        for (Map.Entry<String, Integer> entry : facetResults.entrySet()) {
            String term = entry.getKey();
            Integer count = entry.getValue();

            this._counts.put(term, count);
        }
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getFieldName()
     */
    public String getFieldName() {
        return this._fieldName;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getTermCollector(java.lang.String)
     */
    public TermCollector getTermCollector(String term) {
        Integer count = this._counts.get(term);

        return new ElasticsearchDefaultTermCollector(term, count.intValue());
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.FacetCollector#getTermCollectors()
     */
    public List<TermCollector> getTermCollectors() {
        if (this._termCollectors != null) {
            return this._termCollectors;
        }

        List<TermCollector> termCollectors = new ArrayList<TermCollector>();

        for (Map.Entry<String, Integer> entry : this._counts.entrySet()) {
            Integer count = entry.getValue();

            TermCollector termCollector = new ElasticsearchDefaultTermCollector((String) entry.getKey(),
                    count.intValue());

            termCollectors.add(termCollector);
        }

        this._termCollectors = termCollectors;

        return this._termCollectors;
    }
    
    /** The _counts. */
    private Map<String, Integer> _counts = new HashMap<String, Integer>();
    
    /** The _field name. */
    private String _fieldName;
    
    /** The _term collectors. */
    private List<TermCollector> _termCollectors;
}
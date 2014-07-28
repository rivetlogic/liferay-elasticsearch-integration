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

import com.liferay.portal.kernel.search.facet.collector.TermCollector;

/**
 * The Class ElasticsearchDefaultTermCollector.
 */
public class ElasticsearchDefaultTermCollector implements TermCollector {

    /**
     * Instantiates a new elasticsearch default term collector.
     *
     * @param term the term
     * @param frequency the frequency
     */
    public ElasticsearchDefaultTermCollector(String term, int frequency) {
        this.term = term;
        this.frequency = frequency;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.TermCollector#getTerm()
     */
    @Override
    public String getTerm() {
        return this.term;
    }

    /* (non-Javadoc)
     * @see com.liferay.portal.kernel.search.facet.collector.TermCollector#getFrequency()
     */
    @Override
    public int getFrequency() {
        return this.frequency;
    }

    
    /** The term. */
    private String term;
    
    /** The frequency. */
    private int frequency;
}

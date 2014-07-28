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

package com.rivetlogic.portal.search.elasticsearch.exception;


/**
 * The Class ElasticsearchIndexingException.
 */
public class ElasticsearchIndexingException extends Exception {

    /**
     * Instantiates a new elasticsearch indexing exception.
     */
    public ElasticsearchIndexingException() {
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param msg the msg
     */
    public ElasticsearchIndexingException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param t the t
     */
    public ElasticsearchIndexingException(Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new elasticsearch indexing exception.
     *
     * @param msg the msg
     * @param t the t
     */
    public ElasticsearchIndexingException(String msg, Throwable t) {
        super(msg, t);
    }

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4419932835671285073L;
    
}

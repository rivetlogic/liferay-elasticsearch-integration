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

package com.rivetlogic.elasticsearch.portlet.exception;

/**
 * The Class ElasticsearchAutocompleteException.
 */
public class ElasticsearchAutocompleteException extends Exception{

    /**
     * Instantiates a new elasticsearch autocomplete exception.
     */
    public ElasticsearchAutocompleteException() {
    }

    /**
     * Instantiates a new elasticsearch autocomplete exception.
     *
     * @param msg the msg
     */
    public ElasticsearchAutocompleteException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new elasticsearch autocomplete exception.
     *
     * @param t the t
     */
    public ElasticsearchAutocompleteException(Throwable t) {
        super(t);
    }

    /**
     * Instantiates a new elasticsearch autocomplete exception.
     *
     * @param msg the msg
     * @param t the t
     */
    public ElasticsearchAutocompleteException(String msg, Throwable t) {
        super(msg, t);
    }
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4419932835671285078L;
}

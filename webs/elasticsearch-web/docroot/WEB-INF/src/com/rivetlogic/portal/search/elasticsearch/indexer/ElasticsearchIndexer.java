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
package com.rivetlogic.portal.search.elasticsearch.indexer;

import java.util.Collection;

import com.liferay.portal.kernel.search.Document;
import com.rivetlogic.portal.search.elasticsearch.exception.ElasticsearchIndexingException;
import com.rivetlogic.portal.search.elasticsearch.indexer.document.ElasticserachJSONDocument;

/**
 * The Interface ElasticsearchIndexer.
 */
public interface ElasticsearchIndexer {

    /**
     * Process document.
     *
     * @param document the document
     * @return the elasticserach json document
     * @throws ElasticsearchIndexingException the elasticsearch indexing exception
     */
    public ElasticserachJSONDocument processDocument(Document document) throws ElasticsearchIndexingException;

    /**
     * Process documents.
     *
     * @param documents the documents
     * @return the collection
     * @throws ElasticsearchIndexingException the elasticsearch indexing exception
     */
    public Collection<ElasticserachJSONDocument> processDocuments(Collection<Document> documents) throws ElasticsearchIndexingException;
}

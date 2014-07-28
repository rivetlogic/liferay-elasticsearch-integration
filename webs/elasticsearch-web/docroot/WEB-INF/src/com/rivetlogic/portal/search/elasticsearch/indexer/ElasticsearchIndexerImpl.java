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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.rivetlogic.portal.search.elasticsearch.exception.ElasticsearchIndexingException;
import com.rivetlogic.portal.search.elasticsearch.indexer.document.ElasticsearchDocumentJSONBuilder;
import com.rivetlogic.portal.search.elasticsearch.indexer.document.ElasticserachJSONDocument;

/**
 * The Class ElasticsearchIndexerImpl.
 */
public class ElasticsearchIndexerImpl implements ElasticsearchIndexer {

    /**
     * Gets the document json builder.
     *
     * @return the document json builder
     */
    public ElasticsearchDocumentJSONBuilder getDocumentJSONBuilder() {
        return documentJSONBuilder;
    }

    /**
     * Sets the document json builder.
     *
     * @param documentJSONBuilder the new document json builder
     */
    public void setDocumentJSONBuilder(ElasticsearchDocumentJSONBuilder documentJSONBuilder) {
        this.documentJSONBuilder = documentJSONBuilder;
    }

    /* (non-Javadoc)
     * @see com.rivetlogic.portal.search.elasticsearch.indexer.ElasticsearchIndexer#processDocuments(java.util.Collection)
     */
    public Collection<ElasticserachJSONDocument> processDocuments(Collection<Document> documents) throws ElasticsearchIndexingException {
        _log.info("Processing multiple document objects for elasticsearch indexing");

        Collection<ElasticserachJSONDocument> esDocuments = new ArrayList<ElasticserachJSONDocument>();
        // transform Document object into JSON object and send it to
        // elasticsearch server for indexing
        for (Document doc : documents) {
            esDocuments.add(documentJSONBuilder.convertToJSON(doc));
        }

        return esDocuments;
    }

    /* (non-Javadoc)
     * @see com.rivetlogic.portal.search.elasticsearch.indexer.ElasticsearchIndexer#processDocument(com.liferay.portal.kernel.search.Document)
     */
    public ElasticserachJSONDocument processDocument(Document document) throws ElasticsearchIndexingException {
        Collection<Document> documents = new ArrayList<Document>();
        documents.add(document);
        _log.info("Processing Document to update elasticsearch indexes");

        List<ElasticserachJSONDocument> esDocuments = (List<ElasticserachJSONDocument>) processDocuments(documents);
        return esDocuments.get(0);
    }
    
    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchIndexerImpl.class);
    
    /** The document json builder. */
    private ElasticsearchDocumentJSONBuilder documentJSONBuilder;
}

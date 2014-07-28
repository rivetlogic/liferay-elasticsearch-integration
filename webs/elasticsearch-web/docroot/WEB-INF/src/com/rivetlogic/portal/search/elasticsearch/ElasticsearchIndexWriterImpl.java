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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexWriter;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentComparator;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.rivetlogic.portal.search.elasticsearch.exception.ElasticsearchIndexingException;
import com.rivetlogic.portal.search.elasticsearch.indexer.ElasticsearchIndexer;
import com.rivetlogic.portal.search.elasticsearch.indexer.document.ElasticserachJSONDocument;
import com.rivetlogic.portal.search.elasticsearch.util.ElasticsearchConnector;

/**
 * The Class ElasticsearchIndexWriterImpl.
 */
public class ElasticsearchIndexWriterImpl extends BaseIndexWriter {

    /**
     * Sets the es connector.
     * 
     * @param esConnector
     *            the new es connector
     */
    public void setEsConnector(ElasticsearchConnector esConnector) {
        this._esConnector = esConnector;
    }

    /**
     * Sets the indexer.
     * 
     * @param indexer
     *            the new indexer
     */
    public void setIndexer(ElasticsearchIndexer indexer) {
        this._indexer = indexer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#addDocument(com.liferay.
     * portal.kernel.search.SearchContext,
     * com.liferay.portal.kernel.search.Document)
     */
    @Override
    public void addDocument(SearchContext searchContext, Document document) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Add document for elasticsearch indexing");
        }
        processIt(document);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#addDocuments(com.liferay
     * .portal.kernel.search.SearchContext, java.util.Collection)
     */
    public void addDocuments(SearchContext searchContext, Collection<Document> documents) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Add documents for elasticsearch indexing");
        }
        /** This is to sort the Documents with version field from oldest to latest updates to
        retain the modifications */
        DocumentComparator documentComparator = new DocumentComparator(true, false);
        documentComparator.addOrderBy(ElasticsearchIndexerConstants.VERSION);
        Collections.sort((List<Document>) documents, documentComparator);
        
        for (Document document : documents) {
            updateDocument(searchContext, document);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#deleteDocument(com.liferay
     * .portal.kernel.search.SearchContext, java.lang.String)
     */
    public void deleteDocument(SearchContext searchContext, String uid) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Delete document from elasticsearch indexices");
        }
        deleteIndexByQuery(uid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#deleteDocuments(com.liferay
     * .portal.kernel.search.SearchContext, java.util.Collection)
     */
    public void deleteDocuments(SearchContext searchContext, Collection<String> uids) throws SearchException {
        for (String uid : uids) {
            deleteDocument(searchContext, uid);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#deletePortletDocuments(com
     * .liferay.portal.kernel.search.SearchContext, java.lang.String)
     */
    public void deletePortletDocuments(SearchContext searchContext, String portletId) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Delete portlet documents from elasticsearch indexing");
        }
        throw new SearchException("Portlet deployment documents are not supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#updateDocument(com.liferay
     * .portal.kernel.search.SearchContext,
     * com.liferay.portal.kernel.search.Document)
     */
    public void updateDocument(SearchContext searchContext, Document document) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Update document from elasticsearch indexing");
        }
        processIt(document);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.liferay.portal.kernel.search.IndexWriter#updateDocuments(com.liferay
     * .portal.kernel.search.SearchContext, java.util.Collection)
     */
    public void updateDocuments(SearchContext searchContext, Collection<Document> documents) throws SearchException {

        /** This is to sort the Documents with version field from oldest to latest updates to
        retain the modifications */
        DocumentComparator documentComparator = new DocumentComparator(true, false);
        documentComparator.addOrderBy(ElasticsearchIndexerConstants.VERSION);
        Collections.sort((List<Document>) documents, documentComparator);
        
        for (Document document : documents) {
            updateDocument(searchContext, document);
        }
    }

    /**
     * Process it.
     * 
     * @param document
     *            the document
     * @throws SearchException
     *             the search exception
     */
    private void processIt(Document document) throws SearchException {
        if (_log.isDebugEnabled()) {
            _log.debug("Processing document for elasticsearch indexing");
        }
        try {
            ElasticserachJSONDocument elasticserachJSONDocument = _indexer.processDocument(document);
            writeIndex(elasticserachJSONDocument);
        } catch (ElasticsearchIndexingException e) {
            throw new SearchException(e);
        }
    }

    /**
     * A method to persist Liferay index to Elasticsearch server document.
     * 
     * @param esDocument
     *            the json document
     */
    private void writeIndex(ElasticserachJSONDocument esDocument) {

        try {
            if (esDocument.isError()) {
                _log.warn("Coudln't store document in index. Error..." + esDocument.getErrorMessage());

            } else {
                Client client = _esConnector.getClient();
                IndexResponse response = client
                        .prepareIndex(ElasticsearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX,
                                esDocument.getIndexType(), esDocument.getId()).setSource(esDocument.getJsonDocument())
                        .execute().actionGet();
                if (_log.isDebugEnabled()) {
                    _log.debug("Document indexed successfully with Id:" + esDocument.getId() + " ,Type:"
                            + esDocument.getIndexType() + " ,Updated index version:" + response.getVersion());
                }
            }
        } catch (NoNodeAvailableException noNodeEx) {
            _log.error("No node available:" + noNodeEx.getDetailedMessage());
        }
    }

    
    /**
     * Delete index by query.
     * 
     * @param uid
     *            the uid
     */
    private void deleteIndexByQuery(String uid) {

        try {
            /** Don't handle plugin deployment documents, skip them */
            if(!uid.endsWith(ElasticsearchIndexerConstants.WAR)){
                Client client = _esConnector.getClient();
                DeleteByQueryResponse response = client
                        .prepareDeleteByQuery(ElasticsearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                        .setQuery(QueryBuilders.queryString(ElasticsearchIndexerConstants.ELASTIC_SEARCH_QUERY_UID + uid))
                        .execute().actionGet();
                
                if (_log.isDebugEnabled()) {
                    _log.debug("Document deleted successfully with Id:" + uid + " , Status:" + response.status());
                }
            }
        } catch (NoNodeAvailableException noNodeEx) {
            _log.error("No node available:" + noNodeEx.getDetailedMessage());
        } catch (IndexMissingException indexMissingEx) {
            _log.error("No index availabe:" + indexMissingEx.getDetailedMessage());
        }
    }
    
    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchIndexWriterImpl.class);
    
    /** The _indexer. */
    private ElasticsearchIndexer _indexer;
    
    /** The _es connector. */
    private ElasticsearchConnector _esConnector;
}

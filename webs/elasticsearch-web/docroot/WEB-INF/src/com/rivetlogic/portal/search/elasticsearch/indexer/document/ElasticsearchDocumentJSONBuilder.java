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
package com.rivetlogic.portal.search.elasticsearch.indexer.document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.util.portlet.PortletProps;
import com.rivetlogic.portal.search.elasticsearch.ElasticsearchIndexerConstants;

/**
 * The Class ElasticsearchDocumentJSONBuilder.
 *
 */
public class ElasticsearchDocumentJSONBuilder {

    /**
     * Init method.
     */
    public void loadExcludedTypes() {
        String cslExcludedType = PortletProps.get(ElasticsearchIndexerConstants.ES_KEY_EXCLUDED_INDEXTYPE);
        if(Validator.isNotNull(cslExcludedType)){
            excludedTypes = new HashSet<String>();
            for(String excludedType : cslExcludedType.split(StringPool.COMMA)){
                excludedTypes.add(excludedType);
            }
            if(_log.isDebugEnabled()){
                _log.debug("Loaded Excluded index types are:"+cslExcludedType);
            }
            _log.info("Loaded Excluded index types are:"+cslExcludedType);
        } else {
            if(_log.isDebugEnabled()){
                _log.debug("Excluded index types are not defined");
            }
            _log.info("Excluded index types are not defined");
        }
    }
    
    /**
     * Convert to json.
     * 
     * @param document
     *            the document
     * @return the string
     */
    public ElasticserachJSONDocument convertToJSON(final Document document) {
        
        Map<String, Field> fields = document.getFields();
        ElasticserachJSONDocument elasticserachJSONDocument = new ElasticserachJSONDocument();
        
        try {
            XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();
            
            Field classnameField = document.getField(ElasticsearchIndexerConstants.ENTRY_CLASSNAME);
            String entryClassName = (classnameField == null)? "": classnameField.getValue();
            
            /**
             * Handle all error scenarios prior to conversion
             */
            if(isDocumentHidden(document)){
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(ElasticserachJSONDocument.DocumentError.HIDDEN_DOCUMENT.toString());
                return elasticserachJSONDocument;
            }
            if(entryClassName.isEmpty()){
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(ElasticserachJSONDocument.DocumentError.MISSING_ENTRYCLASSNAME.toString());
                return elasticserachJSONDocument;
            }
            if(isExcludedType(entryClassName)){
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage("Index Type:"+entryClassName+StringPool.COMMA+ElasticserachJSONDocument.DocumentError.EXCLUDED_TYPE.toString());
                return elasticserachJSONDocument;
            }

            /**
             * To avoid multiple documents for versioned assets such as Journal articles, DL entry etc
             * the primary Id will be Indextype + Entry class PK. The primary Id is to maintain uniqueness
             * in ES server database and nothing to do with UID or is not used for any other purpose.
             */
            Field classPKField = document.getField(ElasticsearchIndexerConstants.ENTRY_CLASSPK);
            String entryClassPK = (classPKField == null)? "": classPKField.getValue();
            if(entryClassPK.isEmpty()){
                elasticserachJSONDocument.setError(true);
                elasticserachJSONDocument.setErrorMessage(ElasticserachJSONDocument.DocumentError.MISSING_CLASSPK.toString());
                return elasticserachJSONDocument;
            }
                
            /** Replace '.' by '_' in Entry class name,since '.' is not recommended by Elasticsearch in Index type */
            String indexType = entryClassName.replace(StringPool.PERIOD, StringPool.UNDERLINE);
            elasticserachJSONDocument.setIndexType(indexType);
            
            elasticserachJSONDocument.setId(indexType+entryClassPK);
            
            /** Create a JSON string for remaining fields of document */
            for (Iterator<Map.Entry<String, Field>> it = fields.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Field> entry = it.next();
                Field field = entry.getValue();
                contentBuilder.field(entry.getKey(), field.getValue());
            }
            contentBuilder.endObject();
            
            elasticserachJSONDocument.setJsonDocument(contentBuilder.string());
            if(_log.isDebugEnabled()){
                _log.debug("Liferay Document converted to ESJSON document successfully:"+contentBuilder.string());
            }
        } catch (IOException e) {
            _log.error("IO Error during converstion of Liferay Document to JSON format"+e.getMessage());
        }
        return elasticserachJSONDocument;
    }


    /**
     * Check if liferay Document is of type hidden.
     *
     * @param document the document
     * @return true, if is document hidden
     */
    private boolean isDocumentHidden(Document document){
        Field hiddenField = document.getField(ElasticsearchIndexerConstants.HIDDEN);
        String hiddenFlag = (hiddenField == null)? "" : hiddenField.getValue();
        if(StringPool.TRUE.equalsIgnoreCase(hiddenFlag)){
            return true;
        }
        return false;
    }
    
    /**
     * Check if EntryClassname is com.liferay.portal.kernel.plugin.PluginPackage/ExportImportHelper which need not be indexed
     *
     * @param indexType the index type
     * @return true, if is excluded type
     */
    private boolean isExcludedType(String indexType) {
        if(indexType != null && excludedTypes != null) {
            for(String excludedType : excludedTypes){
                if(indexType.toLowerCase().contains(excludedType.toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }
    
    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchDocumentJSONBuilder.class);
    
    /** The excluded types. */
    private Set<String> excludedTypes;
}

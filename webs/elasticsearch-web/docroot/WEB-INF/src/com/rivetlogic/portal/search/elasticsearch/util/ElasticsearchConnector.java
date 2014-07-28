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

package com.rivetlogic.portal.search.elasticsearch.util;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.env.FailedToResolveConfigException;
import org.elasticsearch.indices.IndexAlreadyExistsException;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.rivetlogic.portal.search.elasticsearch.ElasticsearchIndexerConstants;

/**
 * The Class ElasticsearchConnector.
 * 
 */
public class ElasticsearchConnector {
    
    /**
     * Inits the transport client.
     */
    public void initESSetup() {

        try {
            String esServerHome = PropsUtil.get(ElasticsearchIndexerConstants.ES_KEY_HOME_PATH);
            String esClusterName = PropsUtil.get(ElasticsearchIndexerConstants.ES_KEY_CLUSTERNAME);

            if (Validator.isNull(esServerHome) || esServerHome.isEmpty()) {
                throw new ElasticsearchException("Elasticsearch server home folder is not configured...");
            }
            
            /** Create a settings object with custom attributes and build */
            ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder().classLoader(Settings.class.getClassLoader())
                                                .put(ElasticsearchIndexerConstants.ES_SETTING_PATH_HOME, esServerHome)
                                                .put(ElasticsearchIndexerConstants.ES_SETTING_CLIENT_SNIFF, true);

            if (Validator.isNotNull(esClusterName) && !esClusterName.isEmpty() && !ElasticsearchIndexerConstants.ELASTIC_SEARCH.equalsIgnoreCase(esClusterName)) {
                settingsBuilder.put(ElasticsearchIndexerConstants.ES_SETTING_CLUSTERNAME, esClusterName);

                if(_log.isDebugEnabled()){
                    _log.debug("Elasticsearch cluster name is not configured to default:" + esClusterName);
                }
            }

            String csElasticsearchNodes = PropsUtil.get(ElasticsearchIndexerConstants.ES_KEY_NODE);
            if (Validator.isNull(csElasticsearchNodes) || csElasticsearchNodes.isEmpty()) {
                throw new ElasticsearchException("Elasticsearch server node is not configured...");
            }

            String[] nodeList = csElasticsearchNodes.split(StringPool.COMMA);
            InetSocketTransportAddress[] transportAddresses = new InetSocketTransportAddress[nodeList.length];

            /** Prepare a list of Hosts */
            for (int i = 0; i < nodeList.length; i++) {
                String[] hostnames = nodeList[i].split(StringPool.COLON);
                InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(hostnames[0],
                        Integer.parseInt(hostnames[1]));
                transportAddresses[i] = transportAddress;
            }
            
            client = new TransportClient(settingsBuilder.build()).addTransportAddresses(transportAddresses);
            _log.info("Successfully created Transport client........");
            /**
             * Check if Liferay index already exists, else create one with
             * default mapping The Index creation is one time setup, so it is
             * important to check if index already exists before creation
             */
            if (!isLiferayIndexExists()) {
                createLiferayIndexInESServer();
            }
        } catch (FailedToResolveConfigException configEx) {
            _log.error("Error while connecting to Elasticsearch server:" + configEx.getMessage());
        } 
    }

    /**
     * The Close is run during destroying the spring context. The client object
     * need to be closed to avoid overlock exceptions
     */
    public void close() {
        _log.info("About to close Client........");
        if (client != null) {
            client.close();
        }
        _log.info("Successfully closed Client........");
    }

    /**
     * Gets the client.
     * 
     * @return the client
     */
    public Client getClient() {
        return client;
    }

    /**
     * Checks if Liferay index exists.
     * 
     * @return true, if liferay index exists in Elasticsearch server
     */
    private boolean isLiferayIndexExists() {
        IndicesExistsResponse existsResponse = client.admin().indices()
                .exists(new IndicesExistsRequest(ElasticsearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX))
                .actionGet();
        if (_log.isDebugEnabled()) {
            _log.debug("Liferay index exists:" + existsResponse.isExists());
        }

        return existsResponse.isExists();
    }

    /**
     * Creates the liferay index in Elasticsearch server with default dynamic
     * mapping template.
     */
    private void createLiferayIndexInESServer() {
        try {
            CreateIndexResponse createIndexResponse = client.admin().indices()
                    .prepareCreate(ElasticsearchIndexerConstants.ELASTIC_SEARCH_LIFERAY_INDEX)
                    .addMapping("_default_", loadMappings())
                    .setSettings(loadSettings())
                    .execute().actionGet();

            _log.info("Index created with dynamic template mapping provided, Result:"
                    + createIndexResponse.isAcknowledged());
        } catch (IndexAlreadyExistsException iaeEx) {
            _log.warn("Index already exists, no need to create again....");
        } catch (Exception e) {
        	_log.error("Failed to load file for elasticsearch mapping settings", e);
        }
    }
    
    /**
     * builds mappings for suggestions feature and fields aliasing
     * @return
     * @throws Exception
     */
    private XContentBuilder loadMappings() throws Exception {
    	XContentBuilder mappingBuilder = XContentFactory.jsonBuilder()
				.startObject()
					.startArray("dynamic_templates")
						.startObject()
	    					.startObject("modified_template")
	    						.field("match", "modified")
	    						.startObject("mapping")
	    							.field("type", "multi_field")
	    							.startObject("fields")
	    								.startObject("modified_date")
	    									.field("type", "long")
	    								.endObject()
	    								.startObject("modified")
	    									.field("type", "string")
	    								.endObject()
	    							.endObject()
	    						.endObject()
	    					.endObject()
    					.endObject()
    					.startObject()
	    					.startObject("base")
	    						.field("match", "*")
	    						.field("unmatch", ElasticsearchIndexerConstants.ENTRY_CLASSNAME)
	    						.startObject("mapping")
	    							.field("type", "multi_field")
	    							.startObject("fields")
	    								.startObject("{name}")
	    									.field("type", "{dynamic_type}")
	    								.endObject()
	    								.startObject("ngrams")
	    									.field("type", "string")
	    									.field("index_analyzer", "nGram_analyzer")
	    									.field("search_analyzer", "whitespace_analyzer")
	    								.endObject()
	    							.endObject()
	    						.endObject()
	    					.endObject()
    					.endObject()
    				.endArray()
	    		.endObject();
    	return mappingBuilder;
    }
    
    /**
     * builds settings for suggestions feature
     * @return
     * @throws Exception
     */
    private XContentBuilder loadSettings() throws Exception {
    	XContentBuilder settingBuilder = XContentFactory.jsonBuilder()
    			.startObject()
    				.startObject("analysis")
    					.startObject("filter")
    						.startObject("filternGram")
    							.field("max_gram", 15)
    							.field("min_gram", 2)
    							.field("type", "edgeNGram")
    							.field("term_vector","with_positions_offsets")
    							.field("version", "4.1")	// version information is provided to make the highlighting work properly
    							.humanReadable(true)
    						.endObject()
    					.endObject()
	    				.startObject("analyzer")
	    					.startObject("nGram_analyzer")
	    					    .field("type", "custom")
	    					    .field("tokenizer", "letter")
	    					    .startArray("filter")
	    							.value("stop")
	    							.value("lowercase")
	    							.value("filternGram")
	    						.endArray()
	    					.endObject()
	    					.startObject("whitespace_analyzer")
	    						.field("type", "custom")
	    						.field("tokenizer", "letter")
	    						.startArray("filter")
	    							.value("lowercase")
	    							.value("stop")
	    						.endArray()
	    					.endObject()
	    				.endObject()							    					
    				.endObject()
    			.endObject();
    	return settingBuilder;
    }
    
    /** The Constant _log. */
    private final static Log _log = LogFactoryUtil.getLog(ElasticsearchConnector.class);
    
    /** The client. */
    private Client client = null;
}

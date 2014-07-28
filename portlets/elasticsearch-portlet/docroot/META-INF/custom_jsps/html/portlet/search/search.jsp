<%--
/**
 * Copyright (C) 2014 Rivet Logic Corporation. All rights reserved.
 */
/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */
--%>

<%@ include file="/html/portlet/search/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

if (Validator.isNotNull(redirect)) {
	portletDisplay.setURLBack(redirect);
}

String primarySearch = ParamUtil.getString(request, "primarySearch");

if (Validator.isNotNull(primarySearch)) {
	portalPreferences.setValue(PortletKeys.SEARCH, "primary-search", primarySearch);
}
else {
	primarySearch = portalPreferences.getValue(PortletKeys.SEARCH, "primary-search", StringPool.BLANK);
}

long groupId = ParamUtil.getLong(request, "groupId");

String keywords = ParamUtil.getString(request, "keywords");

String format = ParamUtil.getString(request, "format");

List<String> portletTitles = new ArrayList<String>();

PortletURL portletURL = PortletURLUtil.getCurrent(renderRequest, renderResponse);

request.setAttribute("search.jsp-portletURL", portletURL);
request.setAttribute("search.jsp-returnToFullPageURL", portletDisplay.getURLBack());

/* Rivetlogic customization starts  */
String selectedClassName = ParamUtil.getString(request, "entryClassName");
String selectedUserId = ParamUtil.getString(request, "userId");
String selectedCategoryIds = ParamUtil.getString(request, "assetCategoryIds");
String selectedFolderId = ParamUtil.getString(request, "folderId");
String selectedRange = ParamUtil.getString(request, "modified");
/* Rivetlogic customization ends  */
%>

<liferay-portlet:renderURL varImpl="searchURL">
	<portlet:param name="struts_action" value="/search/search" />
</liferay-portlet:renderURL>

<aui:form action="<%= searchURL %>" method="get" name="fm" onSubmit="event.preventDefault();">
	<liferay-portlet:renderURLParams varImpl="searchURL" />
	<aui:input name="<%= SearchContainer.DEFAULT_CUR_PARAM %>" type="hidden" value="<%= ParamUtil.getInteger(request, SearchContainer.DEFAULT_CUR_PARAM, SearchContainer.DEFAULT_CUR) %>" />
	<aui:input name="format" type="hidden" value="<%= format %>" />

	<aui:fieldset id="searchContainer">
		<aui:input autoFocus="<%= windowState.equals(WindowState.MAXIMIZED) %>" inlineField="<%= true %>" label="" name="keywords" size="30" value="<%= HtmlUtil.escape(keywords) %>" />

		<aui:input inlineField="<%= true %>" label="" name="search" src='<%= themeDisplay.getPathThemeImages() + "/common/search.png" %>' title="search" type="image" />

		<aui:input inlineField="<%= true %>" label="" name="clearSearch" src='<%= themeDisplay.getPathThemeImages() + "/common/close.png" %>' title="clear-search" type="image" />
	</aui:fieldset>

	<div class="lfr-token-list" id="<portlet:namespace />searchTokens">
		<div class="lfr-token-list-content" id="<portlet:namespace />searchTokensContent"></div>
	</div>

	<aui:script use="liferay-token-list">
		Liferay.namespace('Search').tokenList = new Liferay.TokenList(
			{
				after: {
					close: function(event) {
						var item = event.item;

						var fieldValues = item.attr('data-fieldValues').split();

						A.Array.each(
							fieldValues,
							function(item, index, collection) {
								var values = item.split('|');

								var field = A.one('#' + values[0]);

								if (field) {
									field.val(values[1]);
								}
							}
						);

						var clearFields = A.all('#' + event.item.attr('data-clearFields').split().join(',#'));

						clearFields.remove();

						if (fieldValues.length || clearFields.size()) {
							submitForm(document.<portlet:namespace />fm);
						}
					}
				},
				boundingBox: '#<portlet:namespace />searchTokens',
				contentBox: '#<portlet:namespace />searchTokensContent'
			}
		).render();
	</aui:script>

	<%@ include file="/html/portlet/search/main_search.jspf" %>

	<c:if test="<%= displayOpenSearchResults %>">
		<liferay-ui:panel collapsible="<%= true %>" cssClass="open-search-panel" extended="<%= true %>" id="searchOpenSearchPanelContainer" persistState="<%= true %>" title="open-search">
			<%@ include file="/html/portlet/search/open_search.jspf" %>
		</liferay-ui:panel>
	</c:if>
</aui:form>

<aui:script use="aui-base">
	A.on(
		'click',
		function(event) {
			var targetId = event.target.get('id');

			if (targetId === '<portlet:namespace />search') {
				<portlet:namespace />search();
			}
			else if (targetId === '<portlet:namespace />clearSearch') {
				<portlet:renderURL copyCurrentRenderParameters="<%= false %>" var="clearSearchURL">
					<portlet:param name="groupId" value="0" />
				</portlet:renderURL>

				window.location.href = '<%= clearSearchURL %>';
			}
		},
		'#<portlet:namespace />searchContainer'
	);

	var searchContainer = A.one('.portlet-search .result .lfr-search-container');

	if (searchContainer) {
		searchContainer.delegate(
			'click',
			function(event) {
				document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value = 1;

				submitForm(document.<portlet:namespace />fm);

				event.preventDefault();
			},
			'.page-links a.first'
		);

		searchContainer.delegate(
			'click',
			function(event) {
				document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value = parseInt(document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value) - 1;

				submitForm(document.<portlet:namespace />fm);

				event.preventDefault();
			},
			'.page-links a.previous'
		);

		searchContainer.delegate(
			'click',
			function(event) {
				document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value = parseInt(document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value) + 1;

				submitForm(document.<portlet:namespace />fm);

				event.preventDefault();
			},
			'.page-links a.next'
		);
	}

	var resultsGrid = A.one('.portlet-search .result .searchcontainer-content');

	if (resultsGrid) {
		resultsGrid.delegate(
			'click',
			function(event) {
				var handle = event.currentTarget;
				var rowTD = handle.ancestor('.table-cell');

				var documentFields = rowTD.one('.asset-entry .asset-entry-fields');

				if (handle.text() == '[+]') {
					documentFields.show();
					handle.text('[-]');
				}
				else if (handle.text() == '[-]') {
					documentFields.hide();
					handle.text('[+]');
				}
			},
			'.table-cell .asset-entry .toggle-details'
		);
	}

	Liferay.provide(
		window,
		'<portlet:namespace />addSearchProvider',
		function() {
			window.external.AddSearchProvider("<%= themeDisplay.getPortalURL() %><%= PortalUtil.getPathMain() %>/search/open_search_description.xml?p_l_id=<%= themeDisplay.getPlid() %>&groupId=<%= groupId %>");
		},
		['aui-base']
	);

	Liferay.provide(
		window,
		'<portlet:namespace />search',
		function() {
			document.<portlet:namespace />fm.<portlet:namespace /><%= SearchContainer.DEFAULT_CUR_PARAM %>.value = 1;

			var keywords = document.<portlet:namespace />fm.<portlet:namespace />keywords.value;

			keywords = keywords.replace(/^\s+|\s+$/, '');

			if (keywords != '') {
				submitForm(document.<portlet:namespace />fm);
			}
		},
		['aui-base']
	);
	
	<!-- RivetLogic customization starts -->
    AUI().use('autocomplete-list','aui-base','liferay-portlet-url','aui-io-request','autocomplete-filters','autocomplete-highlighters',function (A) {
        
        var keywordNode = '*[name=<portlet:namespace />keywords]';
        var responseData;
        var imgUrl = {};

		new A.AutoCompleteList({
			allowBrowserAutocomplete: false,
	        enableCache: false,
	        queryDelay: 0,
			minQueryLength: 3,
			inputNode: keywordNode,
			resultTextLocator:function (result) {
                var suggestion = '';
				var objKeys = Object.keys(result);
                for (i = 1; i < objKeys.length; i++){
                	suggestion = result[objKeys[i]];
                }
                return suggestion;
              },
			render: 'true',
			resultHighlighter: 'subWordMatch',
			resultFormatter: function (query, results) {
                return A.Array.map(results, function (result) {
				switch (result.raw.entryClassName){
					case "com.liferay.portlet.bookmarks.model.BookmarksFolder":
						imgUrl.url = "ratings/star_hover.png"; 
						break;
					case "com.liferay.portlet.documentlibrary.model.DLFileEntry":
						imgUrl.url = "common/clip.png"; 
						break;
					case "com.liferay.portlet.blogs.model.BlogsEntry":
						imgUrl.url = "blogs/blogs.png"; 
						break;
					case "com.liferay.portlet.journal.model.JournalArticle":
						imgUrl.url = "common/history.png"; 
						break;
					case "com.liferay.portlet.bookmarks.model.BookmarksEntry":
						imgUrl.url = "ratings/star_hover.png"; 
						break;
					case "com.liferay.portlet.documentlibrary.model.DLFolder":
						imgUrl.url = "common/folder.png"; 
						break;
					case "com.liferay.portal.model.User":
						imgUrl.url = "common/user_icon.png"; 
						break;
					case "com.liferay.portal.model.Organization":
						imgUrl.url = "common/organization_icon.png"; 
						break;
					case "com.liferay.portlet.messageboards.model.MBMessage":
						imgUrl.url = "common/message.png"; 
						break;
					case "com.liferay.portlet.messageboards.model.MBThread":
						imgUrl.url = "common/message.png";
						break;
					case "com.liferay.portlet.wiki.model.WikiPage":
						imgUrl.url = "common/pages.png"; 
						break;
					default:
						imgUrl.url = "file_system/small/page.png";
						break;
				}
                	var template = '<img src=\"<%=themeDisplay.getPathThemeImages()%>/' + imgUrl.url + '\" />' + ' ' + result.highlighted;
                    return A.Lang.sub(template, result.raw);
                });
            },
            source:function(query, callback){
	            var keyword= A.one(keywordNode).get('value');
	            
		        var resourceURL =Liferay.PortletURL.createResourceURL();
	            resourceURL.setPortletId("elasticsearch_WAR_elasticsearchportlet");
				resourceURL.setResourceId("suggestions");
	            resourceURL.setParameter("searchText", keyword);
	            resourceURL.setParameter("groupId", "<%= groupId %>");
	            resourceURL.setParameter("entryClassName", "<%= selectedClassName %>");
	            resourceURL.setParameter("userId", "<%= selectedUserId %>");
	            resourceURL.setParameter("assetCategoryIds", "<%= selectedCategoryIds %>");
	            resourceURL.setParameter("folderId", "<%= selectedFolderId %>");
	            resourceURL.setParameter("modified", "<%= selectedRange %>");
	            
	            var ioRequest = A.io.request(resourceURL.toString(),{
	                dataType: 'json',
	                method:'POST',
	                autoLoad:false,
	                sync:false,
	                on: {
	                    success:function(){
	                       var data = this.get('responseData');
	                       callback(data);
	                    },
	                    failure:function(){
	                        console.log("Unable to get suggestions.Call failed...");
	                    }
	                }
	            });   
	            ioRequest.start();
	        },
	     });
    });
    AUI().use('event', 'node', function(A) {
   		A.one('#<portlet:namespace />keywords').on('keyup',function(event){
   		    if(event.keyCode==13){
   		    	<portlet:namespace />search();
   		    }
			A.all('.yui3-aclist-item').on('click', function(event){
					console.log(event.target._data.result.text)
					document.<portlet:namespace />fm.<portlet:namespace />keywords.value = event.target._data.result.text;
  				<portlet:namespace />search();
			});
   		});
	});
    <!-- RivetLogic customization ends -->
</aui:script>

<%
String pageSubtitle = LanguageUtil.get(pageContext, "search-results");
String pageDescription = LanguageUtil.get(pageContext, "search-results");
String pageKeywords = LanguageUtil.get(pageContext, "search");

if (!portletTitles.isEmpty()) {
	pageDescription = LanguageUtil.get(pageContext, "searched") + StringPool.SPACE + StringUtil.merge(portletTitles, StringPool.COMMA_AND_SPACE);
}

if (Validator.isNotNull(keywords)) {
	pageKeywords = keywords;

	if (StringUtil.startsWith(pageKeywords, Field.ASSET_TAG_NAMES + StringPool.COLON)) {
		pageKeywords = StringUtil.replace(pageKeywords, Field.ASSET_TAG_NAMES + StringPool.COLON, StringPool.BLANK);
	}
}

PortalUtil.setPageSubtitle(pageSubtitle, request);
PortalUtil.setPageDescription(pageDescription, request);
PortalUtil.setPageKeywords(pageKeywords, request);
%>

<%!
private static Log _log = LogFactoryUtil.getLog("portal-web.docroot.html.portlet.search.search_jsp");
%>
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

<%@ include file="/html/taglib/ui/search/init.jsp" %>

<%
long groupId = ParamUtil.getLong(request, namespace + "groupId");

Group group = themeDisplay.getScopeGroup();

String keywords = ParamUtil.getString(request, namespace + "keywords");

PortletURL portletURL = null;

if (portletResponse != null) {
	LiferayPortletResponse liferayPortletResponse = (LiferayPortletResponse)portletResponse;

	portletURL = liferayPortletResponse.createLiferayPortletURL(PortletKeys.SEARCH, PortletRequest.RENDER_PHASE);
}
else {
	portletURL = new PortletURLImpl(request, PortletKeys.SEARCH, plid, PortletRequest.RENDER_PHASE);
}

portletURL.setParameter("struts_action", "/search/search");
portletURL.setParameter("redirect", currentURL);
portletURL.setPortletMode(PortletMode.VIEW);
portletURL.setWindowState(WindowState.MAXIMIZED);

pageContext.setAttribute("portletURL", portletURL);
%>

<form action="<%= HtmlUtil.escapeAttribute(portletURL.toString()) %>" method="get" name="<%= randomNamespace %><%= namespace %>fm" onSubmit="<%= randomNamespace %><%= namespace %>search(); return false;">
<liferay-portlet:renderURLParams varImpl="portletURL" />

<input name="<%= namespace %>keywords" id="<%= namespace %>keywords" size="30" title="<liferay-ui:message key="search" />" type="text" value="<%= HtmlUtil.escapeAttribute(keywords) %>" onSubmit="<%= randomNamespace %><%= namespace %>search(); return false;"/>

<select name="<%= namespace %>groupId" title="<liferay-ui:message key="scope" /> ">
	<option value="0" <%= (groupId == 0) ? "selected" : "" %>><liferay-ui:message key="everything" /></option>
	<option value="<%= group.getGroupId() %>" <%= (groupId != 0) ? "selected" : "" %>><liferay-ui:message key='<%= "this-" + (group.isOrganization() ? "organization" : "site") %>' /></option>
</select>

<input align="absmiddle" border="0" src="<%= themeDisplay.getPathThemeImages() %>/common/search.png" title="<liferay-ui:message key="search" />" type="image" />

<aui:script>
	function <%= randomNamespace %><%= namespace %>search() {
		var keywords = document.<%= randomNamespace %><%= namespace %>fm.<%= namespace %>keywords.value;
		

		keywords = keywords.replace(/^\s+|\s+$/, '');
		console.log(keywords);
		if (keywords != '') {
			submitForm(document.<%= randomNamespace %><%= namespace %>fm);
		}
	}
	
	<!-- RivetLogic customization starts -->
	AUI().use('autocomplete-list','aui-base','liferay-portlet-url','aui-io-request','autocomplete-filters','autocomplete-highlighters',function (A) {
        
        var keywordNode = '*[name=<%= namespace %>keywords]';
        var groupIdNode = '*[name=<%= namespace %>groupId]';
        var responseData;
		var imgUrl = {};

		new A.AutoCompleteList({
			allowBrowserAutocomplete: false,
	        queryDelay: 0.1,
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
	            var keyword = A.one(keywordNode).get('value');
	            var scopeGroupId = A.one(groupIdNode).get('value');
	            
		        var resourceURL = Liferay.PortletURL.createResourceURL();
	            resourceURL.setPortletId("elasticsearch_WAR_elasticsearchportlet");
				resourceURL.setResourceId("suggestions");
	            resourceURL.setParameter("searchText",keyword);
                resourceURL.setParameter("groupId", scopeGroupId);	            
	            
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
   		A.one('#<%= namespace %>keywords').on('keyup',function(event){
   		    if(event.keyCode==13){
   		    	<%= randomNamespace %><%= namespace %>search();
   		    }
			A.all('.yui3-aclist-item').on('click', function(event){
					console.log(event.target._data.result.text)
					document.<%= randomNamespace %><%= namespace %>fm.<%= namespace %>keywords.value = event.target._data.result.text;
  				<%= randomNamespace %><%= namespace %>search();
			});
   		});
	});
	<!-- RivetLogic customization ends -->
</aui:script>
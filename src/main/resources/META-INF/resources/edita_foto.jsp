<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="com.liferay.portal.kernel.util.PropsKeys"%>
<%@page import="com.liferay.portal.kernel.util.PrefsPropsUtil"%>
<%@page import="com.liferay.portal.kernel.upload.UploadException"%>
<%@page import="com.liferay.portal.kernel.upload.UploadRequestSizeException"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<portlet:defineObjects />
<liferay-theme:defineObjects />

<%

long selUserId = ParamUtil.getLong(request, "selUserId");
com.liferay.portal.kernel.model.User selUser = com.liferay.portal.kernel.service.UserLocalServiceUtil.getUserById(selUserId);
%>

<portlet:renderURL var="redirectEditaUsuarioURL" >
	<portlet:param name="jspPage" value="/edita_ramal.jsp" />
	<portlet:param name="nomeUsuario" value="<%= selUser.getScreenName() %>"></portlet:param>
</portlet:renderURL>
<portlet:actionURL var="editUserPortraitURL" name="editUserPortrait">
	<portlet:param name="redirect" value="<%= redirectEditaUsuarioURL %>"/>
	<portlet:param name="nomeUsuario" value="<%= selUser.getScreenName() %>"></portlet:param>
</portlet:actionURL>

	<aui:form action="<%= editUserPortraitURL %>" enctype="multipart/form-data" method="post" name="fm">
		
		<aui:input name="p_u_i_d" type="hidden" value="<%= selUserId %>" />
		<liferay-ui:success key="msg-atualizacao-foto-com-sucesso" message="Altera��o de Imagem realizada com sucesso!" />
		<liferay-ui:error exception="<%= UploadException.class %>" message="Um erro inesperado ocorreu durante o upload do seu arquivo." />
		<liferay-ui:error exception="<%= UploadRequestSizeException.class %>" message="A imagem n�o pode ultrapassar as seguintes dimens�es: 120px x 100px.">
		
			<%
			long imageMaxSize = PrefsPropsUtil.getLong(PropsKeys.USERS_IMAGE_MAX_SIZE) / 1024;
			%>
    		<liferay-ui:message arguments="<%= imageMaxSize %>" key="please-enter-a-file-with-a-valid-file-size-no-larger-than-x" />
		</liferay-ui:error>
	
	
		<aui:fieldset>
			<aui:input label='<%= LanguageUtil.format( com.liferay.portal.kernel.util.LocaleUtil.getDefault() , "upload-a-gif-or-jpeg-that-is-x-pixels-tall-and-x-pixels-wide", new Object[] {"300", "300"}, false) %>' name="imagem" size="500" type="file" />
			
			<aui:button-row>
				<button class="btn " type="submit"> Salvar </button> 
				<button class="btn btn-danger" onclick="window.history.back();" type="button"> Cancelar </button>
			</aui:button-row>
		</aui:fieldset>
	</aui:form>
<script>
	var portletNameSpaceRamalFacil = '<portlet:namespace/>';
</script>
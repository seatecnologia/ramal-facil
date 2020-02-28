<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@page import="com.liferay.portal.kernel.util.PropsKeys"%>
<%@page import="com.liferay.portal.kernel.util.PrefsPropsUtil"%>
<%@page import="com.liferay.portal.kernel.upload.UploadException"%>
<%@page import="com.liferay.portal.kernel.upload.UploadRequestSizeException"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@page import="java.io.Serializable"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="javax.portlet.PortletRequest"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="com.liferay.portal.kernel.servlet.SessionErrors"%>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>

<portlet:defineObjects />
<liferay-theme:defineObjects />


<%
	String screenName = ParamUtil.getString(request, "nomeUsuario", "");
    com.liferay.portal.kernel.model.User selUser = null;
	String ramal = "";
	String area = "";

		try {
			selUser = com.liferay.portal.kernel.service.UserLocalServiceUtil.getUserByScreenName(themeDisplay.getCompanyId(), screenName);
			
			if (screenName != "") {
				ramal = com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil.getValue(selUser.getCompanyId(), com.liferay.portal.kernel.model.User.class.getName(), "CUSTOM_FIELDS", "ramal", selUser.getPrimaryKey()).getData(); 
				area = com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil.getValue(selUser.getCompanyId(), com.liferay.portal.kernel.model.User.class.getName(), "CUSTOM_FIELDS", "area", selUser.getPrimaryKey()).getData(); 				
			}
		} catch(com.liferay.portal.kernel.exception.NoSuchUserException e){
		}
%>

	<c:choose>
		<c:when test="<%= selUser!= null %>">
			<c:if test='<%= area != "" %>'>	
				<portlet:actionURL name="atualizarRamalUsuario" var="atualizarRamalUsuarioURL" />
				<portlet:actionURL name="sincronizarUsuarioLdap" var="sincronizarUsuarioLdapURL" />
				
				<liferay-ui:success key="msg-atualizacao-foto-com-sucesso" message="Altera��o de Imagem realizada com sucesso!" />
				<liferay-ui:success key="msg-atualizacao-efetuada-com-sucesso" message="A atualiza��o foi realizada com sucesso! " />
				<liferay-ui:success key="user-desativado" message="O Usu�rio foi DESATIVADO no Portal" />
				<liferay-ui:success key="user-ativado" message="O Usu�rio foi ATIVADO no Portal" />
				<liferay-ui:success key="user-normal" message="Usu�rio sem Altera��es no LDAP" />
				<liferay-ui:success key="user-excluido" message="O Usu�rio foi EXCLU�DO do Portal" />
				
				<liferay-ui:error key="msg-erro-atualizar-ramal" message="A atualiza��o n�o pode ser realizada!" />
		        <liferay-ui:error exception="<%= UploadException.class %>" message="Um erro inesperado ocorreu durante o upload do seu arquivo." />
		        <liferay-ui:error exception="<%= UploadRequestSizeException.class %>" message="A imagem n�o pode ultrapassar as seguintes dimens�es: 120px x 100px.">
		        
		            <%
		            long imageMaxSize = PrefsPropsUtil.getLong(PropsKeys.USERS_IMAGE_MAX_SIZE) / 1024;
		            %>
		            <liferay-ui:message arguments="<%= imageMaxSize %>" key="please-enter-a-file-with-a-valid-file-size-no-larger-than-x" />
		            Por favor, insira um arquivo com um tamanho de arquivo v�lido n�o maior que <%= imageMaxSize %>.
		        </liferay-ui:error>
		    
				
				<c:if test='${fn:length(user.expandoBridge.getAttribute("area")) > 0}'>
					<div class="aui-field aui-field-wrapper">
						<div class="aui-field-wrapper-content">
							<label class="aui-field-label"><liferay-ui:message key="area" /></label>
							<span id="pqai_area"><%= area %></span>
						</div>
					</div>
				</c:if>
				<div class="row-fluid">
					<aui:form method="post" name="formAtualizar">
						
							<div class="span12 text-center">
								<aui:input name="redirect" type="hidden" value="<%= themeDisplay.getURLCurrent() %>"/>
								<aui:input name="selUserId" type="hidden" value="<%= String.valueOf(selUser.getUserId()) %>"/>
								<aui:input name="nomeUsuario" type="hidden" value="<%= String.valueOf(selUser.getScreenName()) %>"/>
								
								<portlet:renderURL var="alterarFotoURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>" >
									<portlet:param name="jspPage" value="/edita_foto.jsp"/>
									<portlet:param name="selUserId" value="<%= String.valueOf(selUser.getUserId()) %>"/>
								</portlet:renderURL>
								
								<img src="<%= selUser.getPortraitURL(themeDisplay) %>">
							</div>
							<div class="span12 text-center">
								<aui:a href="<%= alterarFotoURL %>" cssClass="btn">Alterar Imagem</aui:a>
							</div>
							
							<h5 class="text-center"><c:out value="<%= selUser.getFullName() %>" /></h5>
							
							<div class="span12 text-center">
								<aui:input name="ramal" value="<%= ramal %>" label="ramal" inputCssClass="ramal" cssClass="inputSearch" disabled="true" />
							</div>
							
							<div class="span12 text-center">
								<aui:button type="button" value="sincronizar-ad"  onClick="submitForm(1)" />
							</div>
						
						
					</aui:form>
				</div>
				
				<script type="text/javascript" charset="utf-8">
				 	var A=AUI();
				 	
				</script>
			</c:if>
			<c:if test='<%= area.isEmpty() %>'>
				<div class="portlet-msg-error">
					O usu�rio n�o est� habilitado a estar no Ramal F�cil
				</div>
			</c:if>
		</c:when>
		<c:otherwise>
			<div class="portlet-msg-error">
					Usu�rio n�o encontrado!
			</div>
		</c:otherwise>
	</c:choose>

<script>
	var portletNameSpaceRamalFacil = '<portlet:namespace/>';
	//window.parent.AUI().one('#classificadosIframeDialog .btn.close').on('click',function(){window.parent.location.reload()})	
	
	
</script>

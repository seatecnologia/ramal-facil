<%@ include file="/init.jsp" %>

<%@page import="java.io.Serializable" %>
<%@page import="com.liferay.portal.kernel.util.HtmlUtil" %>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ page import="com.liferay.portal.kernel.language.LanguageUtil" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %>


<script type="text/javascript">

AUI().ready(function(A){


	A.all('#'+portletNameSpaceRamalFacil+'btnMeuRamal').each(function(node){
		node.on('click',function(node){
			modalRamalFacil({titulo:'Meu Ramal',url:urlMyAccount});
		});
	});
	
	A.all('.'+portletNameSpaceRamalFacil+'btnGerenciarUsuario').each(function(node){
		node.on('click',function(btn){
			modalRamalFacil({
				titulo:	btn.currentTarget._node.attributes['data-title'].value,
				url:	urlGerenciarUsuario + btn.currentTarget._node.attributes['data-login'].value
			});
		});
	});
	
	
});

function modalRamalFacil(param){
	Liferay.Util.openWindow(
			{
				cache: false,	
				dialog: {
					align: Liferay.Util.Window.ALIGN_CENTER,
					modal:true,
					width:450,
					height:530,
					on:{
						close: function(){
							window.location.reload();
						}
					}
				},
				id: 'classificadosIframeDialog',
				title: param.titulo,
				uri: param.url
				
			}
		);
}
</script>

<portlet:actionURL name="pesquisarUsuario" var="pesquisarRamalUrl"></portlet:actionURL>
<portlet:actionURL name="pesquisaGerenciar" var="pesquisaGerenciarUrl"></portlet:actionURL>

<liferay-ui:error key="erro-pesquisa" message="Deve-se informar ao menos um dos campos da pesquisa."></liferay-ui:error>
<liferay-ui:error key="erro-nome-usuario" message="Nesta Consulta, informe o login do usu�rio."></liferay-ui:error>

<portlet:actionURL name="pesquisarUsuario" var="pesquisarRamalUrl"></portlet:actionURL>
<portlet:renderURL var="urlMyAccount" windowState="<%= LiferayWindowState.POP_UP.toString() %>" >
	<portlet:param name="jspPage" value="/edita_ramal.jsp"/>
</portlet:renderURL>

<portlet:renderURL var="gerenciarRamalFacil" windowState="<%= LiferayWindowState.POP_UP.toString() %>" >
	<portlet:param name="jspPage" value="/edita_ramal.jsp"/>
</portlet:renderURL>


<div class="span12">
	<form class="form" method="post" action="<%= pesquisarRamalUrl %>" name="<portlet:namespace/>pesquisaRamalFacil" > 
		<div class="row-fluid">
			<div class="span4 form-group">
				<label>Área</label>
				<select class="form-control span12" name="<portlet:namespace/>area" id="<portlet:namespace/>area">
					<option value="">-- selecione --</option>
					<c:forEach items="${areas}" var="area">
						<option value="${area}">${area}</option>
					</c:forEach>
				</select>
			</div>
			<div class="span4 form-group">
				<label>Nome</label>
				<input type="text" class="form-control span12" name="<portlet:namespace/>nome" id="<portlet:namespace/>nome"/>
			</div>
			<div class="span4 form-group">
				<label>Ramal</label>
				<input type="number" min="1" max="9999" class="form-control span12" name="<portlet:namespace/>ramal" id="<portlet:namespace/>ramal"/>
			</div>
			
		</div>
		<div class="row-fluid">
			<div class="span12"> 
				<c:if test="<%= permissionChecker.isSignedIn() %>">
					<button type="button" id="<portlet:namespace/>btnMeuRamal" class="btn btn-primary"><i class="icon-edit"></i> Meu Perfil</button>
				</c:if>
				
				<button type="submit" id="<portlet:namespace/>btnPesquar" class="btn btn-default"><i class="icon-search"></i> Pesquisar</button>
			</div>
		</div>
	</form>
</div>
<hr>
<div class="row-fluid">
	<div class="span12">
		<c:if test="${areasUsuarios ne null}">
			<c:choose>
				<c:when test="${fn:length(users) > 0}">
					<c:forEach items="${areasUsuarios}" var="Area">
						<div class="row-fluid">
							<h3>${Area.getNome()}</h3>
							<c:forEach items="${Area.getUsers()}" var="userArea">
								<div class="asset-abstract">
									<div alt="${userArea.fullName}" title="${userArea.fullName}" class="span5 img" style="background-image: url('${userPortraits[userArea.userId]}')">
										
									</div>
									<div class="span7" >
										<h5 class="asset-title">
										<c:choose>
											<c:when test="${permissaoGerenciar}">
											<a href="javascript:void(0)" class="<portlet:namespace/>btnGerenciarUsuario" alt="${userArea.fullName}" title="${userArea.fullName}"
												data-title="${userArea.fullName}" 
												data-login="${userArea.screenName}"><i class="icon-edit"></i> ${userArea.fullName}</a>
											</c:when>
											<c:otherwise>
												<a href="javascript:void()">${userArea.fullName}</a>
											</c:otherwise>
										</c:choose>
										
										</h5>
									
										<div class="input-prepend input-append" style="margin-bottom:0px">
											<span class="add-on" onclick="window.location.href='mailto:${userArea.emailAddress}'" style="cursor:pointer" alt="${userArea.emailAddress}" title="${userArea.emailAddress}">
												<i class="icon-envelope"></i>
											</span>
											<c:if test='${fn:length(userArea.expandoBridge.getAttribute("ramal")) > 0}'>
												<span class="add-on"><i class="icon-phone"></i></span>
							              		<span class="add-on">
							              			${userArea.expandoBridge.getAttribute("ramal")}
							              			${ userArea.expandoBridge.getAttribute("area") }
							              			
							              		</span>
											</c:if>
											
										</div>
									</div>
								</div>
							</c:forEach>
						</div>
					</c:forEach>
				</c:when>
				<c:otherwise>
					Nenhum resultado encontrado.
				</c:otherwise>
			</c:choose>
		</c:if>
	</div>
</div>


<script>
	var portletNameSpaceRamalFacil = '<portlet:namespace/>';
	
	console.log('user: '+'<%= String.valueOf(user.getScreenName()) %>');
	console.log('urlMyAccount: '+'<%= urlMyAccount %>');
	console.log('gerenciarRamalFacil: '+'<%= gerenciarRamalFacil %>');
	
	var portletNameSpaceRamalFacil = '<portlet:namespace/>';
	var urlMyAccount = Liferay.Util.addParams('<portlet:namespace/>nomeUsuario='+'<%= String.valueOf(user.getScreenName()) %>', '<%= urlMyAccount %>');
	var urlGerenciarUsuario = Liferay.Util.addParams('<portlet:namespace/>nomeUsuario=', '<%= gerenciarRamalFacil %>');	

</script>


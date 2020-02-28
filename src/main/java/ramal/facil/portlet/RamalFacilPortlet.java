package ramal.facil.portlet;

import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.search.BooleanQuery;
import com.liferay.portal.kernel.search.BooleanQueryFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.IndexSearcher;
import com.liferay.portal.kernel.search.ParseException;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchContextFactory;
import com.liferay.portal.kernel.search.SearchEngine;
import com.liferay.portal.kernel.search.SearchEngineHelperUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserServiceUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadException;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;

import ramal.facil.constants.RamalFacilPortletKeys;
import ramal.facil.model.Area;
//import util.PortalLDAPUtil;

@SuppressWarnings("deprecation")
/**
 * @author ee
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=RamalFacil",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + RamalFacilPortletKeys.RAMALFACIL,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class RamalFacilPortlet extends MVCPortlet {
	
	private Log console = LogFactoryUtil.getLog(RamalFacilPortlet.class.getName());
	
	private static final String[] ALLOWED_USER_ACCOUNT_CONTROL = new String[] { "512", "544", "66048", "262656" }; // Valores
	public static final String AREA_IN_EXPANDO = "area";
	public static final String FIELD_AREA = "expando/custom_fields/area";
	public static final String FIELD_NAME = "fullName";
	public static final String FIELD_RAMAL = "expando/custom_fields/ramal";
	private static final String NOME_USUARIO = "nomeUsuario";
	public static final String RAMAL_IN_EXPANDO = "ramal";
	public static final String SCREEN_NAME = "screenName";

	
	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws PortletException
	 */
	public void atualizarRamalUsuario(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException {
		Long selUserId = ParamUtil.getLong(actionRequest, "selUserId");
		try {
			User user = UserLocalServiceUtil.getUser(selUserId);
			UserLocalServiceUtil.updateUser(user); // Atualiza o usu�rio, para aparecer na pesquisa por ramal

		} catch (SystemException | PortalException e) {// NOSONAR
			SessionErrors.add(actionRequest, "msg-erro-atualizar-ramal");
			console.error(e, e);
		}
		sendRedirect(actionRequest, actionResponse);
		SessionMessages.add(actionRequest, "msg-atualizacao-efetuada-com-sucesso");
	}
	
	private BooleanQuery createQueryGerenciar(String screenName) {
		SearchContext searchContext = new SearchContext();
		searchContext.setSearchEngineId(SearchEngineUtil.getDefaultSearchEngineId());

		BooleanQuery busca = BooleanQueryFactoryUtil.create(searchContext);
		busca.addRequiredTerm(Field.ENTRY_CLASS_NAME, User.class.getName());

		if (screenName != null && screenName.length() > 0) {
			busca.addRequiredTerm(SCREEN_NAME, screenName);
		}

		busca.addRequiredTerm(Field.STATUS, 0);

		return busca;

	}
	
	private Sort createSortAttributes() {
		Sort sort = new Sort();
		sort.setFieldName("firstName");
		sort.setType(Sort.STRING_TYPE);
		sort.setReverse(false);
		return sort;
	}




	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {
		// Popula o selectBox das ï¿½reas dos usuï¿½rios
		List<String> areas = getAreas(renderRequest);
		renderRequest.setAttribute("areas", areas);
		// Verifica permissao do usuario atual para ver o "gerenciar"
		permissaoGerenciar(renderRequest);
		super.doView(renderRequest, renderResponse);
	}
	
	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws Exception
	 */
	public void editUserPortrait(ActionRequest actionRequest, ActionResponse actionResponse)
			throws IOException, PortalException {

		UploadPortletRequest uploadPortletRequest = PortalUtil.getUploadPortletRequest(actionRequest);
		String screenName = ParamUtil.getString(actionRequest, NOME_USUARIO);

		// Captura o Id do usu�rio pesquisado para edi��o pelo request do portlet
		String redirect = ParamUtil.getString(actionRequest, "redirect");
		long selUserId = ParamUtil.getLong(uploadPortletRequest, "p_u_i_d");

		InputStream inputStream = uploadPortletRequest.getFileAsStream("imagem");

		if (inputStream == null) {
			throw new UploadException();
		}
		byte[] bytes = FileUtil.getBytes(inputStream);

		try {
			UserServiceUtil.updatePortrait(selUserId, bytes);
			SessionMessages.add(actionRequest, "msg-atualizacao-foto-com-sucesso");
			actionResponse.sendRedirect(redirect);

		} catch (PortalException | SystemException e) {
				SessionErrors.add(actionRequest, e.getClass().getName());
				actionResponse.setRenderParameter("jspPage", "/edita_ramal.jsp");
				actionResponse.setRenderParameter("p_u_i_d", String.valueOf(selUserId));
				actionResponse.setRenderParameter("selUserId", String.valueOf(selUserId));
				actionResponse.setRenderParameter(NOME_USUARIO, screenName);
		}
	}

	
	private List<String> getAreas(PortletRequest request)  {
		List<String> areas = new ArrayList<>();
		try {
			ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			List<ExpandoValue> areasTmp = ExpandoValueLocalServiceUtil.getDefaultTableColumnValues(
					themeDisplay.getCompanyId(), User.class.getName(), "area", QueryUtil.ALL_POS, QueryUtil.ALL_POS);
			for (ExpandoValue expandoValue : areasTmp) {
				if(expandoValue.getData() != null) {
					String area = expandoValue.getData();
					if (!area.isEmpty()) {
						areas.add(area);
					}
				}
			}
		} catch (SystemException e) {
			console.error(e, e);
		}
		Set<String> resultado = new HashSet<>(areas);
		return ListUtil.sort(ListUtil.fromCollection(resultado));
	}
	
	private Map<Long, String> pesquisaRamal(ActionRequest actionRequest, ThemeDisplay themeDisplay, String area,
			String nome, String ramal) {
		Map<Long, String> userPortraits = new HashMap<>();
		try {
			List<User> users = new ArrayList<>();
			List<Area> listAreas = pegarAreas(actionRequest);
			pegarInfoUsuario(themeDisplay, area, nome, ramal, userPortraits, users, listAreas);
			List<Area> listAreasFinal = new ArrayList<>();
			for (Area area_fim : listAreas) {
				for (User tempUser : users) {
					String tempUserArea = ExpandoValueLocalServiceUtil.getValue(tempUser.getCompanyId(), User.class.getName(), "CUSTOM_FIELDS", "area", tempUser.getPrimaryKey()).getData(); 
					if (tempUserArea.contains(area_fim.getNome())) {
						if(!area_fim.getUsers().contains(tempUser)) {
							area_fim.addUser(tempUser);							
						}
						if(!listAreasFinal.contains(area_fim)) {
							listAreasFinal.add(area_fim);							
						}
					}
				}
			}
			actionRequest.setAttribute("areasUsuarios", listAreasFinal);
			actionRequest.setAttribute("users", users);
			actionRequest.setAttribute("area", area);
			actionRequest.setAttribute("nome", nome);
			actionRequest.setAttribute(RAMAL_IN_EXPANDO, ramal);
		} catch (SystemException | PortalException e) {
			console.error(e, e);
		}
		return userPortraits;
	}
	
	/**
	 * @param actionRequest
	 * @param actionResponse
	 * @throws IOException
	 * @throws PortletException
	 * @throws ParseException
	 */
	public void pesquisarUsuario(ActionRequest actionRequest, ActionResponse actionResponse) {
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

		// Recupera os dados do formul�rio
		String area = ParamUtil.getString(actionRequest, "area");
		String nome = ParamUtil.getString(actionRequest, "nome");
		String ramal = ParamUtil.getString(actionRequest, RAMAL_IN_EXPANDO);

		// Retira espa�os e verifica se o campo � nulo
		area = (area == null) ? "" : area.trim();
		nome = (nome == null) ? "" : nome.trim();
		ramal = (ramal == null) ? "" : ramal.trim();

		// Valida se pelo menos algum dos campos foi informado
		if (area.length() <= 0 && nome.length() <= 0 && ramal.length() <= 0) {
			SessionErrors.add(actionRequest, "erro-pesquisa");
			return;
		}

		Map<Long, String> userPortraits = pesquisaRamal(actionRequest, themeDisplay, area, nome, ramal);
		// Copia os par�metros de volta pro request para manter os campos pesquisados
		PortalUtil.copyRequestParameters(actionRequest, actionResponse);
		// Coloca a lista de usu�rios pesquisados e suas imagens no request
		actionRequest.setAttribute("userPortraits", userPortraits);
	}
	
	/**
	 * @param actionRequest
	 * @return
	 */
	private List<Area> pegarAreas(ActionRequest actionRequest) {
		List<Area> listAreas = new ArrayList<>();
		for (String Nomearea_ : getAreas(actionRequest)) {
			Area ar = new Area();
			ar.setNome(Nomearea_);
			ar.setUsers(new ArrayList<User>());
			listAreas.add(ar);
		}
		return listAreas;
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortletException
	 */
//	public void sincronizarUsuarioLdap(ActionRequest request, ActionResponse response) throws IOException {
//
//		Long companyId = PortalUtil.getCompanyId(request);
//		String screenName = ParamUtil.getString(request, NOME_USUARIO);
//		try {
//			User selUser = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
//			Long ldapServerId = PortalLDAPUtil.getLdapServerId(companyId, screenName);
//			SearchResult result = (SearchResult) PortalLDAPUtil.getUser(ldapServerId, companyId, screenName);
//			if (result != null) {
//				String userAccountControl = LDAPUtil.getAttributeString(result.getAttributes(), "userAccountControl");
//				if (!ArrayUtil.contains(ALLOWED_USER_ACCOUNT_CONTROL, userAccountControl)) {
//					selUser.setStatus(WorkflowConstants.STATUS_INACTIVE);
//					UserLocalServiceUtil.updateUser(selUser);
//					SessionMessages.add(request, "user-desativado");
//				} else if (!selUser.isActive()) {
//					selUser.setStatus(WorkflowConstants.STATUS_APPROVED);
//					UserLocalServiceUtil.updateUser(selUser);
//					SessionMessages.add(request, "user-ativado");
//				}
//				PortalLDAPImporterUtil.importLDAPUserByScreenName(companyId, screenName);
//				SessionMessages.add(request, "user-normal");
//			} else {
//				UserLocalServiceUtil.deleteUser(selUser);
//				SessionMessages.add(request, "user-excluido");
//			}
//		} catch (Exception e) {// NOSONAR
//			console.error(e, e);
//		}
//		sendRedirect(request, response);
//	}
	
	/*
	 * METODOS UTILIT�RIOS
	 */

	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws PortletException
	 */
	public void pesquisaGerenciar(ActionRequest request, ActionResponse response) {

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		String area = "";
		String screenName = ParamUtil.getString(request, NOME_USUARIO);
		request.setAttribute(NOME_USUARIO, screenName);

		screenName = (screenName == null) ? "" : screenName.trim();

		if (screenName.isEmpty()) {
			SessionErrors.add(request, "erro-nome-usuario");
			return;
		} else if (screenName.length() <= 0) {
			SessionErrors.add(request, "erro-pesquisa");
			return;
		}

		BooleanQuery busca = createQueryGerenciar(screenName);
		List<User> users = new ArrayList<>();
		Map<Long, String> userPortraits = new HashMap<>();

		try {
			HttpServletRequest httpReq = PortalUtil.getHttpServletRequest(request);
			SearchContext searchContext = SearchContextFactory.getInstance(httpReq);
			SearchEngine searchEngine = SearchEngineHelperUtil.getSearchEngine(searchContext.getSearchEngineId());
			IndexSearcher indexSearcher = searchEngine.getIndexSearcher();

			Hits hits = indexSearcher.search(searchContext, busca);
			List<Document> hitsList = hits.toList();

			for (Document hit : hitsList) {

				User user = UserLocalServiceUtil.getUser(GetterUtil.getLong(hit.get(Field.ENTRY_CLASS_PK)));
				String userName = user.getScreenName().trim();

				if (userName != null && userName.length() > 0) {
					userPortraits.put(user.getUserId(), user.getPortraitURL(themeDisplay));
					users.add(user);
				}
				String expandoValue = ExpandoValueLocalServiceUtil.getValue(user.getCompanyId(), User.class.getName(), "CUSTOM_FIELDS", "area", user.getPrimaryKey()).getData(); 

				if (expandoValue != null && expandoValue.length() > 0) {
					userPortraits.put(user.getUserId(), user.getPortraitURL(themeDisplay));
					users.add(user);
				}

				area = expandoValue;

				if (area!=null&&area.equals("")) {
					SessionErrors.add(request, "erro-area-usuario");
					return;
				}
			}
		} catch (SystemException | PortalException e) {
			console.error(e, e);
		}

		PortalUtil.copyRequestParameters(request, response);

		// Coloca a lista de usu�rios pesquisados e suas imagens no request
		request.setAttribute("users", users);
		request.setAttribute("userPortraits", userPortraits);
	}
	
	private boolean permissaoGerenciar(PortletRequest request) {

		boolean permissaoGerenciar = false;

		ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
		try {
			long groupID = themeDisplay.getScopeGroupId();
			User user = UserLocalServiceUtil.getUser(themeDisplay.getUserId());
			permissaoGerenciar = canAccessLock(groupID,user , themeDisplay.getPortletDisplay().getId());
		}catch (Exception e) {
		}
		
		request.setAttribute("permissaoGerenciar", permissaoGerenciar);
		return permissaoGerenciar;
	}
	
	
	public boolean canAccessLock(long groupCreatedId, User user, String plId){
		if(PortalUtil.isOmniadmin(user.getUserId())){
			return true;
		}

		PermissionChecker permissionChecker = null;
		try {
			permissionChecker = PermissionCheckerFactoryUtil.create(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		if(permissionChecker != null && permissionChecker.hasPermission(groupCreatedId, plId,groupCreatedId,"ACCESSLOCK")){
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param themeDisplay
	 * @param area
	 * @param nome
	 * @param ramal
	 * @param userPortraits
	 * @param users
	 * @param listAreas
	 * @throws PortalException
	 * @throws SystemException
	 */
	private void pegarInfoUsuario(ThemeDisplay themeDisplay, String area, String nome, String ramal,
			Map<Long, String> userPortraits, List<User> users, List<Area> listAreas)
			throws PortalException, SystemException {
		List<User> usuarios = getUsuarios(area, nome, ramal);
		for (User usuario : usuarios) {
			User user = UserLocalServiceUtil.getUser(usuario.getUserId());
			String expandoValue = ExpandoValueLocalServiceUtil.getValue(user.getCompanyId(), User.class.getName(), "CUSTOM_FIELDS", "area", user.getPrimaryKey()).getData() != null
					? ExpandoValueLocalServiceUtil.getValue(user.getCompanyId(), User.class.getName(), "CUSTOM_FIELDS", "area", user.getPrimaryKey()).getData() 
					: "";

			if (expandoValue != null && expandoValue.length() > 0
					&& (area.length() <= 0 || (area.length() > 0 && expandoValue.equals(area)))) {
				userPortraits.put(user.getUserId(), user.getPortraitURL(themeDisplay));
				users.add(user);
			}
			for (Area ar : listAreas) {
				if (expandoValue != null && ar.getNome().trim().equals(expandoValue.trim())) {
					ar.getUsers().add(user);
				}
			}
		}
	}
	

	/**
	 * Metodo que retorna todos os usuarios do liferay ou pela area ou pelo nome ou
	 * pelo ramal.
	 *
	 * @param area  - paramentro de Area do usuario.
	 * @param nome  - paramentro do nome do usuario.
	 * @param ramal - paramento do ramalo do usuario.
	 * @return List<User> - retorna a lista de ususario.
	 */
	public List<User> getUsuarios(String area, String nome, String ramal) {
		List<User> usuarios = new ArrayList<>();
		ClassLoader cl = PortalClassLoaderUtil.getClassLoader();

		try {
			DynamicQuery queryExpandoValue = DynamicQueryFactoryUtil.forClass(ExpandoValue.class, "expandovalue", cl);
			queryExpandoValue.setProjection(ProjectionFactoryUtil.property("classPK"))
					.add(PropertyFactoryUtil.forName("expandovalue.classPK").eqProperty("user.userId"));

			if (area != null && area.length() > 0) {
				queryExpandoValue.add(RestrictionsFactoryUtil.eq("expandovalue.data", area.trim()));
			}
			if (ramal != null && ramal.length() > 0) {
				queryExpandoValue.add(RestrictionsFactoryUtil.eq("expandovalue.data", ramal.trim()));
			}

			DynamicQuery queryUsuario = DynamicQueryFactoryUtil.forClass(User.class, "user", cl);
			queryUsuario.add(PropertyFactoryUtil.forName("user.userId").in(queryExpandoValue));

			if (nome != null && nome.length() > 0) {
				Criterion criterion = RestrictionsFactoryUtil.like("user.screenName", "%" + nome.trim() + "%");
				String[] nomeTmp = nome.split(" ");
				for (int i = 0; i < nome.split(" ").length; i++) {
					if (i == 0) {
						criterion = RestrictionsFactoryUtil.or(criterion,
								RestrictionsFactoryUtil.ilike("user.firstName", "%" + nomeTmp[i] + "%"));
					} else {
						criterion = RestrictionsFactoryUtil.ilike("user.firstName", "%" + nomeTmp[i] + "%");
					}
					criterion = RestrictionsFactoryUtil.or(criterion,
							RestrictionsFactoryUtil.ilike("user.middleName", "%" + nomeTmp[i] + "%"));
					criterion = RestrictionsFactoryUtil.or(criterion,
							RestrictionsFactoryUtil.ilike("user.lastName", "%" + nomeTmp[i] + "%"));
					queryUsuario.add(criterion);
				}
			}

			queryUsuario.addOrder(OrderFactoryUtil.asc("user.screenName"));

			usuarios = UserLocalServiceUtil.dynamicQuery(queryUsuario);

		} catch (SystemException e) {
			console.error(e, e);
		}

		return usuarios;
	}
	
}
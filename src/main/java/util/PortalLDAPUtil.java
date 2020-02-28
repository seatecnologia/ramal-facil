//package util;
//
//import com.liferay.portal.kernel.exception.SystemException;
//import com.liferay.portal.kernel.log.Log;
//import com.liferay.portal.kernel.log.LogFactoryUtil;
//import com.liferay.portal.kernel.util.PrefsPropsUtil;
//import com.liferay.portal.kernel.util.PropertiesUtil;
//import com.liferay.portal.kernel.util.PropsKeys;
//import com.liferay.portal.kernel.util.PropsUtil;
//import com.liferay.portal.kernel.util.StringBundler;
//import com.liferay.portal.kernel.util.StringPool;
//import com.liferay.portal.kernel.util.StringUtil;
//import com.liferay.portal.kernel.util.Validator;
//
//import java.io.IOException;
//import java.util.Properties;
//
//import javax.naming.Binding;
//import javax.naming.Context;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
//import javax.naming.directory.SearchControls;
//import javax.naming.directory.SearchResult;
//import javax.naming.ldap.InitialLdapContext;
//import javax.naming.ldap.LdapContext;
//
///**
// * Descrição do Fonte
// *
// * @author 22 de nov de 2018: Carlos.Guedes
// *         <DD>
// */
//@SuppressWarnings("deprecation")
//public class PortalLDAPUtil {
//
//   private static Log console = LogFactoryUtil.getLog(PortalLDAPUtil.class);
//
//   private PortalLDAPUtil() {
//	   //
//   }
//   /**
//    * @param ldapServerId
//    * @param companyId
//    * @return
// * @throws SystemException 
//    * @throws Exception
//    */
//   @SuppressWarnings("deprecation")
//public static LdapContext getContext(final long ldapServerId, final long companyId) throws SystemException  {
//
//      String postfix = StringPool.PERIOD + ldapServerId;
//
//      String baseProviderURL = PrefsPropsUtil.getString(companyId, PropsKeys. + postfix);
//      String pricipal = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_SECURITY_PRINCIPAL + postfix);
//      String credentials = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_SECURITY_CREDENTIALS + postfix);
//
//      return getContext(companyId, baseProviderURL, pricipal, credentials);
//   }
//
//   /**
//    * @param companyId
//    * @param providerURL
//    * @param principal
//    * @param credentials
//    * @return
// * @throws SystemException 
//    * @throws Exception
//    */
//   public static LdapContext getContext(final long companyId, final String providerURL, final String principal, final String credentials) throws SystemException
//       {
//
//      Properties environmentProperties = new Properties();
//
//      environmentProperties.put(Context.INITIAL_CONTEXT_FACTORY, PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_FACTORY_INITIAL));
//      environmentProperties.put(Context.PROVIDER_URL, providerURL);
//      environmentProperties.put(Context.SECURITY_PRINCIPAL, principal);
//      environmentProperties.put(Context.SECURITY_CREDENTIALS, credentials);
//      environmentProperties.put(Context.REFERRAL, PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_REFERRAL));
//
//      Properties ldapConnectionProperties = PropsUtil.getProperties(PropsKeys.LDAP_CONNECTION_PROPERTY_PREFIX, true);
//
//      PropertiesUtil.merge(environmentProperties, ldapConnectionProperties);
//
//      LdapContext ldapContext = null;
//
//      try {
//         ldapContext = new InitialLdapContext(environmentProperties, null);
//      }
//      catch (NamingException e) {
//         console.error(e, e);
//      }
//
//      return ldapContext;
//   }
//
//   /**
//    * @param companyId
//    * @param screenName
//    * @return
// * @throws SystemException 
// * @throws NamingException 
// * @throws IOException 
//    * @throws Exception
//    */
//   public static long getLdapServerId(final long companyId, final String screenName) throws SystemException, IOException, NamingException {
//      long[] ldapServerIds = StringUtil.split(PrefsPropsUtil.getString(companyId, "ldap.server.ids"), 0L);
//
//      for (long ldapServerId : ldapServerIds) {
//         if (hasUser(ldapServerId, companyId, screenName)) {
//            return ldapServerId;
//         }
//      }
//
//      if (ldapServerIds.length <= 0) {
//         return 0;
//      }
//
//      return ldapServerIds[0];
//   }
//
//   /**
//    * @param ldapServerId
//    * @param companyId
//    * @param screenName
//    * @return
// * @throws SystemException 
// * @throws IOException 
// * @throws NamingException 
//    * @throws Exception
//    */
//   public static Binding getUser(final long ldapServerId, final long companyId, final String screenName) throws SystemException, IOException, NamingException {
//      String postfix = StringPool.PERIOD + ldapServerId;
//
//      LdapContext ldapContext = getContext(ldapServerId, companyId);
//
//      NamingEnumeration<SearchResult> enu = null;
//
//      try {
//         if (ldapContext == null) {
//            return null;
//         }
//
//         String baseDN = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_BASE_DN + postfix);
//
//         String filter = null;
//
//         String userFilter = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_IMPORT_USER_SEARCH_FILTER + postfix);
//
//         // Remove restri oo de usu rios ativos
//         userFilter = userFilter.replace("(mail=*)", "");
//         userFilter = userFilter.replace("(userPrincipalName=*)", "");
//
//         Properties userMappings = getUserMappings(ldapServerId, companyId);
//
//         String login = null;
//         String loginMapping = null;
//
//         // Utiliza o screenName para buscar o usu rio (sAmAccountName)
//         login = screenName;
//         loginMapping = userMappings.getProperty("screenName");
//
//         if (Validator.isNotNull(userFilter)) {
//            StringBundler sb = new StringBundler(11);
//
//            sb.append(StringPool.OPEN_PARENTHESIS);
//            sb.append(StringPool.AMPERSAND);
//            sb.append(StringPool.OPEN_PARENTHESIS);
//            sb.append(loginMapping);
//            sb.append(StringPool.EQUAL);
//            sb.append(login);
//            sb.append(StringPool.CLOSE_PARENTHESIS);
//            sb.append(StringPool.OPEN_PARENTHESIS);
//            sb.append(userFilter);
//            sb.append(StringPool.CLOSE_PARENTHESIS);
//            sb.append(StringPool.CLOSE_PARENTHESIS);
//
//            filter = sb.toString();
//         }
//         else {
//            StringBundler sb = new StringBundler(5);
//
//            sb.append(StringPool.OPEN_PARENTHESIS);
//            sb.append(loginMapping);
//            sb.append(StringPool.EQUAL);
//            sb.append(login);
//            sb.append(StringPool.CLOSE_PARENTHESIS);
//
//            filter = sb.toString();
//         }
//
//         SearchControls searchControls = new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 0, null, false, false);
//
//         enu = ldapContext.search(baseDN, filter, searchControls);
//      }
//      catch (StringIndexOutOfBoundsException e) {
//         throw e;
//      }
//      finally {
//         if (ldapContext != null) {
//            ldapContext.close();
//         }
//      }
//
//      if (enu.hasMoreElements()) {
//         Binding binding = enu.nextElement();
//
//         enu.close();
//
//         return binding;
//      }
//      else {
//         return null;
//      }
//   }
//
//   /**
//    * @param ldapServerId
//    * @param companyId
//    * @return
// * @throws SystemException 
//    * @throws Exception
//    */
//   public static Properties getUserMappings(final long ldapServerId, final long companyId) throws IOException, SystemException {
//
//      String postfix = StringPool.PERIOD + ldapServerId;
//
//      Properties userMappings = PropertiesUtil.load(PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_USER_MAPPINGS + postfix));
//
//      return userMappings;
//   }
//
//   /**
//    * @param ldapServerId
//    * @param companyId
//    * @param screenName
//    * @return
// * @throws NamingException 
// * @throws IOException 
// * @throws SystemException 
//    * @throws Exception
//    */
//   public static boolean hasUser(final long ldapServerId, final long companyId, final String screenName) throws SystemException, IOException, NamingException {
//      if (getUser(ldapServerId, companyId, screenName) != null) {
//         return true;
//      }
//      return false;
//   }
//}

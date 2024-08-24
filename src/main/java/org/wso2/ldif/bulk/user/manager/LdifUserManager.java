package org.wso2.ldif.bulk.user.manager;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.ldif.bulk.user.manager.constants.Constants;
import org.wso2.ldif.bulk.user.manager.exceptions.LdifUserImportException;
import org.wso2.ldif.bulk.user.manager.internal.LdifUserManagerDataHolder;
import org.wso2.ldif.bulk.user.manager.internal.LdifUserManagerServiceComponent;
import org.wso2.ldif.bulk.user.manager.utils.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LdifUserManager {

    private static final Log log = LogFactory.getLog(LdifUserManager.class);

    UserStoreManager userStoreManager;
    Map<String, String> claimMapping;

    public LdifUserManager() {
        try {
            Utils.loadProperties();
            Utils.loadAttributeClaimMappings();
            userStoreManager = getUserStoreManager();

        } catch (UserStoreException | LdifUserImportException e) {
            log.error(e.getMessage(), e);
        }

    }

    public void addUsers() throws LdifUserImportException {

        /**
        Map<String, String> claimMapping = new HashMap<>();
        claimMapping.put("http://wso2.org/claims/mobile", "mobile");
        claimMapping.put("http://wso2.org/claims/emailaddress", "mail");
        claimMapping.put("http://wso2.org/claims/givenname", "firstName");
         **/

        /**
        Map<String, String> userAttributeValues = new HashMap<>();
        claimMapping.put("mobile", "0711231231");
        claimMapping.put("mail", "alex@gmail.com");
        claimMapping.put("firstName", "Alex");
         **/

        Properties configs = LdifUserManagerDataHolder.getInstance().getConfigs();

        HashMap<String, String> summary = new HashMap<>();

        claimMapping = LdifUserManagerDataHolder.getInstance().getClaimAttributeMappings();
        String usernameAttribute = configs.getProperty(Constants.ConfigProperties.USER_NAME_ATTRIBUTE);
        String passwordAttribute = configs.getProperty(Constants.ConfigProperties.PASSWORD_ATTRIBUTE);
        String passwordAlgValue = configs.getProperty(Constants.ConfigProperties.PASSWORD_ALG);
        boolean removeAlgFromPassword = Boolean.parseBoolean(configs.
                getProperty(Constants.ConfigProperties.REMOVE_ALG_FROM_PASSWORD));
        String userStoreDomain = configs.getProperty(Constants.ConfigProperties.USER_STORE_DOMAIN);
        boolean continueOnError = Boolean.parseBoolean(configs.
                getProperty(Constants.ConfigProperties.CONTINUE_ON_ERROR));

        UniqueIDUserStoreManager uniqueIDUserStoreManager = (UniqueIDUserStoreManager) userStoreManager;

        String ldifFilePath = LdifUserManagerDataHolder.getInstance().
                getConfigs().getProperty(Constants.ConfigProperties.LDIF_FILE_PATH);
        try (FileInputStream inputStream = new FileInputStream(ldifFilePath)) {
            LDIFReader ldifReader = new LDIFReader(inputStream);
            Entry entry;
            try {
                while ((entry = ldifReader.readEntry()) != null) {

                    if (entry.hasObjectClass("InetOrgPerson")) {
                        Map<String, String> userAttributeValues = new HashMap<>();
                        for (Attribute attribute : entry.getAttributes()) {
                            userAttributeValues.put(attribute.getName(), attribute.getValue());
                        }

                        String username = userStoreDomain + "/" + userAttributeValues.get(usernameAttribute);
                        String password = userAttributeValues.get(passwordAttribute);
                        if (removeAlgFromPassword) {
                            password = password.replace(passwordAlgValue, "");
                        }
                        Map<String, String> claimList = Utils.getClaimValueMappings(claimMapping, userAttributeValues);

                        try {
                            uniqueIDUserStoreManager.addUserWithID(username, password, null, claimList, null);
                            log.info(entry.getDN() + " is added");
                            summary.put(entry.getDN(), "Successfully created");
                        } catch (org.wso2.carbon.user.core.UserStoreException e) {
                            log.error(e.getMessage(), e);
                            summary.put(entry.getDN(), e.getMessage());
                            if (!continueOnError) {
                                break;
                            }
                        }
                    }
                }
            } catch (LDIFException e) {
                throw new LdifUserImportException("Error while reading ldif data", e);
            }
        } catch (IOException e) {
            throw new LdifUserImportException("Error while reading ldif file input stream", e);
        }

        if (MapUtils.isNotEmpty(summary)) {
            System.out.println("User import completed");
            Utils.createImportSummary(summary);
        }
    }

    private UserStoreManager getUserStoreManager() throws UserStoreException {
        return LdifUserManagerDataHolder.getInstance().getRealmService().
                getBootstrapRealm().getUserStoreManager();
    }
}

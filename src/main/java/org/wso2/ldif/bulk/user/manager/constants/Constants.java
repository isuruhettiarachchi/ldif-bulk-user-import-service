package org.wso2.ldif.bulk.user.manager.constants;

public class Constants {

    public final static String CONFIG_PROPERTIES_FILE = "./repository/conf/config.properties";

    public static final String USER_DIRECTORY_SYSTEM_VARIABLE = "user.dir";
    public static final String OUTPUT_DIRECTORY = "output";
    public static final String IMPORT_SUMMARY_FILE_NAME = "bulk-import-summary.txt";

    public static class ConfigProperties {
        public static final String USER_STORE_DOMAIN = "userStoreDomain";
        public static final String USER_NAME_ATTRIBUTE = "userNameAttribute";
        public static final String CONTINUE_ON_ERROR = "continueOnError";
        public static final String REMOVE_ALG_FROM_PASSWORD = "removeAlgFromPassword";
        public static final String PASSWORD_ALG = "passwordAlg";
        public static final String USER_OBJECT = "userObject";
        public static final String LDIF_FILE_PATH = "LdifFilePath";
        public static final String LDAP_ATTRIBUTE_MAPPING_FILE_PATH = "LdapAttributeMappingFilePath";
        public static final String PASSWORD_ATTRIBUTE = "passwordAttribute";

        public static final String ADD_USERS = "AddUsers";
    }
}

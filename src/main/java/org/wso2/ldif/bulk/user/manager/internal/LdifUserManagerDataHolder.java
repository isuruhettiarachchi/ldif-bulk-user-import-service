package org.wso2.ldif.bulk.user.manager.internal;

import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;
import java.util.Properties;

public class LdifUserManagerDataHolder {

    private static LdifUserManagerDataHolder dataHolder = new LdifUserManagerDataHolder();
    private RealmService realmService;
    private Map<String, String> claimAttributeMappings;
    private Properties configs;

    public static LdifUserManagerDataHolder getInstance() {
        return dataHolder;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public void setClaimAttributeMappings(Map<String, String> claimAttributeMappings) {
        this.claimAttributeMappings = claimAttributeMappings;
    }

    public Map<String, String> getClaimAttributeMappings() {
        return claimAttributeMappings;
    }

    public void setConfigs(Properties properties) {
        this.configs = properties;
    }

    public Properties getConfigs() {
        return configs;
    }

}

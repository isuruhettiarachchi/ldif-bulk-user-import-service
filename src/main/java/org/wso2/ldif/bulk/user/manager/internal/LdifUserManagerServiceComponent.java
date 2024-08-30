package org.wso2.ldif.bulk.user.manager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.ldif.bulk.user.manager.LdifUserManager;
import org.wso2.ldif.bulk.user.manager.constants.Constants;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component(name = "org.wso2.ldif.bulk.user.import.service",
        immediate = true)
public class LdifUserManagerServiceComponent {

    private static final Log log = LogFactory.getLog(LdifUserManagerServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            log.info("activating ldif service");
            LdifUserManager.loadConfigs();
            if (Boolean.parseBoolean(LdifUserManagerDataHolder.getInstance().
                    getConfigs().getProperty(Constants.ConfigProperties.ADD_USERS))) {
                Callable<Boolean> ldifUserManagerThread = new LdifUserManager();
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(ldifUserManagerThread);
            }
        } catch (Throwable e) {
            log.error("Error while activating component", e);
        }

    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

    }

    @Reference(name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        LdifUserManagerDataHolder.getInstance().setRealmService(realmService);
        log.debug("Setting realm service");
    }

    protected void unsetRealmService(RealmService realmService) {
        LdifUserManagerDataHolder.getInstance().setRealmService(null);
        log.debug("Unsetting realm service");
    }


}

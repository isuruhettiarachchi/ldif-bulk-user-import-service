package org.wso2.ldif.bulk.user.manager.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceManager {

    private static final Log log = LogFactory.getLog(DataSourceManager.class);
    private static DataSource umDataSource;
    private static DataSourceManager dataSourceManager = null;

    public static DataSourceManager getInstance() {
        if (DataSourceManager.dataSourceManager == null) {
            DataSourceManager.dataSourceManager = new DataSourceManager();
        }
        return DataSourceManager.dataSourceManager;
    }

    public void initUmDataSource(RealmConfiguration realmConfiguration) {
        umDataSource = DatabaseUtil.createUserStoreDataSource(realmConfiguration);
    }

    public Connection getDBConnection() {
        Connection dbConnection;
        try {
            dbConnection = umDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return dbConnection;
    }
}

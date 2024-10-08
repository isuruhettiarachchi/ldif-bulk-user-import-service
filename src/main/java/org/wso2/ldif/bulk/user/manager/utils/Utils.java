package org.wso2.ldif.bulk.user.manager.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.ldif.bulk.user.manager.exceptions.LdifUserImportException;
import org.wso2.ldif.bulk.user.manager.internal.LdifUserManagerDataHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.wso2.ldif.bulk.user.manager.constants.Constants.*;

public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    public static void loadProperties() throws LdifUserImportException {
        try (FileInputStream inputStream = new FileInputStream(CONFIG_PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            LdifUserManagerDataHolder.getInstance().setConfigs(properties);

        } catch (IOException e) {
            log.error("Error reading configs", e);
            throw new LdifUserImportException("Error reading configs", e);
        }
    }

    public static void loadAttributeClaimMappings() throws LdifUserImportException {
        String filePath = LdifUserManagerDataHolder.getInstance().getConfigs().
                getProperty(ConfigProperties.LDAP_ATTRIBUTE_MAPPING_FILE_PATH);
        Map<String, String> userAttributeMappings = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath)) {
            Properties properties = new Properties();
            properties.load(fis);

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                userAttributeMappings.put(key, value);
            }
        } catch (IOException e) {
            log.error("Error loading attribute claim mappings from file", e);
            throw new LdifUserImportException("Error loading attribute claim mappings from file", e);
        }

        LdifUserManagerDataHolder.getInstance().setClaimAttributeMappings(swapKeysAndValues(userAttributeMappings));
    }

    public static Map<String, String> getClaimValueMappings(Map<String, String> claimMappings,
                                                            Map<String, String> userAttributes) {
        return claimMappings.entrySet()
                .stream()
                .filter(entry -> userAttributes.containsKey(entry.getValue())) // Filter out missing keys if necessary
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> userAttributes.get(entry.getValue())
                ));
    }

    public static <K, V> Map<V, K> swapKeysAndValues(Map<K, V> map) {
        Map<V, K> swappedMap = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            swappedMap.put(entry.getValue(), entry.getKey());
        }
        return swappedMap;
    }

    public static void createImportSummary(HashMap<String, String> responseSummary) {

        Path directoryPath = Paths.get(System.getProperty(USER_DIRECTORY_SYSTEM_VARIABLE), OUTPUT_DIRECTORY);
        File outputFile = new File(Paths.get(directoryPath.toString(), IMPORT_SUMMARY_FILE_NAME).toString());

        // Delete the existing file.
        if (outputFile.exists() && !outputFile.isDirectory()) {
            boolean fileDeletionState = outputFile.delete();
            if (!fileDeletionState) {
                return;
            }
        }

        // Create the directory if not exists.
        File directory = new File(directoryPath.toString());
        if (!directory.exists()) {
            boolean directoryCreationState = directory.mkdir();
            if (!directoryCreationState) {
                return;
            }
        }

        // Create the new file.
        try {
            boolean fileCreationState = outputFile.createNewFile();
            if (!fileCreationState) {
                return;
            }
        } catch (IOException e) {
            return;
        }

        // Write the summary to the file.
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (Map.Entry<String, String> entry : responseSummary.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + System.lineSeparator());
            }
            log.info("HashMap has been written to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Error while writing ldif user import summary", e);
        }
    }

    public static void getUserStoreManager() throws UserStoreException {
        LdifUserManagerDataHolder.getInstance().setUserStoreManager(LdifUserManagerDataHolder.
                getInstance().getRealmService().
                getBootstrapRealm().getUserStoreManager());

    }

    public static String getSaltString(byte[] decodedPasswordHash, Charset encodedCharset) {

        char[] decodedPasswordCharArray = encodedCharset.decode(ByteBuffer.wrap(decodedPasswordHash)).array();

        char[] saltCharArray = Arrays.copyOfRange(decodedPasswordCharArray, 64, decodedPasswordHash.length);

        return new String(saltCharArray);

    }

    public static String getBase64EncodedSaltedPassword(byte[] decodedPasswordHash) {

        byte[] passwordHash = new byte[64];
        System.arraycopy(decodedPasswordHash, 0, passwordHash, 0, passwordHash.length);

        return Base64.getEncoder().encodeToString(passwordHash);

    }

    public static Charset getLidfEncodedCharset(String encodedCharset) {
        if (Objects.equals(encodedCharset, CHARSET_ISO_8859_1)) {
            return StandardCharsets.ISO_8859_1;
        } else {
            return StandardCharsets.UTF_8;
        }
    }

    public static void updateSaltValue(Connection connection, String username, String salt) {
        try {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(SqlQueries.UPDATE_SALT);
            preparedStatement.setString(1, salt);
            preparedStatement.setString(2, username);
            preparedStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            log.error("Error when updating salt value", e);
            throw new RuntimeException(e);
        }
    }


}

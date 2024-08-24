package org.wso2.ldif.bulk.user.manager.exceptions;

public class LdifUserImportException extends Exception{

    public LdifUserImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public LdifUserImportException(String message) {
        super(message);
    }
}

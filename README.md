# LDIF BULK USER IMPORT SERVICE

This service will read a ldif file at the server startup and create users.

## How to use

1. Build the project
   ```shell
   mvn clean install
   ```
2. Copy the `ldif.direct.bulk.user.import-<version>.jar` file to `<IS_HOME>/repository/components/dropins` directory.
3. Create a `config.properties` file inside `<IS_HOME>/repository/conf` directory.
   ```properties
   userStoreDomain=PRIMARY
   userNameAttribute=cn
   passwordAttribute=userPassword
   continueOnError=false
   removeAlgFromPassword=true
   passwordAlg={SHA512}
   userObject=InetOrgPerson
   LdifFilePath=~/export_users_SHA512.ldif
   LdapAttributeMappingFilePath=~/mappings.properties
   AddUsers=true
   ```
   | Configuration                  | Description                                                                            | Sample value            |
   |--------------------------------|----------------------------------------------------------------------------------------|-------------------------|
   | `userStoreDomain`              | User store domain name user needs to be added                                          | `PRIMARY`               |
   | `userNameAttribute`            | User name attribute defined in the LDIF file                                           | `cn`                    |
   | `passwordAttribute`            | Password attribute defined in the LDFI file                                            | `userPassword`          |
   | `continueOnError`              | Continue adding users if an error occurred                                             | `true`                  |
   | `removeAlgFromPassword`        | Remove hashing algorithm definition from password text                                 | `true`                  |
   | `passwordAlg`                  | Hashing algorithm text that needs to be removed from password text                     | `{SHA512}`              |
   | `userObject`                   | LDAP Object to defined to create users                                                 | `InetOrgPerson`         |
   | `LdifFilePath`                 | Path to the input LDIF file. This file should contain the user details to be imported. | `~/users.ldif`          |
   | `LdapAttributeMappingFilePath` | Path to the file where LDAP attributes to claim URI mappings available                 | `~/mappings.properties` |
   | `AddUsers`                     | Add users after activating the component                                               | `true`                  |

4. Create a LDAP attributes to claims mapping `properties` file. Path of this file should be defined in the `LdapAttributeMappingFilePath` property of `config.properties` file. User will be created with the claims defined in this file. Any other LDAP attribute will be discarded.

   ```properties
   cn="http://wso2.org/claims/username"
   giveName="http://wso2.org/claims/givenname"
   familyName="http://wso2.org/claims/lastname"
   mobile="http://wso2.org/claims/mobile"
   mail="http://wso2.org/claims/emailaddress"
   ```
   
5. Start the server
   ```shell
   sh wso2server.sh
   ```

### Note

- The service generate a summary report in the `<IS_HOME>/output` directory after importing all the users.

# AAF Shibboleth Extensions

A [Shibboleth IdP custom extension](https://wiki.shibboleth.net/confluence/display/SHIB2/IdPDevCustomExtension) which 
enables [auEduPersonSharedToken](http://wiki.aaf.edu.au/tech-info/attributes/auedupersonsharedtoken) for Shibboleth 
IdP 3.3.1.

The following features are provided:

- auEduPersonSharedToken generation.
- Database integration for storing and retrieving auEduPersonSharedToken values.  

**IMPORTANT:** The generation of the auEduPersonSharedToken relies on the user's identifier (`sourceAttributeID`),  
the IdP's Entity ID  and the private seed (`salt`). Change of the inputs will change the auEduPersonSharedToken value.
This is likely to happen due to the change of the user's identifier, home institution, upgrade of the IdP and so on.
In a production environment, the auEduPersonSharedToken must be only generated **once** then persisted in 
the institution's database for future use.

# Requirements
- Shibboleth IdP 3.3.1 operating with Java 8 or later.
- A database for auEduPersonSharedToken storage. It is **strongly** recommended administrators configure regular 
backups and monitoring for this database. **Loss of this data will disable federated access for your users**.

# Deployment
## 1. Configure database

Set up your database with the following schema [db/schema.sql](db/schema.sql).

For example, to configure a local MySQL instance:

```
$ mysql
mysql> create database idp_db;
mysql> grant all privileges on idp_db.* to idp_admin@localhost identified by '<your_password>';
mysql> \u idp_db
mysql> (Paste db/schema.sql)
```  

## 2. Configure resolvers

in `$IDP_HOME/conf/attribute-resolver.xml`:

Import the definition
```
xsi:schemaLocation="...
                    urn:mace:aaf.edu.au:shibboleth:2.0:resolver:dc classpath:/schema/aaf-shib-ext-dc.xsd
```

Define the `DataConnector`
```
<resolver:DataConnector xsi:type="SharedToken" xmlns="urn:mace:aaf.edu.au:shibboleth:2.0:resolver:dc"
                    id="sharedToken"
                    sourceAttributeId="uniqueIdentifier"
                    salt="Ez8m1HDSLBxu0JNcPEywmOpy+apq4Niw9kEMmAyWbhJqcfAb"
                    dataSource="jdbc/DS_idp_admin"
                    primaryKeyName="uid">
                    <resolver:Dependency ref="..." />
</resolver:DataConnector>
``` 

Attributes:

- `id`: (mandatory) the unique identifier for the data connector.
- `sourceAttributeID`: used for computing the sharedToken — ideally a unique identifier that never changes.
- `salt`: (mandatory)  a string of random data, used when computing sharedToken. Must be at least 16 characters. N.B. Once set, 
this value **must never change**. Please keep a copy of this value. This value can be generated with the openssl 
command: 
`openssl rand -base64 36 2>/dev/null`
- `dataSource`: (mandatory)  the container managed datasource identifier. Please see the relevant application server's instructions 
for installing a JNDI datasource. Also ensure the specified JDBC driver is on the classpath of your application server.
 For example, to configure a MySQL JNDI datasource for Jetty:
    1. Place [mysql-connector-java-5.1.35-bin.jar](http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.35.tar.gz) in `/opt/jetty/lib/ext/`
    2. Configure a [JNDI Datasource](https://wiki.eclipse.org/Jetty/Howto/Configure_JNDI_Datasource)
    3. Restart app server
- `primaryKeyName`: (optional) The column name used for the primary key in the shared token database table. The default is 'uid'
which works for MySQL databases but is a reserved word for ORACLE.

## 3. Configure logging

Use the pattern: `"au.edu.aaf.shibext"` in your logging configuration to enable logging.

For example, Shibboleth's `$IDP_HOME/conf/logback.xml` can use the configuration:
```
    <logger name="au.edu.aaf.shibext" level="DEBUG"/>
```
Unless specified, the log information will appear in `$IDP_HOME/logs/idp-process.log`.

## 4. Building the library

1. Install [Gradle](https://docs.gradle.org/current/userguide/installation.html)
2. Run the command `./gradlew clean build`
3. Generated jar will be in `build/libs`

## 5. Installing the library

1. Copy the jar file to `$IDP_HOME/edit-webapp/WEB-INF/lib/`
2. Re-run the installer `sh $IDP_HOME/bin/build.sh`
3. Restart the app server


package au.edu.aaf.shibext.sharedtoken.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Resolves a pre-configured DataSource instance from JNDI.
 *
 * @author rianniello
 */
public class DataSourceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceResolver.class);

    /**
     * Resolves a DataSource corresponding to dataSourceIdentifier.
     *
     * @param dataSourceIdentifier The identifier of the JNDI/container managed DataSource.
     * @return a DataSource instance corresponding to dataSourceIdentifier
     */
    public DataSource lookup(String dataSourceIdentifier) {
        LOG.debug("Looking up dataSource " + dataSourceIdentifier);
        try {
            InitialContext initialContext = new InitialContext();
            return (DataSource) initialContext.lookup(dataSourceIdentifier);
        } catch (NamingException e) {
            LOG.error("NamingException occurred while looking up dataSource '" + dataSourceIdentifier + "'", e);
            throw new RuntimeException(e); //NOPMD
        }
    }
}

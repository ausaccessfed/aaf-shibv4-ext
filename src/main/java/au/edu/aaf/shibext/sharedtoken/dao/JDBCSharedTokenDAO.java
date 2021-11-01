package au.edu.aaf.shibext.sharedtoken.dao;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import java.time.Duration;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import javax.annotation.Nonnull;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;

/**
 * JdbcTemplate driven SharedTokenDAO implementation.
 *
 * @author rianniello
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public class JDBCSharedTokenDAO extends AbstractInitializableComponent implements SharedTokenDAO {

    private static final Logger log = LoggerFactory.getLogger(JDBCSharedTokenDAO.class);
    private static final String SELECT_SHARED_TOKEN = "select sharedtoken from tb_st where %s = ?";
    private static final String INSERT_SHARED_TOKEN = "insert into tb_st(%s, sharedtoken) values (?, ?)";

    /** JDBC data source for retrieving connections. */
    @NonnullAfterInit private DataSource dataSource;

    /** Timeout of SQL queries. */
    @Nonnull private Duration queryTimeout;

    /** Number of times to retry a transaction if it rolls back. */
    @NonNegative private int transactionRetry;

    /** Whether to fail if the database cannot be verified.  */
    private boolean verifyDatabase;


    private JdbcTemplate jdbcTemplate;


    public JDBCSharedTokenDAO() {
	transactionRetry = 3;
	queryTimeout = Duration.ofSeconds(5);
        verifyDatabase = true;
        jdbcTemplate = null;
    }

    /**
     * Instantiate a JdbcTemplate using the input dataSource.
     *
     * @param dataSource A dataSource instance configured for SharedToken data access.
     */
    public JDBCSharedTokenDAO(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves the SharedToken corresponding to the uid.
     *
     * @param uid The user identifier (primary key)
     * @param primaryKeyName The name of the primary key
     * @return The SharedToken value or null if not found
     */
    @Override
    public String getSharedToken(String uid, String primaryKeyName) {
        log.debug("getSharedToken with primary key '{}' for user '{}'", primaryKeyName, uid);

        String sqlSelect = String.format (SELECT_SHARED_TOKEN, primaryKeyName);

        verifyArgumentIsNotBlankOrNull(uid, "uid");

        List<String> uids = jdbcTemplate.query(sqlSelect,
                new Object[]{uid}, (rs, rowNum) -> {
                    return rs.getString("sharedtoken");
                });

        if (uids.size() == 1) {
            String sharedToken = uids.get(0);
            log.debug("sharedToken: {}", sharedToken);
            return sharedToken;
        }

        log.debug("sharedToken not found");
        return null;
    }

    /**
     * Persists SharedToken for an associated uid.
     *
     * @param uid         The user identifier (primary key) to save
     * @param sharedToken the SharedToken value to save
     * @param primaryKeyName The name of the primary key
     */
    @Override
    public void persistSharedToken(String uid, String sharedToken, String primaryKeyName) {
        log.debug("Persisting shared token with '{}' '{}' and sharedToken '{}' ", primaryKeyName, uid, sharedToken);

        String sqlInsert = String.format (INSERT_SHARED_TOKEN, primaryKeyName);

        verifyArgumentIsNotBlankOrNull(uid, "uid");
        verifyArgumentIsNotBlankOrNull(sharedToken, "sharedToken");

        int affectedRows = jdbcTemplate.update(sqlInsert, uid, sharedToken);
        log.debug("{} affected rows when persisting shared token", affectedRows);
    }

    private void verifyArgumentIsNotBlankOrNull(String arg, String argumentName) {
        if (StringUtils.isBlank(arg)) {
            throw new IllegalArgumentException(argumentName + " cannot be blank or null");
        }
    }


    /**
     * Get the source datasource used to communicate with the database.
     *
     * @return the data source;
     */
    @NonnullAfterInit public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Get the source datasource used to communicate with the database.
     *
     * @param source the data source;
     */
    public void setDataSource(@Nonnull final DataSource source) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        dataSource = Constraint.isNotNull(source, "DataSource cannot be null");

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get the SQL query timeout.
     *
     * @return the timeout
     */
    @Nonnull public Duration getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Set the SQL query timeout. Defaults to 5s.
     *
     * @param timeout the timeout to set
     */
    public void setQueryTimeout(@Nonnull final Duration timeout) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        Constraint.isNotNull(timeout, "Timeout cannot be null");
        Constraint.isFalse(timeout.isNegative(), "Timeout cannot be negative");

        queryTimeout = timeout;
    }

    /**
     * Get the number of retries to attempt for a failed transaction.
     *
     * @return number of retries
     */
    public int getTransactionRetries() {
        return transactionRetry;
    }

    /**
     * Set the number of retries to attempt for a failed transaction. Defaults to 3.
     *
     * @param retries the number of retries
     */
    public void setTransactionRetries(@NonNegative final int retries) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        transactionRetry = Constraint.isGreaterThanOrEqual(0, retries, "Retries must be greater than or equal to 0");
    }

    /**
     * Get whether to allow startup if the database cannot be verified.
     *
     * @return whether to allow startup if the database cannot be verified
     */
    public boolean getVerifyDatabase() {
        return verifyDatabase;
    }

    /**
     * Set whether to allow startup if the database cannot be verified.
     *
     * <p>Verification consists not only of a liveness check, but the successful insertion of
     * a dummy row, a failure to insert a duplicate, and then deletion of the row.</p>
     *
     * @param flag flag to set
     */
    public void setVerifyDatabase(final boolean flag) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        verifyDatabase = flag;
    }
}

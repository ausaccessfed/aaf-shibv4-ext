package au.edu.aaf.shibext.sharedtoken.dao;

/**
 * Defines contract to retrieve and store SharedToken values.
 *
 * @author rianniello
 */
public interface SharedTokenDAO {
    /**
     * Retrieves the SharedToken corresponding to the uid.
     *
     * @param uid The user identifier (primary key)
     * @param primaryKeyName The name of the primary key
     * @return The SharedToken value
     */
    String getSharedToken(String uid, String primaryKeyName);

    /**
     * Persists SharedToken with an associated uid.
     *
     * @param uid         The user identifier (primary key) to save
     * @param sharedToken the SharedToken value to save
     * @param primaryKeyName The name of the primary key
     */
    void persistSharedToken(String uid, String sharedToken, String primaryKeyName);
}

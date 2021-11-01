package au.edu.aaf.shibext.sharedtoken.generator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import static org.apache.commons.codec.binary.Base64.encodeBase64URLSafe;
import static org.apache.commons.codec.digest.DigestUtils.sha1;

/**
 * Generates new auEduPersonSharedToken values.
 * <p>
 * The following algorithm is used:
 * <pre>base64(sha1(resolvedSourceAttribute + idpIdentifier + salt))</pre>
 *
 * @author rianniello
 * @see <a href="http://wiki.aaf.edu.au/tech-info/attributes/auedupersonsharedtoken">auedupersonsharedtoken</a>
 */
public class AuEduPersonSharedTokenGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(AuEduPersonSharedTokenGenerator.class);
    private static final int MINIMUM_SALT_LENGTH = 16;

    /**
     * Generates auEduPersonSharedToken using parameters resolvedSourceAttribute, idPIdentifier and salt.
     *
     * @param resolvedSourceAttribute The resolved source attribute - ideally a unique identifier that never
     *                                changes.
     * @param idPIdentifier           Typically set as IdP's EntityID. Example value: 'https://idp.domain.edu
     *                                .au/idp/shibboleth'
     * @param salt                    String of random data, must be at least 16 characters.
     * @return a new auEduPersonSharedToken
     * @throws java.lang.IllegalArgumentException if any parameters are null or blank
     */
    public String generate(String resolvedSourceAttribute, String idPIdentifier, String salt) {

        LOG.debug("Generating auEduPersonSharedToken with resolvedSourceAttribute: {}" +
                ", idPIdentifier: {}, salt: {}", resolvedSourceAttribute, idPIdentifier, salt);

        verifyArgumentIsNotBlankOrNull(resolvedSourceAttribute, "resolvedSourceAttribute");
        verifyArgumentIsNotBlankOrNull(idPIdentifier, "idPIdentifier");
        verifyArgumentIsNotBlankOrNull(salt, "salt");
        verifySaltLength(salt);

        byte[] auEduPersonSharedToken = buildAuEduPersonSharedToken(resolvedSourceAttribute, idPIdentifier, salt);

        String auEduPersonSharedTokenAsString = new String(auEduPersonSharedToken, Charset.forName("UTF-8"));
        LOG.debug("Generated auEduPersonSharedToken -> " + auEduPersonSharedTokenAsString);
        return auEduPersonSharedTokenAsString;
    }

    private void verifySaltLength(String salt) {
        if (salt.length() < MINIMUM_SALT_LENGTH) {
            throw new IllegalArgumentException("salt must be at least " + MINIMUM_SALT_LENGTH + " characters");
        }
    }

    private byte[] buildAuEduPersonSharedToken(String resolvedSourceAttribute, String idPIdentifier, String salt) {
        return encodeBase64URLSafe(sha1(resolvedSourceAttribute + idPIdentifier + salt));
    }

    private void verifyArgumentIsNotBlankOrNull(String arg, String argumentName) {
        if (StringUtils.isBlank(arg)) {
            throw new IllegalArgumentException(argumentName + " cannot be blank or null");
        }
    }

}

package au.edu.aaf.shibext.handler;

import au.edu.aaf.shibext.sharedtoken.dataconnector.SharedTokenDataConnectorBeanDefinitionParser;
import net.shibboleth.ext.spring.util.BaseSpringNamespaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the SharedTokenDataConnectorBeanDefinitionParser.
 *
 * @author rianniello
 * @see SharedTokenDataConnectorBeanDefinitionParser
 */
public class ShibExtensionNamespaceHandler extends BaseSpringNamespaceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ShibExtensionNamespaceHandler.class);
    public static final String NAMESPACE = "urn:mace:aaf.edu.au:shibboleth:2.0:resolver";

    /**
     * Initialise and register a SharedTokenDataConnectorBeanDefinitionParser.
     *
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init
     */
    @Override
    public void init() {
        registerBeanDefinitionParser(SharedTokenDataConnectorBeanDefinitionParser.TYPE_NAME,
                new SharedTokenDataConnectorBeanDefinitionParser());
    }
}

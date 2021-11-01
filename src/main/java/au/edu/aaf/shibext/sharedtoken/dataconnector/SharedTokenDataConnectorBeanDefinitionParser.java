package au.edu.aaf.shibext.sharedtoken.dataconnector;

import au.edu.aaf.shibext.sharedtoken.dao.JDBCSharedTokenDAO;

import au.edu.aaf.shibext.handler.ShibExtensionNamespaceHandler;
import au.edu.aaf.shibext.sharedtoken.dataconnector.SharedTokenDataConnector;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import net.shibboleth.idp.attribute.resolver.spring.BaseResolverPluginParser;
/* import net.shibboleth.idp.attribute.resolver.spring.dc.AbstractDataConnectorParser; */
/* import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler; */
/* import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty; */
import net.shibboleth.utilities.java.support.primitive.StringSupport;
/* import net.shibboleth.utilities.java.support.xml.ElementSupport; */

import net.shibboleth.idp.attribute.resolver.spring.impl.InputAttributeDefinitionParser;
import net.shibboleth.idp.attribute.resolver.spring.impl.InputDataConnectorParser;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import net.shibboleth.ext.spring.util.SpringSupport;
import org.springframework.beans.factory.BeanCreationException;

import net.shibboleth.idp.attribute.resolver.spring.dc.impl.ManagedConnectionParser;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport;
import net.shibboleth.utilities.java.support.primitive.DeprecationSupport.ObjectType;

import org.springframework.beans.factory.config.BeanDefinition;
import net.shibboleth.idp.attribute.resolver.spring.impl.AttributeResolverNamespaceHandler;

/**
 * Facilitates the loading of values from xml configuration to SharedTokenDataConnector.
 *
 * @author rianniello
 * @see SharedTokenDataConnector
 */

public class SharedTokenDataConnectorBeanDefinitionParser extends BaseResolverPluginParser {

    /** Schema type name. */
    @Nonnull public static final QName TYPE_NAME =
            new QName(ShibExtensionNamespaceHandler.NAMESPACE, "SharedToken");

    /** resolver:ContainerManagedConnection.*/
    @Nonnull public static final QName CONTAINER_MANAGED_CONNECTION_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "ContainerManagedConnection");

    /** resolver:SimpleManagedConnection.*/
    @Nonnull public static final QName SIMPLE_MANAGED_CONNECTION_RESOLVER =
            new QName(AttributeResolverNamespaceHandler.NAMESPACE, "SimpleManagedConnection");

    /** aaf:BeanManagedConnection.*/
    @Nonnull public static final QName BEAN_MANAGED_CONNECTION_RESOLVER =
            new QName(ShibExtensionNamespaceHandler.NAMESPACE, "BeanManagedConnection");

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SharedTokenDataConnectorBeanDefinitionParser.class);

    /** {@inheritDoc} */
    @Override protected Class<SharedTokenDataConnector> getBeanClass(final Element element) {
        return SharedTokenDataConnector.class;
    }

    /** {@inheritDoc} */
    @Override protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {
        doParse(config, parserContext, builder, "auEduPersonSharedToken", "uid"); 
        doParsePart2(config, parserContext, builder);
    }

    protected void doParse(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder, @Nullable final String generatedIdDefaultName,
            @Nullable final String primaryKeyNameDefaultName) {
        super.doParse(config, parserContext, builder);

        final String generatedAttribute;
        if (config.hasAttributeNS(null, "generatedAttributeID")) {
            generatedAttribute = StringSupport.trimOrNull(config.getAttributeNS(null, "generatedAttributeID"));
        } else {
            generatedAttribute = generatedIdDefaultName;
        }
        builder.addPropertyValue("generatedAttributeID", generatedAttribute);

        final String salt;
        salt = StringSupport.trimOrNull(config.getAttributeNS(null, "salt"));
        builder.addPropertyValue("salt", salt);

        final String primaryKeyName;
        if (config.hasAttributeNS(null, "primaryKeyName")) {
            primaryKeyName = StringSupport.trimOrNull(config.getAttributeNS(null, "primaryKeyName"));
        } else {
            primaryKeyName = primaryKeyNameDefaultName;
        }

        builder.addPropertyValue("primaryKeyName", primaryKeyName);
    }



    protected void doParsePart2(@Nonnull final Element config, @Nonnull final ParserContext parserContext,
            @Nonnull final BeanDefinitionBuilder builder) {

        final List<Element> attributeDependencyElements =
                ElementSupport.getChildElements(config, InputAttributeDefinitionParser.ELEMENT_NAME);
        final List<Element> dataConnectorDependencyElements =
                ElementSupport.getChildElements(config, InputDataConnectorParser.ELEMENT_NAME);
        if (null != attributeDependencyElements && !attributeDependencyElements.isEmpty() ||
            null != dataConnectorDependencyElements && !dataConnectorDependencyElements.isEmpty()) {
            if (failOnDependencies()) {
                log.error("{} Dependencies are not allowed.", getLogPrefix());
                throw new BeanCreationException(getLogPrefix() + " has meaningless Dependencies statements");
            }
            if (warnOnDependencies()) {
                log.warn("{} Dependencies are not allowed.", getLogPrefix());
            }
        }
        builder.addPropertyValue("attributeDependencies",
                SpringSupport.parseCustomElements(attributeDependencyElements, parserContext, builder));
        builder.addPropertyValue("dataConnectorDependencies",
                SpringSupport.parseCustomElements(dataConnectorDependencyElements, parserContext, builder));

        builder.addPropertyValue("SharedTokenStore", doJDBCSharedTokenStore(config, parserContext));
    }

    @Nonnull protected BeanDefinition doJDBCSharedTokenStore(@Nonnull final Element config,
            @Nonnull final ParserContext parserContext) {

        final BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(JDBCSharedTokenDAO.class);
        builder.setInitMethodName("initialize");
        builder.setDestroyMethodName("destroy");

        final String beanDataSource = getAAFBeanDataSourceID(config);

        if (beanDataSource != null) {
            builder.addPropertyReference("dataSource", beanDataSource);
        } else {
            builder.addPropertyValue("dataSource", getv2DataSource(config));
        }

        if (config.hasAttributeNS(null, "queryTimeout")) {
            builder.addPropertyValue("queryTimeout", config.getAttributeNS(null, "queryTimeout"));
        }

        if (config.hasAttributeNS(null, "transactionRetries")) {
            builder.addPropertyValue("transactionRetries",
                    StringSupport.trimOrNull(config.getAttributeNS(null, "transactionRetries")));
        }

        if (config.hasAttributeNS(null, "failFast")) {
            // V4 Deprecation
            DeprecationSupport.warnOnce(ObjectType.ATTRIBUTE, "failFast",
                    parserContext.getReaderContext().getResource().getDescription(), "failFastInitialize");
            builder.addPropertyValue("verifyDatabase",
                    StringSupport.trimOrNull(config.getAttributeNS(null, "failFast")));
        }
        if (config.hasAttributeNS(null, "failFastInitialize")) {
            builder.addPropertyValue("verifyDatabase",
                    StringSupport.trimOrNull(config.getAttributeNS(null, "failFastInitialize")));
        }

        return builder.getBeanDefinition();
    }


/*
    private String getAttribute(Element pluginConfig, String attributeId) {
        return pluginConfig.getAttributeNS(null, attributeId);
    }
*/
    protected boolean failOnDependencies() {
        return false;
    }
    protected boolean warnOnDependencies() {
        return false;
    }

    protected BeanDefinition getv2DataSource(@Nonnull final Element config) {
        final ManagedConnectionParser parser = new ManagedConnectionParser(config);
        return parser.createDataSource();
    }

    private String getAAFBeanDataSourceID(@Nonnull final Element config) {

        final List<Element> beanManagedElements = ElementSupport.getChildElements(config,
                BEAN_MANAGED_CONNECTION_RESOLVER);

        if (beanManagedElements.isEmpty()) {
            return null;
        }

        if (beanManagedElements.size() > 1) {
            log.warn("Only one <BeanManagedConnection> should be specified; the first one has been consulted");
        }

        final List<Element> managedElements = ElementSupport.getChildElements(config,
                CONTAINER_MANAGED_CONNECTION_RESOLVER);
        managedElements.addAll(ElementSupport.getChildElements(config, SIMPLE_MANAGED_CONNECTION_RESOLVER));

        if (managedElements.size() > 0) {
            log.warn("<BeanManagedConnection> is incompatible with <ContainerManagedConnection>"
                    + "or <SimpleManagedConnection>. The <BeanManagedConnection> has been used");
        }

        return StringSupport.trimOrNull(ElementSupport.getElementContentAsString(beanManagedElements.get(0)));
    }
}



package org.jboss.as.test.integration.security.vault;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ALLOW_RESOURCE_SERVICE_RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VAULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VAULT_OPTIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SYSTEM_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.as.test.integration.security.common.Utils;
import org.jboss.as.test.integration.security.common.VaultHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test case to attempt to put vaulted passwords as system properties.
 * <p/>
 * JIRA - WFLY-1904.
 *
 * @author navssurtani
 */
@RunWith(Arquillian.class)
@ServerSetup(VaultSystemPropertyTestCase.VaultSystemPropertyServerSetup.class)
public class VaultSystemPropertyTestCase {

    private static final Logger logger = Logger.getLogger(VaultSystemPropertyTestCase.class);

    private static final String RESOURCE_LOCATION = VaultSystemPropertyTestCase.class.getProtectionDomain()
            .getCodeSource().getLocation().getFile() + "security/sysprop-vault/";
    private static final String ATTRIBUTE_NAME = "system-property-password";
    private static final String VAULT_BLOCK = "props_Block";
    private static final String PASSWORD = "password";
    private static final String SYSTEM_PROPERTY_KEY = "org.jboss.as.test.integration.security.vault.vaultedPassword";

    private VaultHandler testVaultHandler;

    /* The deployment to kick off this test */
    @Deployment
    public static WebArchive deployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "vault-sys-prop.war");
        war.addClass(VaultHandler.class);
        return war;
    }

    @Test
    public void testSystemProperty() {
        // Now we are going to try and get that system property out and make sure that it's the same String as that
        // vaulted password.
        String expectedVaultedString = buildExpectedString();
        String actualVaultedString = System.getProperty(SYSTEM_PROPERTY_KEY);
        logger.info("(Test Method) Expected vaulted String: " + expectedVaultedString);
        Assert.assertFalse(expectedVaultedString == null);
        Assert.assertFalse(actualVaultedString == null);
        Assert.assertEquals(expectedVaultedString, actualVaultedString);
    }

    private String buildExpectedString() {
        testVaultHandler = new VaultHandler();
        String vaultedPassword = testVaultHandler.addSecuredAttribute(VAULT_BLOCK, ATTRIBUTE_NAME,
                PASSWORD.toCharArray());
        logger.info("vaultedPassword built from test handler: "  + vaultedPassword);
        return "{$" + vaultedPassword + "}";
    }



   /* Inner ServerSetup class to start the vault and add in a value as the system property.*/

    static class VaultSystemPropertyServerSetup implements ServerSetupTask {

        private VaultHandler setupVaultHandler;

        @Override
        public void setup(ManagementClient client, String containerId) throws Exception {
            logger.info("RESOURCE_LOCATION = " + RESOURCE_LOCATION);

            ModelNode op = new ModelNode();
            op.get(OP).set(ADD);
            op.get(OP_ADDR).add(CORE_SERVICE, VAULT);
            ModelNode vaultOption = op.get(VAULT_OPTIONS);

            // Pause for a second, we need to get the VaultHandler going. Basically the vault,
            // we can edit some of the default configurations for it and add in the password.

            setupVaultHandler = new VaultHandler(RESOURCE_LOCATION);

            // Add in the security attributes.
            String vaultedPasswordString = setupVaultHandler.addSecuredAttribute(VAULT_BLOCK, ATTRIBUTE_NAME,
                    PASSWORD.toCharArray());
            // We need the password in the form '{$VAULT::VAULT_BLOCK::attributeName::1}'
            // We already have the main body from the variable 'vaultedPasswordString'
            vaultedPasswordString = "{$" + vaultedPasswordString + "}";
            logger.info("(ServerSetup) Expected vaulted String:: " + vaultedPasswordString);

            // Now we set the vault options that we have from the handler to our vaultOptions object.
            vaultOption.get("KEYSTORE_URL").set(setupVaultHandler.getKeyStore());
            vaultOption.get("KEYSTORE_PASSWORD").set(setupVaultHandler.getMaskedKeyStorePassword());
            vaultOption.get("KEYSTORE_ALIAS").set(setupVaultHandler.getAlias());
            vaultOption.get("SALT").set(setupVaultHandler.getSalt());
            vaultOption.get("ITERATION_COUNT").set(setupVaultHandler.getIterationCountAsString());
            vaultOption.get("ENC_FILE_DIR").set(setupVaultHandler.getEncodedVaultFileDirectory());


            // system property Operation
            ModelNode spOp = new ModelNode();
            spOp.get(OP).set(ADD);
            spOp.get(OP_ADDR).add("system-property", SYSTEM_PROPERTY_KEY);
            spOp.get(VALUE).set(vaultedPasswordString);

            // Execute the operations
            client.getControllerClient().execute(new OperationBuilder(op).build());
            client.getControllerClient().execute(new OperationBuilder(spOp).build());
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
            logger.debug("Tearing down.");

            // Delete the vault that we created.
            ModelNode op = new ModelNode();
            op.get(OP).set(REMOVE);
            op.get(OP_ADDR).add(CORE_SERVICE, VAULT);
            managementClient.getControllerClient().execute(new OperationBuilder(op).build());

            // remove temporary files
            setupVaultHandler.cleanUp();
        }
    }
}

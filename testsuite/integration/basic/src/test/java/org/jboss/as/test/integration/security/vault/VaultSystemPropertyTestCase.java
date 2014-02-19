package org.jboss.as.test.integration.security.vault;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ALLOW_RESOURCE_SERVICE_RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CORE_SERVICE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VAULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VAULT_OPTIONS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.h2.tools.Server;
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
   private static final String VAULT_BLOCK = "props_Block";
   private static final String PASSWORD = "password";


   @Test
   public void testStuff() {
      // Empty method for now.
   }



   /* Inner ServerSetup class to start the vault and add in a value as the system property.*/

   static class VaultSystemPropertyServerSetup implements ServerSetupTask {

      private VaultHandler vaultHandler;

      @Override
      public void setup(ManagementClient client, String containerId) {
         logger.info("RESOURCE_LOCATION = " + RESOURCE_LOCATION);

         ModelNode op = new ModelNode();
         op.get(OP).set(ADD);
         op.get(OP_ADDR).add(CORE_SERVICE, VAULT);
         ModelNode vaultOptions = op.get(VAULT_OPTIONS);

         // Pause for a second, we need to get the VaultHandler going. Basically the vault,
         // we can edit some of the default configurations for it and add in the password.

         vaultHandler = new VaultHandler(RESOURCE_LOCATION);

         // Add in the security attributes.
         String attributeName = "system-property-password";
         String vaultedPasswordString = vaultHandler.addSecuredAttribute(VAULT_BLOCK, attributeName,
               PASSWORD.toCharArray());

         // Now we set the vault options that we have from the handler to our vaultOptions object.
         vaultOptions.get("KEYSTORE_URL").set(vaultHandler.getKeyStore());


      }

      @Override
      public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
         logger.debug("Tearing down.");


      }
   }


}

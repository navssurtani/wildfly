package org.jboss.as.test.integration.ejb.stateless.timerless;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;


/**
* WFLY-28 fixed a bug where the ejb timer service created empty directories for ejb's whether they had a timer or not.
* WFLY-1612 pertains to the writing of the test case to make sure that this bug does not re-occur.
*
* @author navssurtani
*/
@RunWith(Arquillian.class)
public class TimerServiceEmptyDirectoryCreationTestCase {

    @ContainerResource
    private ManagementClient managementClient;

    @Deployment
    public static Archive<?> deploy() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "ejbWithNoTimer.jar");
        jar.addPackage(TimerServiceEmptyDirectoryCreationTestCase.class.getPackage());
        return jar;
    }

    @Test
    public void testBuildEmptyDirectory() {
        String workDir = System.getProperty("user.dir");
        System.out.println("Working directory is: " + workDir);
        String serverHome = workDir.substring(0, workDir.indexOf("workdir")) + "jbossas/";
        File serverDirectory = new File(serverHome);
        Assert.assertTrue(serverDirectory.exists());

        System.out.println("Server home is: " + serverHome);
        assert false;
    }
}

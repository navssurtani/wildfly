package org.jboss.as.test.integration.ejb.stateless.timerless;

import javax.ejb.Remote;

/**
 * @see TimerServiceEmptyDirectoryCreationTestCase
 * @author navssurtani
 */
@Remote
public interface TimerlessRemote {
    public String talk(String s);
}

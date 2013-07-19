package org.jboss.as.test.integration.ejb.stateless.timerless;

import javax.ejb.Stateless;
import java.util.logging.Logger;

/**
 * @see TimerServiceEmptyDirectoryCreationTestCase
 * @author navssurtani
 */
@Stateless
public class TimerlessBean implements TimerlessRemote {

    private static final Logger logger = Logger.getLogger(TimerlessBean.class.getName());

    @Override
    public String talk(String s) {
        logger.finest("Talking.");
        return "Spoken " + s;
    }
}

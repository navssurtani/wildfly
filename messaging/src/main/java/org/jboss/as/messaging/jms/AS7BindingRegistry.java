/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.messaging.jms;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jboss.as.messaging.BinderServiceUtil.installBinderService;
import static org.jboss.as.messaging.logging.MessagingLogger.ROOT_LOGGER;

import java.util.Locale;

import org.hornetq.spi.core.naming.BindingRegistry;
import org.jboss.as.messaging.logging.MessagingLogger;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StabilityMonitor;

/**
 * A {@link BindingRegistry} implementation for JBoss AS7.
 *
 * @author Jason T. Greene
 * @author Jaikiran Pai
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class AS7BindingRegistry implements BindingRegistry {

    private final ServiceContainer container;

    public AS7BindingRegistry(ServiceContainer container) {
        this.container = container;
    }

    @Override
    public Object getContext() {
        // NOOP
        return null;
    }

    @Override
    public void setContext(Object ctx) {
        // NOOP
    }

    @Override
    public Object lookup(String name) {
        // NOOP
        return null;
    }

    @Override
    public boolean bind(String name, Object obj) {
        if (name == null || name.isEmpty()) {
            throw MessagingLogger.ROOT_LOGGER.cannotBindJndiName();
        }
        installBinderService(container, name, obj);
        ROOT_LOGGER.boundJndiName(name);
        return true;
    }

    /**
     * Unbind the resource and wait until the corresponding binding service is effectively removed.
     */
    @Override
    public void unbind(String name) {
        if (name == null || name.isEmpty()) {
            throw MessagingLogger.ROOT_LOGGER.cannotUnbindJndiName();
        }
        final ContextNames.BindInfo bindInfo = ContextNames.bindInfoFor(name);
        ServiceController<?> bindingService = container.getService(bindInfo.getBinderServiceName());
        if (bindingService == null) {
            ROOT_LOGGER.debugf("Cannot unbind %s since no binding exists with that name", name);
            return;
        }
        // remove the binding service
        bindingService.setMode(ServiceController.Mode.REMOVE);
        final StabilityMonitor monitor = new StabilityMonitor();
        monitor.addController(bindingService);
        try {
            monitor.awaitStability();
            ROOT_LOGGER.unboundJndiName(bindInfo.getAbsoluteJndiName());
        } catch (InterruptedException e) {
            ROOT_LOGGER.failedToUnbindJndiName(name, 5, SECONDS.toString().toLowerCase(Locale.US));
        } finally {
            monitor.removeController(bindingService);
        }
    }

    @Override
    public void close() {
        // NOOP
    }
}

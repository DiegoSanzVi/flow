/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.internal.AbstractRouteRegistry;
import com.vaadin.flow.router.internal.RouteConfiguration;

/**
 * SessionRouteRegistry is a mutable route registry that is valid in the scope
 * of VaadinSession. Routes can be added and removed from this registry and any
 * overlap with the registered routes between session and global scope will be
 * handled so that session scope paths overrides global paths.
 */
public class SessionRouteRegistry extends AbstractRouteRegistry {

    // SessionRegistry can not be used without a parentRegistry
    private RouteRegistry parentRegistry;

    private final VaadinSession session;

    /**
     * Package protected constructor for the session route registry.
     * <p>
     * This is only applicable for VaadinSession as no other registry will be
     * taken into consideration.
     *
     * @param session
     *         vaadin session that this session registry is
     * @param service
     *         vaadin service for session lifecycle listening
     */
    SessionRouteRegistry(VaadinSession session, VaadinService service) {
        this.session = session;
        if (service != null) {
            // this is here only due to SerializationTest#testVaadinSession which
            // doesn't accept mocks or service parts as they bring in sun.misc.Launcher$AppClassLoader
            // Get the global route registry from the service
            this.parentRegistry = service.getRouteRegistry();
        }
    }

    /**
     * Get the session registry for VaadinSession. If no SessionRegistry exists
     * then one will be created for given VaadinSession.
     *
     * @param session
     *         vaadin session to get registry for
     * @return session registry for given session
     */
    public static RouteRegistry getSessionRegistry(VaadinSession session) {
        Objects.requireNonNull(session,
                "Null session is not supported for session route registry");
        SessionRouteRegistry registry = session
                .getAttribute(SessionRouteRegistry.class);
        if (registry == null) {
            registry = new SessionRouteRegistry(session, session.getService());
            session.setAttribute(SessionRouteRegistry.class, registry);
        }
        if (!registry.session.equals(session)) {
            throw new IllegalStateException(
                    "Session has as the attribute a route registered to another session");
        }
        return registry;
    }

    @Override
    public List<RouteData> getRegisteredRoutes() {
        List<RouteData> routes = new ArrayList<>(super.getRegisteredRoutes());

        List<RouteData> registeredRoutes = parentRegistry.getRegisteredRoutes();
        if (!registeredRoutes.isEmpty()) {
            Set<String> collect = routes.stream().map(RouteData::getUrl)
                    .collect(Collectors.toSet());
            registeredRoutes.stream()
                    .filter(data -> !collect.contains(data.getUrl()))
                    .forEach(routes::add);
        }

        return routes;
    }

    /**
     * Clear all registered routes from this SessionRouteRegistry.
     */
    public void clear() {
        configure(RouteConfiguration::clear);
    }

    @Override
    public Optional<Class<? extends Component>> getNavigationTarget(
            String pathString) {
        Objects.requireNonNull(pathString, "pathString must not be null.");
        Optional<Class<? extends Component>> navigationTarget = getNavigationTarget(
                pathString, Collections.emptyList());

        if (navigationTarget.isPresent()) {
            return navigationTarget;
        }

        return parentRegistry.getNavigationTarget(pathString);
    }

    @Override
    public Optional<Class<? extends Component>> getNavigationTarget(
            String pathString, List<String> segments) {
        Objects.requireNonNull(pathString, "pathString must not be null.");
        if (getConfiguration().hasRoute(pathString, segments)) {
            return getConfiguration().getRoute(pathString, segments);
        }

        return parentRegistry.getNavigationTarget(pathString, segments);
    }

    @Override
    public Optional<String> getTargetUrl(
            Class<? extends Component> navigationTarget) {

        Optional<String> targetUrl = super.getTargetUrl(navigationTarget);
        if (targetUrl.isPresent()) {
            return targetUrl;
        }

        return parentRegistry.getTargetUrl(navigationTarget);
    }

    @Override
    public List<Class<? extends RouterLayout>> getRouteLayouts(String path,
            Class<? extends Component> navigationTarget) {
        if (getConfiguration().hasRoute(path)) {
            return super.getRouteLayouts(path, navigationTarget);
        }
        return parentRegistry.getRouteLayouts(path, navigationTarget);
    }
}

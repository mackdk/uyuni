/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.reflect.TypeToken;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import spark.Route;
import spark.Spark;

public final class IssSparkHelper {

    private static final Logger LOGGER = LogManager.getLogger(IssSparkHelper.class);

    private static final HubFactory HUB_FACTORY = new HubFactory();

    private IssSparkHelper() {
        // Prevent instantiation
    }

    /**
     * Use in ISS routes to specify the method requires api key authentication
     *
     * @param route the route
     * @return the route
     */
    public static Route usingTokenAuthentication(RouteWithIssToken route) {
        return (request, response) -> {
            String authorization = request.headers("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer")) {
                Spark.halt(HttpServletResponse.SC_BAD_REQUEST);
            }

            String serializedToken = authorization.substring(7);
            IssAccessToken issuedToken = HUB_FACTORY.lookupIssuedToken(serializedToken);

            if (issuedToken == null || issuedToken.isExpired() || !issuedToken.isValid()) {
                response.status(HttpServletResponse.SC_UNAUTHORIZED);
                return json(response, ResultJson.error("Invalid token provided"), new TypeToken<>() { });
            }

            try {
                Token token = issuedToken.getParsedToken();
                String fqdn = token.getClaim("fqdn", String.class);
                if (fqdn == null || !fqdn.equals(issuedToken.getServerFqdn())) {
                    response.status(HttpServletResponse.SC_UNAUTHORIZED);
                    return json(response, ResultJson.error("Invalid token provided"), new TypeToken<>() { });
                }

                return route.handle(request, response, issuedToken);
            }
            catch (TokenParsingException ex) {
                response.status(HttpServletResponse.SC_UNAUTHORIZED);
                return json(response, ResultJson.error("Invalid token provided"), new TypeToken<>() { });
            }
            finally {
                var authenticationService = AuthenticationServiceFactory.getInstance().getAuthenticationService();
                authenticationService.invalidate(request.raw(), response.raw());
            }
        };
    }

    /**
     * Use in ISS routes only accessible from a hub
     * @param route the route
     * @return the route
     */
    public static RouteWithIssToken allowingOnlyHub(RouteWithIssToken route) {
        return allowingOnly(List.of(IssRole.HUB), route);
    }

    /**
     * Use in ISS routes only accessible from a peripheral
     * @param route the route
     * @return the route
     */
    public static RouteWithIssToken allowingOnlyPeripheral(RouteWithIssToken route) {
        return allowingOnly(List.of(IssRole.PERIPHERAL), route);
    }

    /**
     * Use in ISS routes only accessible from an unregistered server
     * @param route the route
     * @return the route
     */
    public static RouteWithIssToken allowingOnlyUnregistered(RouteWithIssToken route) {
        return allowingOnly(List.of(), route);
    }

    private static RouteWithIssToken allowingOnly(List<IssRole> allowedRoles, RouteWithIssToken route) {
        return (request, response, issAccessToken) -> {
            String fqdn = issAccessToken.getServerFqdn();
            Optional<IssHub> issHub = HUB_FACTORY.lookupIssHubByFqdn(fqdn);
            Optional<IssPeripheral> issPeripheral = HUB_FACTORY.lookupIssPeripheralByFqdn(fqdn);

            if (isRouteForbidden(allowedRoles, issHub.isPresent(), issPeripheral.isPresent())) {
                response.status(HttpServletResponse.SC_FORBIDDEN);
                return json(response, ResultJson.error("Token does not allow access to this resource"),
                    new TypeToken<>() { });

            }

            return route.handle(request, response, issAccessToken);
        };
    }

    private static boolean isRouteForbidden(List<IssRole> allowedRoles, boolean isHub, boolean isPeripheral) {
        // No role allowed, only unregistered server can use this route
        if (allowedRoles.isEmpty() && (isHub || isPeripheral)) {
            return true;
        }

        // Only hub servers are allowed
        if (allowedRoles.contains(IssRole.HUB) && !isHub) {
            return true;
        }

        // Only peripheral servers are allowed
        if (allowedRoles.contains(IssRole.PERIPHERAL) && !isPeripheral) {
            return true;
        }

        return false;
    }
}
/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub;

import static com.suse.manager.hub.IssSparkHelper.allowingOnlyHub;
import static com.suse.manager.hub.IssSparkHelper.allowingOnlyPeripheral;
import static com.suse.manager.hub.IssSparkHelper.allowingOnlyUnregistered;
import static com.suse.manager.hub.IssSparkHelper.usingTokenAuthentication;
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.badRequest;
import static com.suse.manager.webui.utils.SparkApplicationHelper.message;
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
import static spark.Spark.post;

import com.redhat.rhn.domain.credentials.HubSCCCredentials;

import com.suse.manager.model.hub.IssAccessToken;
import com.suse.manager.model.hub.RegisterJson;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;
import com.suse.manager.webui.utils.token.TokenParsingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import spark.Request;
import spark.Response;

public class SyncController {

    private static final Logger LOGGER = LogManager.getLogger(SyncController.class);

    private final HubManager hubManager;

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    /**
     * Default constructor
     */
    public SyncController() {
        this(new HubManager());
    }

    /**
     * Builds an instance with the specified hub manager
     * @param hubManagerIn the hub manager
     */
    public SyncController(HubManager hubManagerIn) {
        this.hubManager = hubManagerIn;
    }

    /**
     * Initialize the API routes
     */
    public void initRoutes() {
        post("/iss/sync/ping", asJson(usingTokenAuthentication(this::ping)));
        post("/iss/sync/register", asJson(usingTokenAuthentication(allowingOnlyUnregistered(this::register))));
        post("/iss/sync/storeCredentials", asJson(usingTokenAuthentication(allowingOnlyHub(this::storeCredentials))));
        post("/iss/sync/generateCredentials",
            asJson(usingTokenAuthentication(allowingOnlyPeripheral(this::generateCredentials))));
    }

    // Basic ping to check if the system is up
    private String ping(Request request, Response response, IssAccessToken token) {
        return message(response, "Pinged from %s".formatted(token.getServerFqdn()));
    }

    private String register(Request request, Response response, IssAccessToken token) {
        RegisterJson registerRequest = GSON.fromJson(request.body(), RegisterJson.class);

        String tokenToStore = registerRequest.getToken();
        if (StringUtils.isEmpty(tokenToStore)) {
            LOGGER.error("No token received in the request for server {}", token.getServerFqdn());
            return badRequest(response, "Required token is missing");
        }

        try {
            hubManager.storeAccessToken(token, tokenToStore);
            hubManager.saveNewServer(token, registerRequest.getRole(), registerRequest.getRootCA());

            return success(response);
        }
        catch (TokenParsingException ex) {
            LOGGER.error("Unable to parse the received token for server {}", token.getServerFqdn());
            return badRequest(response, "The specified token is not parseable");
        }
    }

    private String generateCredentials(Request request, Response response, IssAccessToken token) {
        try {
            HubSCCCredentials credentials = hubManager.generateSCCCredentials(token);
            return success(response, new SCCCredentialsJson(credentials.getUsername(), credentials.getPassword()));
        }
        catch (IllegalArgumentException ex) {
            // This should never happen, fqdn guaranteed be a peripheral after calling allowingOnlyPeripheral() when
            // initializing the route.
            return badRequest(response, "Specified FQDN is not a known peripheral");
        }
    }

    private String storeCredentials(Request request, Response response, IssAccessToken token) {
        SCCCredentialsJson storeRequest = GSON.fromJson(request.body(), SCCCredentialsJson.class);

        try {
            hubManager.storeSCCCredentials(token, storeRequest.getUsername(), storeRequest.getPassword());
            return success(response);
        }
        catch (IllegalArgumentException ex) {
            // This should never happen, fqdn guaranteed be a hub after calling allowingOnlyHub() on route init.
            return badRequest(response, "Specified FQDN is not a known hub");
        }
    }
}
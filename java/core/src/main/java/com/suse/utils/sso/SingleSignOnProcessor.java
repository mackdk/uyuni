/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
/*
 * This file contains code derived from the OneLogin Java SAML Toolkit,
 * licensed under the MIT license.
 *
 * Copyright (c) 2010-2016 OneLogin, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.suse.utils.sso;

import com.redhat.rhn.common.util.ServletUtils;

import com.onelogin.saml2.authn.AuthnRequest;
import com.onelogin.saml2.authn.AuthnRequestParams;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.http.HttpRequest;
import com.onelogin.saml2.logout.LogoutRequest;
import com.onelogin.saml2.logout.LogoutResponse;
import com.onelogin.saml2.logout.LogoutResponseParams;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Constants;
import com.onelogin.saml2.util.Util;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Single Sign On Processing logic. This class is a refactoring and a simplification for Uyuni use case of
 * {@code com.onelogin.saml2.Auth} provided by OneLogin Java SAML library, which was needed due to the incompatibility
 * of the original code with the Jakarta Servlet API.
 */
public class SingleSignOnProcessor {

    private static final Logger LOGGER = LogManager.getLogger(SingleSignOnProcessor.class);

    private final Saml2Settings settings;

    /**
     * Default constructor.
     *
     * @param settingsIn the saml settings
     */
    public SingleSignOnProcessor(Saml2Settings settingsIn) {
        this.settings = settingsIn;
    }

    /**
     * Initiates the SSO process.
     * @param returnTo the url to return to
     * @param request the request
     * @param response the response
     * @return the redirect location
     * @throws SingleSignOnException when the request cannot be processed
     * @throws IOException when the saml request cannot be extracted
     * @throws URISyntaxException when the redirect location url is not valid
     */
    public String login(String returnTo, HttpServletRequest request, HttpServletResponse response)
            throws SingleSignOnException, IOException, URISyntaxException {
        Map<String, String> parameters = new LinkedHashMap<>();

        AuthnRequest authnRequest = new AuthnRequest(settings, new AuthnRequestParams(false, false, true));
        String samlRequest = authnRequest.getEncodedAuthnRequest();
        parameters.put("SAMLRequest", samlRequest);

        String relayState = Objects.requireNonNullElseGet(returnTo, () -> ServletUtils.getAbsoluteRequestUrl(request));
        if (!relayState.isEmpty()) {
            parameters.put("RelayState", relayState);
        }

        if (settings.getAuthnRequestsSigned()) {
            String sigAlg = settings.getSignatureAlgorithm();
            String signature = buildSignature(samlRequest, relayState, sigAlg, "SAMLRequest");
            parameters.put("SigAlg", sigAlg);
            parameters.put("Signature", signature);
        }

        String ssoUrl = settings.getIdpSingleSignOnServiceUrl().toString();

        LOGGER.debug("AuthNRequest sent to {} --> {}", ssoUrl, samlRequest);
        return ServletUtils.sendRedirect(response, ssoUrl, parameters);
    }

    /**
     * Process the SAML Response sent by the IdP.
     * @param request the request
     * @return the saml response
     * @throws SingleSignOnException if the request cannot be processed
     */
    public SamlResponse processAuthResponse(HttpServletRequest request) throws SingleSignOnException {
        HttpRequest samlRequest = asSamlRequest(request);
        String samlResponseParameter = samlRequest.getParameter("SAMLResponse");

        if (samlResponseParameter == null) {
            LOGGER.error("SAML Response not found, Only supported HTTP_POST Binding");
            throw new SingleSignOnException("SAML Response not found, Only supported HTTP_POST Binding");
        }

        try {
            return new SamlResponse(settings, samlRequest);
        }
        catch (Exception ex) {
            throw new SingleSignOnException("Unable to create SamlResponse", ex);
        }
    }

    /**
     * Process the SAML Logout Response / Logout Request sent by the IdP.
     * @param request the request
     * @param response the response
     * @throws Exception when processing the request fails
     */
    public void processSLO(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpRequest httpRequest = asSamlRequest(request);

        String samlRequestParameter = httpRequest.getParameter("SAMLRequest");
        String samlResponseParameter = httpRequest.getParameter("SAMLResponse");

        if (samlResponseParameter != null) {
            LogoutResponse logoutResponse = new LogoutResponse(settings, httpRequest);
            boolean valid = BooleanUtils.isTrue(logoutResponse.isValid());

            if (!valid) {
                LOGGER.error("processSLO error. invalid_logout_response");
                LOGGER.debug(" --> {}", samlResponseParameter);
            }
            else {
                String status = logoutResponse.getStatus();
                if (status != null && status.equals(Constants.STATUS_SUCCESS)) {
                    LOGGER.debug("processSLO success --> {}", samlResponseParameter);
                    request.getSession().invalidate();
                }
                else {
                    LOGGER.error("processSLO error. logout_not_success");
                    LOGGER.debug(" --> {}", samlResponseParameter);
                }
            }
        }
        else {
            if (samlRequestParameter == null) {
                throw new SingleSignOnException("SAML LogoutRequest/LogoutResponse not found. " +
                                                    "Only supported HTTP_REDIRECT Binding");
            }

            LogoutRequest logoutRequest = new LogoutRequest(settings, httpRequest);
            boolean valid = BooleanUtils.isTrue(logoutRequest.isValid());

            if (!valid) {
                LOGGER.error("processSLO error. invalid_logout_request");
                LOGGER.debug(" --> {}", samlRequestParameter);
            }
            else {
                LOGGER.debug("processSLO success --> {}", samlRequestParameter);
                request.getSession().invalidate();

                String inResponseTo = logoutRequest.getId();
                LogoutResponseParams params = new LogoutResponseParams(inResponseTo, Constants.STATUS_SUCCESS);
                LogoutResponse logoutResponseBuilder = new LogoutResponse(settings, params);
                String samlLogoutResponse = logoutResponseBuilder.getEncodedLogoutResponse();
                Map<String, String> parameters = new LinkedHashMap<>();
                parameters.put("SAMLResponse", samlLogoutResponse);
                String relayState = request.getParameter("RelayState");
                if (relayState != null) {
                    parameters.put("RelayState", relayState);
                }

                if (settings.getLogoutResponseSigned()) {
                    String sigAlg = settings.getSignatureAlgorithm();
                    String signature = buildSignature(samlLogoutResponse, relayState, sigAlg, "SAMLResponse");
                    parameters.put("SigAlg", sigAlg);
                    parameters.put("Signature", signature);
                }

                String sloUrl = settings.getIdpSingleLogoutServiceResponseUrl().toString();
                LOGGER.debug("Logout response sent to {} --> {}", sloUrl, samlLogoutResponse);
                ServletUtils.sendRedirect(response, sloUrl, parameters);
            }
        }
    }

    /**
     * Initiates the SLO process.
     * @param request the request
     * @param response the response
     * @return the redirect location
     * @throws SingleSignOnException when the logout request cannot be sent
     */
    public String logout(HttpServletRequest request, HttpServletResponse response) throws SingleSignOnException {
        Map<String, String> parameters = new HashMap<>();

        try {
            LogoutRequest logoutRequest = new LogoutRequest(settings);
            String samlLogoutRequest = logoutRequest.getEncodedLogoutRequest();
            parameters.put("SAMLRequest", samlLogoutRequest);
            String relayState = ServletUtils.getAbsoluteRequestUrl(request);

            if (!relayState.isEmpty()) {
                parameters.put("RelayState", relayState);
            }

            if (settings.getLogoutRequestSigned()) {
                String sigAlg = settings.getSignatureAlgorithm();
                String signature = buildSignature(samlLogoutRequest, relayState, sigAlg, "SAMLRequest");
                parameters.put("SigAlg", sigAlg);
                parameters.put("Signature", signature);
            }

            String sloUrl = settings.getIdpSingleLogoutServiceUrl().toString();
            LOGGER.debug("Logout request sent to {} --> {}", sloUrl, samlLogoutRequest);

            return ServletUtils.sendRedirect(response, sloUrl, parameters);
        }
        catch (Exception ex) {
            throw new SingleSignOnException("Unable to send logout request", ex);
        }
    }

    private String buildSignature(String samlMessage, String relayState, String signAlgorithm, String type)
            throws SingleSignOnException {
        if (!settings.checkSPCerts()) {
            throw new SingleSignOnException("Trying to sign the " + type + " but can't load the SP private key");
        }

        PrivateKey key = settings.getSPkey();

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(type).append("=").append(Util.urlEncoder(samlMessage));
        if (StringUtils.isNotEmpty(relayState)) {
            messageBuilder.append("&RelayState=").append(Util.urlEncoder(relayState));
        }

        String algorithm = StringUtils.defaultIfEmpty(signAlgorithm, Constants.RSA_SHA1);
        messageBuilder.append("&SigAlg=").append(Util.urlEncoder(algorithm));

        try {
            return Util.base64encoder(Util.sign(messageBuilder.toString(), key, algorithm));
        }
        catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new SingleSignOnException("Unable to compute signature for " + type, e);
        }
    }

    private static HttpRequest asSamlRequest(HttpServletRequest req) {
        Map<String, String[]> paramsAsArray = req.getParameterMap();
        Map<String, List<String>> paramsAsList = new HashMap<>();

        for (Map.Entry<String, String[]> param : paramsAsArray.entrySet()) {
            paramsAsList.put(param.getKey(), Arrays.asList(param.getValue()));
        }

        return new HttpRequest(req.getRequestURL().toString(), paramsAsList, req.getQueryString());
    }
}

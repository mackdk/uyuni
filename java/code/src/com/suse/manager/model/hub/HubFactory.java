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
package com.suse.manager.model.hub;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class HubFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(HubFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Save a {@link IssHub} object
     * @param issHubIn object to save
     */
    public void save(IssHub issHubIn) {
        saveObject(issHubIn);
    }

    /**
     * Save a {@link IssPeripheral} object
     * @param issPeripheralIn object to save
     */
    public void save(IssPeripheral issPeripheralIn) {
        saveObject(issPeripheralIn);
    }

    /**
     * Save a {@link IssPeripheralChannels} object
     * @param issPeripheralChannelsIn object to save
     */
    public void save(IssPeripheralChannels issPeripheralChannelsIn) {
        saveObject(issPeripheralChannelsIn);
    }

    /**
     * Lookup {@link IssHub} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssHub} with the given FQDN or empty
     */
    public Optional<IssHub> lookupIssHubByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssHub WHERE fqdn = :fqdn", IssHub.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * Lookup {@link IssPeripheral} object by its FQDN
     * @param fqdnIn the fqdn
     * @return return {@link IssPeripheral} with the given FQDN or empty
     */
    public Optional<IssPeripheral> lookupIssPeripheralByFqdn(String fqdnIn) {
        return getSession().createQuery("FROM IssPeripheral WHERE fqdn = :fqdn", IssPeripheral.class)
                .setParameter("fqdn", fqdnIn)
                .uniqueResultOptional();
    }

    /**
     * List {@link IssPeripheralChannels} objects which reference the given {@link Channel}
     * @param channelIn the channel
     * @return return the list of {@link IssPeripheralChannels} objects
     */
    public List<IssPeripheralChannels> listIssPeripheralChannelsByChannels(Channel channelIn) {
        return getSession()
                .createQuery("FROM IssPeripheralChannels WHERE channel = :channel", IssPeripheralChannels.class)
                .setParameter("channel", channelIn)
                .list();
    }

    /**
     * Store a new access token
     * @param fqdn the FQDN of the server
     * @param token the token to establish a connection with the specified server
     * @param type the type of token
     * @param expiration when the token is no longer valid
     */
    public void saveToken(String fqdn, String token, TokenType type, Instant expiration) {
        // Lookup if this association already exists
        IssAccessToken accessToken = lookupAccessTokenByFqdnAndType(fqdn, type);
        if (accessToken == null) {
            accessToken = new IssAccessToken(type, token, fqdn, expiration);
        }
        else {
            accessToken.setToken(token);
            accessToken.setValid(true);
            accessToken.setExpirationDate(Date.from(expiration));
        }

        // Store the new token
        getSession().saveOrUpdate(accessToken);
    }

    /**
     * Returns the issued access token information matching the given token
     * @param token the string representation of the token
     * @return the issued token, if present
     */
    public IssAccessToken lookupIssuedToken(String token) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.token = :token", IssAccessToken.class)
            .setParameter("type", TokenType.ISSUED)
            .setParameter("token", token)
            .uniqueResult();
    }

    /**
     * Returns the access token for the specified FQDN
     * @param fqdn the FQDN of the peripheral/hub
     * @return the access token associated to the entity, if present
     */
    public IssAccessToken lookupAccessTokenFor(String fqdn) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.serverFqdn = :fqdn", IssAccessToken.class)
            .setParameter("type", TokenType.CONSUMED)
            .setParameter("fqdn", fqdn)
            .uniqueResult();
    }

    /**
     * Returns the access token of the given type for the specified FQDN
     * @param fqdn the FQDN of the peripheral/hub
     * @param type the type of token
     * @return the access token associated to the entity, if present
     */
    public IssAccessToken lookupAccessTokenByFqdnAndType(String fqdn, TokenType type) {
        return getSession()
            .createQuery("FROM IssAccessToken k WHERE k.type = :type AND k.serverFqdn = :fqdn", IssAccessToken.class)
            .setParameter("type", type)
            .setParameter("fqdn", fqdn)
            .uniqueResult();
    }
}
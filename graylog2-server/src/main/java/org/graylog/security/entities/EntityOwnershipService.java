/**
 * This file is part of Graylog.
 *
 * Graylog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.graylog.security.entities;

import org.graylog.security.Capability;
import org.graylog.security.DBGrantService;
import org.graylog.security.GrantDTO;
import org.graylog2.contentpacks.model.ModelTypes;
import org.graylog2.plugin.database.users.User;
import org.graylog2.utilities.GRN;
import org.graylog2.utilities.GRNRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EntityOwnershipService {
    private static final Logger LOG = LoggerFactory.getLogger(EntityOwnershipService.class);

    private final DBGrantService dbGrantService;
    private final GRNRegistry grnRegistry;


    @Inject
    public EntityOwnershipService(DBGrantService dbGrantService,
                                  GRNRegistry grnRegistry) {
        this.dbGrantService = dbGrantService;
        this.grnRegistry = grnRegistry;
    }

    public void registerNewEventDefinition(String id, User user) {
        final GRN grn = grnRegistry.newGRN(ModelTypes.EVENT_DEFINITION_V1.name(), id);
        registerNewEntity(grn, user);
    }

    public void registerNewView(String id, User user) {
        final GRN grn = grnRegistry.newGRN(ModelTypes.DASHBOARD_V2.name(), id);
        registerNewEntity(grn, user);
    }

    private void registerNewEntity(GRN entity, User user) {
        // Don't create ownership grants for the admin user.
        // They can access anything anyhow
        if (user.isLocalAdmin()) {
            return;
        }

        dbGrantService.create(GrantDTO.builder()
                .capability(Capability.OWN)
                .target(entity)
                .grantee(grnRegistry.ofUser(user))
                .build(), user);
    }
}

package fr.gouv.vitamui.iam.internal.server.common.rest;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;

/**
 * A status controller mock.
 *
 *
 */
public class MockStatusController extends BaseStatusController {

    @Override
    protected Map<String, VitamUIRepository> getRepositories() {
        final Map<String, VitamUIRepository> map = new HashMap<>();
        map.put("Test", mock(VitamUIRepository.class));
        return map;
    }
}

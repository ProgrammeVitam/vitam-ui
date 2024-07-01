package fr.gouv.vitamui.iam.internal.server.common.rest;

import fr.gouv.vitamui.commons.mongo.repository.VitamUIRepository;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

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

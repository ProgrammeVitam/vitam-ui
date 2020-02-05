package fr.gouv.vitamui.ui.commons.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;

import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.rest.client.BaseCrudRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;

public abstract class AbstractCrudServiceTest<T extends IdDto> {

    protected static final String ID = "ID";

    public T createEntite() {
        Mockito.when(getClient().create(any(), any())).thenReturn(buildDto(ID));
        final T dto = getService().create(null, buildDto(null));
        assertThat(dto).isNotNull();
        return dto;
    }

    public T updateEntite() {
        Mockito.when(getClient().update(any(), any())).thenReturn(buildDto(ID));
        final T dtoUpdate = buildDto(null);
        dtoUpdate.setId(ID);
        final T dto = getService().update(null, dtoUpdate);
        assertThat(dto).isNotNull();
        return dto;
    }

    public T updateWithIdEmpty() {
        Mockito.when(getClient().update(any(), any())).thenReturn(buildDto(null));
        final T dto = getService().update(null, buildDto(null));
        assertThat(dto).isNotNull();
        return dto;
    }

    public T patch() {
        Mockito.when(getClient().patch(any(), any())).thenReturn(buildDto(null));
        final Map<String, Object> updates = new HashMap<>();
        updates.put("id", ID);
        final T dto = getService().patch(null, updates, ID);
        return dto;
    }

    public void delete() {
        getService().delete(null, ID);
    }

    protected abstract <C extends ExternalHttpContext> BaseCrudRestClient<T, C> getClient();

    protected abstract T buildDto(String id);

    protected abstract AbstractCrudService<T> getService();
}

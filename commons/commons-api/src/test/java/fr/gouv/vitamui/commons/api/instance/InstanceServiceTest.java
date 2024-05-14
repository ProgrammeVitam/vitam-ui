package fr.gouv.vitamui.commons.api.instance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InstanceServiceTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private InstanceService service;

    @Test
    void shouldBeTrue_whenPropertiesIsTrue() {
        given(environment.getProperty(anyString(), any(Class.class))).willReturn(true);
        assertThat(service.isPrimary()).isTrue();
    }

    @Test
    void shouldBeTrue_whenPropertiesIsMissing() {
        given(environment.getProperty(anyString(), any(Class.class))).willReturn(null);
        assertThat(service.isPrimary()).isTrue();
    }

    @Test
    void shouldBeTrue_whenPropertiesIsFalse() {
        given(environment.getProperty(anyString(), any(Class.class))).willReturn(false);
        assertThat(service.isPrimary()).isFalse();
    }
}

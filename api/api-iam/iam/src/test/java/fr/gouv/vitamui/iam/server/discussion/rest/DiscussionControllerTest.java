package fr.gouv.vitamui.iam.server.discussion.rest;

import fr.gouv.vitamui.iam.server.discussion.service.DiscussionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscussionControllerTest {

    @InjectMocks
    private DiscussionController discussionController;

    @Mock
    private DiscussionService discussionService;

    @Test
    void shouldReturnEntityIdsWithUnreadDiscussions() {
        // GIVEN
        when(
            discussionService.findEntityIdsWithUnreadDiscussions(EntityType.TRANSACTION, List.of("id1", "id2"))
        ).thenReturn(Set.of("id1"));
        DiscussionController.FindUnreadDiscussionsRequest request =
            new DiscussionController.FindUnreadDiscussionsRequest();
        request.setEntityType(EntityType.TRANSACTION);
        request.setEntityIds(List.of("id1", "id2"));

        // WHEN
        Set<String> result = discussionController.findEntityIdsWithUnreadDiscussions(request);

        // THEN
        assertThat(result).containsExactly("id1");
    }
}

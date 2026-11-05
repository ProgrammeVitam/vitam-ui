package fr.gouv.vitamui.pastis.common.util;

import fr.gouv.vitamui.pastis.common.dto.ElementRNG;
import org.junit.jupiter.api.Test;
import org.xml.sax.helpers.AttributesImpl;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class PastisSAX2HandlerTest {

    @Test
    void should_keep_whole_documentation_split_in_many_chunks() {
        final int chunkCount = 300_000;
        final PastisSAX2Handler handler = new PastisSAX2Handler();
        final char[] chunk = { 'a' };

        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            handler.startDocument();
            handler.startElement("", "element", "rng:element", attributes("Title"));
            handler.startElement("", "documentation", "xsd:documentation", new AttributesImpl());
            for (int i = 0; i < chunkCount; i++) {
                handler.characters(chunk, 0, 1);
            }
            handler.endElement("", "documentation", "xsd:documentation");
            handler.endElement("", "element", "rng:element");
        });

        final ElementRNG documentation = handler.getElementRNGRoot().getChildren().get(0).getChildren().get(0);
        assertThat(documentation.getValue()).hasSize(chunkCount);
    }

    private static AttributesImpl attributes(final String name) {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "name", "name", "CDATA", name);
        return attributes;
    }
}

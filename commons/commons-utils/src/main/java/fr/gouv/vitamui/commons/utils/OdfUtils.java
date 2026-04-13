package fr.gouv.vitamui.commons.utils;

import org.odftoolkit.odfdom.dom.attribute.office.OfficeValueTypeAttribute;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.simple.table.Cell;

public final class OdfUtils {

    private OdfUtils() {}

    /**
     * Utility methods for writing content into ODF cells without triggering AWT.
     *
     * Problem:
     * - Cell#setStringValue() indirectly invokes AWT via optimizeCellSize()
     * (through setDisplayTextContent() => JTextField => getFontMetrics()).
     * - In environments without system fonts (e.g. Alpine / minimal JRE),
     * this leads to runtime failures:
     * "Fontconfig head is null, check your fonts or fonts configuration".
     *
     * Solution:
     * - This utility writes directly into the ODT XML DOM:
     *  . sets the value type to STRING
     *  . sets the office:string-value attribute
     *  . creates the text paragraph node
     * - This bypasses any AWT dependency.
     *
     * Usage:
     * - Always use this method instead of Cell#setStringValue().
     *
     * For more details, see:
     * <a href="https://assistance.programmevitam.fr/plugins/tracker/?aid=15864">...</a>
     */
    public static void setCellText(Cell cell, String text) {
        if (text == null) {
            text = "";
        }

        cell.getOdfElement().setOfficeStringValueAttribute(text);
        cell.getOdfElement().setOfficeValueTypeAttribute(OfficeValueTypeAttribute.Value.STRING.toString());

        OdfFileDom dom = (OdfFileDom) cell.getOdfElement().getOwnerDocument();
        OdfTextParagraph paragraph = new OdfTextParagraph(dom);
        paragraph.setTextContent(text);

        cell.getOdfElement().appendChild(paragraph);
    }
}

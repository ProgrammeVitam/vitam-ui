package fr.gouv.vitamui.referential.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
public class ExportAccessionRegisterResultParam {

    //Export CSV headers
    //dateEntree;ID;servProd;servVers;contratEntree;modeEntree;statutJur;nbreArt;gpeobjElec;objElec;volElec;statut

    //French
    public static final String FR_INGEST_DATE = "dateEntree";
    private static final String FR_GUID_FIELD_HEADER_NAME = "ID";
    private static final String FR_ORIGINATING_AGENCY = "servProd";
    private static final String FR_SUBMISION_AGENCY = "servVers";
    private static final String FR_INGEST_CONTRACT = "contratEntree";
    private static final String FR_INGEST_MODE = "modeEntree";
    private static final String FR_LEGAL_STATUS = "statutJur";
    private static final String FR_NBRE_ART = "nbreArt";
    private static final String FR_GPE_OBJ_ELEC = "gpeobjElec";
    private static final String FR_OBJ_ELEC = "objElec";
    private static final String FR_VOL_ELEC = "volElec";
    private static final String FR_STATUT = "statut";

    //English
    public static final String EN_INGEST_DATE = "dateEntree";
    private static final String EN_GUID_FIELD_HEADER_NAME = "ID";
    private static final String EN_ORIGINATING_AGENCY = "servProd";
    private static final String EN_SUBMISION_AGENCY = "servVers";
    private static final String EN_INGEST_CONTRACT = "contratEntree";
    private static final String EN_INGEST_MODE = "modeEntree";
    private static final String EN_LEGAL_STATUS = "statutJur";
    private static final String EN_NBRE_ART = "nbreArt";
    private static final String EN_GPE_OBJ_ELEC = "gpeobjElec";
    private static final String EN_OBJ_ELEC = "objElec";
    private static final String EN_VOL_ELEC = "volElec";
    private static final String EN_STATUT = "statut";
    //Date patterns
    private static final String FR_PATTERN_DATE = "dd/MM/yyyy";
    private static final String EN_PATTERN_DATE = "MM/dd/yyyy";

    private String patternDate;
    private List<String> headers;
    private char separator = ';';

    public ExportAccessionRegisterResultParam(Locale locale) {
        if (locale.equals(Locale.FRENCH)) {
            this.headers = List.of(
                FR_GUID_FIELD_HEADER_NAME,
                FR_INGEST_DATE,
                FR_ORIGINATING_AGENCY,
                FR_SUBMISION_AGENCY,
                FR_INGEST_CONTRACT,
                FR_INGEST_MODE,
                FR_LEGAL_STATUS,
                FR_NBRE_ART,
                FR_GPE_OBJ_ELEC,
                FR_OBJ_ELEC,
                FR_VOL_ELEC,
                FR_STATUT
            );
            this.patternDate = FR_PATTERN_DATE;
        } else if (locale.equals(Locale.ENGLISH)) {
            this.headers = List.of(
                EN_GUID_FIELD_HEADER_NAME,
                EN_INGEST_DATE,
                EN_ORIGINATING_AGENCY,
                EN_SUBMISION_AGENCY,
                EN_INGEST_CONTRACT,
                EN_INGEST_MODE,
                EN_LEGAL_STATUS,
                EN_NBRE_ART,
                EN_GPE_OBJ_ELEC,
                EN_OBJ_ELEC,
                EN_VOL_ELEC,
                EN_STATUT
            );
            this.patternDate = EN_PATTERN_DATE;
        }
    }
}

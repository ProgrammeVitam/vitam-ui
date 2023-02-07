/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2021)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/


package fr.gouv.vitamui.pastis.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.pastis.common.dto.ElementProperties;
import fr.gouv.vitamui.pastis.common.dto.jaxb.AnnotationXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.AnyNameXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.AttributeXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.BaliseXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.ChoiceXml;
import fr.gouv.vitamui.pastis.common.dto.jaxb.DataXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.DocumentationXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.ElementXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.ExceptXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.GrammarXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.NsNameXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.OneOrMoreXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.OptionalXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.StartXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.ValueXML;
import fr.gouv.vitamui.pastis.common.dto.jaxb.ZeroOrMoreXML;
import fr.gouv.vitamui.pastis.common.dto.profiles.Notice;
import fr.gouv.vitamui.pastis.common.dto.profiles.PastisProfile;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileNotice;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileResponse;
import fr.gouv.vitamui.pastis.common.dto.profiles.ProfileType;
import fr.gouv.vitamui.pastis.common.exception.TechnicalException;
import fr.gouv.vitamui.pastis.common.service.JsonFromPUA;
import fr.gouv.vitamui.pastis.common.service.PuaFromJSON;
import fr.gouv.vitamui.pastis.common.service.PuaPastisValidator;
import fr.gouv.vitamui.pastis.common.util.NoticeUtils;
import fr.gouv.vitamui.pastis.common.util.PastisCustomCharacterEscapeHandler;
import fr.gouv.vitamui.pastis.common.util.PastisGetXmlJsonTree;
import fr.gouv.vitamui.pastis.common.util.PastisSAX2Handler;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@Service
public class PastisService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(PastisService.class);

    private static final String APPLICATION_JSON_UTF8 = "application/json; charset=utf-8";
    private final ResourceLoader resourceLoader;
    @Value("${rng.base.file}")
    private String rngFile;
    @Value("${json.template.fileStandalone}")
    private String jsonFileStandalone;
    @Value("${json.template.fileVitam}")
    private String jsonFileVitam;
    @Value("${rng.base.directory}")
    private String rngLocation;
    private final PuaPastisValidator puaPastisValidator;

    private final JsonFromPUA jsonFromPUA;

    private final PuaFromJSON puaFromJSON;
    private List<PastisProfile> pastisProfiles = new ArrayList<>();
    private List<Notice> notices = new ArrayList<>();

    private Random rand;

    @Autowired
    public PastisService(ResourceLoader resourceLoader, PuaPastisValidator puaPastisValidator, JsonFromPUA jsonFromPUA,
        PuaFromJSON puaFromJSON) {
        this.resourceLoader = resourceLoader;
        this.puaPastisValidator = puaPastisValidator;
        this.jsonFromPUA = jsonFromPUA;
        this.puaFromJSON = puaFromJSON;
    }

    public String getArchiveProfile(final ElementProperties json) throws TechnicalException {

        // Recover a statically generated BaliseXML by buildBaliseXMLTree
        json.initTree(json);
        BaliseXML.buildBaliseXMLTree(json, 0, null);
        // Add Recip struct to xml balises tree
        BaliseXML.addRecipTags();
        BaliseXML eparentRng = BaliseXML.getBaliseXMLStatic();
        String response;
        try (
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            JAXBContext contextObj = JAXBContext.newInstance(AttributeXML.class, ElementXML.class, DataXML.class,
                ValueXML.class, OptionalXML.class, OneOrMoreXML.class,
                ZeroOrMoreXML.class, AnnotationXML.class, DocumentationXML.class,
                StartXML.class, GrammarXML.class, ChoiceXml.class, AnyNameXML.class, ExceptXML.class, NsNameXML.class);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshallerObj.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler",
                new PastisCustomCharacterEscapeHandler());
            marshallerObj.marshal(eparentRng, writer);
            response = os.toString(StandardCharsets.UTF_8);
        } catch (JAXBException | IOException e) {
            throw new TechnicalException("Failure RNG Profile could not be generated", e);
        }
        LOGGER.debug("RNG profile generated successfully");
        return response;
    }


    public String getArchiveUnitProfile(final ProfileNotice json, final boolean standalone) throws TechnicalException {
        Notice notice = new Notice();
        if (!standalone && json.getNotice() != null) {
            notice = json.getNotice();

        }
        String controlSchema;
        try {
            controlSchema = puaFromJSON.getControlSchemaFromElementProperties(json.getElementProperties());
        } catch (IOException e) {
            throw new TechnicalException(
                "Problems when deserializing using Jackson with Element Properties of AUP json to have ControlsShema",
                e);
        }
        notice.setControlSchema(controlSchema);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(notice);
        } catch (JsonProcessingException e) {
            throw new TechnicalException("Problems during conversion objectMapper to string", e);
        }
    }

    public Resource getFile(String filename) {
        return new ClassPathResource(rngLocation + filename + ".rng");
    }

    public ProfileResponse createProfile(String type, boolean standalone)
        throws TechnicalException, NoSuchAlgorithmException {
        Resource resource;
        ProfileResponse profileResponse = null;
        if (type != null && !type.isEmpty()) {
            ProfileType profileType = ProfileType.valueOf(type);
            if (type.equals(ProfileType.PA.getType())) {
                resource = new ClassPathResource(rngFile);
            } else {
                if (standalone)
                    resource = new ClassPathResource(jsonFileStandalone);
                else {
                    resource = new ClassPathResource(jsonFileVitam);
                }
            }
            profileResponse = createProfileByType(resource, profileType);
        }
        return profileResponse;
    }

    public ProfileResponse loadProfile(Notice notice) throws TechnicalException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();
        ProfileType fileType = NoticeUtils.getFileType(notice);
        try {
            profileResponse.setId(notice.getId());
            profileResponse.setType(fileType);
            profileResponse.setName(notice.getIdentifier());
            String s = notice.serialiseString();
            JSONObject profileJson = new JSONObject(s);

            if (fileType.equals(ProfileType.PA)) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rngLocation +
                    notice.getPath());
                InputSource inputSource = new InputSource(inputStream);
                XMLReader xmlReader = createXmlReader(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.getElementRNGRoot()));
                LOGGER.info("Starting editing Archive Profile with id : {}", notice.getId());
            } else if (fileType.equals(ProfileType.PUA)) {
                puaPastisValidator.validatePUA(profileJson, false);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
            }
            profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
        } catch (SAXException | IOException e) {
            throw new TechnicalException("Failed to load profile with id " + notice.getId(), e);
        } catch (AssertionError ae) {
            throw new TechnicalException("Failed to load pua with id" + notice.getId(), ae);
        }
        return profileResponse;
    }

    public ElementProperties loadProfilePA(MultipartFile file) {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ElementProperties elementProperties;

        try {
            InputSource inputSource = new InputSource(file.getInputStream());
            XMLReader xmlReader = createXmlReader(handler);
            xmlReader.parse(inputSource);
            elementProperties = getJson.getJsonParsedTree(handler.getElementRNGRoot());
        } catch (SAXException | IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
        return elementProperties;
    }

    private XMLReader createXmlReader(PastisSAX2Handler handler) throws SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // This may not be strictly required as DTDs shouldn't be allowed at all, per previous line.
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        xmlReader.setContentHandler(handler);
        return xmlReader;
    }

    public ProfileResponse createProfileByType(Resource resource, ProfileType profileType)
        throws TechnicalException, NoSuchAlgorithmException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();

        this.rand = SecureRandom.getInstanceStrong();

        try {
            profileResponse.setType(profileType);
            profileResponse.setName(resource.getFilename());

            InputStream fileInputStream = resource.getInputStream();
            InputSource inputSource = new InputSource(resource.getInputStream());

            if (profileType.equals(ProfileType.PA)) {
                XMLReader xmlReader = createXmlReader(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.getElementRNGRoot()));
                LOGGER.info("Starting editing Archive Profile from file : {}", resource.getFilename());

            } else {
                JSONTokener tokener = new JSONTokener(new InputStreamReader(fileInputStream));
                JSONObject profileJson = new JSONObject(tokener);
                puaPastisValidator.validatePUA(profileJson, false);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
                profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
                LOGGER.info("Starting editing Archive Unit Profile with name : {}", resource.getFilename());
            }
        } catch (SAXException | IOException e) {
            throw new TechnicalException("Failed to load profile ", e);
        } catch (AssertionError ae) {
            throw new TechnicalException("Failed to load pua ", ae);
        }
        return profileResponse;
    }

    public ProfileResponse loadProfileFromFile(MultipartFile file, String fileName, boolean standalone)
        throws NoSuchAlgorithmException, TechnicalException {

        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();
        this.rand = SecureRandom.getInstanceStrong();

        try {
            String originalFileName = fileName;
            if (originalFileName != null) {
                String fileExtension = originalFileName.split("\\.")[1];
                String profileName = originalFileName.split("\\.(?=[^\\.]+$)")[0];
                profileResponse.setType(fileExtension.equals("rng") ? ProfileType.PA : ProfileType.PUA);
                profileResponse.setName(profileName);
            }
            InputStream fileInputStream = file.getInputStream();
            InputSource inputSource = new InputSource(file.getInputStream());

            if (profileResponse.getType().equals(ProfileType.PA)) {
                XMLReader xmlReader = createXmlReader(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.getElementRNGRoot()));
                LOGGER.info("Starting editing Archive Profile from file : {}", fileName);

            } else {
                JSONTokener tokener = new JSONTokener(new InputStreamReader(fileInputStream));
                JSONObject profileJson = new JSONObject(tokener);
                puaPastisValidator.validatePUA(profileJson, standalone);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
                profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
                LOGGER.info("Starting editing Archive Unit Profile with name : {}", file.getOriginalFilename());
            }

        } catch (SAXException | IOException e) {
            throw new TechnicalException("Failed to load profile " + fileName, e);
        } catch (AssertionError ae) {
            throw new TechnicalException("Failed to load pua ", ae);
        }

        return profileResponse;
    }

    public List<Notice> getFiles() throws TechnicalException {
        try {
            Resource[] resources = ResourcePatternUtils
                .getResourcePatternResolver(resourceLoader)
                .getResources("classpath*:" + rngLocation + "*.*");

            if (notices.isEmpty()) {
                for (Resource r : resources) {

                    notices.add(new Notice(r));
                }
            }
        } catch (IOException e) {
            throw new TechnicalException("Resource Loader could not retrieve resource", e);
        }
        return notices;
    }
}

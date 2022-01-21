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
import java.net.URISyntaxException;
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
    @Value("${json.template.file}")
    private String jsonFile;
    @Value("${rng.base.directory}")
    private String rngLocation;
    @Autowired
    private PuaPastisValidator puaPastisValidator;

    @Autowired
    private JsonFromPUA jsonFromPUA;

    @Autowired
    private PuaFromJSON puaFromJSON;
    private List<PastisProfile> pastisProfiles = new ArrayList<>();
    private List<Notice> notices = new ArrayList<>();

    @Autowired
    public PastisService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String getArchiveProfile(final ElementProperties json) throws IOException {

        // Recover a statically generated BaliseXML by buildBaliseXMLTree
        json.initTree(json);
        BaliseXML.buildBaliseXMLTree(json, 0, null);
        // Add Recip struct to xml balises tree
        BaliseXML.addRecipTags();
        BaliseXML eparentRng = BaliseXML.baliseXMLStatic;
        String response = null;
        Writer writer = null;
        try {
            JAXBContext contextObj = JAXBContext.newInstance(AttributeXML.class, ElementXML.class, DataXML.class,
                ValueXML.class, OptionalXML.class, OneOrMoreXML.class,
                ZeroOrMoreXML.class, AnnotationXML.class, DocumentationXML.class,
                StartXML.class, GrammarXML.class, ChoiceXml.class, AnyNameXML.class, ExceptXML.class, NsNameXML.class);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshallerObj.setProperty("com.sun.xml.bind.marshaller.CharacterEscapeHandler",
                new PastisCustomCharacterEscapeHandler());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(os, "UTF-8");

            marshallerObj.marshal(eparentRng, writer);
            response = new String(os.toByteArray(), "UTF-8");

        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JAXBException e1) {
            e1.printStackTrace();
        } finally {
            writer.close();
        }

        LOGGER.info("RNG profile generated successfully");
        return response;
    }

    public String getArchiveUnitProfile(final ProfileNotice json) throws IOException {

        Notice notice = new Notice();
        if (json.getNotice() != null) {
            notice = json.getNotice();

        } else {
            notice.setId("12133411121213");
        }

        String controlSchema = puaFromJSON.getControlSchemaFromElementProperties(json.getElementProperties());
        notice.setControlSchema(controlSchema);

        ObjectMapper objectMapper = new ObjectMapper();
        String noticeAsString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(notice);

        return noticeAsString;

    }

    public Resource getFile(String filename) {
        return new ClassPathResource(rngLocation + filename + ".rng");
    }

    public ProfileResponse createProfile(String type) throws URISyntaxException, IOException {
        Resource resource;
        ProfileType profileType;
        if (type.equals(ProfileType.PA.toString())) {
            profileType = ProfileType.PA;
            resource = new ClassPathResource(rngFile);
        } else if (type.equals(ProfileType.PUA.toString())) {
            profileType = ProfileType.PUA;
            resource = new ClassPathResource(jsonFile);
        } else {
            return null;
        }
        return createProfileByType(resource, profileType);
    }

    public ProfileResponse loadProfile(Notice notice) throws IOException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();

        try {
            profileResponse.setId(notice.getId());
            profileResponse.setType(NoticeUtils.getFileType(notice));
            profileResponse.setName(notice.getIdentifier());
            String s = notice.serialiseString();
            JSONObject profileJson = new JSONObject(s);

            if (NoticeUtils.getFileType(notice).equals(ProfileType.PA)) {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rngLocation +
                    notice.getPath());
                InputSource inputSource = new InputSource(inputStream);
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.elementRNGRoot));
                LOGGER.info("Starting editing Archive Profile with id : {}", notice.getId());
            } else if (NoticeUtils.getFileType(notice).equals(ProfileType.PUA)) {
                puaPastisValidator.validatePUA(profileJson);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
            }
            profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
        } catch (SAXException | IOException e) {
            LOGGER.error("Failed to load profile with id : {}", notice.getId());
            return null;
        } catch (AssertionError ae) {
            LOGGER.error("Failed to load pua with id {} and error message {}", notice.getId(), ae.getMessage());
            return null;
        }

        return profileResponse;
    }

    public ElementProperties loadProfilePA(MultipartFile file) throws IOException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ElementProperties elementProperties;

        try {
            InputStream fileInputStream = file.getInputStream();
            InputSource inputSource = new InputSource(file.getInputStream());
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(inputSource);
            elementProperties = getJson.getJsonParsedTree(handler.elementRNGRoot);
        } catch (SAXException | IOException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
        return elementProperties;
    }

    public ProfileResponse createProfileByType(Resource resource, ProfileType profileType) throws IOException {
        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();

        try {
            profileResponse.setType(profileType);
            profileResponse.setName(resource.getFilename());

            InputStream fileInputStream = resource.getInputStream();
            InputSource inputSource = new InputSource(resource.getInputStream());

            if (profileType.equals(ProfileType.PA)) {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.elementRNGRoot));
                LOGGER.info("Starting editing Archive Profile from file : {}", resource.getFilename());

            } else {
                JSONTokener tokener = new JSONTokener(new InputStreamReader(fileInputStream));
                JSONObject profileJson = new JSONObject(tokener);
                puaPastisValidator.validatePUA(profileJson);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
                profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
                LOGGER.info("Starting editing Archive Unit Profile with name : {}", resource.getFilename());
            }

        } catch (SAXException | IOException e) {
            LOGGER.error("Failed to load profile '{}' : " + e.getMessage(), resource.getFilename());
            return null;
        } catch (AssertionError ae) {
            LOGGER.error("Failed to load pua : {}", ae.getMessage());
            return null;
        }

        profileResponse.setId(String.valueOf((Math.abs(new Random().nextLong()) / 1000)));

        return profileResponse;
    }

    public ProfileResponse loadProfileFromFile(MultipartFile file) {

        PastisSAX2Handler handler = new PastisSAX2Handler();
        PastisGetXmlJsonTree getJson = new PastisGetXmlJsonTree();
        ProfileResponse profileResponse = new ProfileResponse();

        try {
            String fileExtension = file.getOriginalFilename().split("\\.")[1];
            String profileName = file.getOriginalFilename().split("\\.(?=[^\\.]+$)")[0];
            profileResponse.setType(fileExtension.equals("rng") ? ProfileType.PA : ProfileType.PUA);
            profileResponse.setName(profileName);

            InputStream fileInputStream = file.getInputStream();
            InputSource inputSource = new InputSource(file.getInputStream());

            if (profileResponse.getType().equals(ProfileType.PA)) {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(handler);
                xmlReader.parse(inputSource);
                profileResponse.setProfile(getJson.getJsonParsedTree(handler.elementRNGRoot));
                LOGGER.info("Starting editing Archive Profile from file : {}", file.getOriginalFilename());

            } else {
                JSONTokener tokener = new JSONTokener(new InputStreamReader(fileInputStream));
                JSONObject profileJson = new JSONObject(tokener);
                puaPastisValidator.validatePUA(profileJson);
                profileResponse.setProfile(jsonFromPUA.getProfileFromPUA(profileJson));
                profileResponse.setNotice(NoticeUtils.getNoticeFromPUA(profileJson));
                LOGGER.info("Starting editing Archive Unit Profile with name : {}", file.getOriginalFilename());
            }

        } catch (SAXException | IOException e) {
            LOGGER.error("Failed to load profile '{}' : " + e.getMessage(), file.getOriginalFilename());
            return null;
        } catch (AssertionError ae) {
            LOGGER.error("Failed to load pua : {}", ae.getMessage());
            return null;
        }

        profileResponse.setId(String.valueOf((Math.abs(new Random().nextLong()) / 1000)));

        return profileResponse;
    }

    public List<Notice> getFiles() throws IOException {
        Resource[] resources = ResourcePatternUtils
            .getResourcePatternResolver(resourceLoader)
            .getResources("classpath*:" + rngLocation + "*.*");

        try {
            if (notices.isEmpty()) {
                for (Resource r : resources) {
                    notices.add(new Notice(r));
                }
            }
            return notices;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


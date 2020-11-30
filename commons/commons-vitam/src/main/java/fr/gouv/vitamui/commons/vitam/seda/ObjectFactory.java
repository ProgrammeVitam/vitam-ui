//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.2 
// Voir <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2020.07.15 à 03:41:18 PM CEST 
//


package fr.gouv.vitamui.commons.vitam.seda;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.gouv.vitamui.commons.vitam.seda package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ObjectGroupExtenstionAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ObjectGroupExtenstionAbstract");
    private final static QName _OtherDimensionsAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "OtherDimensionsAbstract");
    private final static QName _OtherCoreTechnicalMetadataAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "OtherCoreTechnicalMetadataAbstract");
    private final static QName _ArchiveUnitReferenceAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveUnitReferenceAbstract");
    private final static QName _OtherManagementAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "OtherManagementAbstract");
    private final static QName _OtherCodeListAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "OtherCodeListAbstract");
    private final static QName _AgentAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "AgentAbstract");
    private final static QName _EventAbstract_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "EventAbstract");
    private final static QName _Acknowledgement_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "Acknowledgement");
    private final static QName _ArchiveDeliveryRequest_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveDeliveryRequest");
    private final static QName _ArchiveRestitutionRequest_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveRestitutionRequest");
    private final static QName _ArchiveTransfer_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveTransfer");
    private final static QName _ArchiveTransferRequest_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveTransferRequest");
    private final static QName _AuthorizationControlAuthorityRequest_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "AuthorizationControlAuthorityRequest");
    private final static QName _AuthorizationOriginatingAgencyRequest_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "AuthorizationOriginatingAgencyRequest");
    private final static QName _ArchiveDeliveryRequestReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveDeliveryRequestReply");
    private final static QName _ArchiveRestitutionRequestReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveRestitutionRequestReply");
    private final static QName _ArchiveTransferReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveTransferReply");
    private final static QName _ArchiveTransferRequestReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveTransferRequestReply");
    private final static QName _AuthorizationControlAuthorityRequestReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "AuthorizationControlAuthorityRequestReply");
    private final static QName _AuthorizationOriginatingAgencyRequestReply_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "AuthorizationOriginatingAgencyRequestReply");
    private final static QName _ArchiveDestructionNotification_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveDestructionNotification");
    private final static QName _ArchiveModificationNotification_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveModificationNotification");
    private final static QName _Title_QNAME = new QName("http://www.w3.org/1999/xlink", "title");
    private final static QName _Resource_QNAME = new QName("http://www.w3.org/1999/xlink", "resource");
    private final static QName _Locator_QNAME = new QName("http://www.w3.org/1999/xlink", "locator");
    private final static QName _Arc_QNAME = new QName("http://www.w3.org/1999/xlink", "arc");
    private final static QName _ArchiveUnitTypeArchiveUnit_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveUnit");
    private final static QName _ArchiveUnitTypeDataObjectReference_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "DataObjectReference");
    private final static QName _ArchiveUnitTypeDataObjectGroup_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "DataObjectGroup");
    private final static QName _ToDeleteTypeArchiveUnitRefId_QNAME = new QName("fr:gouv:culture:archivesdefrance:seda:v2.1", "ArchiveUnitRefId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.gouv.vitamui.commons.vitam.seda
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CodeType }
     * 
     */
    public CodeType createCodeType() {
        return new CodeType();
    }

    /**
     * Create an instance of {@link AgentType }
     * 
     */
    public AgentType createAgentType() {
        return new AgentType();
    }

    /**
     * Create an instance of {@link AcknowledgementType }
     * 
     */
    public AcknowledgementType createAcknowledgementType() {
        return new AcknowledgementType();
    }

    /**
     * Create an instance of {@link ArchiveDeliveryRequestType }
     * 
     */
    public ArchiveDeliveryRequestType createArchiveDeliveryRequestType() {
        return new ArchiveDeliveryRequestType();
    }

    /**
     * Create an instance of {@link ArchiveRestitutionRequestType }
     * 
     */
    public ArchiveRestitutionRequestType createArchiveRestitutionRequestType() {
        return new ArchiveRestitutionRequestType();
    }

    /**
     * Create an instance of {@link ArchiveTransferType }
     * 
     */
    public ArchiveTransferType createArchiveTransferType() {
        return new ArchiveTransferType();
    }

    /**
     * Create an instance of {@link ArchiveTransferRequestType }
     * 
     */
    public ArchiveTransferRequestType createArchiveTransferRequestType() {
        return new ArchiveTransferRequestType();
    }

    /**
     * Create an instance of {@link AuthorizationControlAuthorityRequestType }
     * 
     */
    public AuthorizationControlAuthorityRequestType createAuthorizationControlAuthorityRequestType() {
        return new AuthorizationControlAuthorityRequestType();
    }

    /**
     * Create an instance of {@link AuthorizationOriginatingAgencyRequestType }
     * 
     */
    public AuthorizationOriginatingAgencyRequestType createAuthorizationOriginatingAgencyRequestType() {
        return new AuthorizationOriginatingAgencyRequestType();
    }

    /**
     * Create an instance of {@link ArchiveDeliveryRequestReplyType }
     * 
     */
    public ArchiveDeliveryRequestReplyType createArchiveDeliveryRequestReplyType() {
        return new ArchiveDeliveryRequestReplyType();
    }

    /**
     * Create an instance of {@link ArchiveRestitutionRequestReplyType }
     * 
     */
    public ArchiveRestitutionRequestReplyType createArchiveRestitutionRequestReplyType() {
        return new ArchiveRestitutionRequestReplyType();
    }

    /**
     * Create an instance of {@link ArchiveTransferReplyType }
     * 
     */
    public ArchiveTransferReplyType createArchiveTransferReplyType() {
        return new ArchiveTransferReplyType();
    }

    /**
     * Create an instance of {@link ArchiveTransferRequestReplyType }
     * 
     */
    public ArchiveTransferRequestReplyType createArchiveTransferRequestReplyType() {
        return new ArchiveTransferRequestReplyType();
    }

    /**
     * Create an instance of {@link AuthorizationControlAuthorityRequestReplyType }
     * 
     */
    public AuthorizationControlAuthorityRequestReplyType createAuthorizationControlAuthorityRequestReplyType() {
        return new AuthorizationControlAuthorityRequestReplyType();
    }

    /**
     * Create an instance of {@link AuthorizationOriginatingAgencyRequestReplyType }
     * 
     */
    public AuthorizationOriginatingAgencyRequestReplyType createAuthorizationOriginatingAgencyRequestReplyType() {
        return new AuthorizationOriginatingAgencyRequestReplyType();
    }

    /**
     * Create an instance of {@link ArchiveDestructionNotificationType }
     * 
     */
    public ArchiveDestructionNotificationType createArchiveDestructionNotificationType() {
        return new ArchiveDestructionNotificationType();
    }

    /**
     * Create an instance of {@link ArchiveModificationNotificationType }
     * 
     */
    public ArchiveModificationNotificationType createArchiveModificationNotificationType() {
        return new ArchiveModificationNotificationType();
    }

    /**
     * Create an instance of {@link CustodialHistoryType }
     * 
     */
    public CustodialHistoryType createCustodialHistoryType() {
        return new CustodialHistoryType();
    }

    /**
     * Create an instance of {@link CustodialHistoryItemType }
     * 
     */
    public CustodialHistoryItemType createCustodialHistoryItemType() {
        return new CustodialHistoryItemType();
    }

    /**
     * Create an instance of {@link KeywordsType }
     * 
     */
    public KeywordsType createKeywordsType() {
        return new KeywordsType();
    }

    /**
     * Create an instance of {@link KeyType }
     * 
     */
    public KeyType createKeyType() {
        return new KeyType();
    }

    /**
     * Create an instance of {@link CoverageType }
     * 
     */
    public CoverageType createCoverageType() {
        return new CoverageType();
    }

    /**
     * Create an instance of {@link RelatedObjectReferenceType }
     * 
     */
    public RelatedObjectReferenceType createRelatedObjectReferenceType() {
        return new RelatedObjectReferenceType();
    }

    /**
     * Create an instance of {@link DataObjectOrArchiveUnitReferenceType }
     * 
     */
    public DataObjectOrArchiveUnitReferenceType createDataObjectOrArchiveUnitReferenceType() {
        return new DataObjectOrArchiveUnitReferenceType();
    }

    /**
     * Create an instance of {@link EventType }
     * 
     */
    public EventType createEventType() {
        return new EventType();
    }

    /**
     * Create an instance of {@link SignatureType }
     * 
     */
    public SignatureType createSignatureType() {
        return new SignatureType();
    }

    /**
     * Create an instance of {@link SignerType }
     * 
     */
    public SignerType createSignerType() {
        return new SignerType();
    }

    /**
     * Create an instance of {@link ValidatorType }
     * 
     */
    public ValidatorType createValidatorType() {
        return new ValidatorType();
    }

    /**
     * Create an instance of {@link ReferencedObjectType }
     * 
     */
    public ReferencedObjectType createReferencedObjectType() {
        return new ReferencedObjectType();
    }

    /**
     * Create an instance of {@link BirthOrDeathPlaceType }
     * 
     */
    public BirthOrDeathPlaceType createBirthOrDeathPlaceType() {
        return new BirthOrDeathPlaceType();
    }

    /**
     * Create an instance of {@link GpsType }
     * 
     */
    public GpsType createGpsType() {
        return new GpsType();
    }

    /**
     * Create an instance of {@link TextType }
     * 
     */
    public TextType createTextType() {
        return new TextType();
    }

    /**
     * Create an instance of {@link IdentifierType }
     * 
     */
    public IdentifierType createIdentifierType() {
        return new IdentifierType();
    }

    /**
     * Create an instance of {@link DataObjectRefType }
     * 
     */
    public DataObjectRefType createDataObjectRefType() {
        return new DataObjectRefType();
    }

    /**
     * Create an instance of {@link MessageDigestBinaryObjectType }
     * 
     */
    public MessageDigestBinaryObjectType createMessageDigestBinaryObjectType() {
        return new MessageDigestBinaryObjectType();
    }

    /**
     * Create an instance of {@link BinaryObjectType }
     * 
     */
    public BinaryObjectType createBinaryObjectType() {
        return new BinaryObjectType();
    }

    /**
     * Create an instance of {@link RelationshipType }
     * 
     */
    public RelationshipType createRelationshipType() {
        return new RelationshipType();
    }

    /**
     * Create an instance of {@link OrganizationType }
     * 
     */
    public OrganizationType createOrganizationType() {
        return new OrganizationType();
    }

    /**
     * Create an instance of {@link OrganizationDescriptiveMetadataType }
     * 
     */
    public OrganizationDescriptiveMetadataType createOrganizationDescriptiveMetadataType() {
        return new OrganizationDescriptiveMetadataType();
    }

    /**
     * Create an instance of {@link SignatureMessageType }
     * 
     */
    public SignatureMessageType createSignatureMessageType() {
        return new SignatureMessageType();
    }

    /**
     * Create an instance of {@link TextTechnicalMetadataType }
     * 
     */
    public TextTechnicalMetadataType createTextTechnicalMetadataType() {
        return new TextTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link DocumentTechnicalMetadataType }
     * 
     */
    public DocumentTechnicalMetadataType createDocumentTechnicalMetadataType() {
        return new DocumentTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link ImageTechnicalMetadataType }
     * 
     */
    public ImageTechnicalMetadataType createImageTechnicalMetadataType() {
        return new ImageTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link AudioTechnicalMetadataType }
     * 
     */
    public AudioTechnicalMetadataType createAudioTechnicalMetadataType() {
        return new AudioTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link VideoTechnicalMetadataType }
     * 
     */
    public VideoTechnicalMetadataType createVideoTechnicalMetadataType() {
        return new VideoTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link DescriptiveTechnicalMetadataType }
     * 
     */
    public DescriptiveTechnicalMetadataType createDescriptiveTechnicalMetadataType() {
        return new DescriptiveTechnicalMetadataType();
    }

    /**
     * Create an instance of {@link RuleIdType }
     * 
     */
    public RuleIdType createRuleIdType() {
        return new RuleIdType();
    }

    /**
     * Create an instance of {@link LogBookType }
     * 
     */
    public LogBookType createLogBookType() {
        return new LogBookType();
    }

    /**
     * Create an instance of {@link AccessRuleType }
     * 
     */
    public AccessRuleType createAccessRuleType() {
        return new AccessRuleType();
    }

    /**
     * Create an instance of {@link DisseminationRuleType }
     * 
     */
    public DisseminationRuleType createDisseminationRuleType() {
        return new DisseminationRuleType();
    }

    /**
     * Create an instance of {@link ReuseRuleType }
     * 
     */
    public ReuseRuleType createReuseRuleType() {
        return new ReuseRuleType();
    }

    /**
     * Create an instance of {@link ClassificationRuleType }
     * 
     */
    public ClassificationRuleType createClassificationRuleType() {
        return new ClassificationRuleType();
    }

    /**
     * Create an instance of {@link StorageRuleType }
     * 
     */
    public StorageRuleType createStorageRuleType() {
        return new StorageRuleType();
    }

    /**
     * Create an instance of {@link AppraisalRuleType }
     * 
     */
    public AppraisalRuleType createAppraisalRuleType() {
        return new AppraisalRuleType();
    }

    /**
     * Create an instance of {@link UpdateOperationType }
     * 
     */
    public UpdateOperationType createUpdateOperationType() {
        return new UpdateOperationType();
    }

    /**
     * Create an instance of {@link ArchiveUnitIdentifierKeyType }
     * 
     */
    public ArchiveUnitIdentifierKeyType createArchiveUnitIdentifierKeyType() {
        return new ArchiveUnitIdentifierKeyType();
    }

    /**
     * Create an instance of {@link ToDeleteType }
     * 
     */
    public ToDeleteType createToDeleteType() {
        return new ToDeleteType();
    }

    /**
     * Create an instance of {@link ArchiveUnitType }
     * 
     */
    public ArchiveUnitType createArchiveUnitType() {
        return new ArchiveUnitType();
    }

    /**
     * Create an instance of {@link ManagementType }
     * 
     */
    public ManagementType createManagementType() {
        return new ManagementType();
    }

    /**
     * Create an instance of {@link ObjectGroupRefType }
     * 
     */
    public ObjectGroupRefType createObjectGroupRefType() {
        return new ObjectGroupRefType();
    }

    /**
     * Create an instance of {@link DescriptiveMetadataContentType }
     * 
     */
    public DescriptiveMetadataContentType createDescriptiveMetadataContentType() {
        return new DescriptiveMetadataContentType();
    }

    /**
     * Create an instance of {@link ManagementHistoryType }
     * 
     */
    public ManagementHistoryType createManagementHistoryType() {
        return new ManagementHistoryType();
    }

    /**
     * Create an instance of {@link ManagementHistoryDataType }
     * 
     */
    public ManagementHistoryDataType createManagementHistoryDataType() {
        return new ManagementHistoryDataType();
    }

    /**
     * Create an instance of {@link BinaryDataObjectType }
     * 
     */
    public BinaryDataObjectType createBinaryDataObjectType() {
        return new BinaryDataObjectType();
    }

    /**
     * Create an instance of {@link CompressedType }
     * 
     */
    public CompressedType createCompressedType() {
        return new CompressedType();
    }

    /**
     * Create an instance of {@link FormatIdentificationType }
     * 
     */
    public FormatIdentificationType createFormatIdentificationType() {
        return new FormatIdentificationType();
    }

    /**
     * Create an instance of {@link FileInfoType }
     * 
     */
    public FileInfoType createFileInfoType() {
        return new FileInfoType();
    }

    /**
     * Create an instance of {@link PhysicalDataObjectType }
     * 
     */
    public PhysicalDataObjectType createPhysicalDataObjectType() {
        return new PhysicalDataObjectType();
    }

    /**
     * Create an instance of {@link CoreMetadataType }
     * 
     */
    public CoreMetadataType createCoreMetadataType() {
        return new CoreMetadataType();
    }

    /**
     * Create an instance of {@link DimensionsType }
     * 
     */
    public DimensionsType createDimensionsType() {
        return new DimensionsType();
    }

    /**
     * Create an instance of {@link MeasurementType }
     * 
     */
    public MeasurementType createMeasurementType() {
        return new MeasurementType();
    }

    /**
     * Create an instance of {@link MeasurementWeightType }
     * 
     */
    public MeasurementWeightType createMeasurementWeightType() {
        return new MeasurementWeightType();
    }

    /**
     * Create an instance of {@link CodeListVersionsType }
     * 
     */
    public CodeListVersionsType createCodeListVersionsType() {
        return new CodeListVersionsType();
    }

    /**
     * Create an instance of {@link DataObjectGroupType }
     * 
     */
    public DataObjectGroupType createDataObjectGroupType() {
        return new DataObjectGroupType();
    }

    /**
     * Create an instance of {@link LogBookOgType }
     * 
     */
    public LogBookOgType createLogBookOgType() {
        return new LogBookOgType();
    }

    /**
     * Create an instance of {@link EventLogBookOgType }
     * 
     */
    public EventLogBookOgType createEventLogBookOgType() {
        return new EventLogBookOgType();
    }

    /**
     * Create an instance of {@link DataObjectPackageType }
     * 
     */
    public DataObjectPackageType createDataObjectPackageType() {
        return new DataObjectPackageType();
    }

    /**
     * Create an instance of {@link ManagementMetadataType }
     * 
     */
    public ManagementMetadataType createManagementMetadataType() {
        return new ManagementMetadataType();
    }

    /**
     * Create an instance of {@link AuthorizationRequestContentType }
     * 
     */
    public AuthorizationRequestContentType createAuthorizationRequestContentType() {
        return new AuthorizationRequestContentType();
    }

    /**
     * Create an instance of {@link DescriptiveMetadataType }
     * 
     */
    public DescriptiveMetadataType createDescriptiveMetadataType() {
        return new DescriptiveMetadataType();
    }

    /**
     * Create an instance of {@link OperationType }
     * 
     */
    public OperationType createOperationType() {
        return new OperationType();
    }

    /**
     * Create an instance of {@link OrganizationWithIdType }
     * 
     */
    public OrganizationWithIdType createOrganizationWithIdType() {
        return new OrganizationWithIdType();
    }

    /**
     * Create an instance of {@link TitleEltType }
     * 
     */
    public TitleEltType createTitleEltType() {
        return new TitleEltType();
    }

    /**
     * Create an instance of {@link ResourceType }
     * 
     */
    public ResourceType createResourceType() {
        return new ResourceType();
    }

    /**
     * Create an instance of {@link LocatorType }
     * 
     */
    public LocatorType createLocatorType() {
        return new LocatorType();
    }

    /**
     * Create an instance of {@link ArcType }
     * 
     */
    public ArcType createArcType() {
        return new ArcType();
    }

    /**
     * Create an instance of {@link Simple }
     * 
     */
    public Simple createSimple() {
        return new Simple();
    }

    /**
     * Create an instance of {@link Extended }
     * 
     */
    public Extended createExtended() {
        return new Extended();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ObjectGroupExtenstionAbstract")
    public JAXBElement<Object> createObjectGroupExtenstionAbstract(Object value) {
        return new JAXBElement<Object>(_ObjectGroupExtenstionAbstract_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "OtherDimensionsAbstract")
    public JAXBElement<Object> createOtherDimensionsAbstract(Object value) {
        return new JAXBElement<Object>(_OtherDimensionsAbstract_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OpenType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link OpenType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "OtherCoreTechnicalMetadataAbstract")
    public JAXBElement<OpenType> createOtherCoreTechnicalMetadataAbstract(OpenType value) {
        return new JAXBElement<OpenType>(_OtherCoreTechnicalMetadataAbstract_QNAME, OpenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveUnitReferenceAbstract")
    public JAXBElement<Object> createArchiveUnitReferenceAbstract(Object value) {
        return new JAXBElement<Object>(_ArchiveUnitReferenceAbstract_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "OtherManagementAbstract")
    public JAXBElement<Object> createOtherManagementAbstract(Object value) {
        return new JAXBElement<Object>(_OtherManagementAbstract_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "OtherCodeListAbstract")
    public JAXBElement<CodeType> createOtherCodeListAbstract(CodeType value) {
        return new JAXBElement<CodeType>(_OtherCodeListAbstract_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AgentType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AgentType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "AgentAbstract")
    public JAXBElement<AgentType> createAgentAbstract(AgentType value) {
        return new JAXBElement<AgentType>(_AgentAbstract_QNAME, AgentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "EventAbstract")
    public JAXBElement<Object> createEventAbstract(Object value) {
        return new JAXBElement<Object>(_EventAbstract_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AcknowledgementType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AcknowledgementType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "Acknowledgement")
    public JAXBElement<AcknowledgementType> createAcknowledgement(AcknowledgementType value) {
        return new JAXBElement<AcknowledgementType>(_Acknowledgement_QNAME, AcknowledgementType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveDeliveryRequestType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveDeliveryRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveDeliveryRequest")
    public JAXBElement<ArchiveDeliveryRequestType> createArchiveDeliveryRequest(ArchiveDeliveryRequestType value) {
        return new JAXBElement<ArchiveDeliveryRequestType>(_ArchiveDeliveryRequest_QNAME, ArchiveDeliveryRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveRestitutionRequestType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveRestitutionRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveRestitutionRequest")
    public JAXBElement<ArchiveRestitutionRequestType> createArchiveRestitutionRequest(ArchiveRestitutionRequestType value) {
        return new JAXBElement<ArchiveRestitutionRequestType>(_ArchiveRestitutionRequest_QNAME, ArchiveRestitutionRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveTransferType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveTransferType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveTransfer")
    public JAXBElement<ArchiveTransferType> createArchiveTransfer(ArchiveTransferType value) {
        return new JAXBElement<ArchiveTransferType>(_ArchiveTransfer_QNAME, ArchiveTransferType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveTransferRequestType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveTransferRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveTransferRequest")
    public JAXBElement<ArchiveTransferRequestType> createArchiveTransferRequest(ArchiveTransferRequestType value) {
        return new JAXBElement<ArchiveTransferRequestType>(_ArchiveTransferRequest_QNAME, ArchiveTransferRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthorizationControlAuthorityRequestType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AuthorizationControlAuthorityRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "AuthorizationControlAuthorityRequest")
    public JAXBElement<AuthorizationControlAuthorityRequestType> createAuthorizationControlAuthorityRequest(AuthorizationControlAuthorityRequestType value) {
        return new JAXBElement<AuthorizationControlAuthorityRequestType>(_AuthorizationControlAuthorityRequest_QNAME, AuthorizationControlAuthorityRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthorizationOriginatingAgencyRequestType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AuthorizationOriginatingAgencyRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "AuthorizationOriginatingAgencyRequest")
    public JAXBElement<AuthorizationOriginatingAgencyRequestType> createAuthorizationOriginatingAgencyRequest(AuthorizationOriginatingAgencyRequestType value) {
        return new JAXBElement<AuthorizationOriginatingAgencyRequestType>(_AuthorizationOriginatingAgencyRequest_QNAME, AuthorizationOriginatingAgencyRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveDeliveryRequestReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveDeliveryRequestReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveDeliveryRequestReply")
    public JAXBElement<ArchiveDeliveryRequestReplyType> createArchiveDeliveryRequestReply(ArchiveDeliveryRequestReplyType value) {
        return new JAXBElement<ArchiveDeliveryRequestReplyType>(_ArchiveDeliveryRequestReply_QNAME, ArchiveDeliveryRequestReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveRestitutionRequestReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveRestitutionRequestReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveRestitutionRequestReply")
    public JAXBElement<ArchiveRestitutionRequestReplyType> createArchiveRestitutionRequestReply(ArchiveRestitutionRequestReplyType value) {
        return new JAXBElement<ArchiveRestitutionRequestReplyType>(_ArchiveRestitutionRequestReply_QNAME, ArchiveRestitutionRequestReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveTransferReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveTransferReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveTransferReply")
    public JAXBElement<ArchiveTransferReplyType> createArchiveTransferReply(ArchiveTransferReplyType value) {
        return new JAXBElement<ArchiveTransferReplyType>(_ArchiveTransferReply_QNAME, ArchiveTransferReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveTransferRequestReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveTransferRequestReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveTransferRequestReply")
    public JAXBElement<ArchiveTransferRequestReplyType> createArchiveTransferRequestReply(ArchiveTransferRequestReplyType value) {
        return new JAXBElement<ArchiveTransferRequestReplyType>(_ArchiveTransferRequestReply_QNAME, ArchiveTransferRequestReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthorizationControlAuthorityRequestReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AuthorizationControlAuthorityRequestReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "AuthorizationControlAuthorityRequestReply")
    public JAXBElement<AuthorizationControlAuthorityRequestReplyType> createAuthorizationControlAuthorityRequestReply(AuthorizationControlAuthorityRequestReplyType value) {
        return new JAXBElement<AuthorizationControlAuthorityRequestReplyType>(_AuthorizationControlAuthorityRequestReply_QNAME, AuthorizationControlAuthorityRequestReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthorizationOriginatingAgencyRequestReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AuthorizationOriginatingAgencyRequestReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "AuthorizationOriginatingAgencyRequestReply")
    public JAXBElement<AuthorizationOriginatingAgencyRequestReplyType> createAuthorizationOriginatingAgencyRequestReply(AuthorizationOriginatingAgencyRequestReplyType value) {
        return new JAXBElement<AuthorizationOriginatingAgencyRequestReplyType>(_AuthorizationOriginatingAgencyRequestReply_QNAME, AuthorizationOriginatingAgencyRequestReplyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveDestructionNotificationType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveDestructionNotificationType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveDestructionNotification")
    public JAXBElement<ArchiveDestructionNotificationType> createArchiveDestructionNotification(ArchiveDestructionNotificationType value) {
        return new JAXBElement<ArchiveDestructionNotificationType>(_ArchiveDestructionNotification_QNAME, ArchiveDestructionNotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveModificationNotificationType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveModificationNotificationType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveModificationNotification")
    public JAXBElement<ArchiveModificationNotificationType> createArchiveModificationNotification(ArchiveModificationNotificationType value) {
        return new JAXBElement<ArchiveModificationNotificationType>(_ArchiveModificationNotification_QNAME, ArchiveModificationNotificationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TitleEltType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TitleEltType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "title")
    public JAXBElement<TitleEltType> createTitle(TitleEltType value) {
        return new JAXBElement<TitleEltType>(_Title_QNAME, TitleEltType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ResourceType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "resource")
    public JAXBElement<ResourceType> createResource(ResourceType value) {
        return new JAXBElement<ResourceType>(_Resource_QNAME, ResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocatorType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LocatorType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "locator")
    public JAXBElement<LocatorType> createLocator(LocatorType value) {
        return new JAXBElement<LocatorType>(_Locator_QNAME, LocatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArcType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArcType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "arc")
    public JAXBElement<ArcType> createArc(ArcType value) {
        return new JAXBElement<ArcType>(_Arc_QNAME, ArcType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ArchiveUnitType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ArchiveUnitType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveUnit", scope = ArchiveUnitType.class)
    public JAXBElement<ArchiveUnitType> createArchiveUnitTypeArchiveUnit(ArchiveUnitType value) {
        return new JAXBElement<ArchiveUnitType>(_ArchiveUnitTypeArchiveUnit_QNAME, ArchiveUnitType.class, ArchiveUnitType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataObjectRefType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DataObjectRefType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "DataObjectReference", scope = ArchiveUnitType.class)
    public JAXBElement<DataObjectRefType> createArchiveUnitTypeDataObjectReference(DataObjectRefType value) {
        return new JAXBElement<DataObjectRefType>(_ArchiveUnitTypeDataObjectReference_QNAME, DataObjectRefType.class, ArchiveUnitType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObjectGroupRefType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ObjectGroupRefType }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "DataObjectGroup", scope = ArchiveUnitType.class)
    public JAXBElement<ObjectGroupRefType> createArchiveUnitTypeDataObjectGroup(ObjectGroupRefType value) {
        return new JAXBElement<ObjectGroupRefType>(_ArchiveUnitTypeDataObjectGroup_QNAME, ObjectGroupRefType.class, ArchiveUnitType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     */
    @XmlElementDecl(namespace = "fr:gouv:culture:archivesdefrance:seda:v2.1", name = "ArchiveUnitRefId", scope = ToDeleteType.class)
    @XmlIDREF
    public JAXBElement<Object> createToDeleteTypeArchiveUnitRefId(Object value) {
        return new JAXBElement<Object>(_ToDeleteTypeArchiveUnitRefId_QNAME, Object.class, ToDeleteType.class, value);
    }

}

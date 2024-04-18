export const orderedFields: string[] = [
  '#originating_agency',
  '#originating_agencies',
  'Generalities',
  'Generalities.DescriptionLevel',
  'Generalities.Title',
  'Generalities.Title_',
  'Generalities.Description',
  'Generalities.Description_',
  'Generalities.Tag',
  'Generalities.Dates',
  'Generalities.Dates.StartDate',
  'Generalities.Dates.EndDate',
  'Generalities.Dates.CreatedDate',
  'Generalities.Dates.TransactedDate',
  'Generalities.Dates.SentDate',
  'Generalities.Dates.ReceivedDate',
  'Generalities.Dates.RegisteredDate',
  'Generalities.Dates.AcquiredDate',
  'Generalities.Dates.DateLitteral',
  'Generalities.Identifiers',
  'Generalities.Identifiers.SystemId',
  'Generalities.Identifiers.OriginatingSystemId',
  'Generalities.Identifiers.OriginatingAgencyArchiveUnitIdentifier',
  'Generalities.Identifiers.TransferringAgencyArchiveUnitIdentifier',
  'Generalities.Identifiers.ArchivalAgencyArchiveUnitIdentifier',
  'Generalities.Identifiers.FilePlanPosition',
  'Generalities.Characteristics',
  'Generalities.Characteristics.Type',
  'Generalities.Characteristics.DocumentType',
  'Generalities.Characteristics.Language',
  'Generalities.Characteristics.DescriptionLanguage',
  'Generalities.Characteristics.Status',
  'Generalities.Characteristics.Source',
  'Generalities.Characteristics.Version',
  'Generalities.Characteristics.OriginatingSystemIdReplyTo',
  'Generalities.Characteristics.TextContent',
  'Generalities.PersistentIdentifier',
  'Generalities.PersistentIdentifier.PersistentIdentifierType',
  'Generalities.PersistentIdentifier.PersistentIdentifierOrigin',
  'Generalities.PersistentIdentifier.PersistentIdentifierReference',
  'Generalities.PersistentIdentifier.PersistentIdentifierContent',
  'CustodialHistory',
  'CustodialHistory.CustodialHistoryItem',
  'CustodialHistory.CustodialHistoryFile',
  'CustodialHistory.CustodialHistoryFile.DataObjectGroupReferenceId',
  'CustodialHistory.CustodialHistoryFile.DataObjectReferenceId',
  'AuthorizedAgent',
  'AuthorizedAgent.FirstName',
  'AuthorizedAgent.BirthName',
  'AuthorizedAgent.FullName',
  'AuthorizedAgent.GivenName',
  'AuthorizedAgent.Gender',
  'AuthorizedAgent.BirthDate',
  'AuthorizedAgent.DeathDate',
  'AuthorizedAgent.BirthPlace',
  'AuthorizedAgent.BirthPlace.Geogname',
  'AuthorizedAgent.BirthPlace.Address',
  'AuthorizedAgent.BirthPlace.PostalCode',
  'AuthorizedAgent.BirthPlace.City',
  'AuthorizedAgent.BirthPlace.Region',
  'AuthorizedAgent.BirthPlace.Country',
  'AuthorizedAgent.DeathPlace',
  'AuthorizedAgent.DeathPlace.Geogname',
  'AuthorizedAgent.DeathPlace.Address',
  'AuthorizedAgent.DeathPlace.PostalCode',
  'AuthorizedAgent.DeathPlace.City',
  'AuthorizedAgent.DeathPlace.Region',
  'AuthorizedAgent.DeathPlace.Country',
  'AuthorizedAgent.Nationality',
  'AuthorizedAgent.Corpname',
  'AuthorizedAgent.Identifier',
  'AuthorizedAgent.Function',
  'AuthorizedAgent.Activity',
  'AuthorizedAgent.Position',
  'AuthorizedAgent.Role',
  'AuthorizedAgent.Mandate',
  'Agent',
  'Agent.FirstName',
  'Agent.BirthName',
  'Agent.FullName',
  'Agent.GivenName',
  'Agent.Gender',
  'Agent.BirthDate',
  'Agent.DeathDate',
  'Agent.BirthPlace',
  'Agent.BirthPlace.Geogname',
  'Agent.BirthPlace.Address',
  'Agent.BirthPlace.PostalCode',
  'Agent.BirthPlace.City',
  'Agent.BirthPlace.Region',
  'Agent.BirthPlace.Country',
  'Agent.DeathPlace',
  'Agent.DeathPlace.Geogname',
  'Agent.DeathPlace.Address',
  'Agent.DeathPlace.PostalCode',
  'Agent.DeathPlace.City',
  'Agent.DeathPlace.Region',
  'Agent.DeathPlace.Country',
  'Agent.Nationality',
  'Agent.Corpname',
  'Agent.Identifier',
  'Agent.Function',
  'Agent.Activity',
  'Agent.Position',
  'Agent.Role',
  'Agent.Mandate',
  'Writer',
  'Writer.FirstName',
  'Writer.BirthName',
  'Writer.FullName',
  'Writer.GivenName',
  'Writer.Gender',
  'Writer.BirthDate',
  'Writer.DeathDate',
  'Writer.BirthPlace',
  'Writer.BirthPlace.Geogname',
  'Writer.BirthPlace.Address',
  'Writer.BirthPlace.PostalCode',
  'Writer.BirthPlace.City',
  'Writer.BirthPlace.Region',
  'Writer.BirthPlace.Country',
  'Writer.DeathPlace',
  'Writer.DeathPlace.Geogname',
  'Writer.DeathPlace.Address',
  'Writer.DeathPlace.PostalCode',
  'Writer.DeathPlace.City',
  'Writer.DeathPlace.Region',
  'Writer.DeathPlace.Country',
  'Writer.Nationality',
  'Writer.Corpname',
  'Writer.Identifier',
  'Writer.Function',
  'Writer.Activity',
  'Writer.Position',
  'Writer.Role',
  'Writer.Mandate',
  'Addressee',
  'Addressee.FirstName',
  'Addressee.BirthName',
  'Addressee.Fullpname',
  'Addressee.GivenName',
  'Addressee.Gender',
  'Addressee.BirthDate',
  'Addressee.DeathDate',
  'Addressee.BirthPlace',
  'Addressee.BirthPlace.Geogname',
  'Addressee.BirthPlace.Address',
  'Addressee.BirthPlace.PostalCode',
  'Addressee.BirthPlace.City',
  'Addressee.BirthPlace.Region',
  'Addressee.BirthPlace.Country',
  'Addressee.DeathPlace',
  'Addressee.DeathPlace.Geogname',
  'Addressee.DeathPlace.Address',
  'Addressee.DeathPlace.PostalCode',
  'Addressee.DeathPlace.City',
  'Addressee.DeathPlace.Region',
  'Addressee.DeathPlace.Country',
  'Addressee.Nationality',
  'Addressee.Corpname',
  'Addressee.Identifier',
  'Addressee.Function',
  'Addressee.Activity',
  'Addressee.Position',
  'Addressee.Role',
  'Addressee.Mandate',
  'Recipient',
  'Recipient.FirstName',
  'Recipient.BirthName',
  'Recipient.FullName',
  'Recipient.GivenName',
  'Recipient.Gender',
  'Recipient.BirthDate',
  'Recipient.DeathDate',
  'Recipient.BirthPlace',
  'Recipient.BirthPlace.Geogname',
  'Recipient.BirthPlace.Address',
  'Recipient.BirthPlace.PostalCode',
  'Recipient.BirthPlace.City',
  'Recipient.BirthPlace.Region',
  'Recipient.BirthPlace.Country',
  'Recipient.DeathPlace',
  'Recipient.DeathPlace.Geogname',
  'Recipient.DeathPlace.Address',
  'Recipient.DeathPlace.PostalCode',
  'Recipient.DeathPlace.City',
  'Recipient.DeathPlace.Region',
  'Recipient.DeathPlace.Country',
  'Recipient.Nationality',
  'Recipient.Corpname',
  'Recipient.Identifier',
  'Recipient.Function',
  'Recipient.Activity',
  'Recipient.Position',
  'Recipient.Role',
  'Recipient.Mandate',
  'Transmitter',
  'Transmitter.FirstName',
  'Transmitter.BirthName',
  'Transmitter.FullName',
  'Transmitter.GivenName',
  'Transmitter.Gender',
  'Transmitter.BirthDate',
  'Transmitter.DeathDate',
  'Transmitter.BirthPlace',
  'Transmitter.BirthPlace.Geogname',
  'Transmitter.BirthPlace.Address',
  'Transmitter.BirthPlace.PostalCode',
  'Transmitter.BirthPlace.City',
  'Transmitter.BirthPlace.Region',
  'Transmitter.BirthPlace.Country',
  'Transmitter.DeathPlace',
  'Transmitter.DeathPlace.Geogname',
  'Transmitter.DeathPlace.Address',
  'Transmitter.DeathPlace.PostalCode',
  'Transmitter.DeathPlace.City',
  'Transmitter.DeathPlace.Region',
  'Transmitter.DeathPlace.Country',
  'Transmitter.Nationality',
  'Transmitter.Corpname',
  'Transmitter.Identifier',
  'Transmitter.Function',
  'Transmitter.Activity',
  'Transmitter.Position',
  'Transmitter.Role',
  'Transmitter.Mandate',
  'Sender',
  'Sender.FirstName',
  'Sender.BirthName',
  'Sender.FullName',
  'Sender.GivenName',
  'Sender.Gender',
  'Sender.BirthDate',
  'Sender.DeathDate',
  'Sender.BirthPlace',
  'Sender.BirthPlace.Geogname',
  'Sender.BirthPlace.Address',
  'Sender.BirthPlace.PostalCode',
  'Sender.BirthPlace.City',
  'Sender.BirthPlace.Region',
  'Sender.BirthPlace.Country',
  'Sender.DeathPlace',
  'Sender.DeathPlace.Geogname',
  'Sender.DeathPlace.Address',
  'Sender.DeathPlace.PostalCode',
  'Sender.DeathPlace.City',
  'Sender.DeathPlace.Region',
  'Sender.DeathPlace.Country',
  'Sender.Nationality',
  'Sender.Corpname',
  'Sender.Identifier',
  'Sender.Function',
  'Sender.Activity',
  'Sender.Position',
  'Sender.Role',
  'Sender.Mandate',
  'Signature',
  'Signature.Masterdata',
  'Signature.ReferencedObject',
  'Signature.ReferencedObject.SignedObjectId',
  'Signature.ReferencedObject.SignedObjectDigest',
  'Signature.Signer',
  'Signature.Signer.SigningTime',
  'Signature.Signer.FirstName',
  'Signature.Signer.BirthName',
  'Signature.Signer.Fullpname',
  'Signature.Signer.GivenName',
  'Signature.Signer.Gender',
  'Signature.Signer.BirthDate',
  'Signature.Signer.DeathDate',
  'Signature.Signer.BirthPlace',
  'Signature.Signer.BirthPlace.Geogname',
  'Signature.Signer.BirthPlace.Address',
  'Signature.Signer.BirthPlace.PostalCode',
  'Signature.Signer.BirthPlace.City',
  'Signature.Signer.BirthPlace.Region',
  'Signature.Signer.BirthPlace.Country',
  'Signature.Signer.DeathPlace',
  'Signature.Signer.DeathPlace.Geogname',
  'Signature.Signer.DeathPlace.Address',
  'Signature.Signer.DeathPlace.PostalCode',
  'Signature.Signer.DeathPlace.City',
  'Signature.Signer.DeathPlace.Region',
  'Signature.Signer.DeathPlace.Country',
  'Signature.Signer.Nationality',
  'Signature.Signer.Corpname',
  'Signature.Signer.Identifier',
  'Signature.Signer.Function',
  'Signature.Signer.Activity',
  'Signature.Signer.Position',
  'Signature.Signer.Role',
  'Signature.Signer.Mandate',
  'Signature.Validator',
  'Signature.Validator.ValidationTime',
  'Signature.Validator.FirstName',
  'Signature.Validator.BirthName',
  'Signature.Validator.Fullpname',
  'Signature.Validator.GivenName',
  'Signature.Validator.Gender',
  'Signature.Validator.BirthDate',
  'Signature.Validator.DeathDate',
  'Signature.Validator.BirthPlace',
  'Signature.Validator.BirthPlace.Geogname',
  'Signature.Validator.BirthPlace.Address',
  'Signature.Validator.BirthPlace.PostalCode',
  'Signature.Validator.BirthPlace.City',
  'Signature.Validator.BirthPlace.Region',
  'Signature.Validator.BirthPlace.Country',
  'Signature.Validator.DeathPlace',
  'Signature.Validator.DeathPlace.Geogname',
  'Signature.Validator.DeathPlace.Address',
  'Signature.Validator.DeathPlace.PostalCode',
  'Signature.Validator.DeathPlace.City',
  'Signature.Validator.DeathPlace.Region',
  'Signature.Validator.DeathPlace.Country',
  'Signature.Validator.Nationality',
  'Signature.Validator.Corpname',
  'Signature.Validator.Identifier',
  'Signature.Validator.Function',
  'Signature.Validator.Activity',
  'Signature.Validator.Position',
  'Signature.Validator.Role',
  'Signature.Validator.Mandate',
  'Event',
  'Event.evId',
  'Event.evTypeProc',
  'Event.evType',
  'Event.evDateTime',
  'Event.evTypeDetail',
  'Event.outcome',
  'Event.outDetail',
  'Event.outMessg',
  'Event.evDetData',
  'Event.LinkingAgentIdentifier',
  'Event.LinkingAgentIdentifier.LinkingAgentIdentifierType',
  'Event.LinkingAgentIdentifier.LinkingAgentIdentifierValue',
  'Event.LinkingAgentIdentifier.LinkingAgentRole',
  'RelatedObjectReference',
  'RelatedObjectReference.IsVersionOf',
  'RelatedObjectReference.IsVersionOf.ArchiveUnitRefId',
  'RelatedObjectReference.IsVersionOf.DataObjectReference',
  'RelatedObjectReference.IsVersionOf.DataObjectReference.DataObjectGroupReferenceId',
  'RelatedObjectReference.IsVersionOf.DataObjectReference.DataObjectReferenceId',
  'RelatedObjectReference.IsVersionOf.RepositoryArchiveUnitPID',
  'RelatedObjectReference.IsVersionOf.RepositoryObjectPID',
  'RelatedObjectReference.IsVersionOf.ExternalReference',
  'RelatedObjectReference.Replaces',
  'RelatedObjectReference.Replaces.ArchiveUnitRefId',
  'RelatedObjectReference.Replaces.DataObjectReference',
  'RelatedObjectReference.Replaces.DataObjectReference.DataObjectGroupReferenceId',
  'RelatedObjectReference.Replaces.DataObjectReference.DataObjectReferenceId',
  'RelatedObjectReference.Replaces.ExternalReference',
  'RelatedObjectReference.Replaces.RepositoryArchiveUnitPID',
  'RelatedObjectReference.Replaces.RepositoryObjectPID',
  'RelatedObjectReference.Requires',
  'RelatedObjectReference.Requires.ArchiveUnitRefId',
  'RelatedObjectReference.Requires.DataObjectReference',
  'RelatedObjectReference.Requires.DataObjectReference.DataObjectGroupReferenceId',
  'RelatedObjectReference.Requires.DataObjectReference.DataObjectReferenceId',
  'RelatedObjectReference.Requires.ExternalReference',
  'RelatedObjectReference.Requires.RepositoryArchiveUnitPID',
  'RelatedObjectReference.Requires.RepositoryObjectPID',
  'RelatedObjectReference.IsPartOf',
  'RelatedObjectReference.IsPartOf.ArchiveUnitRefId',
  'RelatedObjectReference.IsPartOf.DataObjectReference',
  'RelatedObjectReference.IsPartOf.DataObjectReference.DataObjectGroupReferenceId',
  'RelatedObjectReference.IsPartOf.DataObjectReference.DataObjectReferenceId',
  'RelatedObjectReference.IsPartOf.ExternalReference',
  'RelatedObjectReference.IsPartOf.RepositoryArchiveUnitPID',
  'RelatedObjectReference.IsPartOf.RepositoryObjectPID',
  'References',
  'References.ArchiveUnitRefId',
  'References.DataObjectReference',
  'References.DataObjectReference.DataObjectReferenceId',
  'References.DataObjectReference.DataObjectGroupReferenceId',
  'References.RepositoryArchiveUnitPID',
  'References.RepositoryObjectPID',
  'References.ExternalReference',
  'OriginatingAgency',
  'OriginatingAgency.Identifier',
  'SubmissionAgency',
  'SubmissionAgency.Identifier',
  'Keyword',
  'Keyword.KeywordContent',
  'Keyword.KeywordReference',
  'Keyword.KeywordType',
  'Coverage',
  'Coverage.Spatial',
  'Coverage.Temporal',
  'Coverage.Juridictional',
  'Gps',
  'Gps.GpsAltitude',
  'Gps.GpsAltitudeRef',
  'Gps.GpsDateStamp',
  'Gps.GpsLatitude',
  'Gps.GpsLatitudeRef',
  'Gps.GpsLongitude',
  'Gps.GpsLongitudeRef',
  'Gps.GpsVersionID',
  'SigningInformation',
  'SigningInformation.Signature.Signer',
  'SigningInformation.Signature.Signer.SigningTime',
  'SigningInformation.Signature.Signer.FirstName',
  'SigningInformation.Signature.Signer.BirthName',
  'SigningInformation.Signature.Signer.Fullpname',
  'SigningInformation.Signature.Signer.GivenName',
  'SigningInformation.Signature.Signer.Gender',
  'SigningInformation.Signature.Signer.BirthDate',
  'SigningInformation.Signature.Signer.DeathDate',
  'SigningInformation.Signature.Signer.BirthPlace',
  'SigningInformation.Signature.Signer.BirthPlace.Geogname',
  'SigningInformation.Signature.Signer.BirthPlace.Address',
  'SigningInformation.Signature.Signer.BirthPlace.PostalCode',
  'SigningInformation.Signature.Signer.BirthPlace.City',
  'SigningInformation.Signature.Signer.BirthPlace.Region',
  'SigningInformation.Signature.Signer.BirthPlace.Country',
  'SigningInformation.Signature.Signer.DeathPlace',
  'SigningInformation.Signature.Signer.DeathPlace.Geogname',
  'SigningInformation.Signature.Signer.DeathPlace.Address',
  'SigningInformation.Signature.Signer.DeathPlace.PostalCode',
  'SigningInformation.Signature.Signer.DeathPlace.City',
  'SigningInformation.Signature.Signer.DeathPlace.Region',
  'SigningInformation.Signature.Signer.DeathPlace.Country',
  'SigningInformation.Signature.Signer.Nationality',
  'SigningInformation.Signature.Signer.Corpname',
  'SigningInformation.Signature.Signer.Identifier',
  'SigningInformation.Signature.Signer.Function',
  'SigningInformation.Signature.Signer.Activity',
  'SigningInformation.Signature.Signer.Position',
  'SigningInformation.Signature.Signer.Role',
  'SigningInformation.Signature.Signer.Mandate',
  'SigningInformation.Signature.Validator',
  'SigningInformation.Signature.Validator.ValidationTime',
  'SigningInformation.Signature.Validator.FirstName',
  'SigningInformation.Signature.Validator.BirthName',
  'SigningInformation.Signature.Validator.Fullpname',
  'SigningInformation.Signature.Validator.GivenName',
  'SigningInformation.Signature.Validator.Gender',
  'SigningInformation.Signature.Validator.BirthDate',
  'SigningInformation.Signature.Validator.DeathDate',
  'SigningInformation.Signature.Validator.BirthPlace',
  'SigningInformation.Signature.Validator.BirthPlace.Geogname',
  'SigningInformation.Signature.Validator.BirthPlace.Address',
  'SigningInformation.Signature.Validator.BirthPlace.PostalCode',
  'SigningInformation.Signature.Validator.BirthPlace.City',
  'SigningInformation.Signature.Validator.BirthPlace.Region',
  'SigningInformation.Signature.Validator.BirthPlace.Country',
  'SigningInformation.Signature.Validator.DeathPlace',
  'SigningInformation.Signature.Validator.DeathPlace.Geogname',
  'SigningInformation.Signature.Validator.DeathPlace.Address',
  'SigningInformation.Signature.Validator.DeathPlace.PostalCode',
  'SigningInformation.Signature.Validator.DeathPlace.City',
  'SigningInformation.Signature.Validator.DeathPlace.Region',
  'SigningInformation.Signature.Validator.DeathPlace.Country',
  'SigningInformation.Signature.Validator.Nationality',
  'SigningInformation.Signature.Validator.Corpname',
  'SigningInformation.Signature.Validator.Identifier',
  'SigningInformation.Signature.Validator.Function',
  'SigningInformation.Signature.Validator.Activity',
  'SigningInformation.Signature.Validator.Position',
  'SigningInformation.Signature.Validator.Role',
  'SigningInformation.Signature.Validator.Mandate',
];
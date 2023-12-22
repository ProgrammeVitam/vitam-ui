export interface Version {
  DataObjectVersion: string;
  FormatIdentification: {
    MimeType: string;
  };
}

export interface Qualifier {
  qualifier: 'BinaryMaster' | 'PhysicalMaster';
  versions: Version[];
}

export interface ApiUnitObject {
  '#id': string;
  '#qualifiers': Qualifier[];
}

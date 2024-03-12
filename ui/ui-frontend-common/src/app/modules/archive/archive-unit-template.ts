import { DisplayRule } from '../object-viewer/models';

export const customTemplate: DisplayRule[] = [
  {
    Path: '',
    ui: {
      Path: '',
      component: 'group',
      open: true,
      display: true,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: '',
    },
  },
  {
    Path: '#originating_agency',
    ui: {
      Path: '#originating_agency',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 2,
        size: 'small',
      },
      label: 'Service producteur',
      disabled: true,
    },
  },
  {
    Path: '#originating_agencies',
    ui: {
      Path: '#originating_agencies',
      component: 'group',
      open: true,
      display: true,
      layout: {
        columns: 2,
        size: 'small',
      },
      label: 'Services producteurs liés à l’unité archivistique',
      disabled: true,
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Generalities',
      component: 'group',
      open: true,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'Généralités',
    },
  },
  {
    Path: 'DescriptionLevel',
    ui: {
      Path: 'Generalities.DescriptionLevel',
      component: 'select',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'Title',
    ui: {
      Path: 'Generalities.Title',
      component: 'textfield',
      layout: {
        columns: 2,
        size: 'medium',
      },
    },
  },
  {
    Path: 'Title_',
    ui: {
      Path: 'Generalities.Title_',
      component: 'select+textfield',
      layout: {
        columns: 2,
        size: 'medium',
      },
    },
  },
  {
    Path: 'Description',
    ui: {
      Path: 'Generalities.Description',
      component: 'textarea',
      layout: {
        columns: 2,
        size: 'large',
      },
    },
  },
  {
    Path: 'Description_.*',
    ui: {
      Path: 'Generalities.Description_.*',
      component: 'select+textarea',
      layout: {
        columns: 2,
        size: 'large',
      },
    },
  },
  {
    Path: 'Tag',
    ui: {
      Path: 'Generalities.Tag',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Generalities.Dates',
      component: 'group',
      open: false,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'Date(s)',
    },
  },
  {
    Path: 'StartDate',
    ui: {
      Path: 'Generalities.Dates.StartDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'EndDate',
    ui: {
      Path: 'Generalities.Dates.EndDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'CreatedDate',
    ui: {
      Path: 'Generalities.Dates.CreatedDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'TransactedDate',
    ui: {
      Path: 'Generalities.Dates.TransactedDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'SentDate',
    ui: {
      Path: 'Generalities.Dates.SentDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'ReceivedDate',
    ui: {
      Path: 'Generalities.Dates.ReceivedDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'RegisteredDate',
    ui: {
      Path: 'Generalities.Dates.RegisteredDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'AcquiredDate',
    ui: {
      Path: 'Generalities.Dates.AcquiredDate',
      component: 'datepicker',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'DateLitteral',
    ui: {
      Path: 'Generalities.Dates.DateLitteral',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Generalities.Identifiers',
      component: 'group',
      open: false,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'Identifiant(s)',
    },
  },
  {
    Path: 'SystemId',
    ui: {
      Path: 'Generalities.Identifiers.SystemId',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'OriginatingSystemId',
    ui: {
      Path: 'Generalities.Identifiers.OriginatingSystemId',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'OriginatingAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.OriginatingAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'TransferringAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.TransferringAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'ArchivalAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.ArchivalAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'FilePlanPosition',
    ui: {
      Path: 'Generalities.Identifiers.FilePlanPosition',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Generalities.Characteristics',
      component: 'group',
      open: false,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'Caractéristique(s)',
    },
  },
  {
    Path: 'Type',
    ui: {
      Path: 'Generalities.Characteristics.Type',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'DocumentType',
    ui: {
      Path: 'Generalities.Characteristics.DocumentType',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'Language',
    ui: {
      Path: 'Generalities.Characteristics.Language',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'DescriptionLanguage',
    ui: {
      Path: 'Generalities.Characteristics.DescriptionLanguage',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'Status',
    ui: {
      Path: 'Generalities.Characteristics.Status',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'Source',
    ui: {
      Path: 'Generalities.Characteristics.Source',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'Version',
    ui: {
      Path: 'Generalities.Characteristics.Version',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'OriginatingSystemIdReplyTo',
    ui: {
      Path: 'Generalities.Characteristics.OriginatingSystemIdReplyTo',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'small',
      },
    },
  },
  {
    Path: 'TextContent',
    ui: {
      Path: 'Generalities.Characteristics.TextContent',
      component: 'textfield',
      layout: {
        columns: 2,
        size: 'medium',
      },
    },
  },
];

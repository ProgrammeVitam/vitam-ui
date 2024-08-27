/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
      label: "Service producteur responsable de l'entrée",
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
    Path: 'Description_',
    ui: {
      Path: 'Generalities.Description_',
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
  {
    Path: 'PersistentIdentifier',
    ui: {
      Path: 'Generalities.PersistentIdentifier',
      component: 'group',
      layout: {
        columns: 2,
        size: 'medium',
      },
    },
  },

  {
    Path: 'PersistentIdentifier.PersistentIdentifierType',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierType',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'medium',
      },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierOrigin',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierOrigin',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'medium',
      },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierReference',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierReference',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'medium',
      },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierContent',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierContent',
      component: 'textfield',
      layout: {
        columns: 1,
        size: 'medium',
      },
    },
  },
];

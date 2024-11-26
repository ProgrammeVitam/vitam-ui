/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { DisplayRule } from 'vitamui-library';

export const agencyTemplate: DisplayRule[] = [
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
    Path: null,
    ui: {
      Path: 'Identification',
      component: 'group',
      open: true,
      display: true,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.IDENTIFICATION',
    },
  },
  {
    Path: 'identifier',
    ui: {
      Path: 'Identification.Identifier',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.IDENTIFIER',
    },
  },
  {
    Path: 'name',
    ui: {
      Path: 'Identification.Name',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.NAME',
    },
  },
  {
    Path: 'description',
    ui: {
      Path: 'Description',
      component: 'textfield',
      open: true,
      display: false,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.DESCRIPTION',
    },
  },
  {
    Path: 'entityType',
    ui: {
      Path: 'Identification.EntityType',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.ENTITY_TYPE',
    },
  },
  {
    Path: 'nameEntryParallel',
    ui: {
      Path: 'Identification.NameEntryParallel',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.NAME_ENTRY_PARALLEL',
    },
  },
  {
    Path: 'authorizedForm',
    ui: {
      Path: 'Identification.AuthorizedForm',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.AUTHORIZED_FORM',
    },
  },
  {
    Path: 'alternativeForm',
    ui: {
      Path: 'Identification.AlternativeForm',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.ALTERNATIVE_FORM',
    },
  },
  {
    Path: 'entityId',
    ui: {
      Path: 'Identification.EntityId',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.ENTITY_ID',
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Description',
      component: 'group',
      open: true,
      display: true,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.DESCRIPTION',
    },
  },
  {
    Path: 'fromDate',
    ui: {
      Path: 'Description.FromDate',
      component: 'datepicker',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.FROM_DATE',
    },
  },
  {
    Path: 'toDate',
    ui: {
      Path: 'Description.ToDate',
      component: 'datepicker',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.TO_DATE',
    },
  },
  {
    Path: 'functions',
    ui: {
      Path: 'Description.Functions',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.FUNCTIONS',
    },
  },
  {
    Path: 'biogHist',
    ui: {
      Path: 'Description.BiogHist',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.BIOGHIST',
    },
  },
  {
    Path: 'places',
    ui: {
      Path: 'Description.Places',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.PLACES',
    },
  },
  {
    Path: 'legalStatuses',
    ui: {
      Path: 'Description.LegalStatuses',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.LEGAL_STATUSES',
    },
  },
  {
    Path: 'mandates',
    ui: {
      Path: 'Description.Mandates',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.MANDATES',
    },
  },
  {
    Path: 'structureOrGenealogy',
    ui: {
      Path: 'Description.StructureOrGenealogy',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.STRUCTURE_OR_GENEALOGY',
    },
  },
  {
    Path: 'generalContext',
    ui: {
      Path: 'Description.GeneralContext',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.GENERAL_CONTEXT',
    },
  },
  {
    Path: null,
    ui: {
      Path: 'Control',
      component: 'group',
      open: false,
      display: true,
      layout: {
        columns: 2,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.CONTROL',
    },
  },
  {
    Path: 'creationDate',
    ui: {
      Path: 'Control.CreationDate',
      component: 'datepicker',
      open: true,
      display: true,
      disabled: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.CREATION_DATE',
    },
  },
  {
    Path: 'updateDate',
    ui: {
      Path: 'Control.UpdateDate',
      component: 'datepicker',
      open: true,
      display: true,
      disabled: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.UPDATE_DATE',
    },
  },
  {
    Path: 'maintenanceStatus',
    ui: {
      Path: 'Control.MaintenanceStatus',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.MAINTENANCE_STATUS',
    },
  },
  {
    Path: 'localStatus',
    ui: {
      Path: 'Control.LocalStatus',
      component: 'textfield',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'medium',
      },
      label: 'AGENCY.LABEL.LOCAL_STATUS',
    },
  },
  {
    Path: 'sources',
    ui: {
      Path: 'Control.Sources',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.SOURCES',
    },
  },
  {
    Path: 'eventDescription',
    ui: {
      Path: 'Control.EventDescription',
      component: 'textarea',
      open: true,
      display: true,
      layout: {
        columns: 1,
        size: 'large',
      },
      label: 'AGENCY.LABEL.EVENT_DESCRIPTION',
    },
  },
];

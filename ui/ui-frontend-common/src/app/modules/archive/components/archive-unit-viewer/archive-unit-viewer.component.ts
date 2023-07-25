import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { DisplayObjectService, DisplayRule } from '../../../object-viewer/models';
import { MockExtendedOntologyService } from '../../../object-viewer/services/mock-extended-ontology.service';
import { OntologyStrategyDisplayObjectService } from '../../../object-viewer/services/ontology-strategy-display-object.service';
import { OntologyService } from '../../../ontology';

@Component({
  selector: 'vitamui-common-archive-unit-viewer',
  templateUrl: './archive-unit-viewer.component.html',
  styleUrls: ['./archive-unit-viewer.component.scss'],
  providers: [
    { provide: DisplayObjectService, useClass: OntologyStrategyDisplayObjectService },
    { provide: OntologyService, useClass: MockExtendedOntologyService },
  ],
})
export class ArchiveUnitViewerComponent implements OnInit, OnChanges {
  @Input() data!: any;
  @Input() template: DisplayRule[] = [
    {
      path: '',
      ui: {
        path: '',
        component: 'group',
        open: true,
        display: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: '#originating_agency',
      ui: {
        path: '#originating_agency',
        component: 'textfield',
        open: true,
        display: true,
        layout: {
          columns: 2,
          size: 'small',
        },
      },
    },
    {
      path: '#originating_agencies',
      ui: {
        path: '#originating_agencies',
        component: 'textfield',
        open: true,
        display: true,
        layout: {
          columns: 2,
          size: 'small',
        },
      },
    },
    {
      path: null,
      ui: {
        path: 'Generalities',
        component: 'group',
        open: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'DescriptionLevel',
      ui: {
        path: 'Generalities.DescriptionLevel',
        component: 'select',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'Title',
      ui: {
        path: 'Generalities.Title',
        component: 'textfield',
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'Title_.*',
      ui: {
        path: 'Generalities.Title_.*',
        component: 'select+textfield',
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'Description',
      ui: {
        path: 'Generalities.Description',
        component: 'textarea',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    {
      path: 'Description_.*',
      ui: {
        path: 'Generalities.Description_.*',
        component: 'select+textarea',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    {
      path: 'Tag',
      ui: {
        path: 'Generalities.Tag',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: null,
      ui: {
        path: 'Generalities.Dates',
        component: 'group',
        open: false,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'StartDate',
      ui: {
        path: 'Generalities.Dates.StartDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'EndDate',
      ui: {
        path: 'Generalities.Dates.EndDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'CreatedDate',
      ui: {
        path: 'Generalities.Dates.CreatedDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'TransactedDate',
      ui: {
        path: 'Generalities.Dates.TransactedDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'SentDate',
      ui: {
        path: 'Generalities.Dates.SentDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'ReceivedDate',
      ui: {
        path: 'Generalities.Dates.ReceivedDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'RegisteredDate',
      ui: {
        path: 'Generalities.Dates.RegisteredDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'AcquiredDate',
      ui: {
        path: 'Generalities.Dates.AcquiredDate',
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'DateLitteral',
      ui: {
        path: 'Generalities.Dates.DateLitteral',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: null,
      ui: {
        path: 'Generalities.Identifiers',
        component: 'group',
        open: false,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'SystemId',
      ui: {
        path: 'Generalities.Identifiers.SystemId',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'OriginatingSystemId',
      ui: {
        path: 'Generalities.Identifiers.OriginatingSystemId',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'OriginatingAgencyArchiveUnitIdentifier',
      ui: {
        path: 'Generalities.Identifiers.OriginatingAgencyArchiveUnitIdentifier',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'TransferringAgencyArchiveUnitIdentifier',
      ui: {
        path: 'Generalities.Identifiers.TransferringAgencyArchiveUnitIdentifier',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'ArchivalAgencyArchiveUnitIdentifier',
      ui: {
        path: 'Generalities.Identifiers.ArchivalAgencyArchiveUnitIdentifier',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'FilePlanPosition',
      ui: {
        path: 'Generalities.Identifiers.FilePlanPosition',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: null,
      ui: {
        path: 'Generalities.Characteristics',
        component: 'group',
        open: false,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    {
      path: 'Type',
      ui: {
        path: 'Generalities.Characteristics.Type',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'DocumentType',
      ui: {
        path: 'Generalities.Characteristics.DocumentType',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'Language',
      ui: {
        path: 'Generalities.Characteristics.Language',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'DescriptionLanguage',
      ui: {
        path: 'Generalities.Characteristics.DescriptionLanguage',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'Status',
      ui: {
        path: 'Generalities.Characteristics.Status',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'Source',
      ui: {
        path: 'Generalities.Characteristics.Source',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'Version',
      ui: {
        path: 'Generalities.Characteristics.Version',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'OriginatingSystemIdReplyTo',
      ui: {
        path: 'Generalities.Characteristics.OriginatingSystemIdReplyTo',
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    {
      path: 'TextContent',
      ui: {
        path: 'Generalities.Characteristics.TextContent',
        component: 'textfield',
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
  ];
  mode = 'default';

  constructor(private displayObjectService: DisplayObjectService) {}

  ngOnInit(): void {
    this.displayObjectService.setMode(this.mode);
    this.displayObjectService.setTemplate(this.template);
    this.displayObjectService.setData(this.data);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { data, template } = changes;

    if (data) {
      this.displayObjectService.setData(data.currentValue);
    }
    if (template) {
      this.displayObjectService.setTemplate(template.currentValue);
    }
  }
}

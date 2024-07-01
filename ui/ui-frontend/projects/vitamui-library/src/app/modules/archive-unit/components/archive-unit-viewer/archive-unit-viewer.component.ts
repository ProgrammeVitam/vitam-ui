import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { DisplayObjectService, DisplayRule } from '../../../object-viewer/models';
import { customTemplate } from '../../archive-unit-template';
import { ArchiveUnitViewerService } from './archive-unit-viewer.service';
import { ObjectViewerComponent } from '../../../object-viewer/object-viewer.component';

@Component({
  selector: 'vitamui-common-archive-unit-viewer',
  templateUrl: './archive-unit-viewer.component.html',
  styleUrls: ['./archive-unit-viewer.component.scss'],
  providers: [{ provide: DisplayObjectService, useClass: ArchiveUnitViewerService }],
  standalone: true,
  imports: [ObjectViewerComponent],
})
export class ArchiveUnitViewerComponent implements OnInit, OnChanges {
  @Input() data!: any;
  @Input() template: DisplayRule[] = customTemplate;
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

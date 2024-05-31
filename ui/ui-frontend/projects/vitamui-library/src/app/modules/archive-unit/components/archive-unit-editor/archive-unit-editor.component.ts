import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';

import { EditObject } from '../../../object-editor/models/edit-object.model';
import { DisplayRule } from '../../../object-viewer/models';
import { customTemplate } from '../../archive-unit-template';
import { ArchiveUnit } from '../../models/archive-unit';
import { JsonPatchDto } from '../../models/json-patch';
import { ArchiveUnitEditorService } from './archive-unit-editor.service';

@Component({
  selector: 'vitamui-common-archive-unit-editor',
  templateUrl: './archive-unit-editor.component.html',
  styleUrls: ['./archive-unit-editor.component.scss'],
  providers: [ArchiveUnitEditorService],
})
export class ArchiveUnitEditorComponent implements OnInit, OnChanges, OnDestroy {
  @Input() data!: ArchiveUnit;
  @Input() template: DisplayRule[] = customTemplate;

  editObject$ = new BehaviorSubject<EditObject>(null);

  private subscriptions = new Subscription();

  constructor(private archiveUnitEditorService: ArchiveUnitEditorService) {}

  ngOnInit(): void {
    this.archiveUnitEditorService.setTemplate(this.template);
    this.archiveUnitEditorService.setData(this.data);

    this.subscriptions.add(this.archiveUnitEditorService.editObject$.subscribe(this.editObject$));
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { data, template } = changes;

    if (data) this.archiveUnitEditorService.setData(data.currentValue);
    if (template) this.archiveUnitEditorService.setTemplate(template.currentValue);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  getJsonPatch(): JsonPatchDto {
    const jsonPatchDto = this.archiveUnitEditorService.toJsonPatchDto();
    if (jsonPatchDto.jsonPatch.length === 0) throw new Error('No changes to generate in Json Patch');
    return jsonPatchDto;
  }
}

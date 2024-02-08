import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { BehaviorSubject, Observable, Subscription, throwError } from 'rxjs';

import { EditObject } from '../../../object-editor/models/edit-object.model';
import { DisplayRule } from '../../../object-viewer/models';
import { customTemplate } from '../../archive-unit-template';
import { ArchiveUnitService } from '../../archive-unit.service';
import { ArchiveUnit } from '../../models/archive-unit';
import { ArchiveUnitEditorService } from './archive-unit-editor.service';

@Component({
  selector: 'vitamui-common-archive-unit-editor',
  template: `<vitamui-common-object-editor [editObject]="editObject$ | async"></vitamui-common-object-editor>`,
  styles: [],
  providers: [ArchiveUnitEditorService],
})
export class ArchiveUnitEditorComponent implements OnInit, OnChanges, OnDestroy {
  @Input() data!: ArchiveUnit;
  @Input() template: DisplayRule[] = customTemplate;

  editObject$ = new BehaviorSubject<EditObject>(null);

  private subscriptions = new Subscription();

  constructor(
    private archiveUnitEditorService: ArchiveUnitEditorService,
    private archiveUnitService: ArchiveUnitService,
  ) {}

  ngOnInit(): void {
    this.archiveUnitEditorService.setTemplate(this.template);
    this.archiveUnitEditorService.setData(this.data);

    this.archiveUnitEditorService.editObject$.subscribe(this.editObject$);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { data, template } = changes;

    if (data) this.archiveUnitEditorService.setData(data.currentValue);
    if (template) this.archiveUnitEditorService.setTemplate(template.currentValue);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  update(): Observable<{ operationId: String }> {
    const jsonPatchDto = this.archiveUnitEditorService.toJsonPatchDto();

    if (jsonPatchDto.jsonPatch.length === 0) return throwError(new Error('No change to submit'));

    return this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto);
  }
}

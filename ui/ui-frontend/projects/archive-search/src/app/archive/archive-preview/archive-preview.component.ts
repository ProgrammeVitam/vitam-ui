import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { Unit } from '../models/unit.interface';
import { ArchiveService } from '../archive.service';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-archive-preview',
  templateUrl: './archive-preview.component.html',
  styleUrls: ['./archive-preview.component.scss'],
})
export class ArchivePreviewComponent implements OnInit, OnChanges {
  @Input()
  archiveUnit: Unit;
  @Input()
  accessContract: string;
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Input()
  isPopup: boolean;

  tenantIdentifier: number;
  uaPath$: Observable<{ fullPath: string; resumePath: string }>;
  fullPath = false;

  constructor(private archiveService: ArchiveService, private route: ActivatedRoute) {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
    });
  }

  ngOnInit() {
    this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, this.accessContract);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['archiveUnit'].firstChange) {
      return;
    }
    if (changes['archiveUnit'].currentValue['#id'] !== changes['archiveUnit'].previousValue['#id']) {
      this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, this.accessContract);
    }
    this.fullPath = false;
  }

  onDownloadObjectFromUnit(archiveUnit: Unit) {
    return this.archiveService.launchDownloadObjectFromUnit(archiveUnit['#id'], this.tenantIdentifier, this.accessContract);
  }

  emitClose() {
    this.previewClose.emit();
  }

  showArchiveUniteFullPath() {
    this.fullPath = true;
  }
}

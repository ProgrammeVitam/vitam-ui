import { HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ArchiveService } from '../archive.service';
import { Unit } from '../models/unit.interface';

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
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContract);
    this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, headers);
  }

  ngOnChanges(changes: SimpleChanges): void {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContract);
    if (changes.archiveUnit.firstChange) {
      return;
    }
    if (changes.archiveUnit.currentValue['#id'] !== changes.archiveUnit.previousValue['#id']) {
      this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, headers);
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

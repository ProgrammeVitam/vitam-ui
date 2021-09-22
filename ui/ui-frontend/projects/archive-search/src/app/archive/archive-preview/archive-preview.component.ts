import { HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ArchiveService } from '../archive.service';
import { Unit } from '../models/unit.interface';

@Component({
  selector: 'app-archive-preview',
  templateUrl: './archive-preview.component.html',
  styleUrls: ['./archive-preview.component.scss'],
})
export class ArchivePreviewComponent implements OnInit {
  @Input()
  archiveUnit: Unit;
  @Input()
  accessContract: string;
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Input()
  isPopup: boolean;

  tenantIdentifier: string;
  uaPath$: Observable<{ fullPath: string; resumePath: string }>;
  fullPath = false;

  constructor(private archiveService: ArchiveService, private route: ActivatedRoute) {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  ngOnInit() {}

  onDownloadObjectFromUnit(archiveUnit: Unit) {
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContract);

    return this.archiveService.downloadObjectFromUnit(archiveUnit['#id'], headers);
  }

  emitClose() {
    this.previewClose.emit();
  }

  showArchiveUniteFullPath() {
    this.fullPath = true;
  }
}

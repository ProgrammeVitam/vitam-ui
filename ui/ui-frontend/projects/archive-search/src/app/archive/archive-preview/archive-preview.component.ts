import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
 import { Unit } from '../models/unit.interface';
import { HttpHeaders } from '@angular/common/http';
import { ArchiveService } from '../archive.service';
import { StartupService } from 'ui-frontend-common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-archive-preview',
  templateUrl: './archive-preview.component.html',
  styleUrls: ['./archive-preview.component.scss']
})
export class ArchivePreviewComponent  implements OnInit {

  @Input()
  archiveUnit: Unit;
  @Input()
  accessContract : string;
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Input()
  isPopup: boolean;


  tenantIdentifier: string;
  constructor(private archiveService : ArchiveService, private startupService : StartupService, private route: ActivatedRoute) {
    this.route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

  }

  ngOnInit() {

    }


  onDownloadObjectFromUnit(archiveUnit : Unit){
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContract);

   return  this.archiveService.downloadObjectFromUnit(archiveUnit['#id'], archiveUnit.Title, headers);
  }

  emitClose() {
    this.previewClose.emit();
  }



  openPopup() {
    window.open(this.startupService.getConfigStringValue('UI_URL')
      + '/archive-search/tenant/' + this.tenantIdentifier + '/' + this.accessContract + '/id/' + this.archiveUnit['#id'],
        'detailPopup', 'width=684, height=713, resizable=no, location=no');
    this.emitClose();
  }

}

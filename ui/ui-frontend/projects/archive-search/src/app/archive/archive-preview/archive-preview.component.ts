import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
 import { Unit } from '../models/unit.interface';
import { HttpHeaders } from '@angular/common/http';
import { ArchiveService } from '../archive.service';

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
  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  constructor(private archiveService : ArchiveService) {

  }
  ngOnInit() {
  }

  onDownloadArchive(archiveUnit : Unit){
    let headers = new HttpHeaders().append('Content-Type', 'application/json');
    headers = headers.append('X-Access-Contract-Id', this.accessContract);
    const a =  {
      usage : "BinaryMaster",
      version : "1"
    };

  return  this.archiveService.downloadArchiveUnit(archiveUnit['#id'],a, headers);
  }

  emitClose() {
    this.previewClose.emit();
  }
}

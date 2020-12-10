import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
 import { Unit } from '../models/unit.interface';

@Component({
  selector: 'app-archive-preview',
  templateUrl: './archive-preview.component.html',
  styleUrls: ['./archive-preview.component.scss']
})
export class ArchivePreviewComponent  implements OnInit {

  @Input()
  archiveUnit: Unit;
  
  @Output() previewClose: EventEmitter<any> = new EventEmitter();
  
  constructor() {
  
  }
  ngOnInit() {
  }

  onDownloadArchive(){
    //To implement
  }

  emitClose() {
    this.previewClose.emit();
  }
}

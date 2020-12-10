import { Component, Input, OnInit } from '@angular/core';
import { Unit } from '../models/unit.interface';

@Component({
  selector: 'app-archive-unit-information-tab',
  templateUrl: './archive-unit-information-tab.component.html',
  styleUrls: ['./archive-unit-information-tab.component.scss']
})
export class ArchiveUnitInformationTabComponent implements OnInit {

  @Input()
  archiveUnit: Unit;

  constructor() { }

  ngOnInit() {
  }


  onDownloadArchive(){
    
  }

}

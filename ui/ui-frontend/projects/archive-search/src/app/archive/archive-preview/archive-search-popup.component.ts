import { Component, OnInit} from '@angular/core';
import { ActivatedRoute} from '@angular/router';
import { Unit} from '../models/unit.interface';
import { Customer} from 'ui-frontend-common';

@Component({
  selector: 'app-archive-search-popup',
  template: '<app-archive-preview (previewClose)="closePopup()" [archiveUnit] ="archiveUnit" [accessContract]="accessContract" [isPopup]="true"> </app-archive-preview>',
 
})
export class ArchiveSearchPopupComponent implements OnInit {

  archiveUnit: Unit;
  customer: Customer;
  accessContract : string ;

  constructor(private route: ActivatedRoute) {
    this.accessContract = this.route.snapshot.params.accessContractId;
    this.archiveUnit = JSON.parse(this.route.snapshot.data.archiveUnit);

  }

  ngOnInit() {
  }

  closePopup() {
    window.close();
  }

}

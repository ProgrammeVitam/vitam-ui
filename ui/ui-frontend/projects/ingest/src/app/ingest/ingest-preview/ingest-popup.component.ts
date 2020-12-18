
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';


@Component({
  selector: 'app-ingest-popup',
  template: '<app-ingest-preview (previewClose)="closePopup()" [ingest] ="ingest" [isPopup]="true"> </app-ingest-preview>',

})
export class IngestPopupComponent implements OnInit {


  ingest: any;
  

  constructor(private route: ActivatedRoute) {
    this.ingest = this.route.snapshot.data.ingest;

  }

  ngOnInit(): void {

  }

  closePopup() {
    window.close();
  }

}

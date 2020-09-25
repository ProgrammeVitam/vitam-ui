
import { Customer } from 'ui-frontend-common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CustomerService } from 'projects/identity/src/app/core/customer.service';


@Component({
  selector: 'app-ingest-popup',
  template: '<app-ingest-preview (previewClose)="closePopup()" [ingest] ="ingest" [isPopup]="true"> </app-ingest-preview>',

})
export class IngestPopupComponent implements OnInit {


  ingest: any;
  customer: Customer;

  constructor(private route: ActivatedRoute, private customerService: CustomerService) {
    this.customerService.getMyCustomer().subscribe(
      (customer) => this.customer = customer);
    this.ingest = this.route.snapshot.data.ingest;

  }

  ngOnInit(): void {

  }

  closePopup() {
    window.close();
  }

}

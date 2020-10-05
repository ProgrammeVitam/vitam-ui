
import { Customer} from 'ui-frontend-common';

import { Component,  OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CustomerService } from 'projects/identity/src/app/core/customer.service';
//import { IngestComponent } from '../ingest.component';
// import { IngestService } from '../ingest.service';
// import { map, take } from 'rxjs/operators';


@Component({
  selector: 'app-ingest-popup',
  template: '<app-ingest-preview [ingest] ="ingest" [isPopup]="true"> </app-ingest-preview>',
  //templateUrl: './ingest-popup.component.html',

})
export class IngestPopupComponent implements OnInit {

  ingeste: any;
  ingest: any;
  customer: Customer;
  id : string;
  
//@ViewChild(IngestComponent , { static: false } ) child : IngestComponent;




  constructor(private route: ActivatedRoute, private customerService: CustomerService) {
    this.customerService.getMyCustomer().subscribe((customer) => this.customer = customer);

  

    console.log('data data', this.route.snapshot.data);
    this.ingest = this.route.snapshot.data.ingest;
    //this.ingest = this.child.dataIngest;
    route.params.subscribe(params => {
      this.id = params.id;
    });
    console.log(this.id);

    console.log('DATA DATA ', this.ingest);

  }
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }
  

 

  //   ngOnInit() {
  //     console.log("oussama id"+this.id);
      
  // }



  closePopup() {
    window.close();
  }

}

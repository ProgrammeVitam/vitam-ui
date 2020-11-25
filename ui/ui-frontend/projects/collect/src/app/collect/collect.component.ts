import { Component, OnInit } from '@angular/core';
import { CollectApiService } from '../core/api/collect-api.service';
import { SidenavPage, GlobalEventService } from 'ui-frontend-common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-collect',
  templateUrl: './collect.component.html',
  styleUrls: ['./collect.component.scss']
})
export class CollectComponent extends SidenavPage<any> implements OnInit {
  tenantIdentifier: string;
  constructor(private service : CollectApiService, private route: ActivatedRoute, private router: Router,
              globalEventService: GlobalEventService, public dialog: MatDialog) {
    super(route, globalEventService);
    this.route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
    }

    getMessages() {
    this.service.getMessageFromCollect().subscribe(

      data => { console.log('Message from Collect ' + data); },
      errors => {console.log(errors); }
    );

    this.service.getMessageFromVitam().subscribe(

      data => { console.log('Message from VITAM ' + data); },
      errors => {console.log(errors); }
    );

    }
  ngOnInit() {


  }


  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

}

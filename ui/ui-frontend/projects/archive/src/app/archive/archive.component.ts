import { Component, OnInit } from '@angular/core';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { SidenavPage, GlobalEventService } from 'ui-frontend-common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-archive',
  templateUrl: './archive.component.html',
  styleUrls: ['./archive.component.scss']
})
export class ArchiveComponent extends SidenavPage<any> implements OnInit {
  tenantIdentifier: string;
  constructor( private service : ArchiveApiService, private route: ActivatedRoute, private router: Router,
              globalEventService: GlobalEventService, public dialog: MatDialog) {
    super(route, globalEventService);
    this.route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
    }

    getMessages() {
    this.service.getMessageFromArchive().subscribe(

      data => { console.log('Message from Archive ' + data); },
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

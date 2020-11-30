import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'starter-kit-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss']
})
export class BreadcrumbComponent implements OnInit {

  public breadCrumbData = [{label: 'Portail'}, {label: 'Archive', identifier:'ARCHIVE_APP_ID'}, {label: 'Bordereau n°1'}];

  constructor() { }

  ngOnInit() {
  }

  public onClick(val: string): void {
    console.log('[onClick]', val);
  }


}

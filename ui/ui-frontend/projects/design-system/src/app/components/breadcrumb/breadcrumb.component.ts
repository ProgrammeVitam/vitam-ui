import { Component } from '@angular/core';
import { ApplicationId, BreadCrumbData, VitamuiBreadcrumbComponent, VitamuiTitleBreadcrumbComponent } from 'vitamui-library';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.scss'],
  standalone: true,
  imports: [VitamuiTitleBreadcrumbComponent, VitamuiBreadcrumbComponent],
})
export class BreadcrumbComponent {
  public breadCrumbData = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.CUSTOMERS_APP }, { label: 'Client n°1' }];

  public onClick(val: BreadCrumbData): void {
    console.log('[onClick]', val);
  }
}

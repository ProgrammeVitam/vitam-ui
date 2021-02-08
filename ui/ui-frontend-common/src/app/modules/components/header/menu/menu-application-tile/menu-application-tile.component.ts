import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { ApplicationService } from './../../../../application.service';
import { Application } from './../../../../models/application';
import { StartupService } from './../../../../startup.service';

@Component({
  selector: 'vitamui-common-menu-application-tile',
  templateUrl: './menu-application-tile.component.html',
  styleUrls: ['./menu-application-tile.component.scss']
})
export class MenuApplicationTileComponent {

  @Input()
  public application: Application;

  @Input()
  public hlCriteria?: string;

  @Input()
  public menuSelectedTenant: number;

  constructor(
    private router: Router,
    private startupService: StartupService,
    private applicationService: ApplicationService
    ) { }

  openApplication(application: Application): boolean {
    this.applicationService.openApplication(
      application, this.router, this.startupService.getConfigStringValue('UI_URL'), this.menuSelectedTenant);
    return false;
  }

  getApplicationUrl(application: Application): string {
    return this.applicationService.getApplicationUrl(application, this.menuSelectedTenant);
  }

}

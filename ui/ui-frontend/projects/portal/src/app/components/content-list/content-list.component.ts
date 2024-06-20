import { Component, Input, ViewChild } from '@angular/core';
import { MatLegacyTabGroup as MatTabGroup } from '@angular/material/legacy-tabs';
import { Category } from 'vitamui-library';
import { ContentTypeEnum } from './content.enum';
import { Content } from './content.interface';

@Component({
  selector: 'app-content-list',
  templateUrl: './content-list.component.html',
  styleUrls: ['./content-list.component.scss'],
})
export class ContentListComponent {
  @Input() set content(content: Map<Category, Content>) {
    if (content?.size) {
      this._content = content;

      // Subscribe to see more event at loading
      // this.userAlertsService.seeMoreAlerts$()
      //   .pipe(take(1))
      //   .subscribe((seeMoreAlerts: boolean) => this.openAlertsTab(seeMoreAlerts));
    }
  }

  get content() {
    return this._content;
  }

  // @Output() openAlert = new EventEmitter<AlertAnalytics>();
  // @Output() removeAlert = new EventEmitter<AlertAnalytics>();

  @ViewChild('matTabGroup') matTabGroup: MatTabGroup;

  // public UNKOWN_APP_NAME = 'UNKNOWN_APP';
  public CONTENT_TYPE = ContentTypeEnum;
  public tabIndex = 0;

  private _content: Map<Category, Content>;

  // constructor(private appService: ApplicationService, private translateService: TranslateService,
  //   private userAlertsService: UserAlertsService) {}

  // ngAfterViewInit() {
  //   // Subscribe to futher see more events
  //   this.userAlertsService.seeMoreAlerts$().subscribe((seeMoreAlerts: boolean) => this.openAlertsTab(seeMoreAlerts));
  // }
  //
  // public getAppName(appId: string): Observable<string> {
  //   return this.appService.getAppById(appId).pipe(map((app) => app.name));
  // }

  // public getDetails(alert: AlertAnalytics): Observable<string> {
  //   return buildAlertLabel(this.translateService, alert);
  // }

  // public getDate(alert: AlertAnalytics): Date {
  //   return new Date(+alert.creationDate);
  // }

  // private openAlertsTab(seeMoreAlerts: boolean): void {
  //   if (seeMoreAlerts && this.content) {
  //     this.tabIndex = this.content.size - 1;
  //   }
  // }
}

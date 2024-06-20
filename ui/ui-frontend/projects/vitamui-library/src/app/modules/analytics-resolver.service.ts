import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot } from '@angular/router';
import { take } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';
import { ApplicationService } from './application.service';
import { UserAlertsService } from './components/user-alerts';
import { User } from './models/user/user.interface';
import { TenantSelectionService } from './tenant-selection.service';

@Injectable({
  providedIn: 'root',
})
export class AnalyticsResolver {
  private currentApplicationId: string;

  constructor(
    private userApiService: UserApiService,
    private applicationService: ApplicationService,
    private tenantService: TenantSelectionService,
    private userAlertsService: UserAlertsService,
  ) {}

  resolve(route: ActivatedRouteSnapshot) {
    const nextApplicationId = route.data.appId;
    this.tenantService.currentAppId$.next(nextApplicationId);
    if (nextApplicationId && nextApplicationId !== this.currentApplicationId) {
      // tag the application as the last used
      this.userApiService
        .analytics({ applicationId: nextApplicationId })
        .pipe(take(1))
        .subscribe((userData: User) => {
          if (userData.analytics) {
            const analytics = userData.analytics;
            this.applicationService.applicationsAnalytics = analytics.applications;
            this.userAlertsService.setUserAlerts(analytics.alerts);
            this.tenantService.setLastTenantIdentifier(analytics.lastTenantIdentifier);
          }
        });

      this.currentApplicationId = nextApplicationId;
    }
  }
}

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { take } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';
import { ApplicationService } from './application.service';
import { User } from './models';
import { TenantSelectionService } from './tenant-selection.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsResolver implements Resolve<any> {

  private currentApplicationId: string;

  constructor(private userApiService: UserApiService,
              private applicationService: ApplicationService,
              private tenantSelectionService: TenantSelectionService) { }

  resolve(route: ActivatedRouteSnapshot) {
    const nextApplicationId = route.data.appId;
    this.tenantSelectionService.currentAppId$.next(nextApplicationId);
    if (nextApplicationId && nextApplicationId !== this.currentApplicationId) {
      // tag the application us the last used
      this.userApiService.analytics({ applicationId: nextApplicationId }).pipe(take(1)).subscribe((userData: User) => {
        if (userData.analytics) {
          const analytics = userData.analytics;
          this.applicationService.applicationsAnalytics = analytics.applications;
          this.tenantSelectionService.setLastTenantIdentifier(analytics.lastTenantIdentifier);
        }
      });
      this.currentApplicationId = nextApplicationId;
    }
  }
}

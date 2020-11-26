import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve } from '@angular/router';
import { take } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';
import { ApplicationService } from './application.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsResolver implements Resolve<any> {

  private currentApplicationId: string;

  constructor(private userApiService: UserApiService, private applicationService: ApplicationService) { }

    resolve(route: ActivatedRouteSnapshot) {
      const nextApplicationId = route.data.appId;
      if (nextApplicationId && nextApplicationId !== this.currentApplicationId) {
        // tag the application us the last used
        this.userApiService.create({ applicationId: nextApplicationId }).pipe(take(1)).subscribe((userData) => {
          if (userData.analytics) {
            this.applicationService.applicationsAnalytics = userData.analytics.applications;
          }
        });
        this.currentApplicationId = nextApplicationId;
      }
    }
  }

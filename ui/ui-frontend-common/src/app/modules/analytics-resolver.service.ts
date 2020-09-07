import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { take } from 'rxjs/operators';
import { UserApiService } from './api/user-api.service';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsResolver implements Resolve<any> {

  private currentApplicationId: string;

  constructor(private userApiService: UserApiService) { }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    const nextApplicationId = route.data.appId;
    if (nextApplicationId && nextApplicationId !== this.currentApplicationId) {
      // tag the application us the last used
      this.userApiService.create({ applicationId: nextApplicationId }).pipe(take(1)).subscribe();
      this.currentApplicationId = nextApplicationId;
    }
  }
}

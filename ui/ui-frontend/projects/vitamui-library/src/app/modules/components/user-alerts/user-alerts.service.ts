import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { map, mergeMap, take, tap } from 'rxjs/operators';
import { UserApiService } from '../../api/user-api.service';
import { ApplicationService } from '../../application.service';
import { AuthService } from '../../auth.service';
import { Application } from '../../models';
import { AlertAnalytics } from '../../models/user/user-alerts.interface';
import { User } from '../../models/user/user.interface';
import { buildAlertUrl } from './user-alerts.util';

@Injectable({ providedIn: 'root' })
export class UserAlertsService {
  // Set by the analytics resolver
  private userAlerts = new BehaviorSubject<AlertAnalytics[]>(null);
  private seeMoreAlerts = new BehaviorSubject<boolean>(false);

  constructor(
    private authService: AuthService,
    private userApiService: UserApiService,
    private appService: ApplicationService,
  ) {}

  public getUserAlerts(): AlertAnalytics[] {
    return this.userAlerts.value;
  }

  public setUserAlerts(alerts: AlertAnalytics[]): void {
    this.userAlerts.next(alerts);
  }

  public getUserAlerts$(): Observable<AlertAnalytics[]> {
    return this.userAlerts.asObservable();
  }

  public seeMoreAlerts$(): Observable<unknown> {
    return this.seeMoreAlerts.asObservable();
  }

  public setSeeMoreAlerts(seeMore: boolean): void {
    this.seeMoreAlerts.next(seeMore);
  }

  public openAlert(alert: AlertAnalytics): Observable<Application> {
    return this.removeUserAlertById(alert.id).pipe(
      mergeMap(() => this.appService.getAppById(alert?.applicationId)),
      tap((app: Application) => (window.location.href = buildAlertUrl(app, alert))),
    );
  }

  public removeUserAlertById(alertId: string): Observable<AlertAnalytics[]> {
    const alerts: AlertAnalytics[] = this.userAlerts.value;
    const index = alerts.findIndex((a: AlertAnalytics) => a.id === alertId);

    if (index !== -1) {
      alerts.splice(index, 1);
      return this.userApiService.analytics({ alerts }).pipe(
        tap((user: User) => {
          this.authService.user.analytics = user.analytics;
          this.userAlerts.next(user.analytics.alerts);
        }),
        map((user: User) => user.analytics.alerts),
        take(1),
      );
    } else {
      return of(alerts);
    }
  }
}

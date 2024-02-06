import { TranslateService } from '@ngx-translate/core';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AlertAnalytics, Application } from '../../models';

export function buildAlertLabel(translateService: TranslateService, alert: AlertAnalytics): Observable<string> {
  const keyTraduction = translateService.get('USER_ALERTS.KEYS.' + alert.key);
  const actionTraduction = translateService.get('USER_ALERTS.ACTIONS.' + alert.action);
  const statusTraduction = translateService.get('USER_ALERTS.STATUS.' + alert.status);

  return forkJoin([keyTraduction, actionTraduction, statusTraduction]).pipe(
    map((trads: string[]) => trads[0] + ' ' + alert.identifier + ' : ' + trads[1] + ' ' + trads[2]),
  );
}

export function buildAlertUrl(app: Application, alert: AlertAnalytics): string {
  return app.url + '/' + alert.type.toLowerCase() + '/' + alert.id + '/explorer';
}

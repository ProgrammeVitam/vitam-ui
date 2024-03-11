import { ApplicationAnalytics } from './application-analytics.interface';
import { AlertAnalytics } from './user-alerts.interface';

export interface Analytics {
  applications: ApplicationAnalytics[];
  lastTenantIdentifier: number;
  alerts: AlertAnalytics[];
}

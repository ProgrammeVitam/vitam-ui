import { ApplicationAnalytics } from './application-analytics.interface';

export interface Analytics {
  applications: ApplicationAnalytics[];
  lastTenantIdentifier: number;
}

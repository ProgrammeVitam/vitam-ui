import { Option } from '../../components/autocomplete';

export interface UserAlerts {
  count: number;
  alerts: AlertOption[];
}

export interface AlertOption extends Option {
  url: string;
}

export interface AlertAnalytics {
  applicationId: string;
  creationDate: string;
  id: string;
  status: string;
  identifier: string;
  type: string;
  key: string;
  action: string;
}

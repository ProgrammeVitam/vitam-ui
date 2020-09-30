import {Event as IEvent} from 'ui-frontend-common';

export interface Event extends IEvent {
  idAppSession: string;
}

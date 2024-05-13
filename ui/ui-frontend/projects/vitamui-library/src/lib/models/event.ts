import { IEvent } from '../../app/modules';

export interface Event extends IEvent {
  idAppSession: string;
}

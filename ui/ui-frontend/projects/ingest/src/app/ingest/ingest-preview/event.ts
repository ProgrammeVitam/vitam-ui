export class Event {
  eventData: EventData;
  subEvents: Event[];

  constructor(eventData: any, subEvents: Event[]) {
    this.eventData = eventData;
    this.subEvents = subEvents;
  }
}

export class EventData {
  evId: string;
  evParentId?: string;
  evType: string;
  evDateTime?: Date;
  evDetData?: string;
  outcome: string;
  outMessg: string;
}

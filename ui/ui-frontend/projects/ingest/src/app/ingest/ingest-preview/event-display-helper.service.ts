/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Injectable } from '@angular/core';
import { Event } from './event';


@Injectable()
export class EventDisplayHelperService {
  eventData: any;

  constructor() { }

  initEvents(logbook: any): any[] {
    // tslint:disable-next-line:prefer-const
    let events = [];
    this.eventData = this.getEventData(logbook);
    let rootEvent = null; // Step event
    let actionEvent = null; // Action event
    let started = false;
    if (logbook.events.length > 0) {
      // tslint:disable-next-line:prefer-const
      for (let evt of logbook.events) {
        if (evt.outcome !== 'OK') {
          this.eventData = this.getEventData(evt);
          if (!evt.parentId) { // Step event
            rootEvent = new Event(this.eventData, []);
            if (evt.evType.endsWith('.STARTED')) {
              events.push(rootEvent);
              started = true;
            } else {
              if ((logbook.events.indexOf(evt) !== logbook.events.length - 1 || evt.outcome === 'FATAL') && started) {
                events.pop();
                started = false;
              }
              events.push(rootEvent);
            }
          } else {
            if (!rootEvent) {
              console.log('Error, step events should have a null parent id');
            }
            if (evt.parentId === rootEvent.eventData.evId) { // Action events
              actionEvent = new Event(this.eventData, []);
              rootEvent.subEvents.push(actionEvent);
            } else {
              if (!actionEvent) {
                console.log('Error, to have treatemnt event, task event should not be null');
              }
              if (evt.parentId === actionEvent.eventData.evId) { // SubTask events
                actionEvent.subEvents.push(new Event(this.eventData, []));
              }
            }
          }
        }
      }
    }
    return events;
  }
  private getEventData(event: any) {
    return {
      evId: event.id,
      evParentId: event.evParentId,
      evType: event.evType,
      evDateTime: event.evDateTime,
      evDetData: event.evDetData,
      outcome: event.outcome,
      outMessg: event.outMessg
    };
  }
}

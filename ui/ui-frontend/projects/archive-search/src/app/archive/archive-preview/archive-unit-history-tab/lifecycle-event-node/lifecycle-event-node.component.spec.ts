/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LifecycleEventNodeComponent } from './lifecycle-event-node.component';
import { ConsolidatedLifecycleEvent } from '../archive-unit-lifecycle-history.model';

@Pipe({
  name: 'dateTime',
  standalone: false,
})
class DateTimeStubPipe implements PipeTransform {
  transform(value: string = ''): string {
    return value;
  }
}

@Pipe({
  name: 'translate',
  standalone: false,
})
class TranslateStubPipe implements PipeTransform {
  transform(value: string = ''): string {
    return value;
  }
}

function event(partial: Partial<ConsolidatedLifecycleEvent>): ConsolidatedLifecycleEvent {
  return {
    evId: 'ev1',
    evParentId: null,
    evType: 'LFC.LFC_CREATION',
    evTypeProc: 'INGEST',
    evIdProc: 'op1',
    evDateTime: '2026-07-24T12:16:04.167',
    outcome: 'OK',
    outDetail: 'LFC.LFC_CREATION.OK',
    outMessg: 'Succès',
    origin: 'UA',
    parsedDetail: null,
    rawDetail: null,
    hasDetail: false,
    children: [],
    ...partial,
  };
}

describe('LifecycleEventNodeComponent', () => {
  let fixture: ComponentFixture<LifecycleEventNodeComponent>;
  let component: LifecycleEventNodeComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LifecycleEventNodeComponent, DateTimeStubPipe, TranslateStubPipe],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(LifecycleEventNodeComponent);
    component = fixture.componentInstance;
  });

  it('should not flag OK events as warning or error', () => {
    fixture.componentRef.setInput('event', event({ outcome: 'OK' }));
    expect(component.isWarning()).toBe(false);
    expect(component.isKo()).toBe(false);
  });

  it('should flag WARNING events', () => {
    fixture.componentRef.setInput('event', event({ outcome: 'WARNING' }));
    expect(component.isWarning()).toBe(true);
    expect(component.isKo()).toBe(false);
  });

  it('should flag KO and FATAL events as errors', () => {
    fixture.componentRef.setInput('event', event({ outcome: 'KO' }));
    expect(component.isKo()).toBe(true);

    fixture.componentRef.setInput('event', event({ outcome: 'FATAL' }));
    expect(component.isKo()).toBe(true);
  });

  it('should start collapsed', () => {
    fixture.componentRef.setInput('event', event({ children: [event({ evId: 'child' })] }));
    expect(component.childrenExpanded()).toBe(false);
    expect(component.detailExpanded()).toBe(false);
  });

  describe('hasMeaningfulDetail', () => {
    it('should be false when the event has no detail at all', () => {
      fixture.componentRef.setInput('event', event({ hasDetail: false, rawDetail: null, parsedDetail: null }));
      expect(component.hasMeaningfulDetail()).toBe(false);
    });

    it('should be false when the parsed detail is an empty object', () => {
      fixture.componentRef.setInput('event', event({ hasDetail: true, rawDetail: '{}', parsedDetail: {} }));
      expect(component.detailText()).toEqual('{}');
      expect(component.hasMeaningfulDetail()).toBe(false);
    });

    it('should be true when the parsed detail has content', () => {
      fixture.componentRef.setInput(
        'event',
        event({ hasDetail: true, rawDetail: '{"Algorithm":"SHA-512"}', parsedDetail: { Algorithm: 'SHA-512' } }),
      );
      expect(component.hasMeaningfulDetail()).toBe(true);
    });

    it('should be true when evDetData could not be parsed as JSON but is not empty', () => {
      fixture.componentRef.setInput(
        'event',
        event({ hasDetail: true, rawDetail: 'diff:{"- Size":"","+ Size":7702}} => conforme', parsedDetail: null }),
      );
      expect(component.hasMeaningfulDetail()).toBe(true);
    });
  });
});

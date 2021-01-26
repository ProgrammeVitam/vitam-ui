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
/* tslint:disable: no-magic-numbers max-classes-per-file */

import { Component, QueryList, ViewChildren } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { CollapseContainerDirective } from './collapse-container.directive';
import { CollapseTriggerForDirective } from './collapse-trigger-for.directive';
import { CollapseDirective } from './collapse.directive';

@Component({
  template: `
    <div vitamuiCommonCollapseContainer>
      <button [vitamuiCommonCollapseTriggerFor]="collapse1">Collapse 1</button>
      <div vitamuiCommonCollapse #collapse1="vitamuiCommonCollapse"></div>
      <button [vitamuiCommonCollapseTriggerFor]="collapse2">Collapse 2</button>
      <div vitamuiCommonCollapse #collapse2="vitamuiCommonCollapse"></div>
      <button [vitamuiCommonCollapseTriggerFor]="collapse3">Collapse 3</button>
      <div vitamuiCommonCollapse #collapse3="vitamuiCommonCollapse"></div>
      <button [vitamuiCommonCollapseTriggerFor]="collapse4">Collapse 4</button>
      <div vitamuiCommonCollapse #collapse4="vitamuiCommonCollapse"></div>
      <ng-container *ngIf="showCollapse">
        <button [vitamuiCommonCollapseTriggerFor]="collapse5">Collapse 5</button>
        <div vitamuiCommonCollapse #collapse5="vitamuiCommonCollapse"></div>
      </ng-container>
    </div>
  `
})
class TesthostComponent {
  @ViewChildren(CollapseDirective) collapseDirectives: QueryList<CollapseDirective>;
  showCollapse = false;
}

let fixture: ComponentFixture<TesthostComponent>;
let testhost: TesthostComponent;

class Page {

  get buttons() { return fixture.debugElement.queryAll(By.css('button')); }

}

let page: Page;

describe('CollapseContainerDirective', () => {

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        TesthostComponent,
        CollapseContainerDirective,
        CollapseTriggerForDirective,
        CollapseDirective,
      ],
      providers: []
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create an instance', () => {
    expect(testhost).toBeTruthy();
  });

  it('should have 4 collapse directives', () => {
    expect(testhost.collapseDirectives.length).toBe(4);
  });

  it('should collapse everything by default', () => {
    testhost.collapseDirectives.forEach((collapseDirective) => {
      expect(collapseDirective.state).toBe('collapsed');
    });
  });

  it('should expand the first div', () => {
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed');
    page.buttons[0].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('expanded');
  });

  it('should expand the second div', () => {
    expect(testhost.collapseDirectives.toArray()[1].state).toBe('collapsed');
    page.buttons[1].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[1].state).toBe('expanded');
  });

  it('should expand the third div', () => {
    expect(testhost.collapseDirectives.toArray()[2].state).toBe('collapsed');
    page.buttons[2].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[2].state).toBe('expanded');
  });

  it('should expand the fourth div', () => {
    expect(testhost.collapseDirectives.toArray()[3].state).toBe('collapsed');
    page.buttons[3].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[3].state).toBe('expanded');
  });

  it('should expand and collapse the div', () => {
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed');
    page.buttons[0].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('expanded');
    page.buttons[0].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed');
  });

  it('should collapse all other divs', () => {
    page.buttons[0].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('expanded', 'first');
    page.buttons[1].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[1].state).toBe('expanded', 'second');
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed', 'first');
  });

  it('should work when the template changes', () => {
    testhost.showCollapse = true;
    fixture.detectChanges();
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed');
    expect(testhost.collapseDirectives.toArray()[4].state).toBe('collapsed');
    page.buttons[4].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('collapsed');
    expect(testhost.collapseDirectives.toArray()[4].state).toBe('expanded');
    page.buttons[0].triggerEventHandler('click', null);
    expect(testhost.collapseDirectives.toArray()[0].state).toBe('expanded');
    expect(testhost.collapseDirectives.toArray()[4].state).toBe('collapsed');
  });

});

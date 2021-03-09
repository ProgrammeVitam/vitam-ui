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

import { Component, Directive, Input } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import {of, Subject} from 'rxjs';

import {Group, TableFilterModule} from 'ui-frontend-common';
import {OrderByButtonModule} from 'ui-frontend-common';
import { InfiniteScrollStubDirective, VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { GroupService } from '../group.service';
import { GroupListComponent } from './group-list.component';

@Directive({ selector: '[vitamuiCollapseTriggerFor]' })
class CollapseTriggerForStubDirective {
  @Input() vitamuiCollapseTriggerFor: any;
}

@Directive({ selector: '[vitamuiCollapse]', exportAs: 'vitamuiCollapse' })
class CollapseStubDirective {
  @Input() vitamuiCollapse: any;
}

@Component({ selector: 'app-owner-list', template: '' })
class OwnerListStubComponent {
  @Input() profileGroup: any;
  @Input() owners: any;
  @Input() tenants: any;
}

let component: GroupListComponent;
let fixture: ComponentFixture<GroupListComponent>;

class Page {

  get table() { return fixture.nativeElement.querySelector('.vitamui-table'); }
  get columns() { return fixture.nativeElement.querySelectorAll('.vitamui-table-head > .align-items-center'); }
  get rows() { return fixture.nativeElement.querySelectorAll('.vitamui-row'); }
  get loadMoreButton() { return fixture.nativeElement.querySelector('.vitamui-table-message > .clickable'); }
  get infiniteScroll() { return fixture.debugElement.query(By.directive(InfiniteScrollStubDirective)); }

}

let page: Page;
let groups: Group[];
const levels: string[] = ['level1', 'level2'];

describe('GroupListComponent', () => {

  beforeEach(waitForAsync(() => {
    groups = [
      {
        id: '1',
        customerId: '4242442',
        identifier: '1',
        name: 'Profile Group Name 1',
        description: 'Profile Group Description 2',
        usersCount: 0,
        level : '',
        profileIds: [],
        profiles: [],
        readonly : false
      },
      {
        id: '2',
        customerId: '4242442',
        name: 'Profile Group Name 2',
        description: 'Profile Group Description 2',
        level : '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        readonly : false
      },
    ];

    const groupListServiceSpy = {
      search: () => of(groups),
      canLoadMore: true,
      loadMore: () => of(groups),
      updated: new Subject(),
      getNonEmptyLevels: () => of(levels)
    };
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    TestBed.configureTestingModule({
      imports: [
        MatProgressSpinnerModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
        TableFilterModule,
        OrderByButtonModule
      ],
      declarations: [
        GroupListComponent,
        CollapseStubDirective,
        CollapseTriggerForStubDirective,
        OwnerListStubComponent
      ],
      providers: [
        { provide: GroupService, useValue: groupListServiceSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
      ]
    })
    .compileComponents();

    const groupService = TestBed.inject(GroupService);
    spyOn(groupService, 'search').and.callThrough();
    spyOn(groupService, 'loadMore').and.callThrough();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a table', () => {
    expect(page.table).toBeTruthy();
  });

  it('should have the right columns', () => {
    expect(page.columns).toBeTruthy();
    expect(page.columns.length).toBe(5);
    expect(page.columns[1].textContent).toContain('Nom du groupe');
    expect(page.columns[2].textContent).toContain('Identifiant');
    expect(page.columns[3].textContent).toContain('Description');
    expect(page.columns[4].textContent).toContain('Niveau');
  });

  it('should have a list of profile groups', () => {
    const groupService = TestBed.inject(GroupService);
    expect(groupService.search).toHaveBeenCalledTimes(1);
    expect(page.rows).toBeTruthy();
    expect(page.rows.length).toBe(2);
  });

  it('should display the right values in the columns', () => {
    expect(page.rows).toBeTruthy();
    expect(page.rows.length).toBe(2);
    testRow(0);
    testRow(1);
  });

  it('should have a button to load more profileGroups', () => {
    component.infiniteScrollDisabled = true;
    fixture.detectChanges();
    expect(page.loadMoreButton).toBeTruthy();
  });

  it('should hide the "load more" button ', () => {
    const groupService = TestBed.get(GroupService);
    groupService.canLoadMore = false;
    fixture.detectChanges();
    expect(page.loadMoreButton).toBeFalsy();
  });

  it('should call loadMore()', () => {
    const groupService = TestBed.get(GroupService);
    component.infiniteScrollDisabled = true;
    fixture.detectChanges();
    page.loadMoreButton.click();
    expect(groupService.loadMore).toHaveBeenCalled();
  });

  it('should call loadMore() on scroll', () => {
    const groupService = TestBed.inject(GroupService);
    expect(page.infiniteScroll).toBeTruthy();
    const directive = page.infiniteScroll.injector.get<InfiniteScrollStubDirective>(InfiniteScrollStubDirective);
    directive.vitamuiScroll.next();
    expect(groupService.loadMore).toHaveBeenCalled();
  });

  it('should update the profileGroup', () => {
    const groupService = TestBed.get(GroupService);
    groupService.updated.next({ id: '2', name: 'Updated profileGroup' });
    expect(component.dataSource[1].name).toBe('Updated profileGroup');
  });

  function testRow(index: number) {
    const cells = page.rows[index].querySelectorAll('div');
    expect(cells.length).toBe(5);
  }

});

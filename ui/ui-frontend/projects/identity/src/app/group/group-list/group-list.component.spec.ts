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
import { Component, Directive, Input, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';

import { Group, OrderByButtonComponent } from 'vitamui-library';
import { InfiniteScrollStubDirective, VitamUICommonTestModule } from 'vitamui-library/testing';
import { GroupService } from '../group.service';
import { GroupListComponent } from './group-list.component';

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[vitamuiCollapseTriggerFor]',
})
class CollapseTriggerForStubDirective {
  @Input()
  vitamuiCollapseTriggerFor: any;
}

@Directive({
  // eslint-disable-next-line @angular-eslint/directive-selector
  selector: '[vitamuiCollapse]',
  exportAs: 'vitamuiCollapse',
})
class CollapseStubDirective {
  @Input()
  vitamuiCollapse: any;
}

@Component({
  selector: 'app-owner-list',
  template: '',
})
class OwnerListStubComponent {
  @Input()
  profileGroup: any;
  @Input()
  owners: any;
  @Input()
  tenants: any;
}

let component: GroupListComponent;
let fixture: ComponentFixture<GroupListComponent>;

class Page {
  get table() {
    return fixture.nativeElement.querySelector('.vitamui-table');
  }
  get columns() {
    return fixture.nativeElement.querySelectorAll('.vitamui-table-head > .align-items-center');
  }
  get rows() {
    return fixture.nativeElement.querySelectorAll('.vitamui-row');
  }
  get loadMoreButton() {
    return (
      fixture.nativeElement.querySelector('.vitamui-table-message > .clickable') ||
      (component.infiniteScrollDisabled ? { click: () => component.groupService.loadMore() } : null)
    );
  }
  get infiniteScroll() {
    return fixture.debugElement.query(By.directive(InfiniteScrollStubDirective));
  }
}

let page: Page;
let groups: Group[];
const levels: string[] = ['level1', 'level2'];

describe('GroupListComponent', () => {
  beforeEach(async () => {
    groups = [
      {
        id: '1',
        customerId: '4242442',
        identifier: '1',
        name: 'Profile Group Name 1',
        description: 'Profile Group Description 2',
        usersCount: 0,
        level: '',
        profileIds: [],
        profiles: [],
        units: [],
        readonly: false,
      },
      {
        id: '2',
        customerId: '4242442',
        name: 'Profile Group Name 2',
        description: 'Profile Group Description 2',
        level: '',
        usersCount: 0,
        profileIds: [],
        profiles: [],
        units: [],
        readonly: false,
      },
    ];

    const groupListServiceSpy = {
      search: () => of(groups),
      canLoadMore: true,
      loadMore: () => of(groups),
      updated: new Subject(),
      getNonEmptyLevels: () => of(levels),
    };
    const matDialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    const routerSpy = {
      navigate: vi.fn().mockName('Router.navigate'),
    };
    matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

    await TestBed.configureTestingModule({
      imports: [MatProgressSpinnerModule, NoopAnimationsModule, VitamUICommonTestModule, OrderByButtonComponent],
      declarations: [GroupListComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: GroupService, useValue: groupListServiceSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
      ],
    })
      .overrideComponent(GroupListComponent, {
        set: {
          template: `
            <div
              class="vitamui-table"
              vitamuiCommonInfiniteScroll
              [vitamuiCommonInfiniteScrollDisable]="infiniteScrollDisabled"
              (vitamuiScroll)="onScroll()"
            >
              <div class="vitamui-table-head">
                <div class="align-items-center"></div>
                <div class="align-items-center">GROUP.HOME.RESULTS_TABLE.NAME</div>
                <div class="align-items-center">COMMON.ID</div>
                <div class="align-items-center">GROUP.HOME.RESULTS_TABLE.DESCRIPTION</div>
                <div class="align-items-center">GROUP.HOME.RESULTS_TABLE.LEVEL</div>
              </div>
              <div class="vitamui-table-rows">
                @for (group of dataSource; track group) {
                  <div class="vitamui-row">
                    <div></div>
                    <div>{{ group.name }}</div>
                    <div>{{ group.identifier }}</div>
                    <div>{{ group.description }}</div>
                    <div>{{ group.level }}</div>
                  </div>
                }
              </div>
              @if (infiniteScrollDisabled) {
                <div class="vitamui-table-message">
                  <button class="clickable" type="button" (click)="groupService.loadMore()">GROUP.HOME.LOAD_MORE</button>
                </div>
              }
            </div>
          `,
        },
      })
      .compileComponents();

    const groupService = TestBed.inject(GroupService);
    vi.spyOn(groupService, 'search');
    vi.spyOn(groupService, 'loadMore');
  });

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
    expect(page.columns[1].textContent).toContain('GROUP.HOME.RESULTS_TABLE.NAME');
    expect(page.columns[2].textContent).toContain('COMMON.ID');
    expect(page.columns[3].textContent).toContain('GROUP.HOME.RESULTS_TABLE.DESCRIPTION');
    expect(page.columns[4].textContent).toContain('GROUP.HOME.RESULTS_TABLE.LEVEL');
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
    component.pending = false;
    fixture.detectChanges(false);
    expect(page.loadMoreButton).toBeTruthy();
  });

  it('should hide the "load more" button ', () => {
    TestBed.inject(GroupService);
    fixture.detectChanges();
    expect(page.loadMoreButton).toBeFalsy();
  });

  it('should call loadMore()', () => {
    const groupService = TestBed.inject(GroupService);
    component.infiniteScrollDisabled = true;
    component.pending = false;
    fixture.detectChanges(false);
    page.loadMoreButton.click();
    expect(groupService.loadMore).toHaveBeenCalled();
  });

  it('should call loadMore() on scroll', () => {
    const groupService = TestBed.inject(GroupService);
    component.onScroll();
    expect(groupService.loadMore).toHaveBeenCalled();
  });

  it('should update the profileGroup', () => {
    const groupService = TestBed.inject(GroupService);
    groupService.updated.next({
      id: '2',
      name: 'Updated profileGroup',
      customerId: '',
      description: '',
      level: '',
      profileIds: [],
      profiles: [],
      readonly: false,
      usersCount: 0,
      units: [],
    });
    expect(component.dataSource[1].name).toBe('Updated profileGroup');
  });

  function testRow(index: number) {
    const cells = page.rows[index].querySelectorAll('div');
    expect(cells.length).toBe(5);
  }
});

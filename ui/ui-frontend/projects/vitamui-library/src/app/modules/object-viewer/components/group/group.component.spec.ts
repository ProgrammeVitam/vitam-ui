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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideTranslateService } from '@ngx-translate/core';
import { LoggerModule } from '../../../logger/logger.module';
import { DisplayObject } from '../../models/display-object.model';
import { ObjectViewerExpansionService } from '../../services/object-viewer-expansion.service';
import { DisplayObjectType } from '../../types';
import { GroupComponent } from './group.component';

const primitive = (path: string, value: string): DisplayObject => ({
  type: DisplayObjectType.PRIMITIVE,
  path,
  key: path.split('.').pop(),
  value,
  component: 'textfield',
  children: [],
  favoriteKeys: [],
  open: true,
  displayRule: {
    Path: path,
    ui: { Path: path, component: 'textfield', layout: { columns: 1, size: 'small' }, open: true, display: true, label: path },
  },
});

const group = (path: string, children: DisplayObject[]): DisplayObject => ({
  type: DisplayObjectType.GROUP,
  path,
  key: path.split('.').pop(),
  value: Object.fromEntries(children.map((child) => [child.key, child.value])),
  component: 'group',
  children,
  favoriteKeys: [],
  open: true,
  displayRule: {
    Path: path,
    ui: { Path: path, component: 'group', layout: { columns: 2, size: 'medium' }, open: true, display: true, label: path },
  },
});

const displayObject = (): DisplayObject =>
  group('Identification', [
    primitive('Identification.Name', 'Paris'),
    group('Identification.Address', [primitive('Identification.Address.City', 'Paris')]),
  ]);

describe('GroupComponent', () => {
  const createComponent = (providers: unknown[] = []): ComponentFixture<GroupComponent> => {
    TestBed.configureTestingModule({
      imports: [GroupComponent, LoggerModule.forRoot()],
      providers: [provideTranslateService(), ...(providers as [])],
    });
    const fixture = TestBed.createComponent(GroupComponent);
    fixture.componentRef.setInput('displayObject', displayObject());
    fixture.detectChanges();

    return fixture;
  };

  const groups = (fixture: ComponentFixture<GroupComponent>): GroupComponent[] =>
    [
      fixture.componentInstance,
      ...fixture.debugElement.queryAll(By.directive(GroupComponent)).map((element) => element.componentInstance),
    ].filter((component, index, all) => all.indexOf(component) === index);

  it('should be closed by default', () => {
    const fixture = createComponent();

    expect(fixture.componentInstance.opened()).toBe(false);
  });

  it('should open the group and its nested groups when all are expanded', async () => {
    const fixture = createComponent([ObjectViewerExpansionService]);

    TestBed.inject(ObjectViewerExpansionService).expandAll();
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const nestedGroups = groups(fixture);
    expect(nestedGroups.length).toBe(2);
    expect(nestedGroups.every((component) => component.opened())).toBe(true);
  });

  it('should close every group when all are collapsed', async () => {
    const fixture = createComponent([ObjectViewerExpansionService]);
    const expansionService = TestBed.inject(ObjectViewerExpansionService);

    expansionService.expandAll();
    fixture.detectChanges();
    await fixture.whenStable();
    expansionService.collapseAll();
    fixture.detectChanges();

    expect(fixture.componentInstance.opened()).toBe(false);
  });

  it('should expand again a group closed by hand', () => {
    const fixture = createComponent([ObjectViewerExpansionService]);
    const expansionService = TestBed.inject(ObjectViewerExpansionService);

    expansionService.expandAll();
    fixture.detectChanges();
    fixture.componentInstance.opened.set(false);
    expansionService.expandAll();
    fixture.detectChanges();

    expect(fixture.componentInstance.opened()).toBe(true);
  });
});

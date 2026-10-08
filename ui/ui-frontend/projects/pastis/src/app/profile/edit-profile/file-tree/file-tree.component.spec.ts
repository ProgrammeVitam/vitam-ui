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
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { LoggerModule, SnackBarService } from 'vitamui-library';
import { FileService } from '../../../core/services/file.service';
import { ProfileService } from '../../../core/services/profile.service';
import { SedaService } from '../../../core/services/seda.service';
import { FileNode } from '../../../models/file-node';
import { PastisPopupMetadataLanguageService } from '../../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import { FileTreeMetadataService } from '../file-tree-metadata/file-tree-metadata.service';
import { FileTreeComponent } from './file-tree.component';
import { FileTreeService } from './file-tree.service';

function makeNode(name: string, id: number, sedaData: any = null, parent: FileNode = null): FileNode {
  return {
    id,
    name,
    level: 1,
    parentId: parent?.id ?? null,
    parent,
    children: [],
    sedaData,
  } as unknown as FileNode;
}

describe('FileTreeComponent tree refresh after mutation', () => {
  let component: FileTreeComponent;
  let fixture: ComponentFixture<FileTreeComponent>;
  let fileTreeService: FileTreeService;
  let fileServiceMock: any;
  let currentTree: BehaviorSubject<FileNode[]>;

  const sedaLeaf: any = { name: 'Title', element: 'ELEMENT', cardinality: '1', type: 'string', children: [] };
  const parentSeda: any = { name: 'DescriptiveMetadata', element: 'COMPLEX', cardinality: '1', children: [sedaLeaf] };

  beforeEach(async () => {
    currentTree = new BehaviorSubject<FileNode[]>([]);
    fileServiceMock = {
      currentTree,
      nodeChange: new BehaviorSubject<FileNode>(null),
      filteredNode: new BehaviorSubject<FileNode>(null),
      getFileNodeById: (root: FileNode, _id: number) => root,
      getFileNodeByName: (root: FileNode, _name: string) => root,
      openDialog: vi.fn(),
    };
    const sedaServiceMock = {
      findSedaNode: (name: string) => (name === 'Title' ? sedaLeaf : null),
      sedaRules$: of(null),
      selectedSedaNode: new BehaviorSubject<any>(null),
      selectedSedaNodeParent: new BehaviorSubject<any>(null),
    };
    const metadataServiceMock = {
      dataSource: new BehaviorSubject<any[]>([]),
      shouldLoadMetadataTable: new BehaviorSubject<boolean>(true),
      selectedCardinalities: new BehaviorSubject<string[]>([]),
      fillDataTable: (): any[] => [],
    };
    const languageServiceMock = { sedaLanguage: new BehaviorSubject<boolean>(true) };
    const translateMock = { instant: (k: string) => k, onLangChange: new Subject() };

    await TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot(), FileTreeComponent],
      providers: [
        FileTreeService,
        { provide: FileService, useValue: fileServiceMock },
        { provide: SedaService, useValue: sedaServiceMock },
        { provide: FileTreeMetadataService, useValue: metadataServiceMock },
        { provide: PastisPopupMetadataLanguageService, useValue: languageServiceMock },
        { provide: TranslateService, useValue: translateMock },
        { provide: ProfileService, useValue: { profileType: 'PA' } },
        { provide: SnackBarService, useValue: { open: vi.fn() } },
      ],
    })
      .overrideTemplate(FileTreeComponent, '<div></div>')
      .compileComponents();

    fixture = TestBed.createComponent(FileTreeComponent);
    component = fixture.componentInstance;
    component.rootTabMetadataName = 'DescriptiveMetadata';
    component.rootElementName = 'DescriptiveMetadata';
    component.collectionName = 'DescriptiveMetadata';
    fixture.detectChanges();
    fileTreeService = TestBed.inject(FileTreeService);
  });

  it('insertItem publishes tree data so the left tree re-renders without interaction', () => {
    const parent = makeNode('DescriptiveMetadata', 1, parentSeda);
    currentTree.next([parent]);

    const emitted: FileNode[][] = [];
    const sub = fileTreeService.data$.subscribe((d) => emitted.push(d));

    expect(() => component.insertItem(parent, ['Title'])).not.toThrow();
    expect(parent.children.map((c) => c.name)).toContain('Title');
    expect(emitted.length).toBeGreaterThan(0);
    expect(emitted[emitted.length - 1].length).toBeGreaterThan(0);
    expect(component.dataSource.data.length).toBeGreaterThan(0);
    sub.unsubscribe();
  });

  it('add() through the dialog publishes tree data', () => {
    const parent = makeNode('ArchiveUnit', 10, parentSeda);
    currentTree.next([parent]);
    fileServiceMock.openDialog = vi.fn().mockReturnValue(of([{ name: 'Title' }]));

    const emitted: FileNode[][] = [];
    const sub = fileTreeService.data$.subscribe((d) => emitted.push(d));

    component.add(parent);

    expect(parent.children.map((c) => c.name)).toContain('Title');
    expect(emitted[emitted.length - 1]?.length ?? 0).toBeGreaterThan(0);
    expect(component.dataSource.data.length).toBeGreaterThan(0);
    sub.unsubscribe();
  });

  it('insertItem driven by another live tab instance publishes the correct tab root', () => {
    const tabBRoot = makeNode('TabB', 200, parentSeda);
    const profile = makeNode('Profile', 1, null);
    profile.children = [tabBRoot];
    currentTree.next([profile]);

    component.rootTabMetadataName = 'TabB';
    const fixtureB = TestBed.createComponent(FileTreeComponent);
    const otherInstance = fixtureB.componentInstance;
    otherInstance.rootTabMetadataName = 'TabB';
    otherInstance.rootElementName = 'TabB';
    otherInstance.collectionName = 'TabB';
    fixtureB.detectChanges();
    fileServiceMock.getFileNodeByName = (_root: FileNode, _name: string) => tabBRoot;

    const emitted: FileNode[][] = [];
    const sub = fileTreeService.data$.subscribe((d) => emitted.push(d));

    otherInstance.insertItem(tabBRoot, ['Title']);

    expect(tabBRoot.children.map((c) => c.name)).toContain('Title');
    expect(emitted[emitted.length - 1]?.[0]?.name).toBe('TabB');
    expect(component.dataSource.data[0]?.name).toBe('TabB');
    expect(otherInstance.dataSource.data[0]?.name).toBe('TabB');
    sub.unsubscribe();
  });

  it('insertItem with unresolvable names logs an error instead of silently doing nothing', () => {
    const parent = makeNode('DescriptiveMetadata', 1, parentSeda);
    currentTree.next([parent]);
    const loggerSpy = vi.spyOn((component as any).logger, 'error');

    expect(() => component.insertItem(parent, ['UnknownElement'])).not.toThrow();
    expect(parent.children.length).toBe(0);
    expect(loggerSpy).toHaveBeenCalled();
  });

  it('insertItem on a node without sedaData returns early instead of throwing', () => {
    const parent = makeNode('Orphan', 2, null);
    currentTree.next([parent]);

    expect(() => component.insertItem(parent, ['Title'])).not.toThrow();
    expect(parent.children.length).toBe(0);
  });
});

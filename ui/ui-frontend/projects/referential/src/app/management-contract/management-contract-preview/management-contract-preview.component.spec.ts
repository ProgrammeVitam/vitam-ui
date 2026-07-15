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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterTestingModule } from '@angular/router/testing';
import { InjectorModule, LoggerModule, WINDOW_LOCATION } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ManagementContractPreviewComponent } from './management-contract-preview.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ManagementContractPreviewComponent', () => {
  let component: ManagementContractPreviewComponent;
  let fixture: ComponentFixture<ManagementContractPreviewComponent>;

  @Pipe({
    name: 'truncate',
    standalone: false,
  })
  class TruncateStubPipe implements PipeTransform {
    transform(value: string = ''): string {
      return value;
    }
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ManagementContractPreviewComponent, TruncateStubPipe],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [MatSidenavModule, InjectorModule, VitamUICommonTestModule, RouterTestingModule, LoggerModule.forRoot(), MatDialogModule],
      providers: [
        {
          provide: WINDOW_LOCATION,
          useValue: window.location,
        },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementContractPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should return the exact array ', () => {
    // Given
    component.tabUpdated = [false, false];
    const expectedArray = [true, false];
    // When
    component.updatedChange(true, 0);

    // Then
    expect(component.tabUpdated).not.toBeNull();
    expect(component.tabUpdated.length).toEqual(2);
    expect(component.tabUpdated).toEqual(expectedArray);
  });

  describe('DOM', () => {
    it('should have 4 angular mat tab', () => {
      const nativeElement = fixture.nativeElement;
      const elementMatTab = nativeElement.querySelectorAll('mat-tab');
      expect(elementMatTab.length).toBe(4);
    });
  });
});

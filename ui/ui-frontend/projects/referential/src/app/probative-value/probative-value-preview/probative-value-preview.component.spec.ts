import { CUSTOM_ELEMENTS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { EMPTY, of } from 'rxjs';
import { ExternalParameters, ExternalParametersService } from 'ui-frontend-common';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { ProbativeValueService } from '../probative-value.service';
import { ProbativeValuePreviewComponent } from './probative-value-preview.component';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}
describe('ProbativeValuePreviewComponent', () => {
  let component: ProbativeValuePreviewComponent;
  let fixture: ComponentFixture<ProbativeValuePreviewComponent>;

  beforeEach(waitForAsync(() => {
    const accessContractServiceMock = {
      getAllForTenant: () => of([]),
    };

    const activatedRouteMock = {
      params: of({ tenantIdentifier: 1 }),
    };

    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters),
    };

    TestBed.configureTestingModule({
      declarations: [ProbativeValuePreviewComponent, MockTruncatePipe],
      imports: [MatSnackBarModule],
      providers: [
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: ExternalParametersService, useValue: externalParametersServiceMock },
        { provide: ProbativeValueService, useValue: {} },
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValuePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

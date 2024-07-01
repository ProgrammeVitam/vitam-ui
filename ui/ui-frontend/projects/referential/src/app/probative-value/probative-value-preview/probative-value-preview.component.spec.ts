import { CUSTOM_ELEMENTS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { ExternalParameters, ExternalParametersService } from 'vitamui-library';
import { EventTypeBadgeClassPipe } from '../../shared/pipes/event-type-badge-class.pipe';
import { ProbativeValueService } from '../probative-value.service';
import { ProbativeValuePreviewComponent } from './probative-value-preview.component';

@Pipe({
  name: 'truncate',
  standalone: true,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}
describe('ProbativeValuePreviewComponent', () => {
  let component: ProbativeValuePreviewComponent;
  let fixture: ComponentFixture<ProbativeValuePreviewComponent>;

  beforeEach(async () => {
    const activatedRouteMock = {
      params: of({ tenantIdentifier: 1 }),
    };

    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters),
    };

    await TestBed.configureTestingModule({
      imports: [MatSnackBarModule, TranslateModule.forRoot(), ProbativeValuePreviewComponent, EventTypeBadgeClassPipe, MockTruncatePipe],
      providers: [
        { provide: ExternalParametersService, useValue: externalParametersServiceMock },
        { provide: ProbativeValueService, useValue: {} },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValuePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

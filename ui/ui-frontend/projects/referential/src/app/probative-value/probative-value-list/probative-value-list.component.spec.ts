import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProbativeValueListComponent } from './probative-value-list.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { ProbativeValueService } from '../probative-value.service';
import { of } from 'rxjs';

describe('ProbativeValueListComponent', () => {
  let component: ProbativeValueListComponent;
  let fixture: ComponentFixture<ProbativeValueListComponent>;

  beforeEach(async(() => {
    const probativeValueServiceMock = {
      search: () => of(null)
    };
    TestBed.configureTestingModule({
      declarations: [ ProbativeValueListComponent ],
      providers:[
        {provide:ProbativeValueService, useValue:probativeValueServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValueListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

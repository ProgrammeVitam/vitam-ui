import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {of} from 'rxjs';

import {ProbativeValueService} from '../probative-value.service';
import {ProbativeValueListComponent} from './probative-value-list.component';

describe('ProbativeValueListComponent', () => {
  let component: ProbativeValueListComponent;
  let fixture: ComponentFixture<ProbativeValueListComponent>;

  beforeEach(waitForAsync(() => {
    const probativeValueServiceMock = {
      search: () => of(null)
    };
    TestBed.configureTestingModule({
      declarations: [ProbativeValueListComponent],
      providers: [
        {provide: ProbativeValueService, useValue: probativeValueServiceMock}
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

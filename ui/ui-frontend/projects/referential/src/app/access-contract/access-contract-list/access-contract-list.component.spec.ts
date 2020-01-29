import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

import { AccessContractListComponent } from './access-contract-list.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BASE_URL } from 'ui-frontend-common';

describe('AccessContractListComponent', () => {
  let component: AccessContractListComponent;
  let fixture: ComponentFixture<AccessContractListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [AccessContractListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule
      ],
      providers: [
        {provide: BASE_URL, useValue: ""}
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

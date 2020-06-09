import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AccessContractWriteAccessTabComponent } from './access-contract-write-access-tab.component';
import { AccessContractService } from '../../access-contract.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';

// TODO fix test
xdescribe('AccessContractWriteAccessTabComponent', () => {
  let component: AccessContractWriteAccessTabComponent;
  let fixture: ComponentFixture<AccessContractWriteAccessTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports:[
        ReactiveFormsModule,
        VitamUICommonTestModule
      ],
      declarations: [ AccessContractWriteAccessTabComponent ],
      providers: [
        FormBuilder,
        {provide: AccessContractService, useValue: {}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractWriteAccessTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

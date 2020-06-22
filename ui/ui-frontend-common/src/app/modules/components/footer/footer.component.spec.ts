import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material';
import { WINDOW_LOCATION } from './../../injection-tokens';
import { FooterComponent } from './footer.component';
import { MaterialModule } from './material.module';


describe('FooterComponent', () => {
  let component: FooterComponent;
  let fixture: ComponentFixture<FooterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FooterComponent ],
      imports: [MaterialModule, MatFormFieldModule, ReactiveFormsModule],
      providers: [
        { provide: WINDOW_LOCATION, useValue: window.location }
      ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FooterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

});

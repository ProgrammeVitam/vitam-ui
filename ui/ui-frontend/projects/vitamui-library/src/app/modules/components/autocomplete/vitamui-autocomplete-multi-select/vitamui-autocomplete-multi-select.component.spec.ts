import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TranslateModule } from '@ngx-translate/core';
import { VitamUIAutocompleteMultiSelectComponent } from './vitamui-autocomplete-multi-select.component';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('VitamuiAutocompleteMultiSelectComponent', () => {
  let component: VitamUIAutocompleteMultiSelectComponent;
  let fixture: ComponentFixture<VitamUIAutocompleteMultiSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VitamUIAutocompleteMultiSelectComponent],
      imports: [NoopAnimationsModule, TranslateModule.forRoot(), MatSelectModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUIAutocompleteMultiSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

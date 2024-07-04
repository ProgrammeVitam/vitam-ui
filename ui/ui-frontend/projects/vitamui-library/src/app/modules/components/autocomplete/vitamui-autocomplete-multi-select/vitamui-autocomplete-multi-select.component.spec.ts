import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TranslateModule } from '@ngx-translate/core';
import { VitamUIAutocompleteMultiSelectComponent } from './vitamui-autocomplete-multi-select.component';

describe('VitamuiAutocompleteMultiSelectComponent', () => {
  let component: VitamUIAutocompleteMultiSelectComponent;
  let fixture: ComponentFixture<VitamUIAutocompleteMultiSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), VitamUIAutocompleteMultiSelectComponent],
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

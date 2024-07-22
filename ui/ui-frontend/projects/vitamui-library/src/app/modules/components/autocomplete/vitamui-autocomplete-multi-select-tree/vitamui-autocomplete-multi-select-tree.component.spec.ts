import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TranslateModule } from '@ngx-translate/core';
import { VitamUiAutocompleteMultiSelectTreeComponent } from './vitamui-autocomplete-multi-select-tree.component';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

class Item {
  value: string;
}

describe('VitamUiAutocompleteMultiSelectTreeComponent', () => {
  let component: VitamUiAutocompleteMultiSelectTreeComponent<Item>;
  let fixture: ComponentFixture<VitamUiAutocompleteMultiSelectTreeComponent<Item>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VitamUiAutocompleteMultiSelectTreeComponent],
      imports: [NoopAnimationsModule, TranslateModule.forRoot(), MatSelectModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VitamUiAutocompleteMultiSelectTreeComponent<Item>);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

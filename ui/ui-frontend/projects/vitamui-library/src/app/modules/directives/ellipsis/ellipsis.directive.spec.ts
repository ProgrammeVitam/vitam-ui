import { Component, ViewChildren } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

import { EllipsisDirective } from './ellipsis.directive';

@Component({
  template: ` <div vitamuiCommonEllipsis>
    Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc efficitur, eros a blandit rhoncus, neque nunc suscipit metus, ut pretium
    elit nunc a purus. In a lacus nulla. Maecenas sed malesuada nibh.
  </div>`,
})
class TestHostComponent {
  @ViewChildren(EllipsisDirective) ellipsisDirective: EllipsisDirective;
}

let fixture: ComponentFixture<TestHostComponent>;
let testhost: TestHostComponent;

describe('EllipsisDirective', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestHostComponent, EllipsisDirective],
      providers: [],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    expect(testhost).toBeTruthy();
  });

  it('should have a text with ellipsis at the end', () => {
    const directiveEl = fixture.debugElement.query(By.directive(EllipsisDirective));
    expect(directiveEl).not.toBeNull();

    expect(directiveEl.nativeElement.className).toContain('text-ellipsis');
  });
});

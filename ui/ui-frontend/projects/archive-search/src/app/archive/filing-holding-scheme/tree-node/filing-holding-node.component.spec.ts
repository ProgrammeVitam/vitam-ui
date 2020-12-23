import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilingHoldingNodeComponent } from './filing-holding-node.component';
import { NO_ERRORS_SCHEMA, PipeTransform, Pipe } from '@angular/core';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('FilingHoldingNodeComponent', () => {
  let component: FilingHoldingNodeComponent;
  let fixture: ComponentFixture<FilingHoldingNodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
      ],
      declarations: [FilingHoldingNodeComponent, MockTruncatePipe],
      providers: [],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilingHoldingNodeComponent);
    component = fixture.componentInstance;
    component.node = {
      id: 'id',
      title: 'label',
      type: 'RecordGroup',
      children: [],
      vitamId: 'vitamId',
      parents: [],
      checked: true,
      hidden: true
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});










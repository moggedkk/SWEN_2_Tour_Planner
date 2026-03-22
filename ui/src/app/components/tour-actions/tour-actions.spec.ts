import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TourActions } from './tour-actions';

describe('TourActions', () => {
  let component: TourActions;
  let fixture: ComponentFixture<TourActions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TourActions]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TourActions);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

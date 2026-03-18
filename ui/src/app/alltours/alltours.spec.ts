import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Alltours } from './alltours';

describe('Alltours', () => {
  let component: Alltours;
  let fixture: ComponentFixture<Alltours>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Alltours]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Alltours);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

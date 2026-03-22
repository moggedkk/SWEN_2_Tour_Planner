import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Createtours } from './createtours';

describe('Createtours', () => {
  let component: Createtours;
  let fixture: ComponentFixture<Createtours>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Createtours]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Createtours);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

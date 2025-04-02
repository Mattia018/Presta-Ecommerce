import { TestBed } from '@angular/core/testing';

import { CartEventService } from './cart-event.service';

describe('CartEventService', () => {
  let service: CartEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

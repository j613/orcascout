import { TestBed, inject } from '@angular/core/testing';

import { BackendUpdateService } from './backend-update.service';

describe('BackendUpdateService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BackendUpdateService]
    });
  });

  it('should be created', inject([BackendUpdateService], (service: BackendUpdateService) => {
    expect(service).toBeTruthy();
  }));
});

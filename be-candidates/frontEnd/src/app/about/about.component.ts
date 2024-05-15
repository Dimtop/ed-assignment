import { Component, OnInit } from '@angular/core';
import { tap } from 'rxjs/operators';
import { AboutService } from '../services/about.service';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css'],
})
export class AboutComponent implements OnInit {
  backVersion: string;
  frontVersion: string;

  constructor(private aboutService: AboutService) {}

  ngOnInit(): void {
    this.populateMask();
  }

  private populateMask() {
    this.aboutService
      .getVersion('backend')
      .pipe(
        tap((response: any) => {
          this.backVersion = response.body.backend;
        })
      )
      .subscribe();
    this.aboutService
      .getVersion('frontend')
      .pipe(
        tap((response: any) => {
          this.frontVersion = response.bdy.frontend;
        })
      )
      .subscribe();
  }
}

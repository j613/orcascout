import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './components/app.component';

import { SidebarModule} from 'ng-sidebar';
import { AngularFontAwesomeModule } from 'angular-font-awesome';
import { HomeComponent } from './components/home/home.component';
import { MatchScoutingComponent } from './components/match-scouting/match-scouting.component';

const appRoutes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'match-scouting', component: MatchScoutingComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MatchScoutingComponent
  ],
  imports: [
    BrowserModule,
    SidebarModule.forRoot(),
    AngularFontAwesomeModule,
    RouterModule.forRoot(
      appRoutes
    )
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

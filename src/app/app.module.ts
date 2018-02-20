import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule }   from '@angular/forms';

import { SidebarModule} from 'ng-sidebar';
import { AngularFontAwesomeModule } from 'angular-font-awesome';

import { AppComponent } from './components/app.component';
import { HomeComponent } from './components/home/home.component';
import { MatchScoutingComponent } from './components/match-scouting/match-scouting.component';
import { EditMatchScoutingComponent } from './components/edit-match-scouting/edit-match-scouting.component';
import { HeaderComponent } from './templates/header/header.component';
import { PitScoutingComponent } from './components/pit-scouting/pit-scouting.component';

const appRoutes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'match-scouting', component: MatchScoutingComponent },
  { path: 'edit-match-scouting', component: EditMatchScoutingComponent },
  { path: 'pit-scouting', component: PitScoutingComponent },
  { path: '**', redirectTo: '' }
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MatchScoutingComponent,
    EditMatchScoutingComponent,
    HeaderComponent,
    PitScoutingComponent
  ],
  imports: [
    BrowserModule,
    SidebarModule.forRoot(),
    AngularFontAwesomeModule,
    RouterModule.forRoot(
      appRoutes
    ),
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }

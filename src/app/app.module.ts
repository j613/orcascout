import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

// Modules
import { SidebarModule } from 'ng-sidebar';
import { AngularFontAwesomeModule } from 'angular-font-awesome';
import { AppRoutingModule } from './pages/app-routing.module';
// Services
import { BackendUpdateService } from './services/backend-update.service';
// Guards
import { AuthGuard } from './guards/auth-guard.guard';
// Components
import { AppComponent } from './pages/app.component';
import { HomeComponent } from './pages/home/home.component';
import { MatchScoutingComponent } from './pages/match-scouting/match-scouting.component';
import { EditMatchScoutingComponent } from './pages/edit-match-scouting/edit-match-scouting.component';
import { HeaderComponent } from './templates/header/header.component';
import { PitScoutingComponent } from './pages/pit-scouting/pit-scouting.component';
import { AuthService } from './services/auth.service';

@NgModule({
  imports: [
    BrowserModule,
    SidebarModule.forRoot(),
    AngularFontAwesomeModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule
  ],
  declarations: [
    AppComponent,
    HomeComponent,
    MatchScoutingComponent,
    EditMatchScoutingComponent,
    HeaderComponent,
    PitScoutingComponent
  ],
  providers: [
    BackendUpdateService,
    AuthService,
    AuthGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

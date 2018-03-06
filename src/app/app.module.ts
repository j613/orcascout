import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
// Modules
import { SidebarModule } from 'ng-sidebar';
import { AngularFontAwesomeModule } from 'angular-font-awesome';
import { AdminRoutingModule } from './pages/admin/admin-routing.module';
import { AppRoutingModule } from './pages/app-routing.module';
// Services
import { BackendUpdateService } from './services/backend-update.service';
import { AuthService } from './services/auth.service';
// Guards
import { AuthGuard } from './guards/auth-guard.guard';
// Components
import { AppComponent } from './pages/app.component';
import { HomeComponent } from './pages/home/home.component';
import { MatchScoutingComponent } from './pages/match-scouting/match-scouting.component';
import { EditMatchScoutingComponent } from './pages/edit-match-scouting/edit-match-scouting.component';
import { HeaderComponent } from './templates/header/header.component';
import { PitScoutingComponent } from './pages/pit-scouting/pit-scouting.component';
import { LoginComponent } from './pages/login/login.component';
import { UtilsService } from './services/utils.service';
import { MatchScheduleComponent } from './pages/match-schedule/match-schedule.component';
import { RegisterComponent } from './pages/register/register.component';
import { AdminComponent } from './pages/admin/admin.component';
import { AdminUsersComponent } from './pages/admin/admin-users/admin-users.component';

@NgModule({
  imports: [
    BrowserModule,
    SidebarModule.forRoot(),
    AngularFontAwesomeModule,
    FormsModule,
    AdminRoutingModule,
    AppRoutingModule,
    HttpClientModule
  ],
  declarations: [
    AppComponent,
    HomeComponent,
    MatchScoutingComponent,
    EditMatchScoutingComponent,
    HeaderComponent,
    PitScoutingComponent,
    LoginComponent,
    MatchScheduleComponent,
    RegisterComponent,
    AdminComponent,
    AdminUsersComponent
  ],
  providers: [
    BackendUpdateService,
    UtilsService,
    AuthService,
    AuthGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

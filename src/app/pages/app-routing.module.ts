import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from './../guards/auth-guard.guard';

import { HomeComponent } from './home/home.component';
import { MatchScoutingComponent } from './match-scouting/match-scouting.component';
import { EditMatchScoutingComponent } from './edit-match-scouting/edit-match-scouting.component';
import { PitScoutingComponent } from './pit-scouting/pit-scouting.component';
import { LoginComponent } from './login/login.component';
import { MatchScheduleComponent } from './match-schedule/match-schedule.component';

const appRoutes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'login', component: LoginComponent},
    {
        path: '',
        canLoad: [AuthGuard],
        canActivate: [AuthGuard],
        children: [
            { path: 'match-scouting', component: MatchScoutingComponent },
            { path: 'edit-match-scouting', component: EditMatchScoutingComponent },
            { path: 'pit-scouting', component: PitScoutingComponent },
            { path: 'match-schedule', component: MatchScheduleComponent }
        ]
    },
    { path: '**', redirectTo: '' },
  ];

@NgModule({
    imports: [
        RouterModule.forRoot(
            appRoutes
        )
    ],
    exports: [
        RouterModule
    ],
    providers: []
})
export class AppRoutingModule { }

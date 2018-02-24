import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AuthGuard } from './../guards/auth-guard.guard';

import { HomeComponent } from './home/home.component';
import { MatchScoutingComponent } from './match-scouting/match-scouting.component';
import { EditMatchScoutingComponent } from './edit-match-scouting/edit-match-scouting.component';
import { PitScoutingComponent } from './pit-scouting/pit-scouting.component';

const appRoutes: Routes = [
    {
        path: '',
        canLoad: [AuthGuard],
        children: [
            { path: '', component: HomeComponent, canActivate: [AuthGuard] },
            { path: 'match-scouting', component: MatchScoutingComponent },
            { path: 'edit-match-scouting', component: EditMatchScoutingComponent },
            { path: 'pit-scouting', component: PitScoutingComponent },
            { path: '**', redirectTo: '' }
        ]
    }
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

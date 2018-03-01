import { Team } from './team';

export interface Match {
    match_id: string;
    red_teams: Team[];
    blue_teams: Team[];
    red_score: number;
    blue_score: number;
}

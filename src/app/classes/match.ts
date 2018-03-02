import { Team } from './team';

export interface Match {
    key: string;
    comp_level: string;
    match_number: number;
    alliances: {
        red: {
            team_keys: number[],
            score: number
        }
        blue: {
            team_keys: number[],
            score: number
        }
    };
    time: number;
}


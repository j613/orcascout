import { Match } from './match';
import { Team } from './team';

export interface Regional {
    key: string;
    name: string;
    data?: RegionalData;
    scout_data?: Object;
}

export interface RegionalData {
    teams: Team[];
    matches: Match[];
}

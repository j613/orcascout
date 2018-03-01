import { Match } from './match';
import { Team } from './team';

export interface Regional {
    id: string;
    name: string;
    data?: RegionalData;
}

export interface RegionalData {
    teams: Team[];
    matches: Match[];
}

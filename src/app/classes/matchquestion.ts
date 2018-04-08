export interface MatchQuestion {
    // key: string;
    // label: string;
    // type: string;
    name: string;
    display_name: string;
    type: string;
    default_value: string|boolean|number;
    values?: string[];
    min_value?: number;
    max_value?: number;
}

export interface GameTemplate {
    year: number;
    game_name: string;
    autonomous: {
        fields: {
            name: string;
            display_name: string;
            type: string; // boolean, int, text_box, enum
            default_value: string|boolean|number;
            values?: string[];
            min_value?: number;
            max_value?: number;
        }[];
        duration: number;
    };
    teleop: {
        fields: {
            name: string;
            display_name: string;
            type: string;
            default_value: string|boolean|number;
            values?: string[];
            min_value?: number;
            max_value?: number;
        }[];
        duration: number;
    };
}

import { Logo } from './logo.interface';

export interface CustomerTheme {
    colors: {[colorId: string]: string};
    logos: Logo[];
    portalMessage: string;
    portalTitle: string;
}

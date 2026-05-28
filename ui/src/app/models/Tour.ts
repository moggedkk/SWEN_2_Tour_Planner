export enum TransportType {
  Foot = 'foot-walking',
  Bike = 'cycling-regular',
  Car = 'driving-car',
  Hike = 'foot-hiking'
}

export interface TourLog {
  date: string;
  comment: string;
  duration: number; // in minutes
  difficultyRating: string;
  image: string;
}

export interface Tour {
  id?: number;
  name: string;
  start: string;
  end: string;
  difficulty: string;
  description: string;
  transportType: TransportType;
  logs: TourLog[];
  distance: number;
  estimatedTime: number;
  routeInfo?: any;
}

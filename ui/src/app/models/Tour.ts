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

export class Tour {
  public id: number;
  public name: string;
  public start: string;
  public end: string;
  public difficulty: string;
  public description: string;
  public transportType: TransportType;
  public logs: TourLog[] = [];

  // New params for API integration
  public distance: number;      // To be retrieved from OpenRouteservice (in meters/km)
  public estimatedTime: number; // To be retrieved from OpenRouteservice (in seconds/minutes)
  public routeInfo?: any;      // To store the GeoJSON/Polyline for Leaflet display

  constructor(
    id: number,
    name: string,
    start: string,
    end: string,
    difficulty: string,
    description: string,
    transportType: TransportType,
    distance: number = 0,
    estimatedTime: number = 0
  ) {
    this.id = id;
    this.name = name;
    this.start = start;
    this.end = end;
    this.difficulty = difficulty;
    this.description = description;
    this.transportType = transportType;
    this.distance = distance;
    this.estimatedTime = estimatedTime;
  }

}

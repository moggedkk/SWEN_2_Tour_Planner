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
  imageEncoded: string;
  imageName: string;
  // how far the user actually went on this completion.
  // pre-filled with the planned tour distance, user can override
  // (e.g. if they took a detour or only did part of the tour)
  totalDistance: number;
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
  // computed by the backend from the tour's logs:
  //   popularity        = how many logs exist (Low/Medium/High)
  //   childFriendliness = easy + short + low difficulty across logs (Low/Medium/High)
  popularity?: string;
  childFriendliness?: string;
}

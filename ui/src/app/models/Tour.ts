export enum TransportType {
  Foot = 'foot-walking',
  Bike = 'cycling-regular',
  Car = 'driving-car',
  Hike = 'foot-hiking'
}

export class Tour {
  public name: string;
  public start: string;
  public end: string;
  public difficulty: string;
  public description: string;
  public transportType: TransportType;

  // New params for API integration
  public distance: number;      // To be retrieved from OpenRouteservice (in meters/km)
  public estimatedTime: number; // To be retrieved from OpenRouteservice (in seconds/minutes)
  public routeInfo?: any;      // To store the GeoJSON/Polyline for Leaflet display

  constructor(
    name: string,
    start: string,
    end: string,
    difficulty: string,
    description: string,
    transportType: TransportType,
    distance: number = 0,
    estimatedTime: number = 0
  ) {
    this.name = name;
    this.start = start;
    this.end = end;
    this.difficulty = difficulty;
    this.description = description;
    this.transportType = transportType;
    this.distance = distance;
    this.estimatedTime = estimatedTime;
  }

  static GetTours(): Tour[] {
    return [
      new Tour('Vienna City Tour', 'Vienna', 'Vienna', 'Easy', 'A scenic walk through the historic first district.', TransportType.Foot),
      new Tour('Graz Trip', 'Vienna', 'Graz', 'Moderate', 'A journey to the Styrian capital featuring clock towers.', TransportType.Car),
      new Tour('Salzburg Highlights', 'Vienna', 'Salzburg', 'Easy', 'Explore the birthplace of Mozart.', TransportType.Car),
      new Tour('Innsbruck Adventure', 'Salzburg', 'Innsbruck', 'Challenging', 'A trek into the heart of the Tyrolean Alps.', TransportType.Hike),
      new Tour('Danube Valley Ride', 'Linz', 'Krems', 'Moderate', 'Cycling through the UNESCO World Heritage Wachau Valley.', TransportType.Bike),
      new Tour('Alps Panorama Tour', 'Innsbruck', 'Zell am See', 'Challenging', 'Breathtaking high-altitude views.', TransportType.Bike),
      new Tour('Hallstatt Escape', 'Salzburg', 'Hallstatt', 'Moderate', 'Visit the world-famous lakeside village.', TransportType.Car),
      new Tour('Tyrol Explorer', 'Innsbruck', 'Kitzbühel', 'Challenging', 'Rugged paths through famous regions.', TransportType.Hike),
      new Tour('Carinthia Lakes Tour', 'Klagenfurt', 'Villach', 'Easy', 'A relaxing tour around Wörthersee.', TransportType.Bike),
      new Tour('Vorarlberg Route', 'Bregenz', 'Dornbirn', 'Moderate', 'Discover modern architecture and nature.', TransportType.Foot),
      new Tour('Vienna to Prague', 'Vienna', 'Prague', 'Moderate', 'A cross-border cultural expedition.', TransportType.Car),
      new Tour('Vienna to Budapest', 'Vienna', 'Budapest', 'Easy', 'A smooth trip along the Danube.', TransportType.Car),
      new Tour('Munich Weekend Trip', 'Salzburg', 'Munich', 'Moderate', 'A short hop over the border.', TransportType.Car),
      new Tour('Bratislava Express', 'Vienna', 'Bratislava', 'Easy', 'Perfect for a quick day trip.', TransportType.Foot),
      new Tour('Swiss Alps Journey', 'Innsbruck', 'Zurich', 'Challenging', 'An epic mountain crossing.', TransportType.Car),
      new Tour('Northern Italy Escape', 'Innsbruck', 'Bolzano', 'Challenging', 'Cross the Brenner Pass into South Tyrol.', TransportType.Car),
      new Tour('Danube Cycle Path', 'Passau', 'Vienna', 'Moderate', 'The classic long-distance cycling route.', TransportType.Bike)
    ];
  }
}

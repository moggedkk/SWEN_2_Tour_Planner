export class Tour {
  public name: string;
  public start: string;
  public end: string;
  public difficulty: string; 

  constructor(name: string, start: string, end: string, difficulty: string) {
    this.name = name;
    this.start = start;
    this.end = end;
    this.difficulty = difficulty;
  }

  static GetTours(): Tour[] {
    return [
      new Tour('Vienna City Tour', 'Vienna', 'Vienna', 'Easy'),
      new Tour('Graz Trip', 'Vienna', 'Graz', 'Moderate'),
      new Tour('Salzburg Highlights', 'Vienna', 'Salzburg', 'Easy'),
      new Tour('Innsbruck Adventure', 'Salzburg', 'Innsbruck', 'Challenging'),
      new Tour('Danube Valley Ride', 'Linz', 'Krems', 'Moderate'),
      new Tour('Alps Panorama Tour', 'Innsbruck', 'Zell am See', 'Challenging'),
      new Tour('Hallstatt Escape', 'Salzburg', 'Hallstatt', 'Moderate'),
      new Tour('Tyrol Explorer', 'Innsbruck', 'Kitzbühel', 'Challenging'),
      new Tour('Carinthia Lakes Tour', 'Klagenfurt', 'Villach', 'Easy'),
      new Tour('Vorarlberg Route', 'Bregenz', 'Dornbirn', 'Moderate'),
      new Tour('Vienna to Prague', 'Vienna', 'Prague', 'Moderate'),
      new Tour('Vienna to Budapest', 'Vienna', 'Budapest', 'Easy'),
      new Tour('Munich Weekend Trip', 'Salzburg', 'Munich', 'Moderate'),
      new Tour('Bratislava Express', 'Vienna', 'Bratislava', 'Easy'),
      new Tour('Swiss Alps Journey', 'Innsbruck', 'Zurich', 'Challenging'),
      new Tour('Northern Italy Escape', 'Innsbruck', 'Bolzano', 'Challenging'),
      new Tour('Danube Cycle Path', 'Passau', 'Vienna', 'Moderate')
    ];
  }
}
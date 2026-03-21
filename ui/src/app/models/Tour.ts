export class Tour {
  public name: string;
  public start: string;
  public end: string;
  constructor(name: string, start: string, end: string) {
    this.name = name;
    this.start = start;
    this.end = end;
  }

static GetTours(): Tour[] {
  return [
    new Tour('Vienna City Tour', 'Vienna', 'Vienna'),
    new Tour('Graz Trip', 'Vienna', 'Graz'),
    new Tour('Salzburg Highlights', 'Vienna', 'Salzburg'),
    new Tour('Innsbruck Adventure', 'Salzburg', 'Innsbruck'),
    new Tour('Danube Valley Ride', 'Linz', 'Krems'),
    new Tour('Alps Panorama Tour', 'Innsbruck', 'Zell am See'),
    new Tour('Hallstatt Escape', 'Salzburg', 'Hallstatt'),
    new Tour('Tyrol Explorer', 'Innsbruck', 'Kitzbühel'),
    new Tour('Carinthia Lakes Tour', 'Klagenfurt', 'Villach'),
    new Tour('Vorarlberg Route', 'Bregenz', 'Dornbirn'),
    new Tour('Vienna to Prague', 'Vienna', 'Prague'),
    new Tour('Vienna to Budapest', 'Vienna', 'Budapest'),
    new Tour('Munich Weekend Trip', 'Salzburg', 'Munich'),
    new Tour('Bratislava Express', 'Vienna', 'Bratislava'),
    new Tour('Swiss Alps Journey', 'Innsbruck', 'Zurich'),
    new Tour('Northern Italy Escape', 'Innsbruck', 'Bolzano'),
    new Tour('Danube Cycle Path', 'Passau', 'Vienna')
  ];
}
}

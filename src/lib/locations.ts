export type LocationOption = {
  city: string;
  country: string;
};

const LOCATIONS: LocationOption[] = [
  { city: "Madrid", country: "Spain" },
  { city: "Barcelona", country: "Spain" },
  { city: "Valencia", country: "Spain" },
  { city: "Seville", country: "Spain" },
  { city: "Bilbao", country: "Spain" },
  { city: "Malaga", country: "Spain" },
  { city: "Zaragoza", country: "Spain" },
  { city: "Murcia", country: "Spain" },
  { city: "Palma", country: "Spain" },
  { city: "Granada", country: "Spain" },
  { city: "Paris", country: "France" },
  { city: "Lyon", country: "France" },
  { city: "Marseille", country: "France" },
  { city: "Berlin", country: "Germany" },
  { city: "Munich", country: "Germany" },
  { city: "Hamburg", country: "Germany" },
  { city: "Rome", country: "Italy" },
  { city: "Milan", country: "Italy" },
  { city: "Florence", country: "Italy" },
  { city: "Lisbon", country: "Portugal" },
  { city: "Porto", country: "Portugal" },
  { city: "Amsterdam", country: "Netherlands" },
  { city: "Rotterdam", country: "Netherlands" },
  { city: "Brussels", country: "Belgium" },
  { city: "Copenhagen", country: "Denmark" },
  { city: "Stockholm", country: "Sweden" },
  { city: "Oslo", country: "Norway" },
  { city: "Dublin", country: "Ireland" },
  { city: "London", country: "United Kingdom" },
  { city: "Manchester", country: "United Kingdom" },
  { city: "Edinburgh", country: "United Kingdom" },
  { city: "Vienna", country: "Austria" },
  { city: "Zurich", country: "Switzerland" },
  { city: "Geneva", country: "Switzerland" },
  { city: "Prague", country: "Czech Republic" },
  { city: "Warsaw", country: "Poland" },
  { city: "Athens", country: "Greece" },
  { city: "New York", country: "United States" },
  { city: "Los Angeles", country: "United States" },
  { city: "San Francisco", country: "United States" },
  { city: "Chicago", country: "United States" },
  { city: "Seattle", country: "United States" },
  { city: "Austin", country: "United States" },
  { city: "Miami", country: "United States" },
  { city: "Boston", country: "United States" },
  { city: "Toronto", country: "Canada" },
  { city: "Vancouver", country: "Canada" },
  { city: "Montreal", country: "Canada" },
  { city: "Mexico City", country: "Mexico" },
  { city: "Guadalajara", country: "Mexico" },
  { city: "Buenos Aires", country: "Argentina" },
  { city: "Santiago", country: "Chile" },
  { city: "Bogota", country: "Colombia" },
  { city: "Medellin", country: "Colombia" },
  { city: "Lima", country: "Peru" },
  { city: "Sao Paulo", country: "Brazil" },
  { city: "Rio de Janeiro", country: "Brazil" },
  { city: "Tokyo", country: "Japan" },
  { city: "Kyoto", country: "Japan" },
  { city: "Seoul", country: "South Korea" },
  { city: "Singapore", country: "Singapore" },
  { city: "Bangkok", country: "Thailand" },
  { city: "Sydney", country: "Australia" },
  { city: "Melbourne", country: "Australia" },
  { city: "Auckland", country: "New Zealand" },
];

function normalize(value: string) {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function scoreLocation(option: LocationOption, query: string) {
  const city = normalize(option.city);
  const country = normalize(option.country);
  const label = `${city}, ${country}`;

  if (city === query) return 0;
  if (city.startsWith(query)) return 1;
  if (country.startsWith(query)) return 2;
  if (label.startsWith(query)) return 3;
  if (city.includes(query)) return 4;
  if (country.includes(query)) return 5;
  if (label.includes(query)) return 6;

  return null;
}

export function formatLocation(option: LocationOption) {
  return `${option.city}, ${option.country}`;
}

export function parseLocationInput(value: string) {
  const [city = "", ...countryParts] = value.split(",");

  return {
    city: city.trim(),
    country: countryParts.join(",").trim(),
  };
}

export function searchLocations(query: string, limit = 8) {
  const normalizedQuery = normalize(query);
  if (!normalizedQuery) return [];

  return LOCATIONS.map((option, index) => ({
    option,
    index,
    score: scoreLocation(option, normalizedQuery),
  }))
    .filter((item): item is { option: LocationOption; index: number; score: number } =>
      item.score !== null,
    )
    .sort((a, b) => a.score - b.score || a.index - b.index)
    .slice(0, limit)
    .map((item) => item.option);
}

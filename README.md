# Earthquake Web Service Project

## Overview
Developed during a 12-hour university hackathon, this project is a web service that provides information about earthquakes from an SQLite database. It is built using Java with the Spark Java framework, SQLite JDBC driver, and JSON/XML parsers.

## Features
**Earthquake Count Endpoint (/quakecount):** Returns the number of earthquakes above a specified magnitude.
**Earthquakes by Year and Magnitude (/quakesbyyear):** Provides earthquake details for a specific year and minimum magnitude.
**Earthquakes by Location (/quakesbylocation):** Retrieves the nearest earthquakes to a given location.

## Getting Started
1. Clone the repository.
2. Ensure Java, SQLite JDBC driver, and Spark Java framework are installed.
3. Run EarthquakeWebService to start the service.

## Usage
Access the service endpoints through a web browser or an API client:
- http://localhost:8088/test
- http://localhost:8088/quakecount?magnitude=[magnitude]
- http://localhost:8088/quakesbyyear?year=[year]&magnitude=[magnitude]
- http://localhost:8088/quakesbylocation?latitude=[lat]&longitude=[lon]&magnitude=[magnitude]

## Contributing
Contributions to improve the project are welcome. Please follow the usual GitHub fork and pull request workflow.

## License
This project is licensed under the [MIT License](LICENSE). See the [LICENSE](LICENSE) file for details.

## Authors

- [Patryk](https://github.com/pat-maj)

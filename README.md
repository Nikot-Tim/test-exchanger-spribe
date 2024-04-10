# Currency Exchange API

This Spring Boot application provides a REST-full API for currency exchange operations.
It allows users to perform various actions related to currencies, such as getting a list of currencies, retrieving
exchange rates for a currency, and adding new currencies for getting exchange rates.
This application synthesized with a third-party service such as https://exchangeratesapi.io/

## Features

- Add new currencies for getting exchange rates.
- Get exchange rates for a specific currency.
- Retrieve a list of currencies used in the project.
- Scheduled updating of exchange rates from external sources.
- Store exchange rates in memory and log them in the database.
- API documentation using OpenAPI Specification (Swagger).

## Technologies Used

- Java
- Spring Boot
- Gradle
- PostgreSQL
- Liquibase
- Spring Data JPA
- Caffeine Cache(Will be used in next versions instead of Map "in memory DB")
- Swagger (OpenAPI Specification)

## Getting Started

To run this application locally, follow these steps:

1. Clone this repository to your local machine.
2. Make sure you have Java and PostgreSQL installed on your machine.
3. Configure your PostgreSQL database settings in `application.properties` file.
4. Build the project using Gradle.
5. Run the application.
6. Access the API documentation in your web browser at `http://localhost:8080/swagger-ui/index.html#/`.

## API Endpoints

- GET `/currency`: Retrieve a paginated list of all currencies used in project.
- GET `/currency/{currencyCode}/rates`: Retrieve exchange rates for a specific currency.
- POST `/currency/add`: Add a new currency for getting exchange rates.

## Contributing

I used SPEX-CommitNumber (Spribe exchanger) for commits naming

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

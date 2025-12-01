# Book Social Network Backend

A Spring Boot-based backend application for a book social network, allowing users to manage books, borrow/return them, leave feedback, and more.

## Features

- User authentication and authorization with JWT
- Book management (CRUD operations, file uploads for covers)
- Book borrowing and returning system
- Feedback and rating system for books
- Email notifications for account activation
- RESTful API with OpenAPI documentation

## Tech Stack

- **Java**: 21
- **Spring Boot**: 4.0.0
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Security**: Spring Security with JWT
- **Email**: Spring Mail
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven
- **Other**: Lombok, JJWT, Thymeleaf (for email templates)

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose (for database and email server)
- PostgreSQL (or use Docker)

## Installation and Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd book-social-network
   ```

2. **Start the database and email server**:
   ```bash
   docker-compose up -d
   ```
   This starts PostgreSQL on port 5432 and MailDev on ports 1080 (web UI) and 1025 (SMTP).

3. **Configure the application**:
   - Update `booknetwork/src/main/resources/application-dev.yml` if needed (database credentials, etc.).
   - Ensure the JWT secret key is set.

4. **Build and run the application**:
   ```bash
   cd booknetwork
   mvn clean install
   mvn spring-boot:run
   ```
   The application will start on `http://localhost:8081/api/v1/`.

## API Documentation

Once the application is running, access the OpenAPI documentation at:
- Swagger UI: `http://localhost:8081/api/v1/swagger-ui.html`
- API Docs: `http://localhost:8081/api/v1/v3/api-docs`

## Key Endpoints

- **Authentication**: `/auth/register`, `/auth/authenticate`, `/auth/activate-account`
- **Books**: `/books` (CRUD, upload cover, borrow/return)
- **Feedback**: `/feedback` (add, get for books)

## Database

- The application uses PostgreSQL.
- DDL auto-update is enabled (`ddl-auto: update`).
- Database name: `book_social_network`
- Default credentials: username/password

## Email

- Uses MailDev for development (web UI at `http://localhost:1080`).
- Activation emails are sent to the configured frontend URL.

## File Uploads

- Book cover images are stored in `./uploads` directory.
- Max file size: 50MB.

## Testing

- Run tests with `mvn test`.
- Integration tests use the test database.

## Contributing

1. Fork the repository.
2. Create a feature branch.
3. Commit changes.
4. Push and create a pull request.

## License

This project is licensed under [License Name] - see the LICENSE file for details.

# Dokkai Dorimu Backend API

Spring Boot backend for the Dokkai Dorimu web application.

## Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- MySQL
- JWT Authentication
- Maven

## Prerequisites

- JDK 17 or later
- Maven 3.6+
- MySQL 8.0+
- IntelliJ IDEA (recommended)

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/your-username/dokkai-dorimu-backend.git
cd dokkai-dorimu-backend
```

2. Create the database:
```sql
CREATE DATABASE dokkaidorimu;
```

3. Configure application.properties:
- Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`
- Update the database credentials and other configurations

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

### Authentication Endpoints
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - User login

### User Endpoints
- GET `/api/users` - Get all users
- GET `/api/users/{id}` - Get user by ID
- PUT `/api/users/{id}` - Update user
- DELETE `/api/users/{id}` - Delete user

### Article Endpoints
- GET `/api/articles` - Get all articles
- POST `/api/articles` - Create new article
- GET `/api/articles/{id}` - Get article by ID
- PUT `/api/articles/{id}` - Update article
- DELETE `/api/articles/{id}` - Delete article

(Add other endpoints as needed)

## Development

### Branch Naming Convention
- Feature: `feature/feature-name`
- Bugfix: `bugfix/bug-name`
- Hotfix: `hotfix/issue-name`
- Release: `release/version-number`

### Commit Message Convention
```
feat: add user authentication
fix: resolve CORS issue
docs: update API documentation
test: add unit tests for user service
refactor: improve error handling
```

## Testing

Run tests using:
```bash
mvn test
```

## Database Migration

If using Flyway or Liquibase, document migration commands here.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add some feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE.md file for details
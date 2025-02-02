# GreenTrade API

## Project Overview

GreenTrade is an innovative web API designed to revolutionize the sustainable product marketplace. The platform provides a comprehensive solution for verifying, trading, and communicating sustainable products, focusing on transparency, trust, and measurable sustainability.

## Key Features

- Product Verification
- Sustainability Certificate Management
- Secure Transaction Processing
- User Communication System
- Role-Based Access Control

## Technical Specifications

- **Backend Framework**: Spring Boot 3.x
- **Programming Language**: Java 21
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: OpenAPI/Swagger

## Prerequisites

### System Requirements
- Java Development Kit (JDK) 21
- Maven 3.8+
- PostgreSQL 12 or higher
- Git

### Development Environment
- IntelliJ IDEA, Eclipse, or VS Code with Java support
- Postman (recommended for API testing)

## Installation Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/[username]/greentrade.git
cd greentrade
```

### 2. Database Setup
```sql
CREATE DATABASE greentrade;
CREATE USER greentrade WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE greentrade TO greentrade;
```

### 3. Configuration
Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/greentrade
spring.datasource.username=greentrade
spring.datasource.password=your_password
```

### 4. Install Dependencies
```bash
mvn clean install
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

## Test Users

| Email | Password | Role | Description |
|-------|----------|------|-------------|
| admin@greentrade.nl | password123 | ADMIN | Full system access |
| verkoper@greentrade.nl | password123 | SELLER | Product and certificate management |
| koper@greentrade.nl | password123 | BUYER | View and purchase products |

## API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

## Postman Collection

A Postman collection is included in the `GreenTrade-API.postman_collection.json` file. Import this into Postman to test the API endpoints.

## Security

- JWT-based authentication
- Role-based access control
- Endpoint protection based on user roles
- HTTPS communication

## Troubleshooting

- Check database connection settings
- Verify JWT configuration
- Run `mvn clean install` for dependency issues
- Detailed logs available in `application.log`

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Project Link: [https://github.com/[username]/greentrade](https://github.com/[username]/greentrade)

---

**Version**: 1.0
**Last Updated**: [Current Date]

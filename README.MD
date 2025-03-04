# GreenTrade API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12%2B-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

GreenTrade is een duurzaam handelsplatform API dat gebruikers in staat stelt duurzame producten te verifiëren, verhandelen en beheren. Het platform verbindt verkopers van duurzame producten met kopers die bewuste aankoopbeslissingen willen maken, ondersteund door een transparant verificatiesysteem.

## 🌟 Kernfunctionaliteiten

- **Productverificatie**: Producten worden beoordeeld op duurzaamheid door geaccrediteerde beoordelaars
- **Duurzaamheidscertificaten**: Gebruikers kunnen certificaten uploaden en beheren
- **Veilige transacties**: Beveiligde handelsomgeving voor duurzame producten
- **Gebruikerscommunicatie**: Geïntegreerd berichtensysteem tussen gebruikers
- **Role-based access control**: Verschillende toegangsniveaus voor kopers, verkopers en beheerders

## 📋 Technische specificaties

- **Backend Framework**: Spring Boot 3.3.4
- **Programmeertaal**: Java 21
- **Database**: PostgreSQL 12+
- **Authenticatie**: JWT (JSON Web Tokens)
- **API-documentatie**: OpenAPI/Swagger
- **Bestandsopslag**: Lokale bestandssysteem met modulaire interface

## 🛠️ Installatie

### Systeemvereisten
- Java Development Kit (JDK) 21
- Maven 3.8+
- PostgreSQL 12+
- Git

### Stappen
1. Clone de repository:
   ```bash
   git clone https://github.com/damienverkerk/greentrade.git
   cd greentrade
   ```

2. Configureer de database in `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/greentrade
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Bouw het project:
   ```bash
   mvn clean install
   ```

4. Start de applicatie:
   ```bash
   mvn spring-boot:run
   ```

5. De API is nu beschikbaar op `http://localhost:8080`
   - Swagger documentatie: `http://localhost:8080/swagger-ui.html`

## 🔒 Authenticatie en Autorisatie

GreenTrade implementeert role-based access control met drie gebruikersrollen:

- **ROLE_BUYER**: Kan producten bekijken en aankopen
- **ROLE_SELLER**: Kan producten aanmaken en beheren, certificaten uploaden
- **ROLE_ADMIN**: Heeft volledige toegang, kan producten verifiëren

Authenticatie gebeurt via JWT tokens die worden verkregen door het login endpoint.

## 📚 API Endpoints

### Authenticatie
- `POST /api/auth/register` - Nieuwe gebruiker registreren
- `POST /api/auth/login` - Inloggen en een JWT-token verkrijgen

### Producten
- `GET /api/products` - Alle producten ophalen
- `GET /api/products/{id}` - Specifiek product ophalen
- `POST /api/products` - Nieuw product aanmaken (ROLE_SELLER)
- `PUT /api/products/{id}` - Product bijwerken (ROLE_SELLER)
- `DELETE /api/products/{id}` - Product verwijderen (ROLE_SELLER, ROLE_ADMIN)

### Certificaten
- `GET /api/certificates` - Alle certificaten ophalen
- `POST /api/certificates` - Nieuw certificaat aanmaken
- `POST /api/certificates/{id}/file` - Certificaatbestand uploaden
- `GET /api/certificates/{id}/file` - Certificaatbestand downloaden

### Verificatie
- `POST /api/verifications/products/{productId}/submit` - Product indienen voor verificatie
- `POST /api/verifications/{id}/review` - Verificatie beoordelen (ROLE_ADMIN)
- `GET /api/verifications/pending` - Openstaande verificaties ophalen (ROLE_ADMIN)

### Transacties
- `POST /api/transactions` - Nieuwe transactie aanmaken
- `GET /api/transactions/buyer/{buyerId}` - Transacties van een koper ophalen

### Berichten
- `POST /api/messages` - Bericht versturen
- `GET /api/messages/unread/{userId}` - Ongelezen berichten ophalen

## 🧪 Testen

GreenTrade bevat zowel unit tests als integratietests voor alle kernfunctionaliteiten. De tests kunnen worden uitgevoerd met:

```bash
mvn test
```

Voor specifieke testklassen:

```bash
mvn test -Dtest=ProductServiceTest
```

## 📊 Voorbeelddata

De applicatie bevat voorbeeld testdata die automatisch geladen wordt tijdens het opstarten. Deze data bevat:

- Voorgedefinieerde gebruikers (admin, verkoper, koper)
- Voorbeeldproducten met duurzaamheidsscores
- Voorbeeld certificaten en verificaties

## 👥 Gebruikers voor testen

| Email | Wachtwoord | Rol |
|-------|------------|-----|
| admin@greentrade.nl | password123 | ROLE_ADMIN |
| seller@greentrade.nl | password123 | ROLE_SELLER |
| buyer@greentrade.nl | password123 | ROLE_BUYER |

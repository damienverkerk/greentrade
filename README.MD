# GreenTrade API

GreenTrade is een RESTful API gebouwd met Spring Boot die handel in duurzame producten faciliteert. Het platform verbindt kopers met geverifieerde verkopers en biedt functies voor productverificatie, transactiebeheer en duurzaamheidsrapportage.

## **Inhoud**

- [Functies](#functies)
- [Architectuur](#architectuur)
- [Installatie](#installatie)
- [Gebruik](#gebruik)
- [API Endpoints](#api-endpoints)
- [Authenticatie en Autorisatie](#authenticatie-en-autorisatie)
- [Bijdragen](#bijdragen)
- [Licentie](#licentie)

## **Functies**

- Zoeken en filteren van duurzame producten
- Plaatsen en beheren van bestellingen
- Uploaden en verifiëren van duurzaamheidscertificaten
- Beheren van gebruikersaccounts en rollen
- Genereren van duurzaamheidsrapporten

## **Architectuur**

De applicatie is opgebouwd volgens de MVC-architectuur en maakt gebruik van de volgende technologieën:

- **Spring Boot** - Framework voor het bouwen van Java-applicaties
- **Spring Data JPA** - Voor interactie met de database
- **Spring Security** - Voor authenticatie en autorisatie
- **JWT (JSON Web Tokens)** - Voor stateless authenticatie
- **PostgreSQL** - Relationele database
- **Maven** - Build automation tool
- **JUnit & Mockito** - Voor unit- en integratietests

## **Installatie**

Volg deze stappen om de applicatie lokaal op te zetten:

1. **Clone de repository**

   ```bash
   git clone https://github.com/jouw-gebruikersnaam/greentrade.git
   cd greentrade

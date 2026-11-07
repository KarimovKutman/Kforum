# Kforum

A web-based forum application built with **Java 21**, **Spring Boot** and **PostgreSQL**.

Kforum allows users to register, authenticate, create topics, leave comments and interact with other forum members. It also includes role-based access control and administrative functionality.

## Screenshots

### Main Page
<img width="1822" height="756" alt="Kforum Main Page" src="https://github.com/user-attachments/assets/20ed0c6f-51de-4b46-a193-ca874a69909b" />

### Login Page
<img width="1463" height="726" alt="Kforum Login Page" src="https://github.com/user-attachments/assets/362ce938-51e4-4b0f-98c9-ea87803bbcb8" />

### Registration Page
<img width="860" height="883" alt="Kforum Registration Page" src="https://github.com/user-attachments/assets/b90083f3-e91a-4f61-8894-9d7912cfc264" />

---

## What I Learned

While developing **Kforum**, I gained practical experience in Java backend development and learned how different components of a Spring application interact with each other.

### Java & Spring Framework
- Java 21 and Object-Oriented Programming (OOP)
- Spring Boot application development
- Dependency Injection (DI) and Inversion of Control (IoC)
- Spring Beans and Constructor Injection
- Layered Architecture (Controller, Service, Repository)
- Spring MVC and HTTP request handling
- Server-side rendering with Thymeleaf
- Separation of business logic and data access

### Database & ORM
- PostgreSQL and relational database design
- JPA and Hibernate ORM
- Spring Data JPA and JpaRepository
- Entity Mapping and Entity Relationships
- `@ManyToOne`, `@JoinColumn` and `FetchType.LAZY`
- Persistence Context and Dirty Checking
- Transaction Management with `@Transactional`
- Read-only Transactions
- Flush and Persistence Context Synchronization
- JPQL and Custom Database Queries
- DTOs and DTO Projections
- Pagination and Sorting (`Pageable`, `PageRequest`, `Sort`)
- Flyway Database Migrations

### Security & Authentication
- Spring Security Configuration
- Authentication and Authorization
- Role-Based Access Control (RBAC)
- Custom Security Filters (`OncePerRequestFilter`)
- SecurityContextHolder
- UserDetailsService
- BCrypt Password Hashing
- JWT Generation, Validation and Claims
- Bearer Token Authentication
- Session-Based Authentication
- Differences between session-based and token-based authentication

### Validation & Error Handling
- Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`)
- DTO-based Data Transfer
- BindingResult and Form Validation
- Custom Runtime Exceptions
- Data Integrity Violation Handling
- Input Validation and Normalization
- Resource Ownership and Permission Checks

### Additional Technologies
- Maven Dependency Management
- Lombok
- HTML5 and CSS3
- Vanilla JavaScript
- Git and Version Control

---

## Practical Experience

During development, I implemented:

- User registration and authentication.
- Password hashing with BCrypt.
- Role-based access control for regular users and administrators.
- CRUD operations for topics, comments and categories.
- Topic search by title and content.
- Pagination and sorting of forum topics.
- View counting for topics.
- Administrative functionality for managing forum content.
- Category statistics using JPQL queries.
- JWT authentication infrastructure.
- Database schema versioning with Flyway.
- Server-side HTML rendering with Thymeleaf.
- Validation and error handling for user input.

## Technologies Used

**Backend:** Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA

**Database:** PostgreSQL, Hibernate, JPA, Flyway

**Security:** JWT, BCrypt, Spring Security

**Frontend:** Thymeleaf, HTML5, CSS3, JavaScript

**Build Tool:** Maven

**Other:** Lombok, Git

---

## Project Focus

My main goal was to understand backend development beyond simply writing CRUD operations.

I focused on understanding how Spring manages dependencies, how Hibernate interacts with a relational database, how transactions work, how authentication and authorization are implemented, and how different application layers work together.

**Kforum is an ongoing learning project where I apply theoretical knowledge to practical backend development.**

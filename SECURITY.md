# Security Policy — NijaKaam

## 1. Overview

NijaKaam is a rural employment platform built using Java, Spring Boot, MongoDB, and JWT-based authentication.

Security is an important part of the platform. The application implements authentication, role-based authorization, resource ownership validation, input validation, and centralized exception handling.

## 2. Supported Versions

Security fixes are applied to the actively maintained version of the project.

| Version | Security Support |
|---|---|
| Current development version | Supported |
| Older versions | Not guaranteed |

## 3. Authentication

NijaKaam uses JSON Web Tokens (JWT) for authentication.

- Users must register before accessing protected features.
- Users authenticate using their email and password.
- Passwords must be securely hashed before storage.
- Successful login returns an authentication token.
- Protected API requests must include a valid JWT.

### Authorization Header

```http
Authorization: Bearer <JWT_TOKEN>
```

JWT tokens must be kept confidential and must not be shared or committed to GitHub.

## 4. Role-Based Access Control (RBAC)

The application supports three roles:

| Role | Permissions |
|---|---|
| WORKER | Manage own worker profile, browse jobs, apply for jobs, view and withdraw own applications |
| EMPLOYER | Manage own employer profile, create and manage own jobs, view applications for own jobs, update application status |
| ADMIN | Administrative access to protected platform resources |

Access to protected endpoints is controlled using Spring Security.

## 5. Resource Ownership Validation

NijaKaam implements ownership validation to prevent unauthorized access to other users' resources.

Examples:

- An employer can update or delete only their own jobs.
- An employer cannot modify another employer's job.
- A worker can view only their own applications.
- A worker cannot withdraw another worker's application.
- An employer can view and update applications belonging to their own jobs.
- Users cannot change protected ownership fields through request bodies.

Ownership checks are performed in the service layer using the authenticated user's identity.

## 6. API Security

The application uses the following security practices:

- JWT authentication for protected endpoints.
- Role-based endpoint authorization.
- Stateless Spring Security configuration.
- CSRF protection disabled for the stateless JWT API.
- Request validation using Jakarta Bean Validation.
- Protection against unauthorized resource modification.
- Centralized exception handling.
- Appropriate HTTP status codes for authorization and validation failures.

## 7. Input Validation

Request DTOs are used to validate incoming data.

Validation includes:

- Required field checks.
- Email format validation.
- Password length restrictions.
- Phone number format validation.
- Job title and description length restrictions.
- Budget validation.
- Required skills and location validation.

Invalid requests should return HTTP 400 Bad Request.

## 8. Password and Secret Management

The following security practices must be followed:

- Never store plain-text passwords.
- Use a secure password hashing mechanism.
- Never expose passwords, JWT signing secrets, or database credentials.
- Store sensitive configuration in environment variables.
- Never commit `.env` files or credentials to version control.
- Use a strong, randomly generated JWT signing secret.
- Rotate secrets immediately if they are exposed.

Example environment configuration:

```env
JWT_SECRET=your_secure_random_secret
PLATFORM_MONGODB_URI=your_mongodb_connection_string
```

Use placeholder values in `.env.example`. Never put real credentials in that file.

## 9. Error Handling

The application uses centralized exception handling through `@RestControllerAdvice`.

Expected responses include:

| HTTP Status | Meaning |
|---|---|
| 400 | Invalid request or validation failure |
| 401 | Authentication required or invalid credentials |
| 403 | Access denied |
| 404 | Resource not found |
| 500 | Unexpected server error |

Internal exception details, database credentials, and sensitive information must not be exposed in API responses.

## 10. Security Testing

Security testing should include:

- Invalid login credentials.
- Missing or invalid JWT tokens.
- Expired JWT tokens.
- Unauthorized role access.
- Cross-user resource access attempts.
- Cross-employer job modification attempts.
- Unauthorized application withdrawal.
- Unauthorized application status updates.
- Invalid request payloads.
- Duplicate job applications.

API security testing is performed using Postman, with automated tests where available.

## 11. Reporting a Security Vulnerability

If you discover a security vulnerability, please report it privately to the project maintainers.

Please include:

- A description of the vulnerability.
- Steps to reproduce the issue.
- The affected API endpoint or component.
- Potential security impact.
- Supporting evidence, without exposing real user data or credentials.

Do not publicly disclose vulnerabilities before the maintainers have had an opportunity to investigate.

## 12. Security Disclaimer

This security policy describes the intended security practices of NijaKaam. Security controls should be continuously reviewed, tested, and improved as the application evolves.

**Project:** NijaKaam  
**Technology:** Java 21, Spring Boot, Spring Security, JWT, MongoDB  
**Repository:** https://github.com/nagaraj-k00/nijakaam.git

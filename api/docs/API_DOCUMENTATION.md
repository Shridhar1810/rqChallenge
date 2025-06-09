# Employee API Documentation

This documentation provides details about the implementation of the ReliaQuest Employee API, including setup instructions, usage examples, and technical details.

## Table of Contents

1. [Overview](#overview)
2. [Setup Instructions](#setup-instructions)
3. [API Endpoints](#api-endpoints)
4. [Authentication](#authentication)
5. [Technical Implementation](#technical-implementation)
6. [Best Practices Implemented](#best-practices-implemented)

## Overview

This implementation provides a RESTful API for employee management with features like:

- Secure authentication using JWT
- Complete CRUD operations for employee management
- Advanced search functionality
- Salary-based analytics
- Comprehensive error handling and validation
- Detailed logging

## Setup Instructions

### Prerequisites

- Java 11 or higher
- Gradle

### Running the Application

1. Start the mock server first:
   ```
   ./gradlew server:bootRun
   ```

2. In a new terminal, start the API application:
   ```
   ./gradlew api:bootRun
   ```

3. The API will be available at: `http://localhost:8080`

## API Endpoints

### Authentication

#### Register a new user
```
POST /api/v1/auth/register
```
Request body:
```json
{
  "username": "user1",
  "password": "password123"
}
```

#### Login
```
POST /api/v1/auth/login
```
Request body:
```json
{
  "username": "user1",
  "password": "password123"
}
```
Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Employee Operations

All employee endpoints require authentication using the JWT token in the Authorization header:
```
Authorization: Bearer <your-token>
```

#### Get all employees
```
GET /api/v1/employees
```

#### Get employee by ID
```
GET /api/v1/employees/{id}
```

#### Search employees by name
```
GET /api/v1/employees/search/{searchString}
```

#### Get highest salary
```
GET /api/v1/employees/highestSalary
```

#### Get top 10 highest earning employee names
```
GET /api/v1/employees/topTenHighestEarningEmployeeNames
```

#### Create employee
```
POST /api/v1/employees
```
Request body:
```json
{
  "name": "John Doe",
  "salary": 85000,
  "age": 30,
  "job_title": "Software Engineer"
}
```

#### Delete employee
```
DELETE /api/v1/employees/{id}
```

## Authentication

The API uses JWT (JSON Web Token) authentication:

1. Register a user or login with existing credentials
2. Include the JWT token in the Authorization header for all protected endpoints
3. Tokens expire after 24 hours (configurable)

## Technical Implementation

### Project Structure

- **Controller Layer**: RESTful APIs following the specified interface
- **Service Layer**: Business logic with Java 8 features
- **Security**: JWT authentication and authorization
- **Exception Handling**: Global exception handling with detailed responses
- **Validation**: Input validation using Jakarta Bean Validation

### Java 8 Features Used

- **Lambda Expressions**: Used throughout for concise code
- **Stream API**: For efficient data processing
- **Method References**: For cleaner code
- **Optional**: For null-safe operations
- **Functional Interfaces**: For clean error handling

## Best Practices Implemented

1. **Clean Code**:
   - Consistent naming conventions
   - Single responsibility principle
   - Proper method abstraction
   - Comprehensive JavaDocs

2. **Error Handling**:
   - Centralized exception handling
   - Specific exception types
   - Graceful degradation

3. **Security**:
   - JWT authentication
   - Password encryption
   - Input validation
   - HTTPS support (configurable)

4. **Logging**:
   - Structured logging
   - Multiple log levels
   - Contextual information in logs

5. **Testing**:
   - Unit tests for all components
   - Integration tests for APIs
   - Mocking of dependencies
   - Test coverage reporting

6. **Scalability**:
   - Stateless design for horizontal scaling
   - Connection pooling
   - Pagination for large datasets
   - Proper caching mechanisms

7. **Documentation**:
   - API documentation
   - Code comments
   - Setup instructions

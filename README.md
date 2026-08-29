# CloudOps

CloudOps is a cloud-native incident management platform built with Java, Spring Boot, React, and AWS. It demonstrates full-stack development, serverless architecture, secure authentication, REST API design, cloud persistence, containerization, and automated testing.

Authenticated users can create, view, update, and delete operational incidents through a React dashboard backed by a Spring Boot REST API running on AWS Lambda.

## Dashboard

![CloudOps Dashboard](images/cloudops-dashboard.png)

## Architecture

```text
                    User
                      |
                      v
               React Dashboard
                      |
                      v
              Amazon Cognito
                      |
                  JWT Token
                      |
                      v
             Amazon API Gateway
                      |
                JWT Validation
                      |
                      v
                AWS Lambda
                      |
                      v
               Spring Boot API
                      |
                      v
              Amazon DynamoDB

                AWS Lambda
                      |
                      v
             Amazon CloudWatch
```

Users authenticate through Amazon Cognito and receive a short-lived JWT. API Gateway validates the token before allowing requests to reach the Lambda-hosted Spring Boot backend.

The backend accesses DynamoDB through a least-privilege IAM execution role without storing AWS access keys in the application.

## Features

- Secure authentication with Amazon Cognito
- JWT-protected REST API
- Create incidents
- View incidents
- Update incidents
- Delete incidents
- UUID-based incident identifiers
- Incident severity classification
- Incident status tracking
- Incident ownership
- Dashboard statistics
- Responsive React interface
- DynamoDB cloud persistence
- Serverless Java backend with AWS Lambda
- API Gateway integration
- CloudWatch logging
- Input validation
- Automated backend tests
- Frontend linting and production builds
- Dockerized backend

## Incident Model

Each incident contains:

| Field | Description |
|---|---|
| `id` | Automatically generated UUID |
| `title` | Incident title |
| `description` | Detailed incident description |
| `severity` | Operational severity |
| `status` | Current incident status |
| `owner` | Person responsible for the incident |

### Severity Levels

- `LOW`
- `MEDIUM`
- `HIGH`
- `CRITICAL`

### Incident Statuses

- `OPEN`
- `INVESTIGATING`
- `RESOLVED`
- `CLOSED`

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/incidents` | Retrieve all incidents |
| `GET` | `/api/incidents/{id}` | Retrieve an incident by ID |
| `POST` | `/api/incidents` | Create a new incident |
| `PUT` | `/api/incidents/{id}` | Update an existing incident |
| `DELETE` | `/api/incidents/{id}` | Delete an incident |

All deployed API routes require a valid Cognito JWT.

## Technology Stack

### Frontend

- React
- Vite
- JavaScript
- CSS
- AWS Amplify
- Amazon Cognito

### Backend

- Java 21
- Spring Boot
- Spring Web MVC
- Jakarta Validation
- Maven
- AWS SDK for Java 2.x

### AWS

- AWS Lambda
- Amazon API Gateway
- Amazon DynamoDB
- Amazon Cognito
- AWS IAM
- Amazon CloudWatch

### Development and Testing

- Git
- GitHub
- Docker
- Maven
- npm
- ESLint
- JUnit
- Mockito
- MockMvc

## Security

Security is built into the CloudOps architecture.

### Cognito Authentication

Users authenticate through Amazon Cognito.

After successful authentication, the frontend receives a temporary JWT and sends it with API requests:

```http
Authorization: Bearer <JWT>
```

API Gateway validates the JWT before forwarding requests to AWS Lambda.

Unauthenticated requests return:

```text
401 Unauthorized
```

### No Hard-Coded AWS Credentials

CloudOps does not store AWS access keys in:

- Java source code
- React source code
- Docker images
- GitHub
- Application configuration committed to the repository

AWS Lambda receives temporary AWS credentials automatically through its IAM execution role.

### Least-Privilege IAM

The Lambda execution role is restricted to the DynamoDB operations required by CloudOps:

```text
dynamodb:GetItem
dynamodb:PutItem
dynamodb:DeleteItem
dynamodb:Scan
```

Access is restricted to the CloudOps DynamoDB table.

### CORS

API Gateway CORS configuration restricts browser requests to approved frontend origins.

### Frontend Configuration

The Cognito User Pool ID, Cognito Client ID, and API Gateway URL are application identifiers rather than secret credentials.

Passwords, JWTs, AWS access keys, and other sensitive credentials are never committed to the repository.

## Project Structure

```text
CloudOps/
|
|-- frontend/
|   |-- src/
|   |   |-- App.jsx
|   |   |-- App.css
|   |   |-- amplify.js
|   |   `-- main.jsx
|   |
|   |-- package.json
|   |-- package-lock.json
|   `-- vite.config.js
|
|-- src/
|   |-- main/
|   |   |-- java/io/github/ripliquid/cloudops/
|   |   |   |-- config/
|   |   |   |   `-- AwsConfig.java
|   |   |   |
|   |   |   |-- controller/
|   |   |   |   `-- IncidentController.java
|   |   |   |
|   |   |   |-- lambda/
|   |   |   |   `-- StreamLambdaHandler.java
|   |   |   |
|   |   |   |-- model/
|   |   |   |   |-- Incident.java
|   |   |   |   |-- IncidentStatus.java
|   |   |   |   `-- Severity.java
|   |   |   |
|   |   |   |-- repository/
|   |   |   |   `-- IncidentRepository.java
|   |   |   |
|   |   |   `-- service/
|   |   |       `-- IncidentService.java
|   |   |
|   |   `-- resources/
|   |       `-- application.properties
|   |
|   `-- test/
|
|-- images/
|-- Dockerfile
|-- .dockerignore
|-- .gitignore
|-- pom.xml
`-- README.md
```

## Local Development

### Prerequisites

Install:

- Java 21
- Node.js
- npm
- Docker
- AWS CLI

Verify the installations:

```bash
java --version
node --version
npm --version
docker --version
aws --version
```

## Run the Backend

From the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

The local API runs at:

```text
http://localhost:8080
```

Example endpoint:

```text
http://localhost:8080/api/incidents
```

## Run the Frontend

Navigate to the frontend directory:

```powershell
cd frontend
```

Install dependencies:

```powershell
npm install
```

Start the Vite development server:

```powershell
npm run dev
```

Open:

```text
http://localhost:5173
```

## Frontend Environment Configuration

Create:

```text
frontend/.env.local
```

Add:

```env
VITE_COGNITO_USER_POOL_ID=your-user-pool-id
VITE_COGNITO_CLIENT_ID=your-client-id
VITE_API_URL=your-api-gateway-url
```

Do not store passwords, JWTs, AWS access keys, or other secret credentials in this file.

## Testing

### Backend Tests

Run:

```powershell
.\mvnw.cmd test
```

The backend test suite covers service behavior, controller endpoints, validation, CRUD operations, and application startup.

### Frontend Linting

```powershell
cd frontend
npm run lint
```

### Frontend Production Build

```powershell
npm run build
```

## Docker

Build the backend container:

```powershell
docker build -t cloudops .
```

Run the container:

```powershell
docker run --name cloudops-app -p 8080:8080 cloudops
```

CloudOps uses a multi-stage Docker build with Maven and Java 21.

## AWS Deployment

The deployed backend follows this architecture:

```text
API Gateway
    |
    v
AWS Lambda
    |
    v
Spring Boot
    |
    v
DynamoDB
```

Spring Boot runs inside AWS Lambda through the AWS Serverless Java Container adapter.

The Lambda deployment package is built using Maven Shade.

## Serverless Architecture

CloudOps avoids maintaining dedicated application servers.

The serverless design provides:

- Automatic scaling
- Minimal infrastructure management
- Stateless application execution
- Cloud-native persistence
- IAM-based service authentication
- Usage-based cloud resource consumption

## Observability

AWS Lambda sends application logs to Amazon CloudWatch.

CloudWatch can be used to inspect:

- Lambda initialization
- Spring Boot startup
- API requests
- Runtime exceptions
- Invocation duration
- Function failures

## Example Incident

```json
{
  "id": "e6aaf449-2886-4571-8df8-b8fafca6c07f",
  "title": "Authentication Service Failure",
  "description": "Users are unable to authenticate.",
  "severity": "CRITICAL",
  "status": "OPEN",
  "owner": "Daniyal"
}
```

## Current Status

CloudOps currently supports:

- Full CRUD operations
- AWS-hosted Java backend
- DynamoDB persistence
- Cognito authentication
- JWT API authorization
- React dashboard
- Docker containerization
- Backend automated testing
- Frontend linting
- Frontend production builds
- CloudWatch logging

## Planned Improvements

- GitHub Actions CI/CD
- GitHub-to-AWS authentication using OIDC
- Automated Lambda deployment
- Additional CloudWatch metrics and alarms
- Incident search and filtering
- Incident timestamps
- Audit history
- Role-based authorization
- Production frontend hosting

## What This Project Demonstrates

CloudOps demonstrates practical experience with:

- Full-stack software development
- REST API design
- Java and Spring Boot
- React
- AWS serverless architecture
- Authentication and authorization
- Cloud databases
- IAM security
- Docker
- Automated testing
- Cloud deployment
- Production-style software architecture

# Online Library Service (Backend)

[📄 Open ER Diagram & API Blueprint PDF](https://github.com/rohitstacklabs/online-library-service-Backed/raw/main/Online%20Library%20-%20Er%20Diagram%20%26%20Api%20Blueprint.pdf)

Production-ready backend service for the **Online Library Management System**.  
This project is designed with real-world features and production practices in mind.

---

## 📌 Features

- **User Management**: Guest and Logged-in users with memberships
- **Book Catalog**: Search books by category, status, name, or author
- **Borrow & Return**: Users can borrow books for specific days and return them
- **Membership Validation**: System checks user membership before borrowing
- **Email Notifications (SMTP)**:
  - Welcome email on user registration
  - Email sent when a book is added or updated
- **Real-time Notifications (WebSocket)** for status updates
- **Message Queue (Kafka)** for reliable event-driven communication
- **Reports**: Most borrowed categories, user history, top books, etc.
- **Dockerized Deployment**: Everything runs inside containers (PostgreSQL, Redis, Kafka, Spring Boot)

---

## ⚡ Why Kafka, SMTP, WebSocket, Docker?

- **Kafka**: Handles events like "book borrowed" or "book returned" asynchronously. Ensures scalability and reliability.
- **SMTP (Email Service)**: Used to notify users (welcome emails, book updates).
- **WebSocket**: Real-time push notifications without refreshing the page.
- **Docker**: No need to install Kafka, PostgreSQL, Redis, etc. locally. Everything runs with a single command inside containers.

---

## 🚀 Quick Start (2 Steps Only)

Make sure you have **Docker** and **Docker Compose** installed locally.  
Then run the following commands:


# 1. Package the application (without running tests)
mvn clean package -DskipTests

# 2. Build and run everything in Docker (backend + Kafka + Redis + PostgreSQL)
docker-compose up --build -d

🔔 Key Features in Backend + Frontend

User Registration → Sends Welcome Email

Book Added/Updated → Sends Email notification to users

Book Borrowed/Returned → Real-time WebSocket notification

Soft Delete → Books can be deactivated instead of permanently deleted

Future Enhancements:

Microservices-based architecture (split users, books, notifications, reports)

Image upload and storage on AWS S3

Advanced analytics dashboards


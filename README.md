# **Advanced Reservation System**

## **Project Overview**
This project focuses on designing and implementing an advanced reservation system capable of handling a large number of users efficiently. The system aims to identify the nearest available time slots automatically for reservation.

## **Objective**
- Enhance system design and optimization skills.
- Improve transaction management capabilities.
- Evaluate and refine advanced design and technical skills.

## **Key Features**
- **Data Management:** Store and model reservation and user data efficiently.
- **Time Slot Management:** Automatically identify and reserve the nearest available time slot.
- **Concurrency Management:** Prevent double booking of the same time slot by multiple users simultaneously.

## **API Endpoints**
- **POST /api/reservations:** Create a new reservation for a specified time.
- **DELETE /api/reservations/{id}:** Cancel an existing reservation by ID.

## **Technical Requirements**
1. **Language & Framework:** Java with Spring Boot.
2. **Database:** MySQL or H2 for data storage.
3. **API Documentation:** Swagger or OpenAPI.
4. **Authentication:** JWT for secure access (if implemented).
5. **Performance:** API should respond within 100ms under high TPS load.
6. **Scalability:** Capable of managing tables with millions of records.

## **Expectations**
- Clean, readable, and maintainable code.
- Adherence to Spring Boot best practices.
- Optimized database performance.
- Effective caching mechanisms.

## **Database Structure**
### **Available Slots Table**
```sql
CREATE TABLE available_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    is_reserved BOOLEAN DEFAULT FALSE
);

INSERT INTO available_slots (start_time, end_time, is_reserved) VALUES 
('2024-12-29 09:00:00', '2024-12-29 10:00:00', FALSE),
('2024-12-29 10:00:00', '2024-12-29 11:00:00', FALSE);
```

### **Users Table**
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email, password) VALUES 
('user1', 'johndoe@example.com', 'hashed_password_123'),
('user2', 'janedoe@example.com', 'hashed_password_456');
```

## **Additional Notes**
- Provide clear technical documentation upon completion.
- Include a document highlighting potential service improvements.
- Mock test data can be used for initial testing.
- No UI implementation is required for this project.

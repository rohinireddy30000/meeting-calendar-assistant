 # Meeting Calendar Assistant

A RESTful Spring Boot application to help employees manage their meeting calendars, book meetings, find free slots, and detect meeting conflicts.

---

## Features
- Book meetings for any employee (each employee owns their calendar)
- Find free slots for meetings between two employees
- Detect conflicts for a meeting request among multiple participants
- Fully REST-compliant, no UI
- Testable via Postman or any HTTP client

---

## Getting Started

### Prerequisites
- Java 17+
- Maven

### Clone the Repository
```
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name/demo
```

### Build the Project
```
mvn clean package
```

### Run Locally
```
java -jar target/demo-0.0.1-SNAPSHOT.jar
```
- The app will be available at: [http://localhost:8080](http://localhost:8080)

---

## Deploying to Render.com

1. **Connect your GitHub repo to Render.**
2. **Create a new Web Service.**
3. **If prompted for language, select `Docker` (or Java if available).**
4. **Set the build and start commands:**
   - Build Command: `./mvnw clean package` *(or `mvn clean package`)*
   - Start Command: `java -jar target/demo-0.0.1-SNAPSHOT.jar`
5. **(Optional) Add a Dockerfile:**
   ```dockerfile
   FROM openjdk:17-jdk-alpine
   VOLUME /tmp
   COPY target/demo-0.0.1-SNAPSHOT.jar app.jar
   ENTRYPOINT ["java","-jar","/app.jar"]
   ```

---

## API Endpoints

### 1. Book a Meeting
- **POST** `/meetings/book?empName={employeeName}`
- **Description:** Book a meeting for the specified employee.
- **Request Body (JSON):**
  ```json
  {
    "start": "2024-07-09T10:00:00",
    "end": "2024-07-09T11:00:00",
    "title": "Project Sync",
    "description": "Weekly project sync-up"
  }
  ```
- **Response:**
  - `200 OK` — `Meeting booked for John`

### 2. Find Free Slots
- **GET** `/meetings/free-slots?emp1={employee1}&emp2={employee2}&durationMinutes={duration}`
- **Description:** Find all free slots where both employees are available for a meeting of the given duration (in minutes).
- **Response (JSON):**
  ```json
  [
    {
      "start": "2024-07-09T09:00:00",
      "end": "2024-07-09T09:30:00",
      "title": "Free Slot",
      "description": null
    }
    // ... more slots
  ]
  ```

### 3. Find Meeting Conflicts
- **POST** `/meetings/conflicts?participants={employee1}&participants={employee2}&...`
- **Description:** Check which participants have a conflict with the given meeting time.
- **Request Body (JSON):**
  ```json
  {
    "start": "2024-07-09T10:00:00",
    "end": "2024-07-09T11:00:00",
    "title": "Project Sync",
    "description": "Weekly project sync-up"
  }
  ```
- **Response (JSON):**
  ```json
  ["John"]
  ```

---

## Error Handling
- **400 Bad Request:** For missing/invalid parameters or bad JSON
- **500 Internal Server Error:** For unexpected server errors

---

## Testing
- Use [Postman](https://www.postman.com/) or any HTTP client to test the endpoints.
- Run all tests:
  ```sh
  mvn test
  ```

---

## Project Structure
```
demo/
  ├── pom.xml
  ├── src/
  │   ├── main/java/com/example/demo/
  │   │   ├── controller/
  │   │   ├── model/
  │   │   ├── service/
  │   │   └── DemoApplication.java
  │   └── resources/application.properties
  │   └── test/java/com/example/demo/
  │       ├── controller/
  │       ├── model/
  │       └── service/
  ├── .gitignore
  └── README.md
```

---



examples:-
1. Book a Meeting for Rohini
URL:
http://localhost:8080/meetings/book?empName=Rohini
Method: POST
Headers: Content-Type: application/json
Body:
{
  "start": "2024-07-10T14:00:00",
  "end": "2024-07-10T15:00:00",
  "title": "Design Review",
  "description": "Review UI/UX designs with the team"
} // output :- meeting booked for rohini 
2. Find Free Slots for Rohini and Priya
URL:
http://localhost:8080/meetings/free-slots?emp1=Rohini&emp2=Priya&durationMinutes=30
Method: GET
[
  {
    "start": "2024-07-10T09:00:00",
    "end": "2024-07-10T09:30:00",
    "title": "Free Slot",
    "description": null
  },
  {
    "start": "2024-07-10T09:30:00",
    "end": "2024-07-10T10:00:00",
    "title": "Free Slot",
    "description": null
  }

]3. Find Meeting Conflicts for Rohini and Priya
URL:
http://localhost:8080/meetings/conflicts?participants=Rohini&participants=Priya
Method: POST
Headers: Content-Type: application/json

{
  "start": "2024-07-10T14:30:00",
  "end": "2024-07-10T15:30:00",
  "title": "Client Call",
  "description": "Call with client about project status"
} //output:-["Rohini"]


4. No Conflicts Example
URL:
http://localhost:8080/meetings/conflicts?participants=Rohini&participants=Priya
Method: POST
Headers: Content-Type: application/json

{
  "start": "2024-07-10T16:00:00",
  "end": "2024-07-10T17:00:00",
  "title": "Team Sync",
  "description": "Daily team sync-up"
}//no conflicts

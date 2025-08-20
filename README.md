# Pincode Distance Calculator

A Spring Boot REST API that calculates **distance** and **duration** between Indian pincodes using Google Maps APIs with intelligent caching and geocoding.

## ✨ Features
- Calculate distance and duration between two pincodes
- Intelligent database caching to minimize API calls
- Automatic pincode geocoding (lat/long + polygon data)
- RESTful API with comprehensive error handling
- MySQL database integration
- Input validation and unit tests

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- MySQL 8.0 or higher
- Google Cloud account with Maps APIs enabled
- Maven (optional, wrapper included)

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/Somtrip/pincode_distance.git
   cd pincode_distance
   ```

2. **Create the database**
   ```sql
   CREATE DATABASE pincode_distance;
   ```

3. **Enable Google Maps APIs**
   - Create a project in Google Cloud Console
   - Enable:
     - Distance Matrix API
     - Geocoding API
   - Create an API key and (recommended) restrict it to these APIs

4. **Application configuration**

   Update `src/main/resources/application.properties` with your settings:
   ```properties
   # Database spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password

   
   # Google Maps API
   google.api.key=your_google_maps_api_key
   ```

5. **Build and run**
   ```bash
   # Build the project
   ./mvnw clean package

   # Run the application
   ./mvnw spring-boot:run
   ```

The application will start at: http://localhost:8080

---

# API Usage

## Calculate Distance Between Pincodes

**Request**
```http
GET /api/distance?from=141106&to=110060
```

**Response**
```json
{
  "fromPincode": "141106",
  "toPincode": "110060",
  "distanceKm": 350.5,
  "durationMinutes": 300.0,
  "durationFormatted": "5 hr 0 min",
  "routeJson": "{...raw Google API response...}"
}
```

## Error Responses

**Invalid Pincode**
```json
{
  "error": "Invalid pincode: must be 6 digits"
}
```

**API Error**
```json
{
  "error": "Error fetching route from Google Maps API"
}
```

# Order Service - Java 17 to Java 21 Migration Example

A realistic Spring Boot Order Management Service demonstrating migration from Java 17 to Java 21 using OpenRewrite.

## Project Overview

This is a complete, working Spring Boot application that manages customer orders. It includes:
- RESTful API endpoints for order management
- JPA/Hibernate for data persistence
- H2 in-memory database
- Comprehensive unit tests
- Input validation

## Prerequisites

- **Java 17** (for the initial version)
- **Java 21** (for the migrated version)
- **Maven 3.8+**
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

## Current State (Java 17)

This project is currently configured for Java 17 with:
- Spring Boot 3.1.5
- Java 17 language features
- Traditional switch statements
- Standard collection operations

## Project Structure

```
order-service/
├── pom.xml                          # Maven configuration (Java 17)
├── README.md                        # This file
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/orders/
    │   │       ├── OrderServiceApplication.java    # Main application
    │   │       ├── controller/
    │   │       │   └── OrderController.java        # REST API endpoints
    │   │       ├── service/
    │   │       │   └── OrderService.java           # Business logic
    │   │       ├── model/
    │   │       │   ├── Order.java                  # Order entity
    │   │       │   ├── OrderStatus.java            # Status enum
    │   │       │   └── PaymentMethod.java          # Payment enum
    │   │       └── repository/
    │   │           └── OrderRepository.java        # Data access
    │   └── resources/
    │       └── application.yml                     # Application config
    └── test/
        └── java/
            └── com/example/orders/
                └── OrderServiceTest.java            # Unit tests
```

## Building and Running (Java 17)

### 1. Build the Project

```bash
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Run Tests

```bash
mvn test
```

## API Endpoints

Once running, you can test the API:

### Create an Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "customer@example.com",
    "totalAmount": 999.99,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Get All Orders
```bash
curl http://localhost:8080/api/orders
```

### Get Order by ID
```bash
curl http://localhost:8080/api/orders/1
```

### Update Order Status
```bash
curl -X PATCH http://localhost:8080/api/orders/1/status?status=SHIPPED
```

### Get Order Status Message
```bash
curl http://localhost:8080/api/orders/1/status-message
```

### Get Recent Orders
```bash
curl http://localhost:8080/api/orders/recent?limit=5
```

### Delete Order
```bash
curl -X DELETE http://localhost:8080/api/orders/1
```

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:orderdb`
- Username: `sa`
- Password: (leave empty)

## Migrating to Java 21

Follow the blog post instructions to migrate this project to Java 21 using OpenRewrite:

### Step 1: Add OpenRewrite Plugin

Add this to your `pom.xml` in the `<build><plugins>` section:

```xml
<plugin>
    <groupId>org.openrewrite.maven</groupId>
    <artifactId>rewrite-maven-plugin</artifactId>
    <version>5.37.0</version>
    <configuration>
        <activeRecipes>
            <recipe>org.openrewrite.java.migrate.UpgradeToJava21</recipe>
            <recipe>org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3</recipe>
        </activeRecipes>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-migrate-java</artifactId>
            <version>2.20.0</version>
        </dependency>
        <dependency>
            <groupId>org.openrewrite.recipe</groupId>
            <artifactId>rewrite-spring</artifactId>
            <version>5.16.0</version>
        </dependency>
    </dependencies>
</plugin>
```

### Step 2: Run Migration

```bash
# Discover available recipes
mvn rewrite:discover

# Dry run to preview changes
mvn rewrite:dryRun

# Apply the migration
mvn rewrite:run
```

### Step 3: Update JAVA_HOME and Rebuild

```bash
# Set JAVA_HOME to JDK 21
export JAVA_HOME=/path/to/jdk-21

# Clean and rebuild
mvn clean install
```

### Step 4: Test

```bash
mvn test
```

## Post-Migration Enhancements

After migrating to Java 21, you can refactor the code to use:

1. **Pattern Matching for switch** - Modernize `getOrderStatusMessage()`
2. **Sequenced Collections** - Simplify `getRecentOrders()`
3. **Virtual Threads** - Enable in `application.yml`
4. **Record Patterns** - Create DTOs for API responses

## Technologies Used

- Spring Boot 3.1.5 (upgrades to 3.3.0)
- Spring Data JPA
- H2 Database
- Bean Validation
- JUnit 5
- Maven

## Learning Resources

- Blog: [learningfromexperience.org](https://learningfromexperience.org)
- YouTube: [@LearningFromExperience](https://youtube.com/@LearningFromExperience)
- Udemy: Search for "Ayan Bhowmick"

## License

This project is created for educational purposes as part of Java 21 migration tutorials.

## Author

**Ayan Bhowmick**  
Java Instructor | Udemy Course Creator  
Teaching practical Java development to 28,000+ students

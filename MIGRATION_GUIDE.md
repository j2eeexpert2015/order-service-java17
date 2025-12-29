# Java 21 Migration Guide with OpenRewrite

This guide walks you through migrating this Order Service from Java 17 to Java 21 using OpenRewrite automation.

## Prerequisites

- Java 21 installed on your system
- Maven 3.8 or higher
- This project compiling successfully on Java 17

## Step 1: Add OpenRewrite Plugin to pom.xml

Add the following plugin configuration to your `pom.xml` inside the `<build><plugins>` section:

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

## Step 2: Discover Available Recipes

Before making any changes, see what OpenRewrite will do:

```bash
mvn rewrite:discover
```

This command analyzes your project and shows applicable recipes.

Expected output:
```
[INFO] Available Recipes:
[INFO]   - org.openrewrite.java.migrate.UpgradeToJava21
[INFO]   - org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3
```

## Step 3: Dry Run (Preview Changes)

Always preview changes before applying them:

```bash
mvn rewrite:dryRun
```

This generates a patch file at `target/rewrite/rewrite.patch` showing exactly what will change.

Review the patch file to understand the changes.

## Step 4: Apply the Migration

If the dry run looks good, apply the changes:

```bash
mvn rewrite:run
```

This will:
- Update Java version from 17 to 21 in `pom.xml`
- Upgrade Spring Boot from 3.1.5 to 3.3.0
- Update deprecated APIs
- Modernize code patterns

## Step 5: Verify the Changes

Check your updated `pom.xml`. You should see:

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
</parent>
```

## Step 6: Update JAVA_HOME

Ensure your system uses JDK 21:

### On Linux/Mac:
```bash
# Check current version
java -version

# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk-21

# Add to your shell profile for persistence
echo 'export JAVA_HOME=/path/to/jdk-21' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### On Windows:
```cmd
# Check current version
java -version

# Set JAVA_HOME (Command Prompt)
set JAVA_HOME=C:\path\to\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

# Or use System Environment Variables GUI
# System Properties > Environment Variables > JAVA_HOME
```

### Verify:
```bash
java -version
# Should show: java version "21.x.x"

echo $JAVA_HOME
# Should show: /path/to/jdk-21
```

## Step 7: Clean and Rebuild

```bash
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
```

If you see compilation errors, check the troubleshooting section below.

## Step 8: Run Tests

```bash
mvn test
```

All 8 tests should pass:
```
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Step 9: Run the Application

```bash
mvn spring-boot:run
```

Test the API:
```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "test@example.com",
    "totalAmount": 500.00,
    "paymentMethod": "CREDIT_CARD"
  }'

# Get all orders
curl http://localhost:8080/api/orders
```

## Troubleshooting

### Issue 1: "Unsupported class file major version 65"

**Cause:** IDE or Maven using older Java version

**Solution:**
```bash
# Verify JAVA_HOME
echo $JAVA_HOME

# Verify Maven is using correct Java
mvn -version

# If incorrect, update JAVA_HOME and try again
```

### Issue 2: Compilation Error - "invalid target release: 21"

**Cause:** Maven compiler plugin not finding JDK 21

**Solution:**
```bash
# Ensure JAVA_HOME points to JDK 21
export JAVA_HOME=/path/to/jdk-21

# Clean and rebuild
mvn clean install
```

### Issue 3: Dependency Resolution Failures

**Cause:** Maven cache issues

**Solution:**
```bash
# Force update dependencies
mvn clean install -U

# Or clear local Maven cache
rm -rf ~/.m2/repository
mvn clean install
```

### Issue 4: Spring Boot Application Fails to Start

**Cause:** Incompatible dependencies with Spring Boot 3.3

**Solution:**
Check that all dependencies are compatible with Spring Boot 3.3.0. Common issues:
- Ensure using `jakarta.*` instead of `javax.*` packages
- Update any custom dependencies to Spring Boot 3.3 compatible versions

## Post-Migration: Leverage Java 21 Features

### 1. Enable Virtual Threads

Update `src/main/resources/application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

This enables Virtual Threads for all Spring MVC request handling, dramatically improving scalability.

### 2. Refactor to Pattern Matching for switch

Update `OrderService.getOrderStatusMessage()`:

**Before:**
```java
public String getOrderStatusMessage(Order order) {
    switch (order.getStatus()) {
        case PENDING:
            return "Your order is awaiting confirmation";
        case CONFIRMED:
            return "Your order has been confirmed";
        // ... more cases
        default:
            return "Unknown order status";
    }
}
```

**After:**
```java
public String getOrderStatusMessage(Order order) {
    return switch (order.getStatus()) {
        case PENDING -> "Your order is awaiting confirmation";
        case CONFIRMED -> "Your order has been confirmed and will be processed soon";
        case PROCESSING -> "Your order is being processed";
        case SHIPPED -> "Your order has been shipped and is on the way";
        case DELIVERED -> "Your order has been delivered successfully";
        case CANCELLED -> "Your order has been cancelled";
    };
}
```

### 3. Use Sequenced Collections

Update `OrderService.getRecentOrders()`:

**Before:**
```java
public List<Order> getRecentOrders(int limit) {
    List<Order> allOrders = orderRepository.findAll();
    int size = allOrders.size();
    int startIndex = Math.max(0, size - limit);
    return allOrders.subList(startIndex, size)
        .stream()
        .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
        .toList();
}
```

**After:**
```java
public List<Order> getRecentOrders(int limit) {
    List<Order> allOrders = orderRepository.findAll();
    // Java 21: reversed() is now available on List
    return allOrders.reversed()
        .stream()
        .limit(limit)
        .toList();
}
```

### 4. Create Record DTOs

Create `OrderResponse.java`:

```java
package com.example.orders.model;

public record OrderResponse(
    Long id,
    String customerEmail,
    String amount,
    String status,
    String message
) {}
```

Add to `OrderController.java`:

```java
@GetMapping("/{id}/details")
public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable Long id) {
    return orderService.getOrderById(id)
        .map(order -> {
            String message = orderService.getOrderStatusMessage(order);
            return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getTotalAmount().toString(),
                order.getStatus().name(),
                message
            );
        })
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

## Rebuild and Test

After making enhancements:

```bash
mvn clean install
mvn test
mvn spring-boot:run
```

## Migration Checklist

- [ ] Backed up code (Git commit/branch)
- [ ] Added OpenRewrite plugin to pom.xml
- [ ] Ran `mvn rewrite:discover`
- [ ] Ran `mvn rewrite:dryRun` and reviewed changes
- [ ] Ran `mvn rewrite:run`
- [ ] Updated JAVA_HOME to JDK 21
- [ ] Ran `mvn clean install` successfully
- [ ] Ran `mvn test` - all tests pass
- [ ] Updated application.yml for Virtual Threads
- [ ] Refactored code to use Java 21 features
- [ ] Tested application manually
- [ ] Updated documentation

## Benefits Achieved

✅ **Cleaner Code** - Pattern matching and modern syntax  
✅ **Better Performance** - Virtual Threads, JVM improvements  
✅ **Future-Proof** - LTS support until September 2029  
✅ **Modern Features** - Records, Sequenced Collections  
✅ **Improved Scalability** - Virtual Threads for I/O operations

## Next Steps

1. Update CI/CD pipelines to use JDK 21
2. Deploy to staging environment for testing
3. Monitor performance improvements
4. Explore more Java 21 features:
   - String Templates (Preview)
   - Structured Concurrency (Preview)
   - Scoped Values (Preview)

## Need Help?

- Blog: [learningfromexperience.org](https://learningfromexperience.org)
- YouTube: [@LearningFromExperience](https://youtube.com/@LearningFromExperience)
- GitHub: Open an issue on the repository

Happy migrating! 🚀

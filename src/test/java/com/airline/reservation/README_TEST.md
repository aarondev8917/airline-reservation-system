# Unit Tests Documentation

This directory contains comprehensive unit tests for the Airline Reservation System.

## Java Version Requirement

**IMPORTANT**: Tests require **Java 21** to run. The project is configured for Java 21, and Mockito/ByteBuddy (used for mocking) currently supports up to Java 22.

If you have multiple JDKs installed:
1. Set `JAVA_HOME` to point to Java 21 before running tests
2. Verify Java version: `java -version` (should show version 21)

Example on Windows (PowerShell):
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
mvn test
```

Example on Linux/Mac:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
mvn test
```

## Test Structure

```
src/test/
├── java/
│   └── com/airline/reservation/
│       ├── controllers/        # Controller unit tests
│       ├── services/           # Service layer unit tests
│       └── security/           # Security component tests
└── resources/
    └── application-test.properties  # Test configuration
```

## Running Tests

### Using Maven
```bash
# Run all tests
mvn test

# Run tests for a specific class
mvn test -Dtest=AuthServiceTest

# Run tests with coverage report (requires jacoco plugin)
mvn test jacoco:report
```

### Using IDE
- **IntelliJ IDEA**: Right-click on test class or method → Run Test
- **Eclipse**: Right-click on test class → Run As → JUnit Test

## Test Coverage

### Service Layer Tests
- **AuthServiceTest**: Tests user registration, login, and authentication logic
- **BookingServiceTest**: Tests booking creation, confirmation, cancellation, and retrieval
- **FlightServiceTest**: Tests flight creation, seat generation, and validation
- **PassengerServiceTest**: Tests passenger CRUD operations
- **PaymentServiceTest**: Tests payment processing and retrieval

### Controller Layer Tests
- **AuthControllerTest**: Tests authentication endpoints (register, login)
- **BookingControllerTest**: Tests booking REST endpoints

### Security Tests
- **JwtServiceTest**: Tests JWT token generation, validation, and extraction
- **AppUserPrincipalTest**: Tests UserDetails implementation

## Test Configuration

Tests use H2 in-memory database for fast execution. Configuration is in `application-test.properties`:
- H2 database (in-memory)
- JWT secret for token generation
- Simplified logging

## Test Patterns

### Mocking
All tests use Mockito for mocking dependencies:
- `@Mock`: Mock dependencies
- `@InjectMocks`: Inject mocks into the class under test
- `@ExtendWith(MockitoExtension.class)`: Enable Mockito annotations

### Assertions
Tests use JUnit 5 assertions:
- `assertEquals()`, `assertNotNull()`, `assertTrue()`, etc.
- `assertThrows()` for exception testing

### Test Annotations
- `@Test`: Marks a test method
- `@BeforeEach`: Setup method run before each test
- `@DisplayName`: Provides descriptive test names

## Writing New Tests

### Service Test Example
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("MyService Unit Tests")
class MyServiceTest {
    
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Test
    @DisplayName("Should successfully create entity")
    void testCreate_Success() {
        // Given
        when(repository.save(any())).thenReturn(entity);
        
        // When
        ResultDto result = service.create(requestDto);
        
        // Then
        assertNotNull(result);
        verify(repository).save(any());
    }
}
```

### Controller Test Example
```java
@WebMvcTest(MyController.class)
@DisplayName("MyController Unit Tests")
class MyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MyService service;
    
    @Test
    @DisplayName("Should return 200 OK for valid request")
    void testEndpoint_Success() throws Exception {
        when(service.method(any())).thenReturn(response);
        
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

## Best Practices

1. **Isolation**: Each test should be independent
2. **Naming**: Use descriptive test names with `@DisplayName`
3. **AAA Pattern**: Arrange (Given) → Act (When) → Assert (Then)
4. **Mocking**: Mock external dependencies, not the class under test
5. **Coverage**: Aim for high code coverage, especially business logic
6. **Readability**: Keep tests simple and focused on one scenario

## Troubleshooting

### Tests Failing
- Check that all mocks are properly configured
- Verify test data matches expected values
- Ensure test configuration is correct

### Integration Issues
- Verify `application-test.properties` is on classpath
- Check that test dependencies are in `pom.xml`
- Ensure test package structure matches main package structure

## Future Enhancements

- Integration tests for full request/response cycles
- Performance tests for critical paths
- Contract tests for API endpoints
- End-to-end tests for complete workflows


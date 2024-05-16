# European Dynamics Java Assignment - Topalidis Dimitrios

This is my submission for the assignment that I was asked to complete, in the context of the interviewing process for the Java Full-Stack Software Engineer role at
European Dynamics.

For my convenience and your evaluation faciliation, I used the following GitHub repo to complete the assignment: https://github.com/Dimtop/ed-assignment.
The default branch is "main", and you should use that to test a working version of the code.
Every exercise from 1 to 5, as well as the bonus task, were developed in their own branch, named **exercise-<exercise_number>** e.g. exercise-1.
All particular branches were merged into main by opening and **squash merging** a pull request. This means that if you want to see the progression and the specific commit history for a certain exercise, you should look at the respective branch. Note that because I had some "on-save" actions configured in my IDE, you may see some linting changes or reorganized imports. Please ignore those.

### Installation and Exercise 1 (25 points)

For this exercise, I had to fix the wrong usage of the AccountService class in AccountController. AccountController has a private final instance variable named accountService, which was instanciated as

```
private final accountService accountService;
```

instead of

```
private final AccountService accountService;
```

### Exercise 2 (15 points)

When navigating to the About page, the user was getting an error. This was caused by a misspelled URI to the /api/info/{component} endpoint, which returns the version of the component. Spefically, the request made in the **account.component.ts** file was to `/api/info/backssssend` instead of `/api/info/backend`.

The frontend version was requested in the same file. The mistake here was that when accessing the response body, `response.body` was misspelled as `response.bdy`

### Exercise 3 (20 points)

There was a **Calcbalance2** method in the **AccountService**, which was used to calculate the total balance of an account based on it's transactions. What was needed here, was to fetch all of the account's transactions, iterate over them and add the amount of each transaction to the balance if it's of type INCOME or substract if it's of type EXPENSE.

This process was refactored. First of all, I renamed the function to **calculateAccountBalance**, which is a much more readable and descriptive name. I also changed the **Logarsmos** param's name to **account** and its type to AccountDTO from Object.

In order to achieve better performance, I leveraged the native query functionality of Spring Data JPA to create an SQL query that calculates the balance directly in the DB:

```
  @Query(value = "SELECT SUM(CASE WHEN type = 1 THEN amount ELSE -amount END) FROM Transaction WHERE account_id = ?1", nativeQuery = true)
    BigDecimal calculateAccountBalance(long accountId);
```

Then I called the `calculateAccountBalance` method of the **TransactionRepository** from the AccountService's **calculateAccountBalance** function. The previous average calculation time was >150ms and was reduced to average <5ms.

### Exercise 4 (10 points)

For this exercise I added the implementations of the tests that were already defined for the UserController. Normally, if this was a production system, I would add more tests to have better coverage for specific exceptions and/ or edge cases, but in the context of this assignment, I limited my effort to implementing what was already there. I used Mockito and JUnit, which were used in the other tests.

### Exercise 5 (30 points)

I had to add the age property to the User model and implement the relevant functionality both in front end and backend.

- The first step was to create a DB migration. This was done by using **liquibase**, which was already configured in the project.

  I appended a changeSet in the already defined db changelog `/be-candidates/backEnd/src/main/resources/db/changelog/2022/02/19-01-changelog.xml` that alters the _client_user_ table by adding the age column (int), and then assigns a random age between 25 and 55 to all existing users.

  ```
      <changeSet id="1645272976866-17" author="topalidis">
          <sql>
              ALTER TABLE client_user ADD age int;
              UPDATE client_user SET age = FLOOR(RAND()*(55-25)+25);
          </sql>
      </changeSet>

  ```

- Second step was to add

  ```
          @Column(name = "age")
          private Integer age;
  ```

  to the User model and

  ```
  private Integer age;
  ```

  to the UserDTO class.

  I also had to add a value for the age parameter in the User constructor in the BaseTests class.

- Third step was to change the front end user model in **user.service.ts**

- Fourth step was to adjust the html of the edit-user component, in order to display the age for each user in the users list.

- Finally, I had to add an input field when editing or creating a new user, to be able to add a value for the age field. This input is of type number and the validators needed were added in **edit-user.component.ts** (required, min, max, regex patter to ensure only numbers are submitted).

### Extra Credit Exercise (Optional)

For the extra credit exercise, I had to add csv export functionality in the Account view with a button.

There was already an endpoint to get the transactions of a specific account, which supported pagination. I extended this endpoint by adding `produces = { "application/json", "text/csv" }` in:

```
  @GetMapping(value = "getAllForAccount", produces = { "application/json", "text/csv" })
    public ResponseEntity<?> getAllForAccount(@RequestParam(name = "sort", defaultValue = "date") String sort,
            @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            @RequestParam(name = "accountId", defaultValue = "") String accountId) {
        PageRequest pageRequest = PageRequest.of(page, size,
                direction.equals("asc") ? Sort.by(sort).ascending() : Sort.by(sort).descending());
        return ResponseEntity.ok(transactionsService.getAllForAccount(pageRequest, accountId));
    }
}
```

This instructs Spring MVC that this endpoint can produce either. Which one is selected in each request is specified by the content negotiation header **Accept**.

```
curl --location 'http://localhost:8080/api/transactions/getAllForAccount?accountId=2' \
--header 'Accept: text/csv'
```

will produce a csv response, while:

```
curl --location 'http://localhost:8080/api/transactions/getAllForAccount?accountId=2' \
--header 'Accept: application/json'
```

will produce a json response.

For Spring MVC to support this, some things should be done beforehand:

1. The WebConfig class should implement **WebMvcConfigurer**

2. We should add the supported media types for content negotiation in the configuration

```

   @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("json", MediaType.APPLICATION_JSON);
        configurer.mediaType("csv", new MediaType("text", "csv"));
    }
```

2. We should configure a message converter for text/csv content type. There is no built in message converter for this and I couldn't find an open source one from another developer, so I ended up implementing my own. You can find it in `/be-candidates/backEnd/main/java/com/dterz/converters/TransactionsCsvConverter.java`. I also needed to register my new converter in the configuration:

```
  @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
        messageConverters.add(new TransactionsCsvConverter());
    }
```

After those changes, the `GET /api/transactions/getAllForAccount` endpoint can produce either json or csv, based on the Accept header.

This introduced the need to add this header in the AccountController and TransactionsController test, in order to produce the json response needed there.

Now that I had the backend ready, I moved to creating the frontend. The frontend method that calls this endpoint is **getAllForAccount** in `transation.service.ts`. I added an optional param `accept` in it, which is used to configure the accept header and the response type of the request.
Then I added the **downloadTransactionsAsCsv** method in `account.component.ts`, which calls the **getAllForAccount** method, parses the response and downloads the csv file in the browser. This method was binded to the on click event listener of a button that I created in `account.component.html`. This button is responsible for downloading the csv file.


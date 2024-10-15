# parse-any-website

1. Open 2 terminals
  - on the first tab use command:
    
    ```
    mvn clean install && mvn spring-boot:run
    ```
  - on the second tab use command:
    
      ```
      ng serve
      ```
2. go to [http://localhost:4200](http://localhost:4200)
3. follow instructions on the screen.

#### NOTE: 3 Chrome windows will be opened. It is intended behaviour because 3 threads are used to scrap websites and in local environment Selenium is used in none-headless mode.

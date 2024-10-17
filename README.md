# parse-any-website

1. Open 2 terminals
  - on the first tab use command:
    
    ```
    mvn clean install &&  mvn spring-boot:run -Dspring-boot.run.arguments="--parser.threads-amnt=1"
    ```
    where "parser.threads-amnt" is amount of threads which will process pages of a website.
    
  - on the second tab use command:
    
      ```
      npm install && ng serve
      ```
2. go to [http://localhost:4200](http://localhost:4200)
3. follow instructions on the screen.

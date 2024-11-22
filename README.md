# parse-any-website

1. Open 2 terminals
  - on the first tab go to cd ../parser-project/parser and use command:
    
    ```
    mvn clean install &&  mvn spring-boot:run -Dspring-boot.run.arguments="--parser.threads-amnt=1"
    ```
    where "parser.threads-amnt" is amount of threads which will process pages of a website.
    
  - on the second tab go to cd../parser-project/websites-parser-FE and use command:
    
      ```
      npm install && ng serve
      ```
2. go to [http://localhost:4200](http://localhost:4200)
3. follow instructions on the screen.

The verified websites: [https://www.funda.nl/zoeken/huur](https://funda.nl/zoeken/huur?selected_area=%5B%22nl%22%5D), https://www.goodreads.com/list/show/3810.Best_Cozy_Mystery_Series?page=1, https://www.marktplaats.nl/cp/1/antiek-en-kunst/

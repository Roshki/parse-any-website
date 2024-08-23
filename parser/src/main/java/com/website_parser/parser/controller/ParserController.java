package com.website_parser.parser.controller;

import com.website_parser.parser.service.ParserService;
import com.website_parser.parser.service.SavingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ParserController {
    private final ParserService parserService;
    private final SavingService savingService;

    @PostMapping("/send-html")
    public String getHtml(@RequestBody String url) throws MalformedURLException {
        System.out.println("testtest");
        return parserService.getInitialHtmlFromUrl(url);
    }

    @PostMapping("/pagination-tag")
    public List<String> getAllPages(@RequestBody String paginationTag){
        return parserService.getHtmlOfAllPages(paginationTag);
    }

    @PostMapping("/last-page")
    public List<String> getAllPagesBasedOnLastPage(@RequestBody String lastPage) throws ExecutionException, InterruptedException {
        return parserService.getHtmlOfAllPagesBasedOnLastPage(lastPage);
    }

    @PostMapping("/get-info-url")
    public String getAllPagesBasedOnLastPage(@RequestBody Map<String, List<String>> map)  {
        System.out.println(map.size());
        // Print the map
        //map.forEach((key, value) -> System.out.println(key + " -> " + value));
        savingService.exportMapToExcel(map, "data_books.xlsx");
        return "success";
    }
}

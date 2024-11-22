package com.website_parser.parser.schedulers;

import com.website_parser.parser.model.Product;
import com.website_parser.parser.model.Type;
import com.website_parser.parser.model.User;
import com.website_parser.parser.repository.ProductRepository;
import com.website_parser.parser.repository.TypeRepository;
import com.website_parser.parser.repository.UserRepository;
import com.website_parser.parser.service.PaginationService;
import com.website_parser.parser.util.AddFeaturesUtil;
import com.website_parser.parser.util.HtmlContentUtil;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

@EnableAsync
@AllArgsConstructor
@Slf4j
@Component
@Profile("prod")
public class Schedulers {
    PaginationService paginationService;
    ProductRepository productRepository;
    UserRepository userRepository;
    TypeRepository typeRepository;

    @PostConstruct
    public void runOnStartup() throws ExecutionException, InterruptedException {
        log.info("hello there");
        syncData();
    }

    @Scheduled(cron = "0 0 0/3 * * ?")
    @Async
    public void syncData() throws ExecutionException, InterruptedException {
        List<String> pagesList = paginationService.getHtmlOfAllPagesBasedOnLastPage("https://www.marktplaats.nl/l/auto-s/tesla/p/2/#f:10882", "p/", "1", "40", "1", true);

        for (String page : pagesList) {
            ArrayList<String> titles = HtmlContentUtil.retrieveInfoHtml(page, ".hz-Listing-title");
            ArrayList<String> prices = HtmlContentUtil.retrieveInfoHtml(page, ".hz-Listing-price-extended-details");
            ArrayList<String> attr = HtmlContentUtil.retrieveInfoHtml(page, ".hz-Listing-attributes");

            log.info("titles size {}", titles.size());
            log.info("prices size {}", prices.size());
            List<String> updatedPrices = AddFeaturesUtil.getRegex(prices, "\\d{1,3}(\\.\\d{3})*(?=,-)");
            for (int i = 0; i < titles.size(); i++) {
                if (productRepository.findByTitleAndPrice(titles.get(i), updatedPrices.get(i)).isEmpty() && titles.size() == prices.size() && titles.size() == attr.size()) {
                    Optional<User> user = userRepository.findById(1);
                    Optional<Type> type = typeRepository.findById(1);
                    productRepository.save(Product.builder()
                            .title(titles.get(i))
                            .price(updatedPrices.get(i))
                            .attributes(attr.get(i))
                            .user(user.orElseThrow())
                            .type(type.orElseThrow())
                            .build());
                }
            }

        }
    }

    private static JSONObject getAttrJson(String year, String millage, String range) {
        HashMap<String, String> attrMap = new HashMap<>();
        attrMap.put("year", year);
        attrMap.put("millage", millage);
        attrMap.put("range", range);
        return new JSONObject(attrMap);
    }

}

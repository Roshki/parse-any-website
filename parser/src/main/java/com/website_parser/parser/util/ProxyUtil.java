package com.website_parser.parser.util;

import com.hazelcast.shaded.org.json.JSONArray;
import com.hazelcast.shaded.org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class ProxyUtil {

    public static Proxy getRandomProxy() {
        List<String> proxiesList = getProxyUrls();

        Random random = new Random();
        int randomIndex = random.nextInt(proxiesList.size());
        String randomProxy = proxiesList.get(randomIndex);
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(randomProxy);
        // proxy.setSslProxy("http://162.223.90.130:80");

        return proxy;
    }

    public static List<String> getProxyUrls() {
        List<String> proxiesList = new ArrayList<>();
        String json;
        try {
            json = UrlUtil.getContent("https://proxylist.geonode.com/api/proxy-list?protocols=http%2Chttps&filterUpTime=100&limit=500&page=1&sort_by=speed&sort_type=asc");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray dataArray = jsonObject.getJSONArray("data");

            for (int i = 0; i < 100; i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                String ip = dataObject.getString("ip");
                String port = dataObject.getString("port");
                JSONArray protocols = dataObject.getJSONArray("protocols");
                String proxy = protocols.get(0) + "://" + ip + ":" + port;
                //String proxy = ip + ":" + port;
                isReachable(ip, Integer.parseInt(port));
                proxiesList.add(proxy);
            }
        } catch (Exception e) {
           log.warn("not possible to fetch info");
        }
        return proxiesList;
    }

    public static void isReachable(String host, int port) {
        boolean reachable = false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1000);
            reachable = true;
        } catch (IOException e) {
            reachable = false;
        }
        System.out.println("Is host " + host + " on port " + port + " reachable? " + reachable);
    }
}
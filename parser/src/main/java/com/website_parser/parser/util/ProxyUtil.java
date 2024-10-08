package com.website_parser.parser.util;

import com.hazelcast.shaded.org.json.JSONArray;
import com.hazelcast.shaded.org.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class ProxyUtil {

    public static Proxy getProxy() {
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
                String proxyStr = protocols.get(0) + "://" + ip + ":" + port;
                if (isReachable(ip, Integer.parseInt(port))) {
                    System.out.println("reachable proxy is: " + proxyStr);
                    Proxy proxy = new Proxy();
                    proxy.setHttpProxy(proxyStr);
                    proxy.setSslProxy(proxyStr);
                    return proxy;
                }
            }
        } catch (Exception e) {
            log.warn("not possible to fetch info");
        }
        return null;
    }

    public static boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
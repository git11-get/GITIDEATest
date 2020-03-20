package com.atguigu.es.esdemo;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsDemoApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Test
    public void contextLoads() {

        System.out.println(jestClient);
    }

    @Test
    public void saveIndex() throws IOException {

        User user = new User();
        user.setEmail("123@qq.com");
        user.setUserName("heheii");
        Index index = new Index.Builder(user).index("user").type("info").build();
        DocumentResult documentResult = jestClient.execute(index);
        System.out.println("执行--"+documentResult.getId()+"---"+documentResult.getResponseCode());

    }



    @Test
    public void queryIndex() throws IOException {
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"match\": {\n" +
                "      \"userName.keyword\": \"heheii\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Search search = new Search.Builder(query).addIndex("user").addType("info").build();
        SearchResult searchResult = jestClient.execute(search);
        List<SearchResult.Hit<User, Void>> hits = searchResult.getHits(User.class);
        System.out.println("+++++++++"+searchResult.getTotal()+searchResult.getMaxScore());
        for (SearchResult.Hit<User, Void> hit : hits) {
            User user = hit.source;
            System.out.println("========="+user.getEmail());
            System.out.println("========="+user.getUserName());
        }
    }

    @Test
    public void updateIndex() throws IOException {
        String updates = "{\n" +
                "  \"doc\":{\"userName\":\"wowowowowo\"}\n" +
                "}";
        Update update = new Update.Builder(updates).index("user").type("info").id("AXDnANXHJHbnyXKH705X").build();
        DocumentResult execute = jestClient.execute(update);
        System.out.println("-----------------");
    }

    @Test
    public void deleteIndex() throws IOException {
        Delete delete = new Delete.Builder("AXDnANXHJHbnyXKH705X").index("user").type("info").build();
        DocumentResult execute = jestClient.execute(delete);
        System.out.println("响应的码：：："+execute.getResponseCode());
    }




}

class User{
    private String userName;
    private String email;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

package com.atguigu.gmall.search;

import com.atguigu.gmall.vo.search.SearchParam;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    private JestClient jestClient;

    @Autowired
    private SearchProductService searchProductService;

    @Test
    public void contextLoads() throws IOException {
        Search build = new Search.Builder("").addIndex("product").addType("info").build();
        SearchResult execute = jestClient.execute(build);
        System.out.println("总共有多少：--"+execute.getTotal());
    }


    @Test
    public void dslTest(){
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("手机");

        //String[] brand = {"苹果"};
        String[] brand = new String[]{"苹果"};
        searchParam.setBrand(brand);

        String[] cate = new String[]{"19","20"};
        searchParam.setCatelog3(cate);

        searchParam.setPriceFrom(5000);
        searchParam.setPriceTo(10000);

        String[] props = new String[]{"45:4.7","46:4G"};
        searchParam.setProps(props);

        searchProductService.searchProduct(searchParam);
    }






}

package com.dyqking.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallLlistServiceApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

        String query = "{\n" +
                "  \"query\":{\n" +
                "    \"match_all\": {}\n" +
                "  }\n" +
                "}";

        Search search = new Search.Builder(query)
                .addIndex("movie_chn")
                .addType("movie").build();

        SearchResult execute = jestClient.execute(search);
        List<SearchResult.Hit<HashMap, Void>> hits = execute.getHits(HashMap.class);
        for (SearchResult.Hit<HashMap, Void> hit : hits) {
            HashMap source = hit.source;
            System.err.println(source.get("name"));
        }
    }

}

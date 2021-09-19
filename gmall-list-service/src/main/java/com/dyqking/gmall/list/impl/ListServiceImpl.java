package com.dyqking.gmall.list.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.dyqking.gmall.bean.SkuLsInfo;
import com.dyqking.gmall.bean.SkuLsParams;
import com.dyqking.gmall.bean.SkuLsResult;
import com.dyqking.gmall.service.ListService;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.query.QuerySearchRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX = "gmall";

    public static final String ES_TYPE = "SkuInfo";


    @Override
    public void saveSkuInfo(SkuLsInfo skuLsInfo) {
        Index build = new Index.Builder(skuLsInfo)
                .index(ES_INDEX)
                .type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            DocumentResult result = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        String query = makeQueryStringForSearch(skuLsParams);

        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams, searchResult);

        return skuLsResult;
    }

    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult = new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);


        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if (hit.highlight != null && hit.highlight.size() > 0) {
                List<String> list = hit.highlight.get("skuName");
                String skuNameHL = list.get(0);
                skuLsInfo.setSkuName(skuNameHL);
            }
            skuLsInfoList.add(skuLsInfo);
        }
        skuLsResult.setSkuLsInfoList(skuLsInfoList);

        //        long total;
        skuLsResult.setTotal(searchResult.getTotal());

//        long totalPages;
        // 总页数  = (总条数 + 每页显示数 - 1) / 每页显示数
        long totalPage = (searchResult.getTotal() + skuLsParams.getPageSize() - 1) / skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPage);


//        List<String> attrValueIdList;
        ArrayList<String> attrValueIdList = new ArrayList<>();
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        if(groupby_attr!=null){
            List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                attrValueIdList.add(bucket.getKey());
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }

        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //关键字
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            MatchQueryBuilder ma = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(ma);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        //3级分类Id
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {

            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //设置属性值
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            for (int i = 0; i < skuLsParams.getValueId().length; i++) {
                String s = skuLsParams.getValueId()[i];
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", s);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        // from 从第几条开始， size 包含条数
        // from = （ 开始页 - 1 ） * 每页数据
        int from = (skuLsParams.getPageNo() - 1) * skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.out.println("query= " + query);
        return query;
    }
}

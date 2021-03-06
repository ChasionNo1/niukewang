package com.chasion.niukewang;

import com.alibaba.fastjson.JSONObject;
import com.chasion.niukewang.dao.DiscussPostMapper;
import com.chasion.niukewang.dao.elasticsearch.DiscussPostRepository;
import com.chasion.niukewang.entity.DiscussPost;
import jdk.nashorn.internal.objects.NativeString;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import javax.naming.directory.SearchResult;
import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName ElasticsearchTest
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/9 15:09
 */
@SpringBootTest
@ContextConfiguration(classes = NiukewangApplication.class)
public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void test1() {
        // ??????????????????
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(289));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(290));
    }

    @Test
    public void test2() {
        // ??????????????????
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void test3() {
        // ????????????
//        ?????????????????????
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("??????????????????????????????~");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void test4() {
        // ??????
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void test5() {
        // ??????
        // ??????????????????
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                // ????????????????????????
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // ????????????
                .withPageable(PageRequest.of(0, 10))
                // ????????????
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // ??????????????????????????????????????????????????????
        Page<DiscussPost> search = discussPostRepository.search(searchQuery);
        System.out.println(search.getTotalElements());
        System.out.println(search.getTotalPages());
        System.out.println(search.getNumber());
        for (DiscussPost post :
                search) {
            System.out.println(post);
        }
    }

    @Test
    public void test6() {
        // ??????????????????
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                // ????????????????????????
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // ????????????
                .withPageable(PageRequest.of(0, 10))
                // ????????????
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
//        elasticsearchRestTemplate.queryForPage(searchQuery, DiscussPost.class, IndexCoordinates.of())

    }

    @Test
    public void highlightQuery() throws Exception { //1.?????????????????? searchRequest
        //discusspost???????????????????????????
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //2.???????????? HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //?????????????????????????????????????????????
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        //??????????????????????????????html??????  ???????????????????????????????????????
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //3.?????????????????? searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("???????????????", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // ??????????????????????????? ??????????????????????????????
                .from(0).size(10)
                //????????????
                .highlighter(highlightBuilder);
        //4.???????????????????????????????????????
        searchRequest.source(searchSourceBuilder);
        //5.???????????????????????????
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            // ???????????????????????????
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                //title=<span style='color:red'>?????????</span>??????????????????...  }
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                //content=??????????????????<span style='color:red'>?????????</span>????????????...  }
                discussPost.setContent(contentField.getFragments()[0].toString());

            }
            System.out.println(discussPost);
            list.add(discussPost);
        }

    }

}

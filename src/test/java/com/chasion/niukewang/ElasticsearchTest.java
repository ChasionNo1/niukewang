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
        // 添加单个数据
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(289));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(290));
    }

    @Test
    public void test2() {
        // 添加多条数据
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
        // 更新数据
//        再次存储的方式
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人使劲灌水哈！~");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void test4() {
        // 删除
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void test5() {
        // 搜索
        // 构造查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 按照什么字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 设置分页
                .withPageable(PageRequest.of(0, 10))
                // 设置高亮
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 底层获取到了高亮显示的值，但没有返回
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
        // 构造查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 按照什么字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 设置分页
                .withPageable(PageRequest.of(0, 10))
                // 设置高亮
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
//        elasticsearchRestTemplate.queryForPage(searchQuery, DiscussPost.class, IndexCoordinates.of())

    }

    @Test
    public void highlightQuery() throws Exception { //1.创建搜索请求 searchRequest
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //2.配置高亮 HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //为哪些字段匹配到的内容设置高亮
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        //相当于把结果套了一点html标签  然后前端获取到数据就直接用
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //3.构建搜索条件 searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定从哪条开始查询 需要查出的总记录条数
                .from(0).size(10)
                //配置高亮
                .highlighter(highlightBuilder);
        //4.将搜索条件参数传入搜索请求
        searchRequest.source(searchSourceBuilder);
        //5.使用客户端发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            // 处理高亮显示的结果
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                //title=<span style='color:red'>互联网</span>求职暖春计划...  }
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                //content=它是最时尚的<span style='color:red'>互联网</span>公司之一...  }
                discussPost.setContent(contentField.getFragments()[0].toString());

            }
            System.out.println(discussPost);
            list.add(discussPost);
        }

    }

}

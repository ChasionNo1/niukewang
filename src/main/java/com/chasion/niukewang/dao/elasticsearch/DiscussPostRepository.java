package com.chasion.niukewang.dao.elasticsearch;

import com.chasion.niukewang.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @ClassName DiscussPostRepository
 * @Description TODO
 * @Author chasion
 * @Date 2022/7/9 15:06
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}

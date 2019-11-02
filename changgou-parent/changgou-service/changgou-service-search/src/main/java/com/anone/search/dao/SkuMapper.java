package com.anone.search.dao;

import com.anone.search.domain.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuMapper extends ElasticsearchRepository<SkuInfo,Integer> {
}

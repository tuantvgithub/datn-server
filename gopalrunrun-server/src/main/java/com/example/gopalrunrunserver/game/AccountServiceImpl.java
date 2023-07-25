//package com.example.gopalrunrunserver.services.impl;
//
//import com.example.gopalrunrunserver.models.db.DBAccount;
//import com.example.gopalrunrunserver.services.AccountService;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Nullable;
//import javax.annotation.PostConstruct;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//public class AccountServiceImpl implements AccountService {
//  private final MongoTemplate mongoTemplate;
//  private LoadingCache<String, DBAccount> mapDeviceId2Account;
//
//  @PostConstruct
//  private void init() {
//    mapDeviceId2Account = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
//        .build(new CacheLoader<String, DBAccount>() {
//          @Override
//          public DBAccount load(String deviceId) throws Exception {
//            return mongoTemplate.findOne(new Query(Criteria.where("deviceId").is(deviceId)), DBAccount.class);
//          }
//        });
//  }
//
//  @Override
//  @Nullable
//  public DBAccount getAccountByDeviceId(String deviceId) {
//    try {
//      return mapDeviceId2Account.get(deviceId);
//    } catch (Exception e) {
//      return null;
//    }
//  }
//
//  @Override
//  public void createNewAccount(String deviceId) {
//    final DBAccount dbAccount = new DBAccount();
//    dbAccount.setDeviceId(deviceId);
//    final DBAccount createdAccount = mongoTemplate.save(dbAccount);
//    mapDeviceId2Account.put(createdAccount.getDeviceId(), createdAccount);
//  }
//}

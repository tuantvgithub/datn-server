package com.example.gopalrunrunserver.services.impl;

import com.example.gopalrunrunserver.models.db.DBAccount;
import com.example.gopalrunrunserver.services.AccountService;
import com.example.gopalrunrunserver.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
  private final AccountService accountService;

  private final Map<String, String> mapSessionId2DeviceId = new HashMap<>();
  private static final int FAILED = 0;
  private static final int SUCCESS = 1;

  @Override
  public int auth(String sessionId, String deviceId) {
    try {
      final DBAccount dbAccount = accountService.getAccountByDeviceId(deviceId);
      if (dbAccount == null) {
        accountService.createNewAccount(deviceId);
      }
      mapSessionId2DeviceId.put(sessionId, deviceId);
      return SUCCESS;
    } catch (Exception e) {
      log.error("Failed to auth ", e);
      return FAILED;
    }
  }

  @Override
  public boolean isAuthSession(String sessionId) {
    return mapSessionId2DeviceId.containsKey(sessionId);
  }
}

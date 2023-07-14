package com.example.gopalrunrunserver.services;

import com.example.gopalrunrunserver.models.db.DBAccount;

public interface AccountService {
  DBAccount getAccountByDeviceId(String deviceId);
  void createNewAccount(String deviceId);
}

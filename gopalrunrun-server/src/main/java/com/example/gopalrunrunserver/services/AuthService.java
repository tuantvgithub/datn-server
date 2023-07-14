package com.example.gopalrunrunserver.services;

public interface AuthService {
  int auth(String sessionId, String deviceId);
  boolean isAuthSession(String sessionId);
}

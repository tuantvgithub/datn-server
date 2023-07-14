package com.example.gopalrunrunserver.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class GStringUtils {
  private final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private final Random random = new Random();

  public boolean isBlank(String str) {
    return str == null || str.length() < 1;
  }

  public boolean isNotBlank(String str) {
    return !isBlank(str);
  }

  public String random(int length) {
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      final int randomIndex = random.nextInt(CHARACTERS.length());
      final char randomChar = CHARACTERS.charAt(randomIndex);
      sb.append(randomChar);
    }
    return sb.toString();
  }
}

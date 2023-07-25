package com.example.gopalrunrunserver.models.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Document("accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DBAccount {
  private String id;
  private String deviceId;
  private long createdAt = System.currentTimeMillis();
}

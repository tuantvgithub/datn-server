package com.example.gopalrunrunserver.models.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DBAccount {
  @Id
  private String id;
  private String deviceId;
  private long createdAt = System.currentTimeMillis();
}

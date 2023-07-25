package com.example.gopalrunrunserver.models.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String sessionId;
    private int isSnail;
    private float posX;
    private float posY;
}

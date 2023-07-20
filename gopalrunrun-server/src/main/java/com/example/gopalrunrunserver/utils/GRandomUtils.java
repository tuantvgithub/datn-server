package com.example.gopalrunrunserver.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@UtilityClass
public class GRandomUtils {
    private final Random random = new Random();
    public static List<String> selectRandomStrings(List<String> stringList, int n) {
        if (stringList == null || stringList.isEmpty() || n <= 0 || n > stringList.size()) {
            return new ArrayList<>();
        }
        List<String> selectedStrings = new ArrayList<>();
        while (selectedStrings.size() < n) {
            int randomIndex = random.nextInt(stringList.size());
            String randomString = stringList.get(randomIndex);
            if (!selectedStrings.contains(randomString)) {
                selectedStrings.add(randomString);
            }
        }
        return selectedStrings;
    }

    public int generateRandomNumber(int n) {
        return random.nextInt(n + 1);
    }
}

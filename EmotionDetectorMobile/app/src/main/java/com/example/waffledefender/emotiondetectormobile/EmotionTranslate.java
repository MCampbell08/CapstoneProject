package com.example.waffledefender.emotiondetectormobile;

import java.util.ArrayList;

/**
 * Created by WaffleDefender on 11/4/2017.
 */

public class EmotionTranslate {
    private String currentHeartbeat = null;

    private ArrayList<Integer[]> emotionsNumbers = new ArrayList<>();
    private ArrayList<Integer> emotionsNumbersSum = new ArrayList<>();

    private Integer[] relaxedNumbers = {136, 94, 128, 117, 115};
    private Integer[] happyNumbers = {143, 135};
    private Integer[] excitedNumbers = {135, 145, 153};
    private Integer[] stressedNumbers = {172};

    private Integer relaxedAverage = 0;
    private Integer happyAverage = 0;
    private Integer excitedAverage = 0;
    private Integer stressedAverage = 0;

    EmotionTranslate(String heartbeatNum){
        currentHeartbeat = heartbeatNum;
    }

    public EmotionType translate(){
        EmotionType emotionType = null;
        Double currentHeartbeatNumDub = Double.parseDouble(currentHeartbeat);
        Integer currentHeartbeatNum = currentHeartbeatNumDub.intValue();
        averageNumbers();

        if(currentHeartbeatNum == relaxedAverage || (currentHeartbeatNum <= relaxedAverage + 5 && currentHeartbeatNum >= relaxedAverage - 5)){
            emotionType = EmotionType.Relaxed;
        }
        else if(currentHeartbeatNum == happyAverage || (currentHeartbeatNum <= happyAverage + 5 && currentHeartbeatNum >= happyAverage - 5)){
            emotionType = EmotionType.Happy;
        }
        else if(currentHeartbeatNum == excitedAverage || (currentHeartbeatNum <= excitedAverage + 5 && currentHeartbeatNum >= excitedAverage - 5)){
            emotionType = EmotionType.Excited;
        }
        else if(currentHeartbeatNum == stressedAverage || (currentHeartbeatNum <= stressedAverage + 5 && currentHeartbeatNum >= stressedAverage - 5)){
            emotionType = EmotionType.Stressed;
        }

        return emotionType;
    }

    private void averageNumbers(){
        addToArrays();

        for(int i = 0; i < emotionsNumbers.size(); i++){
            for(int j = 0; j < emotionsNumbers.get(i).length; j++){
                Integer[] tempNumbers = emotionsNumbers.get(i);
                emotionsNumbersSum.set(i, emotionsNumbersSum.get(i) + tempNumbers[j]);
            }
            switch(i){
                case 0:
                    relaxedAverage = emotionsNumbersSum.get(i) / emotionsNumbers.get(i).length;

                case 1:
                    happyAverage = emotionsNumbersSum.get(i) / emotionsNumbers.get(i).length;

                case 2:
                    excitedAverage = emotionsNumbersSum.get(i) / emotionsNumbers.get(i).length;

                case 3:
                    stressedAverage = emotionsNumbersSum.get(i) / emotionsNumbers.get(i).length;

            }
        }
    }
    private void addToArrays(){
        emotionsNumbers.add(relaxedNumbers);
        emotionsNumbers.add(happyNumbers);
        emotionsNumbers.add(excitedNumbers);
        emotionsNumbers.add(stressedNumbers);

        emotionsNumbersSum.add(relaxedAverage);
        emotionsNumbersSum.add(happyAverage);
        emotionsNumbersSum.add(excitedAverage);
        emotionsNumbersSum.add(stressedAverage);
    }
}

package com.example.waffledefender.emotiondetectormobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class ChartActivity extends AppCompatActivity {

    private static final int MAX_LINE_CHART_ENTRIES = 10;

    private static Set<String> heartRateValSet;
    private static Set<String> heartRateTimeSet;

    private static ArrayList<String> heartRateVal = new ArrayList<>();
    private static ArrayList<String> heartRateTime = new ArrayList<>();
    private static ArrayList<String> xLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        createLineChart();
    }

    private void createLineChart(){
        SharedPreferences preferences = getSharedPreferences("Preferences", 0);
        heartRateValSet = preferences.getStringSet("heartbeatValues", null);
        heartRateTimeSet = preferences.getStringSet("heartbeatTimeStamps", null);



        List<PointValue> values = new ArrayList<PointValue>();
        values.add(new PointValue(0, 2));
        values.add(new PointValue(1, 4));
        values.add(new PointValue(2, 3));
        values.add(new PointValue(3, 0));

        Line line = new Line(values).setColor(Color.RED).setCubic(false);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        LineChartView chart =(LineChartView)findViewById(R.id.lineChart);
        chart.setInteractive(true);
        chart.setLineChartData(data);

        placeHeartRatesInOrder();
        removeHeartRateIdentifiers();

//        addEntries();
//        addXLabels();

    }

    private void placeHeartRatesInOrder(){
        int counter = 0;
        while(counter < 10){
            for(String s : heartRateValSet){
                if(s.substring(s.length() - 1).equals(String.valueOf(counter))){
                    heartRateVal.add(s);
                }
            }
            for(String s : heartRateTimeSet){
                if(s.substring(s.length() - 1).equals(String.valueOf(counter))){
                    heartRateTime.add(s);
                }
            }
            counter++;
        }
    }

    private void removeHeartRateIdentifiers(){
        for(int i = 0; i < MAX_LINE_CHART_ENTRIES; i++){
            heartRateVal.set(i, heartRateVal.get(i).substring(0, heartRateVal.get(i).length() - 2));
            heartRateTime.set(i, heartRateTime.get(i).substring(0, heartRateTime.get(i).length() - 2));
        }
    }

    private void addEntries(){
    }

    private void addXLabels(){
    }
}

package com.example.waffledefender.emotiondetectormobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ChartActivity extends AppCompatActivity {

    private static final int MAX_LINE_CHART_ENTRIES = 10;

    private static Set<String> heartRateValSet;
    private static Set<String> heartRateTimeSet;

    private ArrayList<String> heartRateVal = new ArrayList<>();
    private ArrayList<String> heartRateTime = new ArrayList<>();

    private static LineChartData data = new LineChartData();

    private static Date date  = new Date();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        createLineChart();
        ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.chartLayout);
        layout.setOnTouchListener(new SwipingListener(this));
    }

    private void createLineChart(){
        SharedPreferences preferences = getSharedPreferences("Preferences", 0);
        heartRateValSet = preferences.getStringSet("heartbeatValues", null);
        heartRateTimeSet = preferences.getStringSet("heartbeatTimeStamps", null);

        data = new LineChartData();
        data.setBaseValue(Float.NEGATIVE_INFINITY);

        placeHeartRatesInOrder();
        removeHeartRateIdentifiers();

        addEntries();
        addXLabels();
        addYLabels();

        LineChartView chart =(LineChartView)findViewById(R.id.lineChart);

        if(checkSameValues()){
            final Viewport viewport = new Viewport(chart.getMaximumViewport());

            viewport.bottom = Float.parseFloat(heartRateVal.get(0)) - 1;
            viewport.top = Float.parseFloat(heartRateVal.get(0)) + 1;
            viewport.left = 0;
            viewport.right = 10;

            chart.setCurrentViewport(viewport);
            chart.setMaximumViewport(viewport);
            chart.setViewportCalculationEnabled(false);
        }
        chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        chart.setVisibility(View.VISIBLE);
        chart.setHorizontalScrollBarEnabled(true);
        chart.setZoomType(ZoomType.HORIZONTAL);
        chart.setInteractive(true);
        chart.setLineChartData(data);

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
        List<PointValue> values = new ArrayList<PointValue>();
        PointValue pointValue;

        Date date = new Date();
        Timestamp timestamp;

        for(int i = 0; i < MAX_LINE_CHART_ENTRIES; i++){
            timestamp = Timestamp.valueOf(heartRateTime.get(i));
            date.setTime(timestamp.getTime());

            pointValue = new PointValue(i, Float.parseFloat(heartRateVal.get(i)));
            pointValue.setLabel("Heartbeat: " + pointValue.getY() + " | Time: " + new SimpleDateFormat((checkSameDate() ? "hh:mm:ss a" : "MMM dd | hh:mm:ss a")).format(date));

            values.add(pointValue);
        }

        Line line = new Line(values).setColor(Color.RED).setCubic(false);
        line.setHasLabelsOnlyForSelected(true);

        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        data.setLines(lines);
    }

    private void addXLabels(){

        List<AxisValue> axisValues = new ArrayList<>();
        String axisValue;
        Date date = new Date();
        Timestamp timestamp;

        for(int i = 0; i < MAX_LINE_CHART_ENTRIES; i++){
            timestamp = Timestamp.valueOf(heartRateTime.get(i));
            date.setTime(timestamp.getTime());

            axisValue = (checkSameDate()) ? new SimpleDateFormat("hh:mm:ss a").format(date) : new SimpleDateFormat("MMM dd | hh:mm:ss a").format(date);
            axisValues.add(new AxisValue(i).setLabel(axisValue));
        }

        Axis xAxis = new Axis(axisValues).setHasTiltedLabels(false);

        xAxis.setName((checkSameDate()) ? "Time Recorded as of " + new SimpleDateFormat("MMM dd").format(date) : "Time Recorded");
        xAxis.setTextColor(Color.BLACK);
        xAxis.setMaxLabelChars((checkSameDate()) ? 6 : 9);

        data.setAxisXBottom(xAxis);
    }

    private void addYLabels(){

        Axis yAxis = new Axis().setHasLines(true).setHasTiltedLabels(true);

        yAxis.setName("Heartbeat");
        yAxis.setTextColor(Color.BLACK);

        data.setAxisYLeft(yAxis);
    }

    private boolean checkSameDate(){
        String previousDate = "";
        String currentDate = "";
        Date date = new Date();
        Timestamp timestamp = null;

        for(int i = 0; i < MAX_LINE_CHART_ENTRIES; i++){
            if(!currentDate.equals(previousDate) && !previousDate.equals("")){
                return false;
            }
            previousDate = currentDate;
            timestamp = Timestamp.valueOf(heartRateTime.get(i));
            date.setTime(timestamp.getTime());
            currentDate = new SimpleDateFormat("MMM dd").format(date);
        }

        return true;
    }

    private boolean checkSameValues(){
        float currentVal = 0f;
        float previousVal = 0f;
        for(int i = 0; i < MAX_LINE_CHART_ENTRIES; i++ ){
            if(currentVal != previousVal && previousVal != 0){
                return false;
            }
            previousVal = currentVal;
            currentVal = Float.parseFloat(heartRateVal.get(i));
        }
        return true;
    }
}

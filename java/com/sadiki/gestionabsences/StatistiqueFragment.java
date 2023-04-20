package com.sadiki.gestionabsences;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sadiki.gestionabsences.Firebase.FirebaseHelper;
import com.sadiki.gestionabsences.Model.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatistiqueFragment extends Fragment {
    Group group;
    BarChart chart;
    ArrayList<String> days = new ArrayList<>();
    Map<Integer, float[]> list = new HashMap<>();
    float present, absent;
    int i = 0;

    public StatistiqueFragment() {
    }

    public StatistiqueFragment(Group group) {
        this.group = group;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistique, container, false);
        chart = view.findViewById(R.id.chart);
        setChart();
        return view;
    }

    private void setChart() {
        int[] colorsData = new int[]{getResources().getColor(R.color.light_green), getResources().getColor(R.color.light_red)};
        BarDataSet barDataSet = new BarDataSet(dataValues(), "");
        barDataSet.setColors(colorsData);
        barDataSet.setStackLabels(new String[]{"Present", "Absent"});
        barDataSet.setBarBorderColor(Color.BLACK);
        barDataSet.setBarBorderWidth(2);

        BarData barData = new BarData(barDataSet);
        chart.setData(barData);
        chart.getDescription().setText("Présence par jour");

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        chart.invalidate();
    }

    private void getData() {
        //Get data from Firebase
        FirebaseHelper db = FirebaseHelper.getInstance();
        CollectionReference absencesRef = db.getCollectionReference("groupes").document(group.getIdGroup()).collection("absence");
        absencesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                        String documentId = documentSnapshot.getId();
                        Map<String, Object> data = documentSnapshot.getData();
                        // Recevoire ID Document qui est sou frome date (142023)
                        //et le transformer a 1/4 pour representer les jour dans le chart
                        String day = documentId.subSequence(0, 1) + "/" + documentId.subSequence(1, 2);
                        days.add(day);
                        present = 0;
                        absent = 0;
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            Object value = entry.getValue();
                            if (value.toString().contains("P"))
                                present++;
                            else
                                absent++;
                        }
                        list.put(i, new float[]{present, absent});
                        i++;
                    }
                    setChart();
                }
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });
    }
    private ArrayList<BarEntry> dataValues() {
        ArrayList<BarEntry> data = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            data.add(new BarEntry((float) i, list.get(i)));
        }
        return data;
    }
}
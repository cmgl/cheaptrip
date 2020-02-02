package com.example.cheaptrip.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cheaptrip.R;
import com.example.cheaptrip.models.TripGasStation;
import com.example.cheaptrip.models.tankerkoenig.OpeningTime;
import com.example.cheaptrip.models.tankerkoenig.Period;

import java.util.List;


public class CalcGasStationFragment extends Fragment {
    private TripGasStation mTripGasStation;         // Gas station of the route

    private TextView mTextViewTitle;                // Textview displaying the title
    private TextView mTextViewPrices;               // TextView displaying the prices
    private TextView mTextViewLocation;          // TextView displaying the location info
    private TextView mTextViewBrand;
    private TextView mTextViewOpening;              // TextView for Opening Times

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_calc_gas, container, false);


        mTextViewTitle = rootView.findViewById(R.id.tv_gasstation_title);
        mTextViewPrices = rootView.findViewById(R.id.tv_gasstation_prices);
        mTextViewLocation= rootView.findViewById(R.id.tv_gasstation_location);
        mTextViewBrand = rootView.findViewById(R.id.tv_gasstation_brand);
        mTextViewOpening = rootView.findViewById(R.id.tv_gasstation_opening);

        if(mTripGasStation != null){
            String textToView = generateText(mTripGasStation);
            mTextViewTitle.setText(textToView);
        }else{
            mTextViewTitle.setText("Chose a Route to display a Gas Station");
        }

        rootView.invalidate();

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if(mTextViewTitle != null && mTextViewLocation != null && mTextViewPrices !=  null
                &&  mTextViewBrand != null&&  mTripGasStation != null){
            setGasStationInfo(mTripGasStation);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mTextViewTitle = view.findViewById(R.id.tv_gasstation_title);
        mTextViewPrices = view.findViewById(R.id.tv_gasstation_prices);
        mTextViewLocation= view.findViewById(R.id.tv_gasstation_location);
        mTextViewBrand = view.findViewById(R.id.tv_gasstation_brand);

        if(mTripGasStation != null){
          setGasStationInfo(mTripGasStation);
        }else{
            mTextViewTitle.setText("Chose a Route to display a Gas Station");
        }
        view.invalidate();
    }

    public void setGasStationInfo(TripGasStation tripGasStation){
        mTripGasStation = tripGasStation;

        String city = tripGasStation.getCity();
        String street = tripGasStation.getStreet();
        String housNumber = tripGasStation.getHousenumber();
        String postCode = tripGasStation.getPostcode();

        String brand = tripGasStation.getBrand();
        String locationName = tripGasStation.getName();

        double e5 = tripGasStation.getPriceE5();
        double e10 = tripGasStation.getPriceE10();
        double diesel = tripGasStation.getPriceDiesel();


        String strE5 = (e5 == 0) ? "n/a" : "" + e5;
        String strE10 = (e10 == 0) ? "n/a" : "" + e10;
        String strDiesel = (diesel == 0) ? "n/a" : "" + diesel;

        String title = locationName;
        String prices = String.format("Prices:\n\tE5: %s\n\tE10: %s\n\tDiesel %s", strE5,strE10,strDiesel);
        String address = String.format("LocationInfo:\n\t%s %s\n\t%s %s", postCode,city,street,housNumber);

        if(mTextViewBrand != null){
            mTextViewBrand.setText(brand);
        }

        if(mTextViewTitle != null){
            mTextViewTitle.setText(title);
            mTextViewTitle.invalidate();
        }

        if(mTextViewPrices != null){
            mTextViewPrices.setText(prices);
            mTextViewPrices.invalidate();
        }

        if(mTextViewLocation != null){
            mTextViewLocation.setText(address);
        }

        List<OpeningTime> openingTimes = tripGasStation.getOpeningTimes().getOpeningTimes();

        String strOpeningTimes = "";

        if(openingTimes == null ||openingTimes.isEmpty()){
            strOpeningTimes = "n/a";
        }else{

            for(Period period :openingTimes.get(0).getPeriods()){
                strOpeningTimes += period.getStartp() + " - " + period.getEndp() + "\n";
            }
        }



        if(mTextViewOpening != null){
            mTextViewOpening.setText(strOpeningTimes);
        }

    }


    private String generateText(TripGasStation tripGasStation){
        String city = tripGasStation.getName();
        String street = tripGasStation.getStreet();
        String housNumber = tripGasStation.getHousenumber();
        String postCode = tripGasStation.getPostcode();

        String brand = tripGasStation.getBrand();
        String locationName = tripGasStation.getLocationName();

        double e5 = tripGasStation.getPriceE5();
        double e10 = tripGasStation.getPriceE10();
        double diesel = tripGasStation.getPriceDiesel();

        String result = String.format("%s\n\nPrices:\n\tE5: %f\n\tE10: %f\n\tDiesel %f\n\nLocationInfo:\n\t%s %s\n\t%s %s\n\t%s",brand,e5,e10,diesel,postCode,city,street,housNumber,locationName);

        return result;
    }
}

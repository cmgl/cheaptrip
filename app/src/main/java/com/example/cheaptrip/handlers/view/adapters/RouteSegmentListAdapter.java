package com.example.cheaptrip.handlers.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.cheaptrip.R;
import com.example.cheaptrip.models.TripRoute;
import com.example.cheaptrip.models.orservice.Segment;
import com.example.cheaptrip.models.orservice.Step;

import java.text.DecimalFormat;
import java.util.List;

public class RouteSegmentListAdapter extends BaseExpandableListAdapter {
    private TripRoute mTripRoute;
    private Context context;
    private List<Segment> segmentList;

    public RouteSegmentListAdapter(Context context, TripRoute tripRoute) {
        this.context = context;
        mTripRoute = tripRoute;
        this.segmentList = tripRoute.getRouteSegments();

    }

    public void updateList(TripRoute tripRoute){
        if(tripRoute == null){
            Log.w("CHEAPTRIP", "Cannot update Segment list: tripRoute is null");
            return;
        }

        List<Segment> segmentList = tripRoute.getRouteSegments();

        if(segmentList == null || segmentList.isEmpty()){
            Log.w("CHEAPTRIP", "Cannot update Segment list: list null or empty");
            return;
        }

        this.mTripRoute = tripRoute;
        this.segmentList = segmentList;
    }

    @Override
    public Step getChild(int listPosition, int expandedListPosition) {
        return segmentList.get(listPosition).getSteps().get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String strWayDescription = getChild(listPosition,expandedListPosition).getInstruction();
        double distance = getChild(listPosition,expandedListPosition).getDistance();
        double duration = getChild(listPosition,expandedListPosition).getDuration();

        /*=============================================
         * Inflate the subItem (way description)
         *=============================================*/
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_row_way_description, null);
        }

        /*=============================================
         * Set the textViews
         *=============================================*/
        TextView txtViewWayDescription = (TextView) convertView.findViewById(R.id.tv_way_decription);
        txtViewWayDescription.setTypeface(null, Typeface.BOLD);
        txtViewWayDescription.setText(strWayDescription);


        DecimalFormat decimalFormat = new DecimalFormat(".#");

        String strDistance;

        if(distance < 3){
            distance = distance * 1000;
            strDistance = (int) distance + " m";
        }else{
            strDistance = decimalFormat.format(distance) + " km";
        }

        double hours = duration/3600;
        double mins =  (hours - (int)hours )*60;
        double  secs =  (mins - (int)mins) * 60;

        String strTime = "";

        if((int)hours != 0){
            strTime += (int)hours + " h ";
        }

        if((int)mins != 0){
            strTime += (int)mins + " m ";
        }

        strTime += (int)secs + " s";



        TextView tvProperties = convertView.findViewById(R.id.tv_route_properties);

        String shownText = strDistance+"\n"+strTime;
        tvProperties.setText(shownText);

        return convertView;
    }



    @Override
    public int getChildrenCount(int listPosition) {
        return segmentList.get(listPosition).getSteps().size();
    }

    @Override
    public Segment getGroup(int listPosition) {
        return segmentList.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.segmentList.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = mTripRoute.getStops().get(listPosition+1).getInfoWindowText();

        double distance = segmentList.get(listPosition).getDistance();
        double duration = segmentList.get(listPosition).getDuration();


        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_row_route_segmet, null);
        }
        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);

        DecimalFormat decimalFormat = new DecimalFormat(".#");
        String strDistance;

        if(distance < 3){
            distance = distance * 1000;
            strDistance = (int) distance + " m";
        }else{
            strDistance = decimalFormat.format(distance) + " km";
        }

        double hours = duration/3600;
        double mins =  (hours - (int)hours )*60;
        double  secs =  (mins - (int)mins) * 60;

        String strTime = "";

        if((int)hours != 0){
            strTime += (int)hours + " h";
        }

        if((int)mins != 0){
            strTime += (int)mins + " m";
        }

        strTime += (int)secs + " s";

        TextView tvProperties = convertView.findViewById(R.id.tv_route_properties);

        String shownText = strDistance+"\n"+strTime;
        tvProperties.setText(shownText);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}

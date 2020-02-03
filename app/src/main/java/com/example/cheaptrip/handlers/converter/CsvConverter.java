package com.example.cheaptrip.handlers.converter;

import android.icu.text.Edits;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;


import com.example.cheaptrip.models.tankerkoenig.Station;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import java.lang.reflect.Type;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.ResponseBody;
import retrofit2.Converter;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * Converter Class that converts CSV Strings into Java Objects.
 * This is done by reading the annotations of Class.
 *
 * This will be Used by the {@link MultiConverterFactory} which takes care of the Annotations.
 *
 * @param <ReferencedClass> The class a CSV-Row gets converted to
 */
public class CsvConverter<ReferencedClass> implements Converter<ResponseBody,List<ReferencedClass>> {

    private Class<ReferencedClass> myClass;     // Class the CSV-String gets converted to

    /**
     * Constructor
     */
    public CsvConverter(Class<ReferencedClass> myClass){
        this.myClass = myClass;
    }

    /**
     * Converts a Retrfofit {@link ResponseBody} to a Java Object.
     *
     * @param responseBody {@link ResponseBody} of a API-Call
     * @return              A List of Java Objects (= CSV Entries)
     * @throws IOException
     */
    @Nullable
    @Override
    public List<ReferencedClass> convert(ResponseBody responseBody) throws IOException {
        String body = responseBody.string();
        return convert(body);
    }


    /**
     * Coverts a CSV-String to a List of Objects(class ReferencedClass).
     *
     * A row of the CSV-String is a Referenced Class Object.
     *
     * This is done by Iterating over the Annotations (SerializedName) and comparing them to
     * the header of the CSV (first line) and the DataSet's Postition (in order to map it to the header).
     *
     * @param body  CSV-String to be converted
     * @return      List of ReferencedClass-Object (containing the rows of the CSV as Java-Objects)
     * @throws IOException
     */
    public  List<ReferencedClass> convert(String body) throws IOException {
        List<ReferencedClass> classList = new ArrayList<>();

        if (body == null || body.length() <= 0){
            return classList;
        }

        BufferedReader reader = new BufferedReader(new StringReader(body));
        /*=======================================================================================
         * Read the Header of the CSV (this is referenced by Annotation SerializedName)
         * and the position will be used to identify the type of the csv rows
         *=======================================================================================*/
        String strHeaders = reader.readLine();
        String [] arrHeaders = strHeaders.split(",");

        List<String> headerList = Arrays.asList(strHeaders.split(","));

        // List holding the name and type of a field ( added in same order as the headers)
        HashMap<String,Pair<String,Type>> fieldPropertyMap = generateFieldNameList(headerList);
        /*=======================================================================================
         * Read the CSV-Lines into a ReferencedClass Object
         *=======================================================================================*/
        String currLine;


        while((currLine = reader.readLine()) != null) {
            currLine = currLine.replace("\"\"","\\\"");     // replace double quotes with escaped quote
            String []arrCurrList = currLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            ReferencedClass referencedObject = null;


            try {
                referencedObject = myClass.newInstance();
            } catch (Exception e) {
                Log.e("CHEAPTRIP", "Error initializing Object referencedClass: " + e.getStackTrace());
                return null;
            }
            /*======================================================================
             * Get the Field of the Referenced Class and get the Annotations
             *======================================================================*/


            for(int i =0 ; i< arrHeaders.length-1;i++){
                String value = arrCurrList[i];
                String header = headerList.get(i);

                if(!fieldPropertyMap.containsKey(header)){
                    continue;
                }

                Pair<String,Type> pair = fieldPropertyMap.get(header);

                String fieldName = pair.first;
                Type varType =  pair.second;


                Field field;
                try {
                    field = myClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    Log.e("CHEAPTRIP", "CSV-Converter:   Could not get field for given name: " + e.getLocalizedMessage());
                    return null;
                }

                field.setAccessible(true);
                /*=============================================================
                 * Set the Field of the fresh Created ReferencedClass instance
                 * by getting the Value of the CSV.
                 * Handle Format Exceptions from CSV-String Value to Field Value
                 *==============================================================*/
                try {
                    if (varType == Double.class || varType == double.class) {
                        Double fieldDouble = Double.parseDouble(value);
                        field.set(referencedObject, fieldDouble);
                    } else if (varType == String.class) {
                        field.set(referencedObject, value);
                    } else if (varType == Integer.class || varType == int.class) {
                        Integer fieldInteger = Integer.parseInt(value);
                        field.set(referencedObject, fieldInteger);
                    } else if (varType == Float.class || varType == float.class) {
                        Float fieldFloat = Float.parseFloat(value);
                        field.set(referencedObject, fieldFloat);
                    } else if (varType == Short.class || varType == short.class) {
                        Short fieldShort = Short.parseShort(value);
                        field.set(referencedObject, fieldShort);
                    } else if (varType == Long.class || varType == long.class) {
                        Long fieldLong = Long.parseLong(value);
                        field.set(referencedObject, fieldLong);
                    } else {
                        Log.e("CHEAPTRIP", "Class " + varType.toString() + " not supported");
                        return null;
                    }

                } catch (NumberFormatException e) {
                    Log.e("CHEAPTRIP", "Could not parse Field: " + e.getLocalizedMessage());
                    return null;
                } catch (IllegalAccessException e) {
                    Log.e("CHEAPTRIP", "Could set Field " + fieldName + ": " + e.getLocalizedMessage());
                    return null;
                }
            }



            /*    // Get the Index of the value with the header defined in the Annotation
             *//*===========================================================
             * Get the Annotation for specific Field
             *===========================================================*//*
                for (Annotation annotation : field.getDeclaredAnnotations()) {
                    if (annotation.annotationType() == SerializedName.class) {
                        for (int i = 0; i < headerList.length; i++) {
                            String header = headerList[i];
                            String value = arrCurrLine[i];

                            SerializedName serializedName = field.getAnnotation(SerializedName.class);

                            *//*==================================================
             * Compare the annotation with the header title
             *==================================================*//*
                            if (serializedName.value().compareTo(header) == 0) {
                                field.setAccessible(true);

                                if (referencedObject == null) {
                                    Log.e("CHEAPTRIP", "Empty Object referencedClass.");
                                    return null;
                                }
                                *//*=============================================================
             * Set the Field of the fresh Created ReferencedClass instance
             * by getting the Value of the CSV.
             * Handle Format Exceptions from CSV-String Value to Field Value
             *==============================================================*//*
                                try {
                                    if (varType == Double.class || varType == double.class) {
                                        Double fieldDouble = Double.parseDouble(value);
                                        field.set(referencedObject, fieldDouble);
                                    } else if (varType == String.class) {
                                        field.set(referencedObject, value);
                                    } else if (varType == Integer.class || varType == int.class) {
                                        Integer fieldInteger = Integer.parseInt(value);
                                        field.set(referencedObject, fieldInteger);
                                    } else if (varType == Float.class || varType == float.class) {
                                        Float fieldFloat = Float.parseFloat(value);
                                        field.set(referencedObject, fieldFloat);
                                    } else if (varType == Short.class || varType == short.class) {
                                        Short fieldShort = Short.parseShort(value);
                                        field.set(referencedObject, fieldShort);
                                    } else if (varType == Long.class || varType == long.class) {
                                        Long fieldLong = Long.parseLong(value);
                                        field.set(referencedObject, fieldLong);
                                    } else {
                                        Log.e("CHEAPTRIP", "Class " + varType.toString() + " not supported");
                                        return null;
                                    }

                                } catch (NumberFormatException e) {
                                    Log.e("CHEAPTRIP", "Could not parse Double: " + e.getStackTrace());
                                    return null;
                                } catch (IllegalAccessException e) {
                                    Log.e("CHEAPTRIP", "Could set Double Field " + varName + ": " + e.getStackTrace());
                                    return null;
                                }
                            }
                        }
                    }
                }*/

            /*======================================================================================
             * Add the Row ( = ReferencedObject Instance ) to the List to return
             *====================================================================================*/
            classList.add(referencedObject);
        }// End while readLine()
        return classList;
    }


   private HashMap<String,Pair<String,Type>> generateFieldNameList(List<String> headerList){

        HashMap<String,Pair<String,Type>> map = new HashMap<>();

        for(String header : headerList) {

            for (Field field : Station.class.getDeclaredFields()) {
                String varName = field.getName();
                Type varType = field.getType();

                Pair<String, Type> nameTypePair = new Pair<>(varName, varType);

                // Get the serialized name of the field with the annotation (if present)
                SerializedName serializedName;

                if (field.isAnnotationPresent(SerializedName.class)) {
                    serializedName = field.getAnnotation(SerializedName.class);
                } else {
                    continue;
                }

                if(serializedName.value().equals(header)){
                    map.put(header,nameTypePair);
                    continue;
                }

                for(String annotation : serializedName.alternate()){
                    if (annotation.equals(header)){
                        map.put(header,nameTypePair);
                        break;
                    }
                }
            }
        }
       return map;
   }

}

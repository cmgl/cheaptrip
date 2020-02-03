package com.example.cheaptrip.handlers.converter;


import com.example.cheaptrip.handlers.converter.annotations.Csv;
import com.example.cheaptrip.handlers.converter.annotations.Json;
import com.example.cheaptrip.handlers.converter.annotations.Raw;
import com.example.cheaptrip.handlers.converter.annotations.Xml;
import com.google.gson.GsonBuilder;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


/**
 * A Converter Factory for Converting REST-API Responses.
 *
 * Based on the annotation it selects the converter to be used to result in a desired format.
 * This is used by the retrofit API to deliver Java Objects of class <ReferencedClass>
 * instead of raw responses.
 *
 * Annotations:
 * -------------
 *      * @XML:     Uses an XML-Converter to deliver an Object of Referenced Class (see {@link Xml})
 *      * @JSON:    Uses an JSON-Converter(Gson) to deliver an Object of ReferencedClass    (see {@link Json}
 *      * @CSV:     Uses the @{@link CsvConverter} to parse CSV (see {@link Csv})
 *      * @RAW:     Delivers the raw response as String (see {@link Raw}
 *
 * @param <ReferencedClass>     Class if the Object to be created from the Responsebody of a REST-API call
 */
public class MultiConverterFactory<ReferencedClass> extends Converter.Factory {

    Class<ReferencedClass> myClass;         // Class to instatiate an object for

    /**
     * Constructor
     * @param myClass   Class of the Object to be Created
     */
    public MultiConverterFactory(Class myClass){
        this.myClass = myClass;
    }

    /**
     * Converts an Retrofit-ResponseBody to a Java Object.
     *
     * @param type          Type to pass to the Persister
     * @param annotations   Annotation to decide which type of response is given or raw for returning
     *                      the response raw (as String)
     * @param retrofit      The retfrofit client returning the Rest-APi response
     *
     * @return              A Converter-Factory for usage by retrofit(Based on the Annotation)
     */
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Xml.class) {
                return SimpleXmlConverterFactory.createNonStrict(
                        new Persister(new AnnotationStrategy())).responseBodyConverter(type, annotations, retrofit);
            }
            if (annotation.annotationType() == Json.class) {
                return GsonConverterFactory.create(new GsonBuilder().setLenient().excludeFieldsWithoutExposeAnnotation().create()).responseBodyConverter(type, annotations, retrofit);
            }

            if (annotation.annotationType() == Csv.class) {
                return new CsvConverter<ReferencedClass>(myClass);
            }

            if (annotation.annotationType() == Raw.class) {
                return ScalarsConverterFactory.create().responseBodyConverter(type,annotations,retrofit);
            }
        }
        return GsonConverterFactory.create(new GsonBuilder().setLenient().excludeFieldsWithoutExposeAnnotation().create()).responseBodyConverter(type, annotations, retrofit);
    }


    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return ScalarsConverterFactory.create().requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
//        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}


package com.example.myhomie;

import android.content.Context;

import java.util.Set;

public class DataLocalManager {
    private static final String DATA_FIREBASE = "DATA_FIREBASE";
    private static final String DATA_FIREBASE_ARRAY = "DATA_FIREBASE_ARRAY";
    private static final String DATA_SET = "DATA_SET";
    private static final String DATA_UPDATE = "DATA_UPDATE";
    private static DataLocalManager instance;
    private MySharedPreferences mySharedPreferences;

    public static void init(Context context){
        instance = new DataLocalManager();
        instance.mySharedPreferences = new MySharedPreferences(context);
    }

    public static DataLocalManager getInstance() {
        if (instance == null){
            instance = new DataLocalManager();
        }
        return instance;
    }

    public static void setDataFirebase(float value) {
        DataLocalManager.getInstance().mySharedPreferences.putFloatValue(DATA_FIREBASE,value);
    }

    public static float getDataFirebase() {
        return  DataLocalManager.getInstance().mySharedPreferences.getFloatValue(DATA_FIREBASE);
    }

    public static void setStringSetFirebase(Set<String> array) {
        DataLocalManager.getInstance().mySharedPreferences.putStringSetValue(DATA_FIREBASE_ARRAY,array);
    }

    public static Set<String> getStringSetFirebase() {
        return  DataLocalManager.getInstance().mySharedPreferences.getStringSetValue(DATA_FIREBASE_ARRAY);
    }

    public static void setIntData_Set(int value){
        DataLocalManager.getInstance().mySharedPreferences.putIntValue(DATA_SET,value);
    }

    public static int getIntData_Set(){
        return  DataLocalManager.getInstance().mySharedPreferences.getIntValue(DATA_SET);
    }

    public static void setIntData_Update(int value){
        DataLocalManager.getInstance().mySharedPreferences.putIntValue(DATA_UPDATE,value);
    }

    public static int getIntData_Update(){
        return  DataLocalManager.getInstance().mySharedPreferences.getIntValue(DATA_UPDATE);
    }
}

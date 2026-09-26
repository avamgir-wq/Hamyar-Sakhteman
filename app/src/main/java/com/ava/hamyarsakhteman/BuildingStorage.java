package com.ava.hamyarsakhteman;

import android.content.Context;
import android.content.SharedPreferences;

public class BuildingStorage {
    private final SharedPreferences pref;

    public BuildingStorage(Context context){
        pref = context.getSharedPreferences("building_data", Context.MODE_PRIVATE);
    }

    public void saveUnit(String unit,String owner,String phone,String parking,String storage,String waterMeter){
        pref.edit()
        .putString(unit+"_owner",owner)
        .putString(unit+"_phone",phone)
        .putString(unit+"_parking",parking)
        .putString(unit+"_storage",storage)
        .putString(unit+"_water",waterMeter)
        .apply();
    }

    public String get(String key){
        return pref.getString(key,"");
    }
}

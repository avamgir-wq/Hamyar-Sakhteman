package com.ava.hamyarsakhteman;

import java.util.ArrayList;
import java.util.List;

public class UnitRepository {
    public static List<Unit> units(){
        List<Unit> list = new ArrayList<>();
        for(int i=1;i<=12;i++){
            list.add(new Unit(i));
        }
        return list;
    }
}

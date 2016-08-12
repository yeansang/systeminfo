package com.nemustech.study.sysinfo;

/**
 * Created by cheolgyoon on 2016. 5. 11..
 */
public class InfoItem {
    public InfoItem() {
    }

    public InfoItem(String name, String value) {
        this.name = name;
        if(value == null){
            this.value = "";
        }else {
            this.value = value;
        }
    }

    String name;
    String value;
}

package com.apriorit.android.processmonitoring.database;

/**
 * Contain all getter and setter methods to maintain single application as an object.
 */
public class AppData {
    private int id;
    private String packageName;
    private String appName;

    public AppData(){
    }

    public AppData(int id, String package_name, String app_name){
        this.id = id;
        this.packageName = package_name;
        this.appName = app_name;
    }

    public AppData(String package_name, String app_name){
        this.packageName = package_name;
        this.appName = app_name;
    }

    public int getID(){
        return this.id;
    }

    public void setID(int id){
        this.id = id;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String name){
        this.packageName = name;
    }

    public String getAppName(){
        return this.appName;
    }

    public void setAppName(String app_name){
        this.appName = app_name;
    }
}

package com.apriorit.android.processmonitoring.device_management;

/**
 * Represents the data in component ListView,
 * which displays the list of applications other's device
 */
class AppDataModel {
    private String PackageName;
    private String AppName;
    private int isBlocked;

    AppDataModel(String PackageName, String name, int isBlocked) {
        this.PackageName = PackageName;
        this.AppName = name;
        this.isBlocked = isBlocked;
    }

    //Defines if application will be blocked on other's device
    void setAccess(int access) {
        isBlocked = access;
    }

    int getAccess() {
        return isBlocked;
    }

    String getAppName() {
        return AppName;
    }

    String getPackageName() {
        return PackageName;
    }
}

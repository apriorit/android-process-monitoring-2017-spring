package com.apriorit.android.processmonitoring.database;

import java.util.List;

/**
 * Defines CRUD(Create, Read, Update and Delete) operations
 */
public interface IDatabaseHandler {
    public void addApplicationData(AppData app);
    public AppData getApplication(int id);
    public List<AppData> getAllApps();
    public int updateApplication(AppData app);
    public int getCountOfBlockedApps();
    public void deleteApplicationData(AppData app);
    public void deleteAppByPackage(String packageName);
    public void deleteAll();
    public void clearBlacklist();
}


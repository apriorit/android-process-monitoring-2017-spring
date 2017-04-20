package com.apriorit.android.processmonitoring.database;

import java.util.List;

/**
 * Defines CRUD(Create, Read, Update and Delete) operations
 */
public interface IDatabaseHandler {
    public void addApplicationData(AppData contact);
    public AppData getApplication(int id);
    public List<AppData> getAllApps();
    public int updateApplication(AppData contact);
    public void deleteApplicationData(AppData contact);
    public void deleteAll();
    public void clearBlacklist();
}


package org.chungles.plugin;

public interface UIPlugin extends StandardPlugin
{
    public void addNode(String IP, String compname);
    public void removeNode(String IP, String compname);
    public void openPreferencesDialog();
}

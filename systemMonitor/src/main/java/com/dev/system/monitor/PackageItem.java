package com.dev.system.monitor;

import android.graphics.drawable.Drawable;

public class PackageItem
{
    private Drawable icon;
    private String name;
    private String packageName;
    private int memoryInKByte;
    
    String getPackageName()
    {
        return packageName;
    }

    void setPackageName(String packageName)
    {
        this.packageName=packageName;
    }

    String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name=name;
    }

    Drawable getIcon()
    {
        return icon;
    }

    void setIcon(Drawable icon)
    {
        this.icon=icon;
    }
    
    int getMemoryInKByte()
    {
    	return memoryInKByte;
    }
    
    void setMemoryInKByte(int memoryInKByte)
    {
    	this.memoryInKByte=memoryInKByte;
    }
}

/*
 * 文件名称:          ResManager.java
 *  
 * 编译器:            android2.2
 * 时间:              上午9:39:36
 */
package com.wxiwei.office.ss.sheetbar;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * TODO: 文件注释
 * <p>
 * <p>
 * Read版本:        Read V1.0
 * <p>
 * 作者:            jqin
 * <p>
 * 日期:            2012-8-27
 * <p>
 * 负责人:           jqin
 * <p>
 * 负责小组:           
 * <p>
 * <p>
 */
public class SheetbarResManager
{
    public SheetbarResManager(Context context)
    {
        this.context = context;
        
        //sheetbar background
        sheetbarBG = loadDrawable(SheetbarResConstant.RESNAME_SHEETBAR_BG);
        
        //shadow
        sheetbarLeftShadow = loadDrawable(SheetbarResConstant.RESNAME_SHEETBAR_SHADOW_LEFT);
        sheetbarRightShadow = loadDrawable(SheetbarResConstant.RESNAME_SHEETBAR_SHADOW_RIGHT);
        
        //hSeparator
        hSeparator = loadDrawable(SheetbarResConstant.RESNAME_SHEETBAR_SEPARATOR_H);
            
        //normal state
        normalLeft = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_LEFT);        
        normalRight = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_RIGHT);
        normalMiddle = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_MIDDLE);
        
        //push state
        pushLeft = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_LEFT);
        pushMiddle = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_MIDDLE);
        pushRight = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_PUSH_RIGHT);
        
        //focus state
        focusLeft = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_LEFT);
        focusMiddle = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_MIDDLE);
        focusRight = loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_FOCUS_RIGHT);
        
    }
    
    public Drawable getDrawable(short resID)
    {
        switch(resID)
        {
            case SheetbarResConstant.RESID_SHEETBAR_BG:
                return sheetbarBG;
                
            case SheetbarResConstant.RESID_SHEETBAR_SHADOW_LEFT:
                return sheetbarLeftShadow;
                
            case SheetbarResConstant.RESID_SHEETBAR_SHADOW_RIGHT:
                return sheetbarRightShadow;
                
            case SheetbarResConstant.RESID_SHEETBAR_SEPARATOR_H:
                return hSeparator;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_LEFT:
                return normalLeft;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_MIDDLE:
                return loadDrawable(SheetbarResConstant.RESNAME_SHEETBUTTON_NORMAL_MIDDLE);
                
            case SheetbarResConstant.RESID_SHEETBUTTON_NORMAL_RIGHT:
                return normalRight;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_LEFT:
                return pushLeft;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_MIDDLE:
                return pushMiddle;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_PUSH_RIGHT:
                return pushRight;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_LEFT:
                return focusLeft;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_MIDDLE:
                return focusMiddle;
                
            case SheetbarResConstant.RESID_SHEETBUTTON_FOCUS_RIGHT:
                return focusRight;
        }
        
        return null;
    }
    
    public void dispose()
    {
        sheetbarBG = null;
        
        sheetbarLeftShadow = null;
        sheetbarRightShadow = null;
        
        hSeparator = null;
        
        normalLeft = null;
        normalMiddle = null;
        normalRight = null;
        
        pushLeft = null;
        pushMiddle =  null;
        pushRight = null;
        
        focusLeft = null;
        focusMiddle = null;
        focusRight = null;
    }
    
    private Drawable loadDrawable(String assetPath)
    {
        try
        {
            InputStream inputStream = context.getAssets().open(assetPath);
            try
            {
                return Drawable.createFromStream(inputStream, assetPath);
            }
            finally
            {
                inputStream.close();
            }
        }
        catch (IOException e)
        {
            return null;
        }
    }
    
    private Context context;
    private Drawable sheetbarBG;
    
    private Drawable sheetbarLeftShadow, sheetbarRightShadow;
    
    private Drawable hSeparator;
    //left
    private Drawable normalLeft;
    private Drawable pushLeft;
    private Drawable focusLeft;
    
    //middle
    private Drawable normalMiddle;
    private Drawable pushMiddle;
    private Drawable focusMiddle;
    
    //right
    private Drawable normalRight;
    private Drawable pushRight;
    private Drawable focusRight;
}

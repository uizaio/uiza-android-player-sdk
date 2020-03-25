package com.uiza.sdk.utils;

import com.uiza.sdk.dialog.hq.UZItem;

import timber.log.Timber;

public class UZPlayerUtils {
    private UZPlayerUtils() {
    }

    //description
    //return SD, HD, FHD, QHD...
    public static UZItem.Format getFormatVideo(String description) {
        String format = UZItem.Format.F_UNKNOW;
        String profile = UZItem.Format.P_UNKNOW;
        if (description.contains(",")) {
            String resolution = description.split(",")[0];
            //LLog.d(TAG, "resolution " + resolution);
            if (resolution.contains(" ")) {
                String[] s = resolution.split(" ");
                if (s.length >= 3) {
                    String s0 = s[0];
                    String s1 = s[2];

                    int w;
                    int h;
                    try {
                        w = Integer.parseInt(s0);
                        h = Integer.parseInt(s1);
                    } catch (Exception e) {
                        Timber.e(e);
                        return new UZItem.Format();
                    }
                    if (w < h) {
                        w = h;
                    }
                    //set profile
                    //https://docs.google.com/spreadsheets/d/13lIsH711GJjttmZzFixph3RZwvP7a7vZhppSFnvsEl8/edit#gid=1297908801
                    if (w <= 480) {
                        profile = UZItem.Format.P_270;
                        format = UZItem.Format.F_SD;
                    } else if (w <= 640) {
                        profile = UZItem.Format.P_360;
                        format = UZItem.Format.F_SD;
                    } else if (w <= 854) {
                        profile = UZItem.Format.P_480;
                        format = UZItem.Format.F_SD;
                    } else if (w <= 1280) {
                        profile = UZItem.Format.P_720;
                        format = UZItem.Format.F_HD;
                    } else if (w <= 1920) {
                        profile = UZItem.Format.P_1080;
                        format = UZItem.Format.F_FHD;
                    } else if (w <= 2560) {
                        profile = UZItem.Format.P_1440;
                        format = UZItem.Format.F_2K;
                    } else if (w <= 3840) {
                        profile = UZItem.Format.P_2160;
                        format = UZItem.Format.F_4K;
                    } else {
                        profile = UZItem.Format.P_UNKNOW;
                        format = UZItem.Format.F_UNKNOW;
                    }
                }
            }
        }
        UZItem.Format f = new UZItem.Format();
        f.setFormat(format);
        f.setProfile(profile);
        return f;
    }

    //description
    //return SD, HD, FHD, QHD...
    public static UZItem.Format getFormatVideo(int width, int height) {
        String format;
        String profile;
        //set profile
        //https://docs.google.com/spreadsheets/d/13lIsH711GJjttmZzFixph3RZwvP7a7vZhppSFnvsEl8/edit#gid=1297908801
        if (width < height) width = height;
        if (width <= 480) {
            profile = UZItem.Format.P_270;
            format = UZItem.Format.F_SD;
        } else if (width <= 640) {
            profile = UZItem.Format.P_360;
            format = UZItem.Format.F_SD;
        } else if (width <= 854) {
            profile = UZItem.Format.P_480;
            format = UZItem.Format.F_SD;
        } else if (width <= 1280) {
            profile = UZItem.Format.P_720;
            format = UZItem.Format.F_HD;
        } else if (width <= 1920) {
            profile = UZItem.Format.P_1080;
            format = UZItem.Format.F_FHD;
        } else if (width <= 2560) {
            profile = UZItem.Format.P_1440;
            format = UZItem.Format.F_2K;
        } else if (width <= 3840) {
            profile = UZItem.Format.P_2160;
            format = UZItem.Format.F_4K;
        } else {
            profile = UZItem.Format.P_UNKNOW;
            format = UZItem.Format.F_UNKNOW;
        }
        UZItem.Format f = new UZItem.Format();
        f.setFormat(format);
        f.setProfile(profile);
        return f;
    }
}

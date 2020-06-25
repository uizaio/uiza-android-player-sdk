package com.uiza.sdk.exceptions;

public interface ErrorConstant {
    int ERR_CODE_0 = 0;
    String ERR_0 = "No internet connection.";

    int ERR_CODE_5 = 5;
    String ERR_5 = "LinkPlay cannot be null or empty.";

    int ERR_CODE_6 = 6;
    String ERR_6 = "Tried to play all linkplay of this entity, but failed.";

    int ERR_CODE_7 = 7;
    String ERR_7 = "Setup failed";

    int ERR_CODE_9 = 9;
    String ERR_9 = "You cannot change skin if player is playing ad.";

    int ERR_CODE_10 = 10;
    String ERR_10 = "Error getHQList null";

    int ERR_CODE_11 = 11;
    String ERR_11 = "Error audio null";

    int ERR_CODE_12 = 12;
    String ERR_12 = "Activity cannot be null";

    int ERR_CODE_13 = 13;
    String ERR_13 = "UZVideo cannot be null";

    int ERR_CODE_14 = 14;
    String ERR_14 = "You must init custom linkPlay first.";

    int ERR_CODE_15 = 15;
    String ERR_15 = "Context cannot be null.";

    int ERR_CODE_16 = 16;
    String ERR_16 = "Domain api cannot be null or empty";

    int ERR_CODE_17 = 17;
    String ERR_17 = "Token cannot be null or empty";

    int ERR_CODE_18 = 18;
    String ERR_18 = "AppID cannot be null or empty";

    int ERR_CODE_19 = 19;
    String ERR_19 = "Cannot use this feature at this time";

    int ERR_CODE_20 = 20;
    String ERR_20 = "Cannot chat messenger now";

    int ERR_CODE_22 = 22;
    String ERR_22 = "Cannot find Messenger App";

    int ERR_CODE_23 = 23;
    String ERR_23 = "Data of this entity is invalid";

    int ERR_CODE_24 = 24;
    String ERR_24 = "Error: Playback exception";

    int ERR_CODE_25 = 25;
    String ERR_25 = "This is the first item of playlist/folder";

    int ERR_CODE_26 = 26;
    String ERR_26 = "This is the first item of playlist/folder";

    int ERR_CODE_400 = 400;
    String ERR_400 = "Bad Request: The request was unacceptable, often due to missing a required parameter.";

    int ERR_CODE_401 = 401;
    String ERR_401 = "Unauthorized: No valid API key provided.";

    int ERR_CODE_404 = 404;
    String ERR_404 = "Not Found: The requested resource does not exist.";

    int ERR_CODE_422 = 422;
    String ERR_422 = "Unprocessable: The syntax of the request entity is incorrect (often is wrong parameter).";

    int ERR_CODE_500 = 500;
    String ERR_500 = "Internal Server Error: We had a problem with our server. Try again later.";

    int ERR_CODE_503 = 503;
    String ERR_503 = "Service Unavailable: The server is overloaded or down for maintenance.";

    int ERR_CODE_504 = 504;
    String ERR_504 = "Exo Player library is missing";

    int ERR_CODE_505 = 505;
    String ERR_505 = "Chromecast library is missing";

    int ERR_CODE_506 = 506;
    String ERR_506 = "IMA ads library is missing";
}

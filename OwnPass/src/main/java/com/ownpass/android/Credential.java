package com.ownpass.android;

/**
 * Created by leo on 10/26/13.
 */
public class Credential{
    public final String url;
    public final String username;
    public final int uuid;
    public final String password;

    //TODO: remove, just for testing
    public Credential(int uuid, String url, String username){
        this.uuid = uuid;
        this.url = url;
        this.username = username;
        this.password = "";
    }

    public Credential(int uuid, String url, String username, String password){
        this.uuid = uuid;
        this.url = url;
        this.username = username;
        this.password = password;
    }

}

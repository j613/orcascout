package com.orca.backend.server;

public class PostData{
    private final String PostData,
                         DataName;

    public PostData(String PostData, String DataName) {
        this.PostData = PostData;
        this.DataName = DataName;
    }

    public String getPostData() {
        return PostData;
    }

    public String getDataName() {
        return DataName;
    }
    /**
     * Get This! 
     */
    public PostData getThis(){
        return this;
    }
}
package com.ydd.yanshi.bean;

import com.ydd.yanshi.volley.Result;

/**
 * NEED
 */
public class UploadImageResult extends Result {

    /**
     * data : {"oUrl":"http://193.8.83.141 :8089/avatar/o/2/10000002.jpg","tUrl":"http://193.8.83.141 :8089/avatar/t/2/10000002.jpg"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * oUrl : http://193.8.83.141 :8089/avatar/o/2/10000002.jpg
         * tUrl : http://193.8.83.141 :8089/avatar/t/2/10000002.jpg
         */

        private String oUrl;
        private String tUrl;

        public String getOUrl() {
            return oUrl;
        }

        public void setOUrl(String oUrl) {
            this.oUrl = oUrl;
        }

        public String getTUrl() {
            return tUrl;
        }

        public void setTUrl(String tUrl) {
            this.tUrl = tUrl;
        }
    }
}

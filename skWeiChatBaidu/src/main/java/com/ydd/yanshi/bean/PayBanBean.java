package com.ydd.yanshi.bean;

public class PayBanBean {

      /**
       * currentTime : 1606909023544
       * data : {"tn":"502343534885303158701"}
       * resultCode : 1
       */

      private long currentTime;
      private DataBean data;
      private int resultCode;

      public long getCurrentTime() {
            return currentTime;
      }

      public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
      }

      public DataBean getData() {
            return data;
      }

      public void setData(DataBean data) {
            this.data = data;
      }

      public int getResultCode() {
            return resultCode;
      }

      public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
      }

      public static class DataBean {
            /**
             * tn : 502343534885303158701
             */

            private String tn;

            public String getTn() {
                  return tn;
            }

            public void setTn(String tn) {
                  this.tn = tn;
            }
      }
}

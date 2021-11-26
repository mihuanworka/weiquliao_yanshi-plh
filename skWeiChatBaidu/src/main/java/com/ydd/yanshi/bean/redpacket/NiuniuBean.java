package com.ydd.yanshi.bean.redpacket;

import java.util.List;

public class NiuniuBean {

      /**
       * currentTime : 1603173721580
       * data : {"code":100,"data":{"count":2,"greetings":"恭喜发财,万事如意","id":"5f8e7d597af6f328b9e0ef34","isLock":"0","money":30,"outTime":1603260121,"over":30,"password":"950328","receiveCount":0,"roomJid":"2431e66b87df4896a725dd36ba7d6200","sendTime":1603173721,"status":1,"toUserId":0,"type":2,"userId":10000107,"userIds":[],"userName":"秋窗风雨"}}
       * resultCode : 1
       */

      private long currentTime;
      private DataBeanX data;
      private int resultCode;

      public long getCurrentTime() {
            return currentTime;
      }

      public void setCurrentTime(long currentTime) {
            this.currentTime = currentTime;
      }

      public DataBeanX getData() {
            return data;
      }

      public void setData(DataBeanX data) {
            this.data = data;
      }

      public int getResultCode() {
            return resultCode;
      }

      public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
      }

      public static class DataBeanX {
            /**
             * code : 100
             * data : {"count":2,"greetings":"恭喜发财,万事如意","id":"5f8e7d597af6f328b9e0ef34","isLock":"0","money":30,"outTime":1603260121,"over":30,"password":"950328","receiveCount":0,"roomJid":"2431e66b87df4896a725dd36ba7d6200","sendTime":1603173721,"status":1,"toUserId":0,"type":2,"userId":10000107,"userIds":[],"userName":"秋窗风雨"}
             */

            private int code;
            private DataBean data;

            public int getCode() {
                  return code;
            }

            public void setCode(int code) {
                  this.code = code;
            }

            public DataBean getData() {
                  return data;
            }

            public void setData(DataBean data) {
                  this.data = data;
            }

            public static class DataBean {
                  /**
                   * count : 2
                   * greetings : 恭喜发财,万事如意
                   * id : 5f8e7d597af6f328b9e0ef34
                   * isLock : 0
                   * money : 30.0
                   * outTime : 1603260121
                   * over : 30.0
                   * password : 950328
                   * receiveCount : 0
                   * roomJid : 2431e66b87df4896a725dd36ba7d6200
                   * sendTime : 1603173721
                   * status : 1
                   * toUserId : 0
                   * type : 2
                   * userId : 10000107
                   * userIds : []
                   * userName : 秋窗风雨
                   */

                  private int count;
                  private String greetings;
                  private String id;
                  private String isLock;
                  private double money;
                  private int outTime;
                  private double over;
                  private String password;
                  private int receiveCount;
                  private String roomJid;
                  private int sendTime;
                  private int status;
                  private int toUserId;
                  private int type;
                  private int userId;
                  private String userName;

                  public String getUser_token() {
                        return user_token;
                  }

                  public void setUser_token(String user_token) {
                        this.user_token = user_token;
                  }

                  private String user_token;
                  private List<?> userIds;

                  public int getCount() {
                        return count;
                  }

                  public void setCount(int count) {
                        this.count = count;
                  }

                  public String getGreetings() {
                        return greetings;
                  }

                  public void setGreetings(String greetings) {
                        this.greetings = greetings;
                  }

                  public String getId() {
                        return id;
                  }

                  public void setId(String id) {
                        this.id = id;
                  }

                  public String getIsLock() {
                        return isLock;
                  }

                  public void setIsLock(String isLock) {
                        this.isLock = isLock;
                  }

                  public double getMoney() {
                        return money;
                  }

                  public void setMoney(double money) {
                        this.money = money;
                  }

                  public int getOutTime() {
                        return outTime;
                  }

                  public void setOutTime(int outTime) {
                        this.outTime = outTime;
                  }

                  public double getOver() {
                        return over;
                  }

                  public void setOver(double over) {
                        this.over = over;
                  }

                  public String getPassword() {
                        return password;
                  }

                  public void setPassword(String password) {
                        this.password = password;
                  }

                  public int getReceiveCount() {
                        return receiveCount;
                  }

                  public void setReceiveCount(int receiveCount) {
                        this.receiveCount = receiveCount;
                  }

                  public String getRoomJid() {
                        return roomJid;
                  }

                  public void setRoomJid(String roomJid) {
                        this.roomJid = roomJid;
                  }

                  public int getSendTime() {
                        return sendTime;
                  }

                  public void setSendTime(int sendTime) {
                        this.sendTime = sendTime;
                  }

                  public int getStatus() {
                        return status;
                  }

                  public void setStatus(int status) {
                        this.status = status;
                  }

                  public int getToUserId() {
                        return toUserId;
                  }

                  public void setToUserId(int toUserId) {
                        this.toUserId = toUserId;
                  }

                  public int getType() {
                        return type;
                  }

                  public void setType(int type) {
                        this.type = type;
                  }

                  public int getUserId() {
                        return userId;
                  }

                  public void setUserId(int userId) {
                        this.userId = userId;
                  }

                  public String getUserName() {
                        return userName;
                  }

                  public void setUserName(String userName) {
                        this.userName = userName;
                  }

                  public List<?> getUserIds() {
                        return userIds;
                  }

                  public void setUserIds(List<?> userIds) {
                        this.userIds = userIds;
                  }
            }
      }
}

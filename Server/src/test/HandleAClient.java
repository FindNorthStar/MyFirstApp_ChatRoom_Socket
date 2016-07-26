package test;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class serves one client
 * Created by FindNS@outlook.com on 2015/11/20.
 * @author Yue Zhao
 */
public class HandleAClient implements Runnable {
    //currentSocket maintains one client connection
    private Connect currentConnect;
    //username maintains the clientname
    private String userName;

    private Connection connection;

    private DataInputStream inputFromClient;

    private String receiveInfo;

    PrintWriter pw = null;

    //private DataOutputStream outputToClient;

    private boolean getConnectSendEstablish(String sendName){
        return this.getTargetConnect(sendName).getIsSendingEstablish();
    }

    /**
     * from username find connection
     * @param toClientName
     * @return
     */
    private Connect getTargetConnect(String toClientName) {
        for (Connect connect : Server.getConnects()) {
            if (connect.getUsername().equals(toClientName)) {
                return connect;
            }
        }
        return null;
    }

    /**
     * 检查离线消息
     * @param targetName
     * @return
     */
    private synchronized boolean checkOfflineMessage(String targetName) {
        try {
            boolean ans;
            Statement statement = connection.createStatement();
            String sql = "select * from offline where targetname = '" + targetName + "'";
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()){
                ans = true;
            }
            else{
                ans = false;
            }
            statement.close();
            return ans;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 发送离线消息
     * @param outputToClient
     */
    private synchronized void sendOfflineMessage(DataOutputStream outputToClient) {
        if (checkOfflineMessage(this.userName)){
            try {
                Statement statement1 = connection.createStatement();
                String sql = "select * from offline where targetname = '" + this.userName + "'";
                ResultSet rs = statement1.executeQuery(sql);

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.currentConnect.getSocket().getOutputStream())));

                //outputToClient = new DataOutputStream(this.currentConnect.getSocket().getOutputStream());

                while (rs.next()){
                    //send the message
                    //outputToClient.writeUTF(rs.getString("message") + "\n");

                    pw.println(rs.getString("message"));
                    pw.flush();

                }
                ///sql
                statement1.close();
                Statement statement2 = connection.createStatement();
                sql = "delete from offline where targetname = '" + this.userName + "'";
                statement2.executeUpdate(sql);
                statement2.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 保存离线消息
     * @param toClientName
     * @param toClientMessage
     */
    private synchronized void saveOfflineMessage(String toClientName, String toClientMessage) {
        try {
            Statement statement = connection.createStatement();
            String sql = "insert into offline values('" + toClientName + "','" + toClientMessage + "')";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * this is to find the friends with the user
     * @param nameToGetFriend
     * @return
     */
    private synchronized List<String> getFriend(String nameToGetFriend){
        List<String> friendName = new ArrayList<String>();
        try {
            Statement statement = connection.createStatement();
            String sql = "select friendname from friend WHERE username = '" + nameToGetFriend +"'";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                if (!friendName.contains(rs.getString("friendname"))) {
                    friendName.add(rs.getString("friendname"));
                }
            }
            sql = "select username from friend WHERE friendname = '" + nameToGetFriend +"'";
            rs = statement.executeQuery(sql);
            while (rs.next()){
                if (!friendName.contains(rs.getString("username"))) {
                    friendName.add(rs.getString("username"));
                }
            }
            statement.close();
            return friendName;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * test if the two users are friends
     * @param userName1
     * @param userName2
     * @return
     */
    private synchronized boolean isFriend(String userName1, String userName2){
        try {
            Statement statement = connection.createStatement();
            String sql = "select friendname from friend WHERE username = '" + userName1 +"'";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()){
                if (userName2.equals(rs.getString("friendname"))){
                    return true;
                }
            }
            sql = "select username from friend WHERE friendname = '" + userName1 +"'";
            rs = statement.executeQuery(sql);
            while (rs.next()){
                if (userName2.equals(rs.getString("username"))){
                    return true;
                }
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * make two users become friends
     * @param userName1
     * @param userName2
     */
    private synchronized void createFriend(String userName1, String userName2){
        if (this.isFriend(userName1,userName2)){
            return;
        }
        else{
            try {
                Statement statement = connection.createStatement();
                String sql = "insert into friend values('" + userName1 + "','" + userName2 + "')";
                statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private void deleteFriend(String sourceName, String targetName) {
        try {
            Statement statement = connection.createStatement();
            String sql = "delete from friend WHERE username = '" + sourceName +"' and friendname = '" + targetName + "'";
            statement.executeUpdate(sql);
            sql = "delete from friend WHERE username = '" + targetName +"' and friendname = '" + sourceName + "'";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * send message to a certain user or everyone
     * @param message
     * @param outputToClient
     * @throws IOException
     */
    private void sendMessage(String message[], DataOutputStream outputToClient) throws IOException {
        String toClientName = message[0].trim();
        String toClientMessage = message[1].trim();

        if (toClientName.equals("All")) {
            //for each established connection
            for (Connect connect : Server.getConnects()) {
                /*outputToClient = new DataOutputStream(connect.getSocket().getOutputStream());
                //send the message
                outputToClient.flush();
                outputToClient.writeUTF(toClientMessage + "\n");
                outputToClient.flush();*/

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connect.getSocket().getOutputStream())));
                pw.println(toClientMessage);
                pw.flush();

            }
        }
        else{
            //check if online
            if (!checkOnline(toClientName)){
                if (this.isFriend(this.userName,toClientName)){
                    /*outputToClient = new DataOutputStream(currentConnect.getSocket().getOutputStream());
                    outputToClient.writeUTF(toClientMessage + "\n");*/

                    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
                    pw.println(toClientMessage);
                    pw.flush();

                    saveOfflineMessage(toClientName,toClientMessage);
                }
            }
            else{
                //find a target client name
                //check if friends
                if (this.userName.equals(toClientName)){
                    /*outputToClient = new DataOutputStream(currentConnect.getSocket().getOutputStream());
                    outputToClient.writeUTF(toClientMessage + "\n");*/

                    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
                    pw.println(toClientMessage);
                    pw.flush();

                }
                else if (this.isFriend(this.userName,toClientName)){
                    /*outputToClient = new DataOutputStream(getTargetConnect(toClientName).getSocket().getOutputStream());
                    outputToClient.writeUTF(toClientMessage + "\n");
                    outputToClient = new DataOutputStream(currentConnect.getSocket().getOutputStream());
                    outputToClient.writeUTF(toClientMessage + "\n");*/

                    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(toClientName).getSocket().getOutputStream())));
                    pw.println(toClientMessage);
                    pw.flush();
                    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
                    pw.println(toClientMessage);
                    pw.flush();

                }
                else{
                    /*outputToClient = new DataOutputStream(currentConnect.getSocket().getOutputStream());
                    outputToClient.writeUTF("当前不是好友,消息无法发送\n");*/

                    pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
                    pw.println("当前不是好友,消息无法发送");
                    pw.flush();
                }
            }

        }
    }

    /**
     * check if target user online
     * @param toClientName
     * @return
     */
    private boolean checkOnline(String toClientName) {
        for (Connect connect : Server.getConnects()){
            if (toClientName.equals(connect.getUsername())){
                return true;
            }
        }
        return false;
    }

    /**
     * inform the two users that they are friends
     * @param userName1
     * @param userName2
     * @param outputToClient
     */
    private void updateFriend(String userName1, String userName2, DataOutputStream outputToClient){
        try {
            /*outputToClient = new DataOutputStream(getTargetConnect(userName1).getSocket().getOutputStream());
            outputToClient.writeUTF(userName2+"(好友)(在线) b9dded77d303219e5a92260272748e0e8e96d0e2");
            outputToClient = new DataOutputStream(getTargetConnect(userName2).getSocket().getOutputStream());
            outputToClient.writeUTF(userName1+"(好友)(在线) b9dded77d303219e5a92260272748e0e8e96d0e2");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(userName1).getSocket().getOutputStream())));
            pw.println(userName2+"(好友)(在线) b9dded77d303219e5a92260272748e0e8e96d0e2");
            pw.flush();
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(userName2).getSocket().getOutputStream())));
            pw.println(userName1+"(好友)(在线) b9dded77d303219e5a92260272748e0e8e96d0e2");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEstablishMessage(String sourceName, String targetName, DataOutputStream outputToClient){
        if (this.isFriend(sourceName,targetName)){//inform the two users that they are already friends
            try {
                /*outputToClient = new DataOutputStream(getTargetConnect(sourceName).getSocket().getOutputStream());
                outputToClient.writeUTF("你和"+targetName+"已经是好友了\n");
                outputToClient = new DataOutputStream(getTargetConnect(targetName).getSocket().getOutputStream());
                outputToClient.writeUTF("你和"+sourceName+"已经是好友了\n");*/

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceName).getSocket().getOutputStream())));
                pw.println("你和"+targetName+"已经是好友了");
                pw.flush();
                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(targetName).getSocket().getOutputStream())));
                pw.println("你和"+sourceName+"已经是好友了");
                pw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (this.getConnectSendEstablish(sourceName)){
            try {
                /*outputToClient = new DataOutputStream(getTargetConnect(sourceName).getSocket().getOutputStream());
                outputToClient.writeUTF("你已经发送过好友请求,请等待对方作出决定后方可继续发送\n");*///this is a establish message to target user

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceName).getSocket().getOutputStream())));
                pw.println("你已经发送过好友请求,请等待对方作出决定后方可继续发送");
                pw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {

                /*outputToClient = new DataOutputStream(getTargetConnect(targetName).getSocket().getOutputStream());
                outputToClient.writeUTF(sourceName+";"+targetName+" 5e9e7c4cfaefb1fa538d3f29e77ee2c026336923");*///this is a establish message to target user

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(targetName).getSocket().getOutputStream())));
                pw.println(sourceName+";"+targetName+" 5e9e7c4cfaefb1fa538d3f29e77ee2c026336923");
                pw.flush();
                this.getTargetConnect(sourceName).setSendingEstablish(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拒绝好友请求
     * @param message
     * @param outputToClient
     */
    private void declineFriend(String[] message, DataOutputStream outputToClient) {
        String userName1 = message[0].trim();
        String userName2 = message[1].replace(" bb4b38f45332ab0ed6a51e7c5c9e645bbcc079e7","").trim();
        try{
            /*outputToClient = new DataOutputStream(getTargetConnect(userName1).getSocket().getOutputStream());
            outputToClient.writeUTF("与"+userName2+"建立好友关系的请求已被拒绝" + "\n");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(userName1).getSocket().getOutputStream())));
            pw.println("与"+userName2+"建立好友关系的请求已被拒绝");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getTargetConnect(userName1).setSendingEstablish(false);
    }

    /**
     * 发送删除好友信息
     * @param sourceName
     * @param targetName
     * @param outputToClient
     */
    private void sendDeleteMessage(String sourceName, String targetName, DataOutputStream outputToClient) {
        if (!this.isFriend(sourceName,targetName)){
            //inform that the two users are not friends, cannot delete friend
            sendCannotDeleteMessage(sourceName,targetName,outputToClient);
        }
        else{
            //delete friend information in MySQL database
            deleteFriend(sourceName,targetName);
            //update the user list in both users
            updateDeleteFriendList(sourceName,targetName,outputToClient);
            //send user delete information to both users
            informDeleteMessage(sourceName,targetName,outputToClient);
        }

    }

    /**
     * 通知删除好友消息
     * @param sourceName
     * @param targetName
     * @param outputToClient
     */
    private void informDeleteMessage(String sourceName, String targetName, DataOutputStream outputToClient) {
        try {
            /*outputToClient = new DataOutputStream(getTargetConnect(sourceName).getSocket().getOutputStream());
            outputToClient.writeUTF("您与" + targetName + "的好友关系已经解除\n");
            outputToClient = new DataOutputStream(getTargetConnect(targetName).getSocket().getOutputStream());
            outputToClient.writeUTF("您与" + sourceName + "的好友关系已经解除\n");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceName).getSocket().getOutputStream())));
            pw.println("您与" + targetName + "的好友关系已经解除");
            pw.flush();
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(targetName).getSocket().getOutputStream())));
            pw.println("您与" + sourceName + "的好友关系已经解除");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新已删除的好友列表
     * @param sourceName
     * @param targetName
     * @param outputToClient
     */
    private void updateDeleteFriendList(String sourceName, String targetName, DataOutputStream outputToClient) {
        try {
            /*outputToClient = new DataOutputStream(getTargetConnect(sourceName).getSocket().getOutputStream());
            outputToClient.writeUTF(targetName+" 53ef052fa26d3fb8b9236de6e6e283e51b24a273");
            outputToClient = new DataOutputStream(getTargetConnect(targetName).getSocket().getOutputStream());
            outputToClient.writeUTF(sourceName+" 53ef052fa26d3fb8b9236de6e6e283e51b24a273");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceName).getSocket().getOutputStream())));
            pw.println(targetName+" 53ef052fa26d3fb8b9236de6e6e283e51b24a273");
            pw.flush();
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(targetName).getSocket().getOutputStream())));
            pw.println(sourceName+" 53ef052fa26d3fb8b9236de6e6e283e51b24a273");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送无法删除好友的消息
     * @param sourceName
     * @param targetName
     * @param outputToClient
     */
    private void sendCannotDeleteMessage(String sourceName, String targetName, DataOutputStream outputToClient) {
        try {
            /*outputToClient = new DataOutputStream(getTargetConnect(sourceName).getSocket().getOutputStream());
            outputToClient.writeUTF("你和"+targetName+"当前不是好友,无法删除好友\n");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceName).getSocket().getOutputStream())));
            pw.println("你和"+targetName+"当前不是好友,无法删除好友");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * create friend between two users and inform them
     * @param message
     * @param outputToClient
     */
    private void establishFriend(String[] message, DataOutputStream outputToClient) {
        String userName1 = message[0].trim();
        String userName2 = message[1].replace(" 769853e342838d691865164489e0fd88c217fbf8","").trim();
        //String toClientMessage = message[2].trim();
        //wait for user to confirm
        if(!isFriend(userName1,userName2)) {
            this.updateFriend(userName1, userName2, outputToClient);//update user lists
        }
        createFriend(userName1,userName2);
        try {
            /*outputToClient = new DataOutputStream(getTargetConnect(userName1).getSocket().getOutputStream());
            outputToClient.writeUTF("与"+userName2+"已建立好友关系" + "\n");
            outputToClient = new DataOutputStream(getTargetConnect(userName2).getSocket().getOutputStream());
            outputToClient.writeUTF("与"+userName1+"已建立好友关系" + "\n");*/

            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(userName1).getSocket().getOutputStream())));
            pw.println("与"+userName2+"已建立好友关系");
            pw.flush();
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(userName2).getSocket().getOutputStream())));
            pw.println("与"+userName1+"已建立好友关系");
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getTargetConnect(userName1).setSendingEstablish(false);
    }

    /**
     * update userlists in Jcombobox if someone enter the room
     * @param sourceUserName
     * @param outputToClient
     */
    public void sendUserLists(String sourceUserName,DataOutputStream outputToClient){
        String existUser = "";
        List<String> userFriends = this.getFriend(sourceUserName);//get the friend list from MySQL Database, may be null
        if (userFriends!=null || !userFriends.isEmpty()) {
            String userNameToAdd;
            for (Connect connect : Server.getConnects()) {//first add friends online and strangers online
                //problem
                userNameToAdd = connect.getUsername();
                if (isFriend(userNameToAdd, sourceUserName)) {
                    userFriends.remove(userNameToAdd);
                    userNameToAdd = userNameToAdd + "(好友)(在线)";//这里可能有问题
                }
                existUser = existUser + userNameToAdd + ";";
            }
            if (!userFriends.isEmpty()) {//second add the friend offline
                for (String userFriend : userFriends) {
                    userNameToAdd = userFriend + "(好友)(离线)";//这里可能有问题
                    existUser = existUser + userNameToAdd + ";";
                }
            }
        }
        else{
            for (Connect connect : Server.getConnects()) {
                //problem may cause
                String userNameToAdd = connect.getUsername();
                existUser = existUser + userNameToAdd + ";";
            }
        }
        if (!existUser.equals("")){
            try {
                /*outputToClient = new DataOutputStream(this.getTargetConnect(sourceUserName).getSocket().getOutputStream());
                outputToClient.writeUTF(existUser.substring(0,existUser.length()-1) + " af8fb5d8d7a247312c1262c6b46a59f51050e12d");*/

                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(sourceUserName).getSocket().getOutputStream())));
                pw.println(existUser.substring(0,existUser.length()-1) + " af8fb5d8d7a247312c1262c6b46a59f51050e12d");
                pw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getFriendListAsString(String sourceUserName){
        String friendList = "";
        List<String> list = this.getFriend(sourceUserName);//get the friend list from MySQL Database, may be null
        if (!list.isEmpty()) {
            for (String s : list) {
                friendList = friendList + s + ";";
            }
        }
        if (!friendList.equals("")){
            friendList = friendList.substring(0,friendList.length()-1);//remove the last ?comma?
        }
        return friendList;
    }

    public String getNotFriendUserAsString(){
        String notFriendUser = "";
        for (Connect connect : Server.getConnects()) {
            //problem may cause
            String userNameToAdd = connect.getUsername();
            //若用户名不是当前连接用户,二者不是好友
            if ((!userNameToAdd.equals(userName)) && (!isFriend(userName,userNameToAdd))) {
                notFriendUser = notFriendUser + userNameToAdd + ";";
            }
        }
        if (!notFriendUser.equals("")) {
            notFriendUser = notFriendUser.substring(0,notFriendUser.length()-1);
        }
        return notFriendUser;
    }

    public void enterRoomCast() throws IOException {
        //群聊点击后响应消息
        for (Connect connect : Server.getConnects()) {
            //DataOutputStream enterRoom = new DataOutputStream(connect.getSocket().getOutputStream());
            //send a message indicating the user enters the room
            //enterRoom.writeUTF(userName + " enters the room\n");
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connect.getSocket().getOutputStream())));
            pw.println("sys:"+userName+" enters the room messagepublic");
            pw.flush();
        }
    }

    public void sendPrivateMessage(String message[]) throws IOException {
        String toClientName = message[0].trim();
        String toClientMessage = message[1].trim();

        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getTargetConnect(toClientName).getSocket().getOutputStream())));
        pw.println(toClientMessage + " messageprivate");
        pw.flush();
        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
        pw.println(toClientMessage + " messageprivate");
        pw.flush();

    }


    public HandleAClient(Connect connect) {
        this.currentConnect = connect;
        this.currentConnect.setSendingEstablish(false);
    }

    public void run() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //connection = DriverManager.getConnection("jdbc:mysql://10.125.103.139:3306/chat","qwe","123");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/chat","root","root");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {

            /*inputFromClient = new DataInputStream(currentConnect.getSocket().getInputStream());
            //receive username
            userName = inputFromClient.readUTF();*/

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(currentConnect.getSocket().getInputStream()));
            String userName = bufferedReader.readLine();

            //pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));

            currentConnect.setUsername(userName);
            //for each established connection


            //发送好友姓名
            String friendName = getFriendListAsString(userName);
            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
            pw.println(friendName + " friendslist");
            pw.flush();

            DataOutputStream outputToClient = null;


            //sendOfflineMessage(outputToClient);


            //DataInputStream inputFromClient = null;
            while (true) {

                try {
                    //inputFromClient = new DataInputStream(currentConnect.getSocket().getInputStream());
                    //waiting for message from this client
                    //Server.setReceiveInfo(inputFromClient.readUTF());

                    receiveInfo = bufferedReader.readLine();

                    String message[] = receiveInfo.split(";");

                    if (receiveInfo.equals("enters the room")){
                        enterRoomCast();
                    }
                    else{

                       /* pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(currentConnect.getSocket().getOutputStream())));
                        pw.println(message[1] + " messageprivate");
                        pw.flush();*/
                        sendPrivateMessage(message);
                    }

                    //continue;
                                        //String message[] = receiveInfo.split(";");
                    /*if(message[1].equals("74661f669fc89cfeef4d48f7ba61ad55c777b9b4")){
                        this.sendUserLists(message[0],outputToClient);
                    }
                    else if (message[1].contains(" 769853e342838d691865164489e0fd88c217fbf8")){//approve the friend
                        establishFriend(message,outputToClient);//建立好友关系,更新用户列表,提示双方用户列表

                    }
                    else if (message[1].contains(" bb4b38f45332ab0ed6a51e7c5c9e645bbcc079e7")){//decline the friend
                        declineFriend(message,outputToClient);
                    }
                    else if (message.length == 2){
                        sendMessage(message,outputToClient);//发送消息,区分离线与在线2种情况
                    }
                    else if (message.length == 3){//length = 3
                        if (message[2].equals("b9dded77d303219e5a92260272748e0e8e96d0e2")){//申请建立好友
                            sendEstablishMessage(message[0],message[1],outputToClient);
                        }
                        //申请删除好友
                        else if (message[2].equals("38da931871c011e0f1ff67d95e8801e90a756f09")){
                            sendDeleteMessage(message[0],message[1],outputToClient);
                        }
                    }*/
                }
                //if lose the connection
                catch (Exception e) {
                    //for each established connection
                    for (Connect connect : Server.getConnects()) {
                        /*DataOutputStream leaveRoom = new DataOutputStream(connect.getSocket().getOutputStream());
                        //send a message indicating the user leaves the room
                        leaveRoom.writeUTF(userName + " leaves the room\n");*/

                        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connect.getSocket().getOutputStream())));
                        pw.println(userName + " leaves the room");
                        pw.flush();

                        String nameInRoom = connect.getUsername();
                        String userNameToDelete = userName;
                        if (!nameInRoom.equals(userName)) {
                            if (isFriend(nameInRoom, userName)){
                                userNameToDelete = userNameToDelete + "(好友)(在线)";
                            }//if two users are friends, then change (在线) to (离线)
                            /*leaveRoom.writeUTF(userNameToDelete + " 706c9530b099afb9277fbc936b82e0f4910d7f41");*/

                            pw.println(userNameToDelete + " 706c9530b099afb9277fbc936b82e0f4910d7f41");
                            pw.flush();
                        }

                    }
                    //for each established connection
                    for (Connect connect : Server.getConnects()) {
                        //find this socket
                        if (connect.getSocket().toString().compareTo(currentConnect.getSocket().toString()) == 0) {
                            //remove the socket
                            Server.getConnects().remove(connect);
                            break;
                        }
                    }
                    System.out.println(currentConnect.getUsername() + " has disconnected. The IP is " + currentConnect.getSocket().getInetAddress());

                    break;
                }
            }
        } catch (IOException e1) {

        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

}

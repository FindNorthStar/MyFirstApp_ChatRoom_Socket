package com.mycompany.myfirstapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 自定义适配器，用来连接自定义的MyUserList和listview(其ID为listView)
 * Created by lenovo on 2016/7/4.
 */
public class UserAdapter extends ArrayAdapter<MyUser> {
    private int resourceID;

    public UserAdapter(Context context,int textViewResourceId, List<MyUser> objects) {
        super(context, textViewResourceId, objects);
        this.resourceID = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyUser user=getItem(position);//获取当前项（行）的用户
        View view;
        ViewHolder viewHolder;
        if(convertView==null){
            //用于将之前加载的布局缓存
            view = LayoutInflater.from(getContext()).inflate(resourceID, null);
            viewHolder = new ViewHolder();
            viewHolder.userImage = (ImageView) view.findViewById(R.id.user_image);
            viewHolder.userName = (TextView) view.findViewById(R.id.user_name);
            viewHolder.recentMsg=(TextView) view.findViewById(R.id.recent_msg);
            view.setTag(viewHolder);
        }
        else{
           view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.userImage.setImageResource(user.getImageID());
        viewHolder.userName.setText(user.getUserName());
        viewHolder.recentMsg.setText(user.getRecentMsg());
        return view;
    }

    //用来缓存之前用ID找到view，提高效率
    class ViewHolder{
        ImageView userImage;
        TextView userName;
        TextView recentMsg;
    }
}

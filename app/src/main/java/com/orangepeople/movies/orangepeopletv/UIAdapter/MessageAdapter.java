package com.orangepeople.movies.orangepeopletv.UIAdapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orangepeople.movies.orangepeopletv.Model.CommentInfo;
import com.orangepeople.movies.orangepeopletv.R;
import com.orangepeople.movies.orangepeopletv.Utils.Util;
import com.orangepeople.movies.orangepeopletv.View.CircleTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/6/7.
 */
public class MessageAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Activity context;
    private Util util;


    public MessageAdapter(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        util = new Util(context);
    }

    private List<CommentInfo> list = new ArrayList<>();

    public void setList(List<CommentInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.message_item, null);
            viewHolder.user_logo = (ImageView) convertView.findViewById(R.id.user_logo);
            viewHolder.me_content = (TextView) convertView.findViewById(R.id.me_content);
            viewHolder.user_name = (TextView) convertView.findViewById(R.id.user_name);
            viewHolder.like_count = (TextView) convertView.findViewById(R.id.like_count);
            viewHolder.me_date = (TextView) convertView.findViewById(R.id.me_date);
            viewHolder.me_item = (LinearLayout) convertView.findViewById(R.id.me_item);
            viewHolder.tuijiandu = (TextView) convertView.findViewById(R.id.tuijiandu);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        setDataToUI(convertView, viewHolder, parent, position);
        return convertView;
    }

    private void setDataToUI(View convertView, ViewHolder viewHolder, ViewGroup parent, int position) {
        final CommentInfo messageInfo = list.get(position);
//        Glide.with(context).load(messageInfo.getPic()).
//                placeholder(R.mipmap.loading)
//                .transform(new CircleTransform(context))
//                .error(R.mipmap.error_images).
//                into(viewHolder.user_logo);

        viewHolder.user_name.setText(messageInfo.getName());
        viewHolder.like_count.setText(messageInfo.getHand());
        viewHolder.me_content.setText(messageInfo.getInfo());
        viewHolder.me_content.scrollTo(0, 0);
        //推荐:   86%
        viewHolder.tuijiandu.setText("推荐:   " + util.createLikeRandom() + "%");
        viewHolder.me_date.setText(messageInfo.getCreatetime().substring(0, 10));
    }

    public class ViewHolder {
        TextView me_content;
        ImageView user_logo;
        TextView user_name;
        TextView like_count;
        TextView me_date;
        TextView tuijiandu;
        LinearLayout me_item;
    }
}

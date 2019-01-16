package com.example.administrator.imageloaderdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

public class ImageAdapter  extends BaseAdapter {
    public  LayoutInflater mInflater;
    private List<String> mList;
    public ImageAdapter(Context context, List<String> list){
        mList = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            view = mInflater.inflate(R.layout.item,viewGroup,false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

//        ImageView imageView = holder.mSquareImageView;
//
//        String tag = (String) imageView.getTag();
//        String uri = (String) getItem(i);
//        if (!uri.equals(tag)){
//            imageView.setImageDrawable();
//        }


        return view;
    }

    static class ViewHolder{
        SquareImageView mSquareImageView;
        public ViewHolder(View convertView){
            mSquareImageView = convertView.findViewById(R.id.image);
        }
    }
}

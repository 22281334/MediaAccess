package comp5216.sydney.edu.au.mediaaccess;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.List;

public class MyGridViewAdapter extends BaseAdapter {

    List<String> paths ;
    Context context;

    public MyGridViewAdapter(List paths,Context context){
        this.context=context;
        this.paths=paths;
    }


    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            // set up item into grid view
            convertView = View.inflate(context, R.layout.activity_my_grid_view_item, null);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // get item path for image
        String path = paths.get(position);
        // load image
        Glide.with(context).load(new File(path))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .into(viewHolder.imageView);
        return convertView;
    }


}

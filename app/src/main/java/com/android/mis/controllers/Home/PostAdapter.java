package com.android.mis.controllers.Home;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.android.mis.R;
import com.android.mis.models.Home.Post;
import com.android.mis.models.Home.Post1;
import com.android.mis.models.Home.PostList;
import com.android.mis.utils.DownloadService;
import com.android.mis.utils.Urls;
import com.android.mis.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajat on 8/3/17.
 */

public class PostAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {

    private List<PostList> allData;
    private Activity activity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Context context;


    public PostAdapter(List<PostList> data, Activity activity) {
        this.activity = activity;
        this.allData = data;
    }

    @Override
    public int getSectionCount() {
        return allData.size();
    }

    @Override
    public int getItemCount(int section) {
        return allData.get(section).getAllPostsOnGivenDate().size();
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int section) {
        String sectionName = allData.get(section).getSectionTitle();
        SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
        sectionViewHolder.sectionTitle.setText(sectionName);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int section, int relativePosition, int absolutePosition) {

        ArrayList<Post1> itemsInSection = allData.get(section).getAllPostsOnGivenDate();

        final Post1 itemName = itemsInSection.get(relativePosition);

        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        itemViewHolder.userOfPost.setText(itemName.getPostIssuedBy());
        itemViewHolder.multiPurposeButton.setText("Download Attachment");

        itemViewHolder.multiPurposeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Urls.notice_base_path+itemName.getPostPath();
                if(checkPermission())
                {
                    Util.startDownload(url,itemName.getPostPath(),activity);
                }
                else{
                    requestPermission(url,itemName.getPostPath());
                }
            }
        });

        if(itemName.getPostIssuedBy().contentEquals(Urls.no_post_message))
        {
            itemViewHolder.clock.setVisibility(View.GONE);
            ((ItemViewHolder) holder).multiPurposeButton.setVisibility(View.GONE);
        }
        itemViewHolder.identificationUserOfPost.setText(itemName.getPostAuthName());
        itemViewHolder.postDetails.setText(itemName.getPostSub());
        itemViewHolder.timeOfPost.setText(itemName.getPostIssueTime());
        itemViewHolder.icon.setImageResource(itemName.getImageDrawable());
        // Try to put a image . for sample i set background color in xml layout file
        // itemViewHolder.itemImage.setBackgroundColor(Color.parseColor("#01579b"));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == VIEW_TYPE_HEADER) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_home_section, parent, false);
            return new SectionViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_home_item, parent, false);
            return new ItemViewHolder(v);
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String url,String name){
        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,url,name},PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Util.startDownload(permissions[1],permissions[2],activity);
                } else {
                    Toast.makeText(activity.getApplicationContext(),"Request dinied",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    // SectionViewHolder Class for Sections
    public static class SectionViewHolder extends RecyclerView.ViewHolder {

        TextView sectionTitle;

        public SectionViewHolder(View itemView) {
            super(itemView);
            sectionTitle = (TextView) itemView.findViewById(R.id.section_button);
        }
    }

    // ItemViewHolder Class for Items in each Section
    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView userOfPost,identificationUserOfPost,postDetails,timeOfPost;
        Button multiPurposeButton;
        ImageView clock;
        ImageButton icon;


        public ItemViewHolder(View itemView) {
            super(itemView);
            userOfPost = (TextView) itemView.findViewById(R.id.user_of_post);
            timeOfPost = (TextView)itemView.findViewById(R.id.time_of_post);
            clock = (ImageView)itemView.findViewById(R.id.clock);
            identificationUserOfPost = (TextView)itemView.findViewById(R.id.identification_user_of_post);
            postDetails = (TextView)itemView.findViewById(R.id.post_details);
            multiPurposeButton = (Button)itemView.findViewById(R.id.multi_purpose_button);
            icon = (ImageButton)itemView.findViewById(R.id.side_icon);
            multiPurposeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), userOfPost.getText(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

package com.special.newsdemo.Fragment;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.special.newsdemo.NewContentActivity;
import com.special.newsdemo.R;
import com.special.newsdemo.model.New;
import com.special.newsdemo.model.NewsResponse;
import com.special.newsdemo.util.HttpUtility;
import com.special.newsdemo.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    private int type = 1;
    private int page = 1;
    private static final String PERFIX_Url = "http://open.twtstudio.com/api/v1/news/";
    private RecyclerView recyclerView;
    private NewsAdpater newsAdpater;
    private List<New> newsList = new ArrayList<New>();
    public SwipeRefreshLayout swipeRefreshLayout;
    private boolean loading = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_news, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_news);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_news);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorProgress);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                page = 1;
                refresh(type,page);
            }
        });
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        newsAdpater = new NewsAdpater(newsList);
        recyclerView.setAdapter(newsAdpater);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = layoutManager.getItemCount();
                int visibleCount = layoutManager.getChildCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (!loading && totalCount - visibleCount <= firstVisibleItem) {
                    page++;
                    loadMore(page);
                }
            }
        });
        return view;
    }
    public void loadMore(int page){
        loading = true;
        String url = PERFIX_Url + type + "/page/" + page;
        HttpUtility.sendOkHttpRequest(url,new Callback(){

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                final NewsResponse news = Utility.handleNewsResponse(responseText);
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        if(news.error_code == -1){
                            if(news.data.size() > 0) {
                                newsList.addAll(news.data);
                                newsAdpater.notifyDataSetChanged();
                                Toast.makeText(getActivity(),"获取更多成功",Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getActivity(),"我是有底线的",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(),"获取新闻失败，消息代码不是-1",Toast.LENGTH_SHORT).show();
                        }
                        loading = false;
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),"获取新闻失败，服务器出错",Toast.LENGTH_SHORT).show();
                    }
                });
                loading = false;
            }
        });
    }
    public void refresh(int type, int page){
        this.type = type;
        this.page = page;
        String url = PERFIX_Url + type + "/page/" + page;
        HttpUtility.sendOkHttpRequest(url,new Callback(){
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                final NewsResponse news = Utility.handleNewsResponse(responseText);
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        if(news.error_code == -1){
                            newsList.clear();
                            newsList.addAll(news.data);
                            newsAdpater.notifyDataSetChanged();
                            Toast.makeText(getActivity(),"获取成功",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getActivity(),"获取新闻失败，消息代码不是-1",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),"获取新闻失败，服务器出错",Toast.LENGTH_SHORT).show();
                    }
                });
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    class NewsAdpater extends RecyclerView.Adapter<NewsAdpater.ViewHolder>{
        private List<New> mNewsList;

        class ViewHolder extends RecyclerView.ViewHolder{
            ImageView newPic;
            TextView newTitle;
            TextView newSum;
            TextView visitCount;
            TextView comment;
            RelativeLayout touchForNewsContent;

            public ViewHolder(View view){
                super(view);
                newPic = (ImageView) view.findViewById(R.id.new_pic);
                newTitle = (TextView) view.findViewById(R.id.new_title);
                newSum = (TextView) view.findViewById(R.id.new_sum);
                visitCount = (TextView) view.findViewById(R.id.visitcount);
                comment = (TextView) view.findViewById(R.id.comment);
                touchForNewsContent = (RelativeLayout) view.findViewById(R.id.new_header);
            }
        }

        public NewsAdpater(List<New> newsList){
            mNewsList = newsList;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item,parent,false);
            final ViewHolder holder = new ViewHolder(view);
            holder.touchForNewsContent.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    New aNew = newsList.get(holder.getAdapterPosition());
                    Intent intent = new Intent(getActivity(), NewContentActivity.class);
                    intent.putExtra("index",aNew.index);
                    startActivity(intent);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            New newItem = mNewsList.get(position);
            Glide.with(getActivity()).load(newItem.pic).placeholder(R.drawable.placeholder).into(holder.newPic);
            holder.newTitle.setText(newItem.subject);
            holder.newSum.setText(newItem.summary);
            holder.visitCount.setText("浏览量：" + String.valueOf(newItem.visitCount));
            holder.comment.setText("评论量：" + String.valueOf(newItem.comments));
        }

        @Override
        public int getItemCount() {
            return mNewsList.size();
        }
    }

}

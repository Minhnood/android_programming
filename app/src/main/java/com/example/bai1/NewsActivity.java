package com.example.bai1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsActivity extends BaseActivity {

    private static final String RSS_URL = "https://vnexpress.net/rss/oto-xe-may.rss";

    private RecyclerView rvNews;
    private NewsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View llEmpty;
    private final List<NewsModel> newsList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        UiUtils.applySystemBarInsets(this);

        rvNews = findViewById(R.id.rvNews);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        llEmpty = findViewById(R.id.llNewsEmpty);
        rvNews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this, newsList);
        rvNews.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.jdm_red);
        swipeRefresh.setOnRefreshListener(this::loadNews);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadNews();
    }

    private void loadNews() {
        swipeRefresh.setRefreshing(true);
        executor.execute(() -> {
            final List<NewsModel> items = fetchRss();
            main.post(() -> {
                swipeRefresh.setRefreshing(false);
                newsList.clear();
                newsList.addAll(items);
                adapter.notifyDataSetChanged();
                boolean empty = newsList.isEmpty();
                llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rvNews.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private List<NewsModel> fetchRss() {
        List<NewsModel> items = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(RSS_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            InputStream inputStream = connection.getInputStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            String title = "", link = "", guid = "", description = "", pubDate = "", imageUrl = "";
            boolean insideItem = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if (tag.equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (insideItem) {
                        if (tag.equalsIgnoreCase("title")) {
                            title = safeText(parser);
                        } else if (tag.equalsIgnoreCase("link")) {
                            link = safeText(parser);
                        } else if (tag.equalsIgnoreCase("guid")) {
                            guid = safeText(parser);
                        } else if (tag.equalsIgnoreCase("description")) {
                            String descHtml = safeText(parser);
                            description = descHtml.replaceAll("<[^>]*>", "").trim();
                            int start = descHtml.indexOf("src=\"");
                            if (start >= 0) {
                                start += 5;
                                int end = descHtml.indexOf("\"", start);
                                if (end > start) imageUrl = descHtml.substring(start, end);
                            }
                        } else if (tag.equalsIgnoreCase("enclosure")) {
                            // Nhiều RSS (VnExpress) để ảnh ở thẻ enclosure — nguồn ảnh đáng tin nhất
                            String type = parser.getAttributeValue(null, "type");
                            String u = parser.getAttributeValue(null, "url");
                            if (u != null && !u.trim().isEmpty()
                                    && (type == null || type.toLowerCase().startsWith("image"))) {
                                imageUrl = u.trim();
                            }
                        } else if (tag.equalsIgnoreCase("media:content")
                                || tag.equalsIgnoreCase("media:thumbnail")) {
                            // Một số feed dùng media:content/thumbnail — chỉ lấy nếu chưa có ảnh
                            String u = parser.getAttributeValue(null, "url");
                            if (u != null && !u.trim().isEmpty()
                                    && (imageUrl == null || imageUrl.isEmpty())) {
                                imageUrl = u.trim();
                            }
                        } else if (tag.equalsIgnoreCase("pubDate")) {
                            pubDate = safeText(parser);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item")) {
                    String finalLink = (link != null && !link.trim().isEmpty()) ? link.trim() : guid.trim();
                    items.add(new NewsModel(title, finalLink, description, pubDate, imageUrl));
                    title = ""; link = ""; guid = ""; description = ""; pubDate = ""; imageUrl = "";
                    insideItem = false;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("NewsActivity", "Error fetching RSS", e);
        } finally {
            if (connection != null) connection.disconnect();
        }
        return items;
    }

    private String safeText(XmlPullParser parser) {
        try {
            String t = parser.nextText();
            return t != null ? t : "";
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}

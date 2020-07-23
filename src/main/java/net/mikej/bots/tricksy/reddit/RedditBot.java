package net.mikej.bots.tricksy.reddit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.reflections.Reflections;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;

public class RedditBot {
    private static RedditClient _client;

    public RedditBot(String username, String password, String clientId, String clientSecret) {
        Credentials oauthCreds = Credentials.script(username, password, clientId, clientSecret);
        UserAgent userAgent = new UserAgent("bot", "net.mikej.bots.tricksy", "1.0.0", "thisisverytricky");
        _client = OAuthHelper.automatic(new OkHttpNetworkAdapter(userAgent), oauthCreds);
    }

    public static RedditBot init(String username, String password, String clientId, String clientSecret) {
        return new RedditBot(username, password, clientId, clientSecret);
    }

    public static void watchNewPosts(String subreddit) throws InstantiationException, IllegalAccessException {
        NewPostWatcher.init(subreddit, _client);
    }

    public static class NewPostWatcher implements Runnable {

        @Override
        public void run() {
            try {
                for (Submission post : getNewPosts(subreddit).next()) {
                    if (seenPosts.contains(post.getFullName())) continue;
                    seenPosts.add(post.getFullName());
                    onNewPost(post);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private String subreddit;
        private RedditClient _client;
        private List<String> seenPosts = new ArrayList<>();
        private List<PostHandler> postHandlers = new ArrayList<>();

        private NewPostWatcher(String subreddit, RedditClient client) {
            this.subreddit = subreddit;
            this._client = client;

            for (Submission post : getNewPosts(subreddit).next()) {
                seenPosts.add(post.getFullName());
            }
        }

        public static void init(String subreddit, RedditClient client)
                throws InstantiationException, IllegalAccessException {
            NewPostWatcher watcher = new NewPostWatcher(subreddit, client);

            Reflections reflections = new Reflections("net.mikej.bots.tricksy.reddit.handlers");
            Set<Class<? extends PostHandler>> classes = reflections.getSubTypesOf(PostHandler.class);

            for (Class<? extends PostHandler> adpt : classes) {
                PostHandler adapter = adpt.newInstance();
                if (!adapter.handlesSubreddit(subreddit)) continue;
                adapter.init();
                watcher.addPostHandler(adapter);
            }
            
            if (!watcher.hasHandlers()) return;

            ScheduledExecutorService schedular = Executors.newScheduledThreadPool(1);
            schedular.scheduleAtFixedRate(watcher, 0, 2, TimeUnit.SECONDS);
        }

        private DefaultPaginator<Submission> getNewPosts(String subreddit) {
            return _client.subreddit(subreddit).posts().sorting(SubredditSort.NEW).limit(100).build();
        }

        private void onNewPost(Submission post) {
            for (PostHandler handler : postHandlers)
                try { handler.handlePost(post); } catch (Exception ex) { ex.printStackTrace(); }
        }

        public String getSubreddit() { return subreddit; }

        public void addPostHandler(PostHandler handler) {
            postHandlers.add(handler);
        }

        public void removePostHandler(PostHandler handler) {
            if (postHandlers.contains(handler))
                postHandlers.remove(handler);
        }

        public boolean hasHandlers() { return postHandlers.size() > 0; }
    }
}
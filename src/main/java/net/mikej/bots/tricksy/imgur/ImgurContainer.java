package net.mikej.bots.tricksy.imgur;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.mikej.bots.tricksy.imgur.models.Image;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class ImgurContainer {
    private static ImgurService _client;

    public static void init(final String clientId) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.imgur.com/")
        .addConverterFactory(JacksonConverterFactory.create()).client(new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {

                @Override
                public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
            
                    Request request = original.newBuilder()
                        .header("User-Agent", "Tricky-Bot-Discord")
                        .header("Authorization", String.format("Client-Id %s", clientId))
                        .build();
            
                    return chain.proceed(request);
                }
            }).build())
        .build();
        _client = retrofit.create(ImgurService.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataResult<T>
    {
        private final T data;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public DataResult(@JsonProperty("data") T data) {
            this.data = data;
        }

        public T getData() { return data; }
    }

    public static List<Image> getAlbumImages(final String albumHash) throws IOException {
        Call<DataResult<List<Image>>> albumImagesCall = _client.getAlbumImages(albumHash);
        Response<DataResult<List<Image>>> albumImagesResponse = albumImagesCall.execute();
        if (!albumImagesResponse.isSuccessful()) return null;
        return albumImagesResponse.body().getData();
    }

    public static Image getImage(final String imageHash) throws IOException {
        Call<DataResult<Image>> imageCall = _client.getImage(imageHash);
        Response<DataResult<Image>> imageResponse = imageCall.execute();
        if (!imageResponse.isSuccessful()) return null;
        return imageResponse.body().getData();
    }
}
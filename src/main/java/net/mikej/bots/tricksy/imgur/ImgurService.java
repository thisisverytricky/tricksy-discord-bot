package net.mikej.bots.tricksy.imgur;

import java.util.List;

import net.mikej.bots.tricksy.imgur.ImgurContainer.DataResult;
import net.mikej.bots.tricksy.imgur.models.Image;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImgurService {
    @GET("/3/album/{albumHash}/images")
    Call<DataResult<List<Image>>> getAlbumImages(@Path("albumHash") final String albumHash);

    @GET("/3/image/{imageHash}")
    Call<DataResult<Image>> getImage(@Path("imageHash") final String imageHash);
}
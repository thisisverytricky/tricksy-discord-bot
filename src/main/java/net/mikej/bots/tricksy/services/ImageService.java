package net.mikej.bots.tricksy.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.mikej.bots.tricksy.imgur.ImgurContainer;
import net.mikej.bots.tricksy.imgur.models.Image;

public class ImageService {
    public static List<String> getImages(String image) throws IOException {
        if (image.matches(".+\\.(?:jpg|gif|png)"))
            return Arrays.asList(new String[] { image });
        else if (image.toLowerCase().contains("imgur"))
            return getImgurImage(image).stream().filter(i -> i.matches(".+\\.(?:jpg|gif|png)")).collect(Collectors.toList());
        return new ArrayList<String>();
    }

    private static List<String> getImgurImage(String image) throws IOException {
        String[] parts = image.split("/");
        if (parts.length > 4) {
            List<Image> images = ImgurContainer.getAlbumImages(parts[parts.length - 1]);
            return images.stream().map(i -> i.getLink()).collect(Collectors.toList());
        } else {
            try {
                Image img = ImgurContainer.getImage(parts[parts.length - 1]);
                return img == null ? new ArrayList<String>() : Arrays.asList(new String[] { img.getLink() });
            } catch (Exception ex) { 
                return Arrays.asList(new String[] { image }); 
            }
        }
    }
}
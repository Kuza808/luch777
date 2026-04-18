package com.restaurant.luch.utils;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageCache {
    private static final Map<String, Image> cache = new HashMap<>();

    public static Image getImage(String url, double width, double height, boolean preserveRatio, boolean smooth) {
        if (url == null || url.isEmpty()) {
            return getPlaceholder(width, height);
        }
        String key = url + "_" + width + "_" + height;
        Image img = cache.get(key);
        if (img == null) {
            try {
                img = new Image(url, width, height, preserveRatio, smooth, true);
                img.exceptionProperty().addListener((obs, oldErr, newErr) -> {
                    if (newErr != null) {
                        System.err.println("Ошибка загрузки изображения: " + url + " - " + newErr.getMessage());
                        cache.put(key, getPlaceholder(width, height));
                    }
                });
                cache.put(key, img);
            } catch (Exception e) {
                img = getPlaceholder(width, height);
                cache.put(key, img);
            }
        }
        return img;
    }

    private static Image getPlaceholder(double width, double height) {
        String key = "placeholder_" + width + "_" + height;
        Image img = cache.get(key);
        if (img == null) {
            try (InputStream is = ImageCache.class.getResourceAsStream("/images/placeholder.png")) {
                if (is != null) {
                    img = new Image(is, width, height, true, true);
                } else {
                    img = createColorPlaceholder(width, height, Color.LIGHTGRAY);
                }
            } catch (Exception e) {
                img = createColorPlaceholder(width, height, Color.LIGHTGRAY);
            }
            cache.put(key, img);
        }
        return img;
    }

    private static Image createColorPlaceholder(double width, double height, Color color) {
        WritableImage wi = new WritableImage((int) width, (int) height);
        var pw = wi.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pw.setColor(x, y, color);
            }
        }
        return wi;
    }
}
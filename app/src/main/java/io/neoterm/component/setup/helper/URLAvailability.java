package io.neoterm.component.setup.helper;

import android.content.Context;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import io.neoterm.utils.NetworkUtils;

/**
 * @author kiva
 */

public final class URLAvailability {
    public enum ResultCode {
        URL_OK,
        URL_CONNECTION_FAILED,
        URL_INVALID,
        URL_NO_INTERNET,
    }

    public static ResultCode checkUrlAvailability(Context context, String urlString) {
        if (!NetworkUtils.INSTANCE.isNetworkAvailable(context)) {
            return ResultCode.URL_NO_INTERNET;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.disconnect();

        } catch (MalformedURLException e) {
            return ResultCode.URL_INVALID;

        } catch (IOException | ClassCastException e) {
            return ResultCode.URL_CONNECTION_FAILED;
        }

        return ResultCode.URL_OK;
    }
}

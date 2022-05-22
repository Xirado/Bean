package at.xirado.bean.misc;

import at.xirado.bean.Bean;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Hastebin {
    private static final String BASE = "https://hastebin.de";

    public static String post(String text, boolean raw) throws IOException {
        return post(text, raw, null);
    }

    public static String post(String text, boolean raw, String extension) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        int length = data.length;

        RequestBody body = RequestBody.create(data);

        Request request = new Request.Builder()
                .url(BASE + "/documents")
                .post(body)
                .header("User-Agent", "Bean Discord Bot (https://github.com/Xirado/Bean)")
                .header("Content-Length", String.valueOf(length))
                .build();

        Response response = Bean.getInstance().getOkHttpClient().newCall(request).execute();

        DataObject object = DataObject.fromJson(response.body().byteStream());

        if (raw)
            return BASE + "/raw/" + object.getString("key");
        else
            return BASE + "/" + object.getString("key") + (extension == null ? "" : "." + extension);

    }
}

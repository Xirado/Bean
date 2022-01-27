package at.xirado.bean.backend.requests;

import okhttp3.OkHttpClient;

public class Requester {

    private final OkHttpClient okHttpClient;

    public Requester() {
        this.okHttpClient = new OkHttpClient();
    }

    public Request request(okhttp3.Request request) {
        return new Request(request, okHttpClient);
    }


}

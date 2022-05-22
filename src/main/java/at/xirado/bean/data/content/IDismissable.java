package at.xirado.bean.data.content;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IDismissable<T> {

    @NotNull
    String getIdentifier();

    @Nullable
    String getMediaUrl();

    @NotNull
    T get();
}

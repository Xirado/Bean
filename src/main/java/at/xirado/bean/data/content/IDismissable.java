package at.xirado.bean.data.content;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDismissable<T> {

    @Nonnull
    String getIdentifier();

    @Nullable
    String getMediaUrl();

    @Nonnull
    T get();
}

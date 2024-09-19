/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.data.content;

import at.xirado.bean.data.database.SQLBuilder;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DismissableContentManager {

    private static final List<IDismissable<?>> DISMISSABLES = new ArrayList<>();

    static {
        DISMISSABLES.add(new RankCustomBackgroundDismissableContent());
        DISMISSABLES.add(new BookmarkDismissableContent());
    }

    private final Map<Long, List<DismissableProgress>> progressCache = ExpiringMap.builder()
            .expiration(5, TimeUnit.MINUTES)
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .build();

    @Nonnull
    public DismissableProgress createDismissableContent(long userId, Class<? extends IDismissable<?>> dismissable,
                                                        DismissableState initialState) {
        return createDismissableContent(userId, dismissable, initialState, true);
    }

    @Nonnull
    public DismissableProgress createDismissableContent(long userId, Class<? extends IDismissable<?>> dismissable,
                                                        DismissableState initialState, boolean shouldStore) {
        IDismissable<?> object = getFromClass(dismissable);
        DismissableProgress progress = new DismissableProgress(userId, object, initialState);
        List<DismissableProgress> progresses = progressCache.computeIfAbsent(userId, (k) -> new ArrayList<>());
        progresses.add(progress);
        if (shouldStore)
            progress.update();
        return progress;
    }

    @Nullable
    public DismissableProgress getProgress(long userId, Class<? extends IDismissable<?>> dismissable, boolean retrieve) {
        return progressCache.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(progress -> dismissable.isInstance(progress.getDismissable()))
                .findFirst()
                .orElse(retrieve ? retrieveProgress(userId, dismissable) : null);
    }

    @Nullable
    public DismissableProgress getProgress(long userId, String identifier, boolean retrieve) {
        Class<? extends IDismissable<?>> dismissable = getClassFromIdentifier(identifier);
        return getProgress(userId, dismissable, retrieve);
    }

    @Nullable
    public DismissableProgress retrieveProgress(long userId, Class<? extends IDismissable<?>> dismissable) {
        IDismissable<?> dismissableObj = getFromClass(dismissable);
        try (var rs = new SQLBuilder("SELECT * FROM dismissable_contents WHERE user_id = ? AND identifier = ?",
                userId, dismissableObj.getIdentifier()).executeQuery()) {
            if (rs.next()) {
                DismissableState state = DismissableState.valueOf(rs.getString("state"));
                return new DismissableProgress(userId, dismissableObj, state);
            }
            return null;
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean hasProgress(long userId, Class<? extends IDismissable<?>> dismissable) {
        return getProgress(userId, dismissable, true) != null;
    }

    @Nonnull
    public IDismissable<?> getFromIdentifier(String identifier) {
        return DISMISSABLES.stream()
                .filter(dismissable -> dismissable.getIdentifier().equals(identifier))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Dismissable with identifier \"" + identifier + "\" found!"));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public Class<? extends IDismissable<?>> getClassFromIdentifier(String identifier) {
        return (Class<? extends IDismissable<?>>) DISMISSABLES.stream()
                .filter(dismissable -> dismissable.getIdentifier().equals(identifier))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Dismissable with identifier \"" + identifier + "\" found!")).getClass();
    }

    @Nonnull
    public IDismissable<?> getFromClass(Class<? extends IDismissable<?>> clazz) {
        return DISMISSABLES.stream()
                .filter(clazz::isInstance)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Passed IDismissable is not registered!"));
    }
}

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

package at.xirado.bean.mee6;

import org.jetbrains.annotations.NotNull;

public class MEE6Request implements Comparable<MEE6Request> {
    private final long guildId;
    private final long authorId;

    private int page = 0;

    public MEE6Request(long guildId, long authorId) {
        this.guildId = guildId;
        this.authorId = authorId;
    }

    public MEE6Request setPage(int page) {
        this.page = page;
        return this;
    }

    public long getAuthorId() {
        return authorId;
    }

    public int getPage() {
        return page;
    }

    public long getGuildId() {
        return guildId;
    }

    @Override
    public int compareTo(@NotNull MEE6Request o) {
        if (this.page == o.page)
            return 0;
        else if (this.page > o.page)
            return 1;
        else
            return -1;
    }
}

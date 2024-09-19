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

package at.xirado.bean.data;

public class RankedUser {
    private final long totalXP;
    private final long guildID;
    private final long userID;

    private final String name;
    private final String discriminator;

    public RankedUser(long guildID, long userID, long totalXP, String name, String discriminator) {
        this.guildID = guildID;
        this.userID = userID;
        this.totalXP = totalXP;
        this.name = name;
        this.discriminator = discriminator;
    }

    @Override
    public String toString() {
        return userID + " (" + name + "#" + discriminator + ") " + totalXP;
    }

    public long getUserID() {
        return userID;
    }

    public String getName() {
        return name;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public long getTotalXP() {
        return totalXP;
    }

    public long getGuildID() {
        return guildID;
    }
}

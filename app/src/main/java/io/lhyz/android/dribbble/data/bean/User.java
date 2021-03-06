/*
 * Copyright (c) 2016 lhyz Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lhyz.android.dribbble.data.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * hello,android
 * Created by lhyz on 2016/8/7.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 3267164831443096444L;

    long id;
    String name;
    @SerializedName("avatar_url")
    String avatarUrl;
    @SerializedName("html_url")
    String host;

    public User(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public User(long id, String name, String avatarUrl, String host) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.host = host;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getHost() {
        return host;
    }
}

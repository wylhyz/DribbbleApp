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
package io.lhyz.android.dribbble.main.recent;

import java.util.List;

import javax.inject.Inject;

import io.lhyz.android.boilerplate.interactor.DefaultSubscriber;
import io.lhyz.android.boilerplate.interactor.Interactor;
import io.lhyz.android.dribbble.DribbbleApp;
import io.lhyz.android.dribbble.data.model.Shot;
import io.lhyz.android.dribbble.di.annotation.Recent;

/**
 * hello,android
 * Created by lhyz on 2016/8/9.
 */
public class RecentPresenter implements RecentContract.Presenter {

    RecentContract.View mView;

    @Inject
    @Recent
    Interactor<List<Shot>> mInteractor;

    public RecentPresenter(RecentContract.View view) {
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void loadRecent() {
        mView.showLoading();
        mInteractor.execute(new DefaultSubscriber<List<Shot>>() {
            @Override
            public void onSuccess(List<Shot> result) {
                mView.hideLoading();
                mView.showRecent(result);
            }

            @Override
            public void onError(Throwable e) {
                mView.hideLoading();
                mView.showEmptyView();
            }
        });
    }

    @Override
    public void start() {
        initInjector();

        loadRecent();
    }

    @Override
    public void pause() {
        mInteractor.unsubscribe();
    }
    @Override
    public void destroy() {
        mInteractor.unsubscribe();
        mView = null;
    }


    void initInjector() {
        DribbbleApp.getAppComponent().inject(this);
    }
}

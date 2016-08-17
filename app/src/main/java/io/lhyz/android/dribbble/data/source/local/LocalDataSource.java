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
package io.lhyz.android.dribbble.data.source.local;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.orhanobut.logger.Logger;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import io.lhyz.android.dribbble.base.DefaultSubscriber;
import io.lhyz.android.dribbble.data.bean.Comment;
import io.lhyz.android.dribbble.data.bean.Shot;
import io.lhyz.android.dribbble.data.mapper.CommentMapper;
import io.lhyz.android.dribbble.data.mapper.ShotMapper;
import io.lhyz.android.dribbble.data.model.CommentModel;
import io.lhyz.android.dribbble.data.model.ShotModel;
import io.lhyz.android.dribbble.data.source.DataSource;
import io.lhyz.android.dribbble.util.TagHelper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * hello,android
 * Created by lhyz on 2016/8/14.
 */
public class LocalDataSource implements DataSource {
    private static final String TAG = TagHelper.from(LocalDataSource.class);

    private static LocalDataSource INSTANCE;

    Dao<CommentModel, Long> mCommentDao;
    Dao<ShotModel, Long> mShotDao;

    CompositeSubscription mCompositeSubscription;

    public LocalDataSource(Context context) {
        CommentDBHelper commentDBHelper = new CommentDBHelper(context);
        ShotDBHelper shotDBHelper = new ShotDBHelper(context);
        try {
            mCommentDao = commentDBHelper.getDao();
            mShotDao = shotDBHelper.getDao();
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
        }

        mCompositeSubscription = new CompositeSubscription();
    }

    public static LocalDataSource getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocalDataSource(context);
        }
        return INSTANCE;
    }

    /**
     * 根据shot列表类型获取列表
     */
    @Override
    public void getShotList(final int type, boolean force, final LoadShotsCallback callback) {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<Shot>>() {
            @Override
            public void call(Subscriber<? super List<Shot>> subscriber) {
                try {
                    QueryBuilder<ShotModel, Long> queryBuilder = mShotDao.queryBuilder();
                    queryBuilder.where().eq("type", type);
                    PreparedQuery<ShotModel> preparedQuery = queryBuilder.prepare();
                    List<ShotModel> shotModels = mShotDao.query(preparedQuery);
                    subscriber.onNext(ShotMapper.getInstance().convert(shotModels));
                    subscriber.onCompleted();
                } catch (SQLException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<Shot>>() {
                    @Override
                    public void onSuccess(List<Shot> result) {
                        callback.onShotsLoaded(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onNoShotsAvailable();
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    /**
     * 获取某个shot的所有comment
     */
    @Override
    public void getCommentList(final long shotId, boolean force, final LoadCommentsCallback callback) {
        Subscription subscription = Observable.create(new Observable.OnSubscribe<List<Comment>>() {
            @Override
            public void call(Subscriber<? super List<Comment>> subscriber) {
                try {
                    QueryBuilder<CommentModel, Long> queryBuilder = mCommentDao.queryBuilder();
                    queryBuilder.where().eq("shotId", shotId);
                    PreparedQuery<CommentModel> preparedQuery = queryBuilder.prepare();
                    List<CommentModel> commentModels = mCommentDao.query(preparedQuery);
                    subscriber.onNext(CommentMapper.getInstance().convert(commentModels));
                    subscriber.onCompleted();
                } catch (SQLException e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultSubscriber<List<Comment>>() {
                    @Override
                    public void onSuccess(List<Comment> result) {
                        callback.onCommentsLoaded(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onNoCommentsAvailable();
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    /**
     * 本地添加一条comment（远程添加完毕之后再调用本地保存）
     * <p>
     * 回调用不上
     */
    @Override
    public void addComment(long shotId, Comment comment, AddCommentCallback callback) {
        CommentModel commentModel = CommentMapper.getInstance().transform(comment);
        commentModel.setShotId(shotId);
        try {
            mCommentDao.create(commentModel);
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    @Override
    public void isLike(long shotId, final LikeCallback callback) {

    }

    @Override
    public void likeShot(long shotId, LikeCallback callback) {

    }

    @Override
    public void unlikeShot(long shotId, LikeCallback callback) {

    }

    @Override
    public void cancel() {
        mCompositeSubscription.clear();
    }


    /**
     * 保存一种类型的shot列表
     */
    @Override
    public void saveShotList(int type, List<Shot> shots) {
        Collection<ShotModel> shotModels = ShotMapper.getInstance().transform(shots);
        try {
            mShotDao.create(shotModels);
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 保存一条shot所有comment列表
     */
    @Override
    public void saveCommentList(long shotId, List<Comment> comments) {
        Collection<CommentModel> commentModels = CommentMapper.getInstance().transform(comments);
        try {
            mCommentDao.create(commentModels);
        } catch (SQLException e) {
            Logger.e(TAG, e.getMessage());
        }
    }
}
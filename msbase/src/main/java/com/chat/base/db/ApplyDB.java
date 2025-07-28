package com.chat.base.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.chat.base.MSBaseApplication;
import com.chat.base.entity.NewFriendEntity;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 好友申请管理
 */
public class ApplyDB {

    private ApplyDB() {

    }

    private static class ApplyDbBinder {
        private final static ApplyDB applyDb = new ApplyDB();
    }

    final static String tableName = "apply_tab";

    public static ApplyDB getInstance() {
        return ApplyDbBinder.applyDb;
    }

    public List<NewFriendEntity> queryAll() {
        List<NewFriendEntity> list = new ArrayList<>();
        Cursor cursor = MSBaseApplication
                .getInstance()
                .getDbHelper()
                .rawQuery(
                        "select * from " + tableName + " order by created_at desc", null);
        if (cursor == null) {
            return list;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            list.add(serializeFriend(cursor));
        }
        cursor.close();
        return list;
    }

    public NewFriendEntity query(String applyUid) {
        NewFriendEntity newFriendEntity = null;
        Cursor cursor = MSBaseApplication
                .getInstance()
                .getDbHelper()
                .rawQuery(
                        "select * from " + tableName + " where apply_uid=" + "\"" + applyUid + "\"", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                newFriendEntity = serializeFriend(cursor);
            }
            cursor.close();
        }
        return newFriendEntity;
    }


    public synchronized long insert(NewFriendEntity friendEntity) {
        ContentValues cv = new ContentValues();
        try {
            cv = getContentValues(friendEntity);
        } catch (Exception e) {
            MSLogUtils.e("新增申请数据错误");
        }
        long result = -1;
        try {
            result = MSBaseApplication.getInstance().getDbHelper()
                    .insert(tableName, cv);
        } catch (Exception e) {
            Log.e("插入数据库异常：", Objects.requireNonNull(e.getMessage()));
        }
        return result;
    }

    /**
     * 批量保存
     *
     * @param list List<NewFriendEntity>
     */
    public synchronized void insert(List<NewFriendEntity> list) {
        if (MSReader.isEmpty(list)) return;
        try {
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                insert(list.get(i));
            }
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            if (MSBaseApplication.getInstance().getDbHelper().getDB().inTransaction()) {
                MSBaseApplication.getInstance().getDbHelper().getDB()
                        .endTransaction();
            }
        }
    }


    public boolean update(NewFriendEntity friendEntity) {
        String[] update = new String[1];
        update[0] = friendEntity.apply_uid;
        ContentValues cv = new ContentValues();
        try {
            cv = getContentValues(friendEntity);
        } catch (Exception e) {
            MSLogUtils.e("修改申请加好友数据错误");
        }
        return MSBaseApplication.getInstance().getDbHelper()
                .update(tableName, cv, "apply_uid=?", update);
    }

    public void delete(String uid) {
        String[] where = new String[1];
        where[0] = uid;
        MSBaseApplication.getInstance().getDbHelper().delete(tableName, "apply_uid=?", where);
    }

    @SuppressLint("Range")
    private NewFriendEntity serializeFriend(Cursor cursor) {
        NewFriendEntity friendEntity = new NewFriendEntity();
        friendEntity.apply_uid = cursor.getString(cursor.getColumnIndex("apply_uid"));
        friendEntity.apply_name = cursor.getString(cursor.getColumnIndex("apply_name"));
        friendEntity.token = cursor.getString(cursor.getColumnIndex("token"));
        friendEntity.status = cursor.getInt(cursor.getColumnIndex("status"));
        friendEntity.remark = cursor.getString(cursor.getColumnIndex("remark"));
        friendEntity.created_at = cursor.getString(cursor.getColumnIndex("created_at"));
        return friendEntity;
    }

    private ContentValues getContentValues(NewFriendEntity friendEntity) {
        ContentValues contentValues = new ContentValues();
        if (friendEntity == null) {
            return contentValues;
        }
        contentValues.put("apply_uid", friendEntity.apply_uid);
        contentValues.put("apply_name", friendEntity.apply_name);
        contentValues.put("token", friendEntity.token);
        contentValues.put("status", friendEntity.status);
        contentValues.put("remark", friendEntity.remark);
        contentValues.put("created_at", friendEntity.created_at);

        return contentValues;
    }

}

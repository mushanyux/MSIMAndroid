package com.chat.uikit.db

import android.content.ContentValues
import android.database.Cursor
import android.text.TextUtils
import com.chat.base.MSBaseApplication
import com.chat.base.db.MSCursor
import com.chat.uikit.enity.ProhibitWord

class ProhibitWordDB private constructor() {
    private val table = "prohibit_words"

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = ProhibitWordDB()
    }

    fun save(list: List<ProhibitWord>) {
        if (list.isEmpty()) return
        val ids = ArrayList<Int>()
        for (word in list) {
            ids.add(word.id)
        }
        val updateList = queryWithsIds(ids)
        val insertCVList = ArrayList<ContentValues>()
        val updateCVList = ArrayList<ContentValues>()
        for (word in list) {
            var isAdd = true
            if (updateList.isNotEmpty()) {
                for (updateWord in updateList) {
                    if (updateWord.id == word.id) {
                        updateCVList.add(getCV(word))
                        isAdd = false
                        break
                    }
                }
            }
            if (isAdd) {
                insertCVList.add(getCV(word))
            }
        }

        try {
            MSBaseApplication.getInstance().dbHelper.db.beginTransaction()
            if (insertCVList.isNotEmpty()) {
                for (cv in insertCVList) {
                    MSBaseApplication.getInstance().dbHelper.insert(table, cv)
                }
            }
            if (updateCVList.isNotEmpty()) {
                for (cv in updateCVList) {
                    val whereValue = arrayOfNulls<String>(1)
                    val sid = cv.get("sid") as Int
                    whereValue[0] = sid.toString()
                    MSBaseApplication.getInstance().dbHelper.update(table, cv, "sid=?", whereValue)
                }
            }
            MSBaseApplication.getInstance().dbHelper.db.setTransactionSuccessful()
        } catch (_: Exception) {
        } finally {
            if (MSBaseApplication.getInstance().dbHelper.db.inTransaction()) {
                MSBaseApplication.getInstance().dbHelper.db.endTransaction()
            }
        }
    }

    fun getMaxVersion(): Long {
        if (MSBaseApplication.getInstance().dbHelper == null) {
            return 0
        }
        val sql = "select * from $table order by `version` desc limit 1"
        val cursor: Cursor = MSBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
        cursor.moveToFirst()
        var num = 0L
        if (!cursor.isAfterLast) {
            val word = serialize(cursor)
            num = word.version
            cursor.moveToNext()
        }
        cursor.close()
        return num
    }

    fun getAll(): ArrayList<ProhibitWord> {
        val sql = "select * from $table where is_deleted=0"
        val result = ArrayList<ProhibitWord>()
        if (MSBaseApplication.getInstance().dbHelper != null) {
            val cursor: Cursor = MSBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
                ?: return result
            run {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    result.add(serialize(cursor))
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }
        return result
    }

    private fun queryWithsIds(list: List<Int>): List<ProhibitWord> {
        val ids = StringBuilder()
        for (id in list) {
            if (!TextUtils.isEmpty(ids)) {
                ids.append(",")
            }
            ids.append(id)
        }
        ids.append(")")
        val sql = String.format("%s%s", "select * from $table where sid in (", ids)
        val result = ArrayList<ProhibitWord>()
        if (MSBaseApplication.getInstance().dbHelper != null) {
            val cursor: Cursor = MSBaseApplication.getInstance().dbHelper.rawQuery(sql, null)
                ?: return result
            run {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    result.add(serialize(cursor))
                    cursor.moveToNext()
                }
            }
            cursor.close()
        }
        return result
    }

    private fun getCV(word: ProhibitWord): ContentValues {
        val cv = ContentValues()
        cv.put("content", word.content)
        cv.put("is_deleted", word.is_deleted)
        cv.put("sid", word.id)
        cv.put("version", word.version)
        cv.put("created_at", word.created_at)
        return cv
    }

    private fun serialize(cursor: Cursor): ProhibitWord {
        val word = ProhibitWord()
        word.content = MSCursor.readString(cursor, "content")
        word.version = MSCursor.readLong(cursor, "version")
        word.is_deleted = MSCursor.readInt(cursor, "is_deleted")
        word.id = MSCursor.readInt(cursor, "sid")
        word.created_at = MSCursor.readString(cursor, "created_at")
        return word
    }
}
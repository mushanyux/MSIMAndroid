package com.chat.uikit.robot.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chat.uikit.robot.entity.MSRobotInlineQueryResult;
import com.chat.uikit.robot.entity.MSSyncRobotEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MSRobotService {
    @POST("robot/sync")
    Observable<List<MSSyncRobotEntity>> syncRobot(@Body JSONArray jsonArray);

    @POST("robot/inline_query")
    Observable<MSRobotInlineQueryResult> inlineQuery(@Body JSONObject jsonObject);
}

package com.superproductivity.superproductivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;

import java.util.Arrays;

public class TaskListWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private SpTask[] tasks;
    private final Context mContext;
    private final Gson gson = new Gson();
    private String lastJsonStr = "NOTHING";

    TaskListWidgetViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        Log.v("TW", "TaskListWidgetViewsFactory: onCreate");
        loadListData();
    }

    @Override
    public void onDataSetChanged() {
        Log.v("TW", "TaskListWidgetViewsFactory: onDataSetChanged");
        loadListData();
    }


    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        if (tasks != null) {
            return tasks.length;
        } else {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.row_layout);
        SpTask task = tasks[position];
        view.setTextViewText(R.id.firstLine, task.title);
        if (task.isDone) {
            view.setInt(R.id.firstLine, "setPaintFlags", Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            view.setTextColor(R.id.firstLine, mContext.getResources().getColor(R.color.mutedText));
            view.setTextViewText(R.id.button, "✓");
        } else {
            view.setInt(R.id.firstLine, "setPaintFlags", Paint.ANTI_ALIAS_FLAG);
            view.setTextColor(R.id.firstLine, mContext.getResources().getColor(R.color.emphasizedText));
            view.setTextViewText(R.id.button, "");
        }

        if (task.category != null && task.category.length() > 0) {
            view.setViewVisibility(R.id.secondLine, View.VISIBLE);
            if (task.categoryHtml != null && task.categoryHtml.length() > 0) {
                view.setTextViewText(R.id.secondLine, Html.fromHtml(task.categoryHtml));
            } else {
                view.setTextViewText(R.id.secondLine, task.category);
            }
        } else {
            view.setViewVisibility(R.id.secondLine, View.GONE);
            view.setTextViewText(R.id.secondLine, "");
        }


        view.setOnClickFillInIntent(R.id.firstLine, this.getPendingSelfIntent());
        view.setOnClickFillInIntent(R.id.button, this.getPendingSelfIntent());

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
//        return true;
        return false;
    }

    private void loadListData() {
        Log.v("TW", "TaskListWidgetViewsFactory: loadListData");
        String jsonStr = null;

        try {
            jsonStr = TaskListDataService.getInstance().getData();
        } catch (Exception e) {
            Log.e("TW", "TaskListWidgetViewsFactory:" + e.toString());
        }

        Log.v("TW", "TaskListWidgetViewsFactory: jsonStr...");

        if (jsonStr != null && !jsonStr.isEmpty()) {
            Log.v("TW", "TaskListWidgetViewsFactory:" + jsonStr.length() + "");
            Log.v("TW", "TaskListWidgetViewsFactory: " + jsonStr);

            if (!jsonStr.equals(lastJsonStr)) {
                SpTask[] newTasks = gson.fromJson(jsonStr, SpTask[].class);
                Log.v("TW", "TaskListWidgetViewsFactory: " + newTasks.toString());
                tasks = newTasks;
                Log.v("TW", "TaskListWidgetViewsFactory: update tasks");
                lastJsonStr = jsonStr;
            }

        } else {
            Log.d("TW", "TaskListWidgetViewsFactory: No jsonStr data (yet)");
        }
    }


    private Intent getPendingSelfIntent() {
        return new Intent(mContext, FullscreenActivity.class);
    }
}

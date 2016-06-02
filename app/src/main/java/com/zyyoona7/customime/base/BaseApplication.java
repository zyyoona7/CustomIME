package com.zyyoona7.customime.base;

import android.app.Application;
import android.content.Context;

public class BaseApplication extends Application {

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
	}

	public static Context getContext() {
		return context;
	}

	public static String composeLocation(String fileName) {
		String dataLocation = "/data/data/" + context.getPackageName() + "/";
		return new StringBuilder().append(dataLocation).append(fileName).toString();
	}

}

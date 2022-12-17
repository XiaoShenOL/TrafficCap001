package com.wakoo.trafficcap001;

import android.os.Binder;

public abstract class ErrorBinder extends Binder {
    public enum Errors {
        ERROR_OK,
        ERROR_NAME_NOT_FOUND,
        ERROR_HOST_UNKNOWN,
        ERROR_IO_CREATION
    }

    abstract public Errors getError();
}

package com.RobinNotBad.BiliClient.util;


import androidx.core.util.Consumer;

public class Result<T> {
    private final Object realResult;
    private boolean isSuccess = false;
    private Result (T val) {
        realResult = new Success<>(val);
        isSuccess = true;
    }

    private Result (Exception e) {
        realResult = new Failure(e);
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public T getOrThrow() throws Exception {
        if (isSuccess() && realResult instanceof Success) {
            Success<T> success = (Success<T>) realResult;
            if (success.value == null) {
                throw new Exception("value is null");
            }
            return success.value;
        }
        throw ((Failure)realResult).error;
    }
    public T getOrNull() {
        if(isSuccess() && realResult instanceof Success) {
            return ((Success<T>)realResult).value;
        }
        return null;
    }

    public Result<T> onSuccess(Consumer<T> onSuccess){
        if(isSuccess()) {
            T value = getOrNull();
            if (value != null) {
                onSuccess.accept(value);
            }
        }
        return this;
    }

    public Result<T> onFailure(Consumer<Throwable> onFailure) {
        if (!isSuccess() && realResult instanceof Failure) {
            Throwable error = ((Failure)realResult).error;
            if (error != null) {
                onFailure.accept(error);
            }
        }
        return this;
    }

    public static <T> Result<T> success(T value) {
        if (value != null) {
            return new Result<>(value);
        } else {
            return new Result<>(new NullPointerException());
        }
    }

    public static <T> Result<T> failure(Exception error) {
        return new Result<>(error);
    }

    private static class Success<T>{
        T value;
        private Success (T value) {
            this.value = value;
        }

    }
    private static class Failure {
        Exception error;
        private Failure(Exception error) {
            this.error = error;
        }
    }
}

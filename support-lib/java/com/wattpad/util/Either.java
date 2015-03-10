package com.wattpad.util;

/**
 * Created by tony on 2015-03-09.
 */
public class Either<Left, Right> {
    private Left mLeft;
    private Right mRight;

    public static <Left, Right> Either<Left, Right> asLeft(Left left) {
        Either<Left, Right> either = new Either<>();
        either.mLeft = left;
        return either;
    }

    public static <Left, Right> Either<Left, Right> asRight(Right right) {
        Either<Left, Right> either = new Either<>();
        either.mRight = right;
        return either;
    }

    private Either () {}

    public Left left() {
        return mLeft;
    }

    public Right right() {
        return mRight;
    }
}

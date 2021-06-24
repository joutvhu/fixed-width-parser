package com.joutvhu.fixedwidth.parser.util;

import lombok.experimental.UtilityClass;

/**
 * Ignore exception executor
 *
 * @author Giao Ho
 * @since 1.0.0
 */
@UtilityClass
public class IgnoreError {
    public <E extends Exception> void execute(NoResultCaller<E> caller) {
        try {
            caller.call();
        } catch (Exception e) {
            // Ignore error
        }
    }

    public <T, E extends Exception> T execute(HaveResultCaller<T, E> caller) {
        try {
            return caller.call();
        } catch (Exception e) {
            return null;
        }
    }

    public <P, E extends Exception> void execute(ArgsCaller<P, E> caller, P... args) {
        try {
            caller.call(args);
        } catch (Exception e) {
            // Ignore error
        }
    }

    public <P, T, E extends Exception> T execute(FullCaller<P, T, E> caller, P... args) {
        try {
            return caller.call(args);
        } catch (Exception e) {
            return null;
        }
    }

    @FunctionalInterface
    public interface NoResultCaller<E extends Exception> {
        void call() throws E;
    }

    @FunctionalInterface
    public interface HaveResultCaller<T, E extends Exception> {
        T call() throws E;
    }

    @FunctionalInterface
    public interface ArgsCaller<P, E extends Exception> {
        void call(P... args) throws E;
    }

    @FunctionalInterface
    public interface FullCaller<P, T, E extends Exception> {
        T call(P... args) throws E;
    }
}

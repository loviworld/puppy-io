package com.lovi.puppy.async.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.lovi.puppy.async.AsyncExecutor;
import com.lovi.puppy.async.handlers.AsyncHandler;
import com.lovi.puppy.async.handlers.Handler;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public class AsyncExecutorImpl<T> implements AsyncExecutor<T>{

	private static ExecutorService executorService = Executors.newFixedThreadPool(1000);
    
    @Override
    public void run(AsyncHandler<T> handler, Handler<T> successHandler, Handler<Throwable> failureHandler) {
    	
    	CompletableFuture.supplyAsync(() -> {
    		return handler.handle();
		}, executorService).thenAccept(s -> {
			successHandler.handle(s);
		}).thenRun(() -> {}).exceptionally(fail->{
			failureHandler.handle(fail);
			return null;
		});
    }
}

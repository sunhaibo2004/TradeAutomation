package com.rami.common.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IWorkers {
	public <T> Future<T> submit(Callable<T> work, int priority);
}

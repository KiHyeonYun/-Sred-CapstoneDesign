/**
 *
 */
package com.example.yun.sred.audio;

import android.os.Handler;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author horiuchi
 *
 */
public class StopableTask extends Thread {

	private final Handler handler = new Handler();
	private final ProgressBar bar;
	protected AtomicBoolean isRunning = new AtomicBoolean(false);

	public StopableTask(ProgressBar bar) {
		this.bar = bar;
	}

	public boolean isRunning() {
		return isRunning.get();
	}

	public boolean stopTask() {
		return isRunning.compareAndSet(true, false);
	}

	public int getMax() {
		return bar.getMax();
	}

	public void setMax(int max) {
		bar.setMax(max);
		bar.setProgress(0);
	}

	public int getValue() {
		return bar.getProgress();
	}

	public void addValue(final int value) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				bar.setProgress(bar.getProgress() + value);
			}
		});
	}
}

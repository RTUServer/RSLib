package io.papermc.paper.util;

public final class IntervalledCounter {

    protected long[] times;
    protected final long interval;
    protected long minTime;
    protected int sum;
    protected int head; // inclusive
    protected int tail; // exclusive

    public IntervalledCounter(final long interval) {
        this.times = new long[8];
        this.interval = interval;
    }

    public void updateCurrentTime() {
        this.updateCurrentTime(System.nanoTime());
    }

    public void updateCurrentTime(final long currentTime) {
        int sum = this.sum;
        int head = this.head;
        final int tail = this.tail;
        final long minTime = currentTime - this.interval;

        final int arrayLen = this.times.length;

        // guard against overflow by using subtraction
        while (head != tail && this.times[head] - minTime < 0) {
            head = (head + 1) % arrayLen;
            --sum;
        }

        this.sum = sum;
        this.head = head;
        this.minTime = minTime;
    }

    public void addTime(final long currTime) {
        // guard against overflow by using subtraction
        if (currTime - this.minTime < 0) {
            return;
        }
        int nextTail = (this.tail + 1) % this.times.length;
        if (nextTail == this.head) {
            this.resize();
            nextTail = (this.tail + 1) % this.times.length;
        }

        this.times[this.tail] = currTime;
        this.tail = nextTail;
    }

    public void updateAndAdd(final int count) {
        final long currTime = System.nanoTime();
        this.updateCurrentTime(currTime);
        for (int i = 0; i < count; ++i) {
            this.addTime(currTime);
        }
    }

    public void updateAndAdd(final int count, final long currTime) {
        this.updateCurrentTime(currTime);
        for (int i = 0; i < count; ++i) {
            this.addTime(currTime);
        }
    }

    private void resize() {
        final long[] oldElements = this.times;
        final long[] newElements = new long[this.times.length * 2];
        this.times = newElements;

        final int head = this.head;
        final int tail = this.tail;
        final int size = tail >= head ? (tail - head) : (tail + (oldElements.length - head));
        this.head = 0;
        this.tail = size;

        if (tail >= head) {
            System.arraycopy(oldElements, head, newElements, 0, size);
        } else {
            System.arraycopy(oldElements, head, newElements, 0, oldElements.length - head);
            System.arraycopy(oldElements, 0, newElements, oldElements.length - head, tail);
        }
    }

    // returns in units per second
    public double getRate() {
        return this.size() / (this.interval * 1.0e-9);
    }

    public int size() {
        final int head = this.head;
        final int tail = this.tail;

        return tail >= head ? (tail - head) : (tail + (this.times.length - head));
    }
}

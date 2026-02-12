package io.github.some_example_name.updater;

public abstract class BaseMyFunction implements MyFunction {
    protected boolean running = false;

    @Override
    public void turnOn() {
        running = true;
    }

    @Override
    public void update(float delta) {
        if (running) {
            this.run(delta);
        }
    }

    protected abstract void run(float delta);

    @Override
    public boolean isTurnOn() {
        return running;
    }

    @Override
    public void turnOff() {
        running = false;
    }
}

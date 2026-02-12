package io.github.some_example_name.model;

import java.util.ArrayList;
import java.util.List;

public class ModelHolder <T>{
    private List<T> list = new ArrayList<>();

    public List<T> getModels() {
        return List.copyOf(list);
    }
    public void addModel (T listener) {
        this.list.add(listener);
    }

    public void removeModel (T listener) {
        this.list.remove(listener);
    }

    public void clear () {
        this.list.clear();
    }

    public void removeModels (List<T> list) {
        this.list.removeAll(list);
    }
}

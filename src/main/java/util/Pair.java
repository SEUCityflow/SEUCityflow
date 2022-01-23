package util;

public class Pair<T, M> {
    private T key;
    private M value;

    public Pair(T key, M value) {
        this.key = key;
        this.value = value;
    }

    public T getKey() {
        return key;
    }

    public M getValue() {
        return value;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public void setValue(M value) {
        this.value = value;
    }
}

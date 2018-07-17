package entity;

/**
 * @author Zhou Guanliang
 * @since 2018/7/12
 */
public class Grid {
    private int value;
    private int draft;
    private boolean preset = false;

    public Grid() {
    }

    public Grid(int value, int draft, boolean preset) {
        this.value = value;
        this.draft = draft;
        this.preset = preset;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getDraft() {
        return draft;
    }

    public void setDraft(int draft) {
        this.draft = draft;
    }

    public boolean isPreset() {
        return preset;
    }

    public void setPreset(boolean preset) {
        this.preset = preset;
    }

    public int getNumber() {
        if (preset) {
            return value;
        } else {
            return draft;
        }
    }
}

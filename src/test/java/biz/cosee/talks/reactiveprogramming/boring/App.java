package biz.cosee.talks.reactiveprogramming.boring;

import io.reactivex.Flowable;

import java.time.LocalTime;

public class App {
    public class Click {

        private LocalTime when;

        Click(LocalTime when) {
            this.when = when;
        }

        public LocalTime getWhen() {
            return when;
        }
    }

    public class Dimension {
        private int width;
        private int height;

        Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }


    public Flowable<Click> onClicks() {
        return Flowable.empty();
    }

    public Flowable<Dimension> onResizes() {
        return Flowable.empty();
    }
}
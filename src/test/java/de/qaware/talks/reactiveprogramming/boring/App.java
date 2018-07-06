package biz.cosee.talks.reactiveprogramming.boring;


import reactor.core.publisher.Flux;

import java.time.LocalTime;

public class App {

    public Flux<MousePosition> mouseDown() {
        return Flux.empty();
    }

    public Flux<MousePosition> mouseMove() {
        return Flux.empty();
    }

    public Flux<MousePosition> mouseUp() {
        return Flux.empty();
    }

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


    public Flux<Click> onClicks() {
        return Flux.empty();
    }

    public Flux<Dimension> onResizes() {
        return Flux.empty();
    }
}


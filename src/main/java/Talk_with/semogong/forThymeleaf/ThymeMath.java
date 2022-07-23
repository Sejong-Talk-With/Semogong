package Talk_with.semogong.forThymeleaf;

import org.springframework.stereotype.Component;

@Component
public class ThymeMath {
    public int round(int a, int b) {
        return Math.round(a/b);
    }
    public int floor(int a, int b) {
        return Math.floorDiv(a, b);
    }
}

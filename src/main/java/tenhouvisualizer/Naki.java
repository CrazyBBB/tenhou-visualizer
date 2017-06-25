package tenhouvisualizer;

public class Naki {
    int[] hai;
    int type; // 0: チー, 1: ポン, 2: 暗カン, 3: 明カン, 4: 加カン
    int nakiIdx;

    public Naki(int[] hai, int type, int nakiIdx) {
        this.hai = hai;
        this.type = type;
        this.nakiIdx = nakiIdx;
    }
}

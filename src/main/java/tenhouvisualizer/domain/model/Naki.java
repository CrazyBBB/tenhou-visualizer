package tenhouvisualizer.domain.model;

public class Naki {
    public int[] hai;
    public int type; // 0: チー, 1: ポン, 2: 暗カン, 3: 明カン, 4: 加カン
    public int nakiIdx;

    public Naki(int[] hai, int type, int nakiIdx) {
        this.hai = hai;
        this.type = type;
        this.nakiIdx = nakiIdx;
    }
}

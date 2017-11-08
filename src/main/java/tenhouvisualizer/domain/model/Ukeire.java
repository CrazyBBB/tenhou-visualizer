package tenhouvisualizer.domain.model;

import java.util.List;

public class Ukeire {
    private int candidateIndex;
    private List<Integer> ukeireIndices;
    private int sum;
    private int syanten;

    public Ukeire(int candidateIndex, List<Integer> ukeireIndices, int sum, int syanten) {
        this.candidateIndex = candidateIndex;
        this.ukeireIndices = ukeireIndices;
        this.sum = sum;
        this.syanten = syanten;
    }

    public int getCandidateIndex() {
        return candidateIndex;
    }

    public List<Integer> getUkeireIndices() {
        return ukeireIndices;
    }

    public int getSize() {
        return ukeireIndices.size();
    }

    public int getSum() {
        return sum;
    }

    public int getSyanten() {
        return syanten;
    }
}

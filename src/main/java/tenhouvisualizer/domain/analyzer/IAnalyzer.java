package tenhouvisualizer.domain.analyzer;

import java.util.ArrayList;

/**
 * 解析用インターフェース
 * todo 座席番号，牌番号の説明
 */
public interface IAnalyzer {
    /**
     * 牌譜の始めに呼び出されるメソッド
     *
     * @param isSanma     三麻: true
     *                    四麻: false
     * @param taku        一般: 0
     *                    上級: 1
     *                    特上: 2
     *                    鳳凰: 3
     * @param isTonnan    東風: false
     *                    東南: true
     * @param isSoku      速ならtrue
     * @param isUseAka    赤有りならtrue
     * @param isAriAri    アリアリならtrue
     * @param playerNames 各プレーヤーの天鳳ID
     * @param playerRates 各プレーヤーのレート
     * @param playerDans  各プレーヤーの段位
     */
    default void startGame(boolean isSanma, int taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri,
                           String[] playerNames, int[] playerRates, String[] playerDans) {
    }

    /**
     * 牌譜の終わりに呼び出されるメソッド
     *
     * @param playerPoints 各プレーヤー最終的な点数
     */
    default void endGame(int[] playerPoints) {
    }

    /**
     * 局の始めに呼び出されるメソッド
     *
     * @param playerPoints     各プレーヤーの点数
     * @param playerHaipais    各プレーヤーの配牌
     * @param oya              親
     * @param bakaze           場風(例: 東)
     * @param kyoku            局
     * @param honba            本場
     * @param kyotaku          供託
     * @param firstDoraDisplay 最初の表示牌
     *                         todo 供託追加
     */
    default void startKyoku(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, int oya, int bakaze, int kyoku, int honba, int kyotaku, int firstDoraDisplay) {
    }

    /**
     * 局の終わりに呼び出されるメソッド
     */
    default void endKyoku() {
    }

    /**
     * 牌をツモったときに呼び出されるメソッド
     *
     * @param position プレーヤーの座席
     * @param tsumoHai ツモった牌
     */
    default void draw(int position, int tsumoHai) {
    }

    /**
     * 牌を切ったときに呼び出されるメソッド
     *
     * @param position プレーヤーの座席
     * @param kiriHai  切った牌
     */
    default void discard(int position, int kiriHai) {
    }

    /**
     * チーしたときに呼び出されるメソッド
     *
     * @param position 鳴いたプレーヤーの座席
     * @param from     鳴かれたプレーヤーの座席
     * @param selfHai  元々持ってた牌
     * @param nakiHai  鳴いた牌
     */
    default void chow(int position, int from, int[] selfHai, int nakiHai) {
    }

    /**
     * ポンしたときに呼び出されるメソッド
     *
     * @param position 鳴いたプレーヤーの座席
     * @param from     鳴かれたプレーヤーの座席
     * @param selfHai  元々持ってた牌
     * @param nakiHai  鳴いた牌
     */
    default void pong(int position, int from, int[] selfHai, int nakiHai) {
    }

    /**
     * 暗カンしたときに呼び出されるメソッド
     *
     * @param position プレーヤーの座席
     * @param selfHai  使用した牌
     */
    default void ankan(int position, int[] selfHai) {
    }

    /**
     * 明カンをしたときに呼び出されるメソッド
     *
     * @param position 鳴いたプレーヤーの座席
     * @param from     鳴かれたプレーヤーの座席
     * @param selfHai  元々持ってた牌
     * @param nakiHai  鳴いた牌
     */
    default void minkan(int position, int from, int[] selfHai, int nakiHai) {
    }

    /**
     * 加カンをしたときに呼び出されるメソッド
     *
     * @param position 鳴いたプレーヤーの座席
     * @param from     鳴かれたプレーヤーの座席
     * @param selfHai  元々持ってた牌
     * @param nakiHai  鳴いた牌
     * @param addHai   追加した牌
     */
    default void kakan(int position, int from, int[] selfHai, int nakiHai, int addHai) {
    }

    /**
     * 北を抜いたときに呼び出されるメソッド
     *
     * @param position 抜いたプレーヤーの座席
     */
    default void kita(int position) {
    }

    /**
     * リーチしたときに呼び出されるメソッド
     *
     * @param position リーチしたプレーヤーの座席
     * @param step     1: リーチ宣言
     *                 2: リーチ成立(宣言牌でロンされたときなどは呼び出されない
     */
    default void reach(int position, int step) {
    }

    /**
     * 新ドラが発生したときに呼び出されるメソッド
     *
     * @param newDoraDisplay 新しい表示牌
     */
    default void addDora(int newDoraDisplay) {
    }

    /**
     * アガったときに呼び出されるメソッド
     * @param position              アガったプレーヤーの座席
     * @param from                  ロンの場合振り込んだプレーヤーの座席
     *                              ツモの場合アガったプレーヤーの座席
     * @param yaku                  アガリの役
     * @param han                   翻
     * @param hu                    符
     * @param score                 点数
     * @param increaseAndDecrease   各プレーヤーの増減後の点数
     */
    default void agari(int position, int from, ArrayList<String> yaku, int han, int hu, int score, int[] increaseAndDecrease) {
    }

    default void ryuukyoku() {
    }
}

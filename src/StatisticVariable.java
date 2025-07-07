public class StatisticVariable {
    String name;
    Integer quantity;
    public StatisticVariable(String name) {
        this.name = name;
        quantity = 0;
    }
    public String toString() {
        return "\t" + name + ": " + quantity;
    }
}
